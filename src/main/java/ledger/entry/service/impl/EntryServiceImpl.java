package ledger.entry.service.impl;

import ledger.asset.dao.AssetDAO;
import ledger.cmmn.util.ParamUtil;
import ledger.entry.TransferAdjustment;
import ledger.entry.dao.EntryDAO;
import ledger.entry.service.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service("entryService")
public class EntryServiceImpl implements EntryService {

    @Autowired
    private EntryDAO entryDAO;

    @Autowired
    private AssetDAO assetDAO;

    @Override
    public List<Map<String, Object>> selectEntryList(Map<String, Object> param) {
        return entryDAO.selectEntryList(param);
    }

    @Override
    public Map<String, Object> selectEntrySum(Map<String, Object> param) {
        return entryDAO.selectEntrySum(param);
    }

    @Override
    public List<Map<String, Object>> selectRecentEntries(Map<String, Object> param) {
        return entryDAO.selectRecentEntries(param);
    }

    @Override
    public Map<String, Object> selectEntry(Map<String, Object> param) {
        return entryDAO.selectEntry(param);
    }

    @Override
    @Transactional
    public int insertEntry(Map<String, Object> param) {
        int inserted = entryDAO.insertEntry(param);
        adjust(param, null, 0);
        return inserted;
    }

    @Override
    @Transactional
    public int updateEntry(Map<String, Object> param) {
        Map<String, Object> old = entryDAO.selectEntryForUpdate(param);
        if (old == null) return 0;
        int updated = entryDAO.updateEntry(param);
        if (updated != 0) adjust(param, assetId(old), ((Number) old.get("amount")).longValue());
        return updated;
    }

    @Override
    @Transactional
    public int deleteEntry(Map<String, Object> param) {
        Map<String, Object> old = entryDAO.selectEntryForUpdate(param);
        if (old == null) return 0;
        int deleted = entryDAO.deleteEntry(param);
        if (deleted != 0) adjust(param, assetId(old), ((Number) old.get("amount")).longValue(), null, 0);
        return deleted;
    }

    private void adjust(Map<String, Object> param, Long oldAssetId, long oldAmount) {
        adjust(param, oldAssetId, oldAmount, assetId(param), ((Number) param.get("amount")).longValue());
    }

    private void adjust(Map<String, Object> param, Long oldAssetId, long oldAmount,
                        Long newAssetId, long newAmount) {
        if (newAssetId != null && !"EXPENSE".equals(param.get("type"))) {
            throw new IllegalArgumentException("자산 이체는 지출 거래에만 설정할 수 있습니다");
        }
        for (TransferAdjustment.Delta delta : TransferAdjustment.between(oldAssetId, oldAmount, newAssetId, newAmount)) {
            if (assetDAO.adjustCashAsset(ParamUtil.map("id", delta.assetId(), "userId", param.get("userId"),
                    "delta", delta.amount())) != 1) {
                throw new IllegalStateException("연결된 자산을 찾을 수 없거나 잔액이 허용 범위를 벗어납니다");
            }
        }
    }

    private static Long assetId(Map<String, Object> row) {
        Object id = row.get("transferAssetId");
        return id == null ? null : ((Number) id).longValue();
    }
}
