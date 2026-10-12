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

// 거래 서비스: DAO 위임과 연결 자산 잔액 반영(트랜잭션)
@Service("entryService")
public class EntryServiceImpl implements EntryService {

    @Autowired
    private EntryDAO entryDAO;

    @Autowired
    private AssetDAO assetDAO;

    // 조건별 거래 목록(날짜 내림차순)
    @Override
    public List<Map<String, Object>> selectEntryList(Map<String, Object> param) {
        return entryDAO.selectEntryList(param);
    }

    // 같은 조건의 건수·수입·지출 합계
    @Override
    public Map<String, Object> selectEntrySum(Map<String, Object> param) {
        return entryDAO.selectEntrySum(param);
    }

    // 오늘까지의 최근 거래
    @Override
    public List<Map<String, Object>> selectRecentEntries(Map<String, Object> param) {
        return entryDAO.selectRecentEntries(param);
    }

    // 내용 자동 채우기: 내용별 가장 최근 구분·카테고리·결제수단·통장·금액
    @Override
    public List<Map<String, Object>> selectTitleSuggestions(Map<String, Object> param) {
        return entryDAO.selectTitleSuggestions(param);
    }

    // 내 거래 하나
    @Override
    public Map<String, Object> selectEntry(Map<String, Object> param) {
        return entryDAO.selectEntry(param);
    }

    // 거래 추가와 연결 자산 잔액 반영을 한 트랜잭션으로
    @Override
    @Transactional
    public int insertEntry(Map<String, Object> param) {
        int inserted = entryDAO.insertEntry(param);
        adjust(param, null, 0);
        return inserted;
    }

    // 여러 건 추가. 하나라도 실패하면 전부 되돌린다
    @Override
    @Transactional
    public int insertEntries(List<Map<String, Object>> entries) {
        for (Map<String, Object> entry : entries) {
            entryDAO.insertEntry(entry);
            adjust(entry, null, 0);
        }
        return entries.size();
    }

    // 거래 수정. 연결 자산은 이전 금액을 되돌리고 새 금액을 반영(같은 자산이면 차액만)
    @Override
    @Transactional
    public int updateEntry(Map<String, Object> param) {
        Map<String, Object> old = entryDAO.selectEntryForUpdate(param);
        if (old == null) return 0;
        int updated = entryDAO.updateEntry(param);
        if (updated != 0) adjust(param, assetId(old), ((Number) old.get("amount")).longValue());
        return updated;
    }

    // 거래 삭제. 연결 자산에 더했던 금액을 뺀다
    @Override
    @Transactional
    public int deleteEntry(Map<String, Object> param) {
        Map<String, Object> old = entryDAO.selectEntryForUpdate(param);
        if (old == null) return 0;
        int deleted = entryDAO.deleteEntry(param);
        if (deleted != 0) adjust(param, assetId(old), ((Number) old.get("amount")).longValue(), null, 0);
        return deleted;
    }

    // 새 값은 param 에서 꺼내 반영
    private void adjust(Map<String, Object> param, Long oldAssetId, long oldAmount) {
        adjust(param, oldAssetId, oldAmount, assetId(param), ((Number) param.get("amount")).longValue());
    }

    // 연결 자산 잔액 반영. 자산이 없거나 범위를 벗어나면 IllegalStateException(트랜잭션 롤백)
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

    // 행의 연결 자산 id. 없으면 null
    private static Long assetId(Map<String, Object> row) {
        Object id = row.get("transferAssetId");
        return id == null ? null : ((Number) id).longValue();
    }
}
