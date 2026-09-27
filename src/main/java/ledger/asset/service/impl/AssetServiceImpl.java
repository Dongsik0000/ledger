package ledger.asset.service.impl;

import ledger.asset.dao.AssetDAO;
import ledger.market.MarketDataClient;
import ledger.market.MarketValue;
import ledger.cmmn.util.ParamUtil;
import ledger.entry.controller.EntryApiController;
import ledger.asset.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("assetService")
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetDAO assetDAO;

    @Autowired
    private MarketDataClient marketData;

    @Override
    public List<Map<String, Object>> selectAssetList(long userId) {
        return assetDAO.selectAssetList(userId);
    }

    @Override
    public Map<String, Object> selectCashAsset(Map<String, Object> param) {
        return assetDAO.selectCashAsset(param);
    }

    @Override
    public boolean hasTransfers(Map<String, Object> param) {
        return assetDAO.hasTransfers(param);
    }

    // 마지막으로 확인한 평가액은 DB에 보관한다. API 실패 시 이전 금액과 기준일을 유지한다.
    @Override
    public boolean refreshStocks(long userId) {
        List<Map<String, Object>> stocks = assetDAO.selectStockList(userId);
        if (stocks.isEmpty()) return true;
        try {
            BigDecimal rate = marketData.usdKrw(false).rate();
            for (Map<String, Object> stock : stocks) {
                MarketDataClient.StockQuote quote = marketData.stock((String) stock.get("stockSymbol"));
                BigDecimal quantity = (BigDecimal) stock.get("stockQuantity");
                long amount = MarketValue.krw(quantity.multiply(quote.price()), rate);
                if (amount < 0 || amount > EntryApiController.AMOUNT_MAX) return false;
                if (stock.get("stockPriceUsd") == null || stock.get("stockUsdKrw") == null
                        || quote.price().compareTo((BigDecimal) stock.get("stockPriceUsd")) != 0
                        || rate.compareTo((BigDecimal) stock.get("stockUsdKrw")) != 0
                        || !quote.asOf().toString().equals(String.valueOf(stock.get("stockQuoteDate")))) {
                    assetDAO.updateStockValuation(ParamUtil.map("userId", userId, "id", stock.get("id"),
                            "amount", amount, "price", quote.price(), "rate", rate, "quoteDate", quote.asOf()));
                }
            }
            return true;
        } catch (MarketDataClient.MarketDataException | ArithmeticException e) {
            return false;
        }
    }

    @Override
    public int insertAsset(Map<String, Object> param) {
        return assetDAO.insertAsset(param);
    }

    @Override
    public int updateAsset(Map<String, Object> param) {
        return assetDAO.updateAsset(param);
    }

    @Override
    public int deleteAsset(Map<String, Object> param) {
        return assetDAO.deleteAsset(param);
    }
}
