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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Alpha Vantage 시세. 키는 서버 환경변수에만 두고 예외/로그에 URL을 남기지 않는다.
@Component
public class MarketDataClient {
    private static final String ENDPOINT = "https://www.alphavantage.co/query?";
    private static final Duration CACHE_TTL = Duration.ofHours(4);
    private static final Duration FAILURE_TTL = Duration.ofMinutes(15);
    private static final Duration REQUEST_INTERVAL = Duration.ofSeconds(2);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, Cached> cache = new HashMap<>();
    private final Map<String, Instant> failures = new HashMap<>();
    private Instant nextRequestAt = Instant.EPOCH;

    @Value("${Globals.Ledger.MarketApiKey}")
    private String apiKey;

    @Value("${Globals.Ledger.MarketRealtime}")
    private boolean realtime;

    public boolean isConfigured() { return apiKey != null && !apiKey.isBlank(); }

    public synchronized FxQuote usdKrw(boolean fresh) {
        JsonNode node = fetch("FX", "function=CURRENCY_EXCHANGE_RATE&from_currency=USD&to_currency=KRW", fresh);
        JsonNode rate = node.path("Realtime Currency Exchange Rate");
        BigDecimal value = positive(rate.path("5. Exchange Rate").asText());
        String asOf = rate.path("6. Last Refreshed").asText();
        if (value == null || asOf.isBlank()) throw new MarketDataException();
        return new FxQuote(value, asOf);
    }

    // 앱이 멈춰 결제일을 놓친 경우에는 과거 결제일의 가장 최근 영업일 종가를 쓴다.
    public synchronized FxQuote usdKrwAt(LocalDate dueDate, LocalDate today) {
        if (!dueDate.isBefore(today)) return usdKrw(true);
        JsonNode series = fetch("FX_DAILY", "function=FX_DAILY&from_symbol=USD&to_symbol=KRW", false)
                .path("Time Series FX (Daily)");
        LocalDate best = null;
        BigDecimal rate = null;
        for (java.util.Iterator<String> dates = series.fieldNames(); dates.hasNext();) {
            String date = dates.next();
            try {
                LocalDate day = LocalDate.parse(date);
                if (!day.isAfter(dueDate) && !day.isBefore(dueDate.minusDays(7))
                        && (best == null || day.isAfter(best))) {
                    BigDecimal candidate = positive(series.path(date).path("4. close").asText());
                    if (candidate != null) { best = day; rate = candidate; }
                }
            } catch (RuntimeException ignored) { /* 잘못된 행은 후보에서 제외 */ }
        }
        if (rate == null) throw new MarketDataException();
        return new FxQuote(rate, best.toString());
    }

    public synchronized StockQuote stock(String symbol) {
        if (symbol == null || !symbol.matches("[A-Z.]{1,12}")) throw new IllegalArgumentException("Invalid symbol");
        JsonNode node = fetch("STOCK:" + symbol, "function=GLOBAL_QUOTE&symbol=" + symbol
                + (realtime ? "&entitlement=realtime" : ""), false);
        JsonNode quote = node.path("Global Quote");
        BigDecimal price = positive(quote.path("05. price").asText());
        String date = quote.path("07. latest trading day").asText();
        if (price == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) throw new MarketDataException();
        try { return new StockQuote(price, LocalDate.parse(date)); }
        catch (RuntimeException e) { throw new MarketDataException(); }
    }

    public synchronized List<StockMatch> searchUsStocks(String keywords) {
        String query = keywords == null ? "" : keywords.trim();
        if (query.length() < 2 || query.length() > 60) throw new IllegalArgumentException("Invalid search query");
        JsonNode matches = fetch("SEARCH:" + query.toLowerCase(java.util.Locale.ROOT),
                "function=SYMBOL_SEARCH&keywords=" + URLEncoder.encode(query, StandardCharsets.UTF_8), false)
                .path("bestMatches");
        return parseUsStockMatches(matches);
    }

    static List<StockMatch> parseUsStockMatches(JsonNode matches) {
        Map<String, StockMatch> results = new LinkedHashMap<>();
        for (JsonNode row : matches) {
            String symbol = row.path("1. symbol").asText();
            String name = row.path("2. name").asText();
            String type = row.path("3. type").asText();
            if (!symbol.matches("[A-Z.]{1,12}") || name.isBlank()
                    || !"United States".equals(row.path("4. region").asText())
                    || !"USD".equals(row.path("8. currency").asText())
                    || !("Equity".equals(type) || "ETF".equals(type))) continue;
            results.putIfAbsent(symbol, new StockMatch(symbol, name, type));
            if (results.size() == 10) break;
        }
        return List.copyOf(results.values());
    }

    private JsonNode fetch(String key, String query, boolean fresh) {
        if (!isConfigured()) throw new MarketDataException();
        Instant now = Instant.now();
        Cached cached = cache.get(key);
        if (!fresh && cached != null && now.isBefore(cached.expiresAt())) return cached.value();
        Instant retryAt = failures.get(key);
        if (retryAt != null && now.isBefore(retryAt)) throw new MarketDataException();
        try {
            long pauseMillis = Duration.between(Instant.now(), nextRequestAt).toMillis();
            if (pauseMillis > 0) Thread.sleep(pauseMillis);
            nextRequestAt = Instant.now().plus(REQUEST_INTERVAL);
            String encodedKey = URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT + query + "&apikey=" + encodedKey))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) throw new MarketDataException();
            JsonNode body = json.readTree(response.body());
            if (body.has("Note") || body.has("Information") || body.has("Error Message")) throw new MarketDataException();
            if ("FX_DAILY".equals(key)) {
                if (!body.path("Time Series FX (Daily)").isObject()) throw new MarketDataException();
            } else if ("FX".equals(key)) {
                JsonNode fx = body.path("Realtime Currency Exchange Rate");
                if (positive(fx.path("5. Exchange Rate").asText()) == null
                        || fx.path("6. Last Refreshed").asText().isBlank()) throw new MarketDataException();
            } else if (key.startsWith("SEARCH:")) {
                if (!body.path("bestMatches").isArray()) throw new MarketDataException();
            } else {
                JsonNode stock = body.path("Global Quote");
                if (positive(stock.path("05. price").asText()) == null
                        || !stock.path("07. latest trading day").asText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                    throw new MarketDataException();
                }
            }
            Duration ttl = realtime && !"FX_DAILY".equals(key) && !key.startsWith("SEARCH:")
                    ? Duration.ofMinutes(1) : CACHE_TTL;
            cache.put(key, new Cached(body, now.plus(ttl)));
            failures.remove(key);
            return body;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failures.put(key, now.plus(FAILURE_TTL));
            throw new MarketDataException();
        } catch (Exception e) {
            failures.put(key, now.plus(FAILURE_TTL));
            throw new MarketDataException();
        }
    }

    private static BigDecimal positive(String text) {
        try { BigDecimal v = new BigDecimal(text); return v.signum() > 0 ? v : null; }
        catch (NumberFormatException e) { return null; }
    }

    private record Cached(JsonNode value, Instant expiresAt) {}
    public record FxQuote(BigDecimal rate, String asOf) {}
    public record StockQuote(BigDecimal price, LocalDate asOf) {}
    public record StockMatch(String symbol, String name, String type) {}
    public static class MarketDataException extends RuntimeException {}
}
