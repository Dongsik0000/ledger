package ledger.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketDataClientTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void searchOnlyOffersUsUsdStocksAndEtfs() throws Exception {
        JsonNode matches = mapper.readTree("""
                [
                  {"1. symbol":"0R0X.LON","2. name":"Tesla","3. type":"Equity","4. region":"United Kingdom","8. currency":"LSE"},
                  {"1. symbol":"TSLA","2. name":"Tesla Inc","3. type":"Equity","4. region":"United States","8. currency":"USD"},
                  {"1. symbol":"TSLA","2. name":"Duplicate","3. type":"Equity","4. region":"United States","8. currency":"USD"},
                  {"1. symbol":"SPYM","2. name":"SPDR Portfolio S&P 500 ETF","3. type":"ETF","4. region":"United States","8. currency":"USD"},
                  {"1. symbol":"TL0.FRK","2. name":"Tesla Inc","3. type":"Equity","4. region":"Frankfurt","8. currency":"EUR"},
                  {"1. symbol":"BAD-1","2. name":"Invalid symbol","3. type":"Equity","4. region":"United States","8. currency":"USD"}
                ]
                """);

        assertEquals(List.of(
                new MarketDataClient.StockMatch("TSLA", "Tesla Inc", "Equity"),
                new MarketDataClient.StockMatch("SPYM", "SPDR Portfolio S&P 500 ETF", "ETF")
        ), MarketDataClient.parseUsStockMatches(matches));
    }
}
