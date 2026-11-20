package ledger.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

// Alpha Vantage 환율·미국 주식 시세. 키는 서버 환경변수에만 두고 예외·로그에 URL 을 남기지 않는다.
// 무료 한도(하루 25회)를 아끼려고 응답을 캐시하고, 실패한 요청은 잠시 다시 부르지 않으며, 요청 사이에 간격을 둔다.
@Component
public class MarketDataClient {

    private static final String ENDPOINT = "https://www.alphavantage.co/query?";
    private static final Duration CACHE_TTL = Duration.ofHours(4);
    private static final Duration REALTIME_TTL = Duration.ofMinutes(1);
    private static final Duration FAILURE_TTL = Duration.ofMinutes(15);
    private static final Duration REQUEST_INTERVAL = Duration.ofSeconds(2);
    private static final String DATE = "\\d{4}-\\d{2}-\\d{2}";
    private static final String SYMBOL = "[A-Z.]{1,12}";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, Cached> cache = new HashMap<>();
    private final Map<String, Instant> failures = new HashMap<>();
    private Instant nextRequestAt = Instant.EPOCH;

    @Value("${Globals.Ledger.MarketApiKey}")
    private String apiKey;

    @Value("${Globals.Ledger.MarketRealtime}")
    private boolean realtime;

    // USD/KRW 현재 환율. fresh 면 캐시를 쓰지 않는다
    public synchronized FxQuote usdKrw(boolean fresh) {
        return fetch("FX", "function=CURRENCY_EXCHANGE_RATE&from_currency=USD&to_currency=KRW", fresh, realtimeTtl(),
                body -> {
                    JsonNode rate = body.path("Realtime Currency Exchange Rate");
                    BigDecimal value = positive(rate.path("5. Exchange Rate").asText());
                    String asOf = rate.path("6. Last Refreshed").asText();
                    if (value == null || asOf.isBlank()) {
                        throw new MarketDataException();
                    }
                    return new FxQuote(value, asOf);
                });
    }

    // 결제일 환율. 오늘 이후면 현재 환율, 앱이 멈춰 지난 결제일을 처리하는 중이면 그날 이전 7일 안의 가장 최근 종가
    public synchronized FxQuote usdKrwAt(LocalDate dueDate, LocalDate today) {
        if (!dueDate.isBefore(today)) {
            return usdKrw(true);
        }
        JsonNode series = fetch("FX_DAILY", "function=FX_DAILY&from_symbol=USD&to_symbol=KRW", false, CACHE_TTL,
                body -> {
                    JsonNode s = body.path("Time Series FX (Daily)");
                    if (!s.isObject()) {
                        throw new MarketDataException();
                    }
                    return s;
                });
        LocalDate best = null;
        BigDecimal rate = null;
        for (Iterator<String> dates = series.fieldNames(); dates.hasNext(); ) {
            String date = dates.next();
            LocalDate day;
            try {
                day = LocalDate.parse(date);
            } catch (RuntimeException e) {
                continue;   // 없는 날짜인 행은 후보에서 뺀다
            }
            if (!day.isAfter(dueDate) && !day.isBefore(dueDate.minusDays(7)) && (best == null || day.isAfter(best))) {
                BigDecimal close = positive(series.path(date).path("4. close").asText());
                if (close != null) {
                    best = day;
                    rate = close;
                }
            }
        }
        if (rate == null) {
            throw new MarketDataException();
        }
        return new FxQuote(rate, best.toString());
    }

    // 미국 주식 최근 주가(최근 거래일 종가, 실시간 이용권이면 실시간)
    public synchronized StockQuote stock(String symbol) {
        if (symbol == null || !symbol.matches(SYMBOL)) {
            throw new IllegalArgumentException("Invalid symbol");
        }
        return fetch("STOCK:" + symbol, "function=GLOBAL_QUOTE&symbol=" + symbol + (realtime ? "&entitlement=realtime" : ""),
                false, realtimeTtl(), body -> {
                    JsonNode quote = body.path("Global Quote");
                    BigDecimal price = positive(quote.path("05. price").asText());
                    String date = quote.path("07. latest trading day").asText();
                    if (price == null || !date.matches(DATE)) {
                        throw new MarketDataException();
                    }
                    return new StockQuote(price, LocalDate.parse(date));
                });
    }

