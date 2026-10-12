package ledger.asset.service;

import java.util.List;
import java.util.Map;

// 자산 DAO 연결. 로직은 AssetApiController 에 있다.
public interface AssetService {

    // 자산 목록(주식 평가 정보 포함)
    List<Map<String, Object>> selectAssetList(long userId);

    // 내 현금성 자산 하나(주식 제외). 없으면 null
    Map<String, Object> selectCashAsset(Map<String, Object> param);

    // 거래·고정 항목이 이 자산으로 이체하도록 연결돼 있는지
    boolean hasTransfers(Map<String, Object> param);

    // 주식 자산을 최신 시세로 다시 평가. 시세를 못 받으면 false(마지막 평가액 유지)
    boolean refreshStocks(long userId);

    // 자산 추가
    int insertAsset(Map<String, Object> param);

    // 자산 수정
    int updateAsset(Map<String, Object> param);

    // 자산 삭제
    int deleteAsset(Map<String, Object> param);
}
