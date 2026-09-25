package ledger.holiday;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

// 공공데이터포털 한국천문연구원 특일정보 getRestDeInfo(공휴일) 호출.
// 명세: https://www.data.go.kr/data/15012690/openapi.do (2026-09-26 확인: solMonth 선택, 응답 XML)
// 키는 서버에서만 쓰고 로그·응답에 남기지 않는다.
@Component
public class HolidayApiClient {

    private static final Logger logger = LoggerFactory.getLogger(HolidayApiClient.class);
    private static final String URL = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Value("${Globals.Ledger.HolidayApiKey}")
    private String apiKey;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    // 그 해 공휴일. 호출·해석에 실패하면 원인을 로그에 남기고 null
    public List<HolidayXmlParser.Holiday> fetch(int year) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL + "?ServiceKey=" + serviceKey()
                            + "&solYear=" + year + "&pageNo=1&numOfRows=100"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.warn("공휴일 API HTTP {} (year={})", response.statusCode(), year);
                return null;
            }
            return HolidayXmlParser.parse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("공휴일 API 호출 중단 (year={})", year);
            return null;
        } catch (IllegalStateException e) {
            // HolidayXmlParser 의 해석·결과 코드 오류(메시지에 키가 없다)
            logger.warn("공휴일 API 응답 오류 (year={}): {}", year, e.getMessage());
            return null;
        } catch (Exception e) {
            // 그 밖의 예외 메시지에는 요청 URL(키 포함)이 들어갈 수 있어 종류만 남긴다
            logger.warn("공휴일 API 호출 실패 (year={}): {}", year, e.getClass().getSimpleName());
            return null;
        }
    }

    // 포털은 "Encoding"(이미 URL 인코딩됨)·"Decoding" 두 형태의 키를 준다. % 가 있으면 인코딩된 키로 본다
    private String serviceKey() {
        String key = apiKey.trim();
        return key.contains("%") ? key : URLEncoder.encode(key, StandardCharsets.UTF_8);
    }
}