    // 미국 상장·USD 종목 검색(query 2~60자)
    public synchronized List<StockMatch> searchUsStocks(String query) {
        String keywords = query == null ? "" : query.trim();
        if (keywords.length() < 2 || keywords.length() > 60) {
            throw new IllegalArgumentException("Invalid search query");
        }
        return fetch("SEARCH:" + keywords.toLowerCase(Locale.ROOT),
                "function=SYMBOL_SEARCH&keywords=" + URLEncoder.encode(keywords, StandardCharsets.UTF_8), false, CACHE_TTL,
                body -> {
                    JsonNode matches = body.path("bestMatches");
                    if (!matches.isArray()) {
                        throw new MarketDataException();
                    }
                    return parseUsStockMatches(matches);
                });
    }

    // 검색 결과 중 미국 상장·USD 주식·ETF 만 최대 10개(같은 코드는 하나)
    static List<StockMatch> parseUsStockMatches(JsonNode matches) {
        Map<String, StockMatch> results = new LinkedHashMap<>();
        for (JsonNode row : matches) {
            String symbol = row.path("1. symbol").asText();
            String name = row.path("2. name").asText();
            String type = row.path("3. type").asText();
            if (!symbol.matches(SYMBOL) || name.isBlank()
                    || !"United States".equals(row.path("4. region").asText())
                    || !"USD".equals(row.path("8. currency").asText())
                    || !("Equity".equals(type) || "ETF".equals(type))) {
                continue;
            }
            results.putIfAbsent(symbol, new StockMatch(symbol, name, type));
            if (results.size() == 10) {
                break;
            }
        }
        return List.copyOf(results.values());
    }

    // 캐시가 유효하면 캐시, 아니면 호출해 parse 결과를 ttl 동안 캐시한다.
    // parse 가 MarketDataException 을 던지면(한도 초과·형식 오류) 캐시하지 않고 FAILURE_TTL 동안 같은 요청을 막는다
    @SuppressWarnings("unchecked")
    private <T> T fetch(String key, String query, boolean fresh, Duration ttl, Function<JsonNode, T> parse) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new MarketDataException();
        }
        Instant now = Instant.now();
        Cached cached = cache.get(key);
        if (!fresh && cached != null && now.isBefore(cached.expiresAt())) {
            return (T) cached.value();
        }
        Instant retryAt = failures.get(key);
        if (retryAt != null && now.isBefore(retryAt)) {
            throw new MarketDataException();
        }
        try {
            long pauseMillis = Duration.between(Instant.now(), nextRequestAt).toMillis();
            if (pauseMillis > 0) {
                Thread.sleep(pauseMillis);
            }
            nextRequestAt = Instant.now().plus(REQUEST_INTERVAL);
            String encodedKey = URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT + query + "&apikey=" + encodedKey))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new MarketDataException();
            }
            JsonNode body = json.readTree(response.body());
            // 한도 초과·키 오류는 200 응답 본문에 안내 문구로 온다
            if (body.has("Note") || body.has("Information") || body.has("Error Message")) {
                throw new MarketDataException();
            }
            T value = parse.apply(body);
            cache.put(key, new Cached(value, now.plus(ttl)));
            failures.remove(key);
            return value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failures.put(key, now.plus(FAILURE_TTL));
            throw new MarketDataException();
        } catch (Exception e) {
            // 예외 메시지에 요청 URL(키 포함)이 들어갈 수 있어 원인을 넘기지 않는다
            failures.put(key, now.plus(FAILURE_TTL));
            throw new MarketDataException();
        }
    }

    // 실시간 이용권이면 시세는 1분, 아니면 4시간 재사용
    private Duration realtimeTtl() {
        return realtime ? REALTIME_TTL : CACHE_TTL;
    }

    // 양수 숫자면 BigDecimal, 아니면 null
    private static BigDecimal positive(String text) {
        try {
            BigDecimal v = new BigDecimal(text);
            return v.signum() > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 캐시한 parse 결과와 만료 시각
    private record Cached(Object value, Instant expiresAt) {
    }

    // 환율과 기준 시각(제공처 문자열)
    public record FxQuote(BigDecimal rate, String asOf) {
    }

    // 주가와 기준 거래일
    public record StockQuote(BigDecimal price, LocalDate asOf) {
    }

    // 종목 검색 결과 하나
    public record StockMatch(String symbol, String name, String type) {
    }

    // 시세를 쓸 수 없음(키 없음·한도 초과·응답 오류·잠시 차단). 화면에는 원인 없이 "조회 실패"로 안내한다
    public static class MarketDataException extends RuntimeException {
    }
}
