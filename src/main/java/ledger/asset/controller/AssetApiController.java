package ledger.asset.controller;

import jakarta.servlet.http.HttpSession;
import ledger.asset.service.AssetService;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.entry.controller.EntryApiController;
import ledger.market.MarketDataClient;
import ledger.market.MarketValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 자산 잔액표(수동 입력). 총 잔액 = 가계부 잔액(대시보드 summary) + 자산 합계
@Controller
@RequestMapping("/ledger/asset")
public class AssetApiController {

    private static final int NAME_MAX = 50;   // asset.name VARCHAR(50)

    @Autowired
    private AssetService assetService;

    @Autowired
    private MarketDataClient marketData;

    @GetMapping
    public String asset() {
        return "asset/assetMain";
    }

    @ResponseBody
    @PostMapping("/list")
    public Response list(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        boolean marketUpdated = assetService.refreshStocks(userId);
        List<Map<String, Object>> assets = assetService.selectAssetList(userId);
        long total = 0;
        for (Map<String, Object> a : assets) {
            total += ((Number) a.get("amount")).longValue();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("assets", assets);
        data.put("total", total);
        data.put("marketUpdated", marketUpdated);
        return Response.of(Constants.SUCCESS, data);
    }

    @ResponseBody
    @PostMapping("/cash-list")
    public Response cashList(HttpSession session) {
        return Response.of(Constants.SUCCESS, assetService.selectAssetList(SessionUtil.getUserId(session)).stream()
                .filter(asset -> asset.get("stockSymbol") == null)
                .map(asset -> Map.of("id", asset.get("id"), "name", asset.get("name")))
                .toList());
    }

    @ResponseBody
    @PostMapping("/search")
    public Response search(@RequestBody HashMap<String, Object> param, HttpSession session) {
        SessionUtil.getUserId(session);
        String query = ParamUtil.str(param, "query").trim();
        if (query.length() < 2 || query.length() > 60) {
            return Response.invalid("종목명이나 코드를 2~60자로 입력해 주세요.");
        }
        try {
            return Response.of(Constants.SUCCESS, marketData.searchUsStocks(query));
        } catch (MarketDataClient.MarketDataException e) {
            return Response.invalid("종목 검색을 사용할 수 없어요. 잠시 후 다시 시도하거나 종목 코드를 직접 입력해 주세요.");
        }
    }

    @ResponseBody
    @PostMapping("/save")
    public Response save(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        String name = ParamUtil.str(param, "name");
        Boolean stock = ParamUtil.has(param, "stock") ? ParamUtil.bool(param, "stock") : Boolean.FALSE;
        Long amount;
        String symbol = null;
        BigDecimal quantity = null;
        BigDecimal price = null;
        BigDecimal rate = null;
        java.time.LocalDate quoteDate = null;
        if (Boolean.TRUE.equals(stock)) {
            if (id != null && assetService.hasTransfers(ParamUtil.map("userId", SessionUtil.getUserId(session), "id", id))) {
                return Response.invalid("거래나 고정 항목에 연결된 자산은 주식 자산으로 바꿀 수 없어요.");
            }
            symbol = ParamUtil.str(param, "symbol").toUpperCase(java.util.Locale.ROOT);
            String quantityText = ParamUtil.str(param, "quantity");
            if (!symbol.matches("[A-Z.]{1,12}") || !quantityText.matches("[0-9]{1,12}(\\.[0-9]{1,6})?")) {
                return Response.invalid("미국 주식 종목 코드와 보유 수량을 확인해 주세요.");
            }
            quantity = new BigDecimal(quantityText);
            if (quantity.signum() <= 0) return Response.invalid("보유 수량을 확인해 주세요.");
            try {
                MarketDataClient.StockQuote quote = marketData.stock(symbol);
                price = quote.price();
                quoteDate = quote.asOf();
                rate = marketData.usdKrw(false).rate();
                amount = MarketValue.krw(quantity.multiply(price), rate);
            } catch (MarketDataClient.MarketDataException | ArithmeticException e) {
                return Response.invalid("주가 또는 환율을 조회하지 못했어요. API 키와 종목 코드를 확인해 주세요.");
            }
        } else {
            amount = ParamUtil.lng(param, "amount");
        }
        if ((ParamUtil.has(param, "id") && id == null) || stock == null || name.isEmpty() || name.length() > NAME_MAX
                || amount == null || amount < 0 || amount > EntryApiController.AMOUNT_MAX) {
            return Response.invalid("자산 이름(50자 이하)과 금액을 확인해 주세요.");
        }
        Map<String, Object> asset = ParamUtil.map("userId", SessionUtil.getUserId(session), "id", id,
                "name", name, "amount", amount, "stockSymbol", symbol, "stockQuantity", quantity,
                "stockPriceUsd", price, "stockUsdKrw", rate, "stockQuoteDate", quoteDate);
        if (id == null) {
            assetService.insertAsset(asset);
        } else if (assetService.updateAsset(asset) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }

    @ResponseBody
    @PostMapping("/delete")
    public Response delete(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 자산을 확인해 주세요.");
        }
        if (assetService.deleteAsset(ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session))) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }
}
