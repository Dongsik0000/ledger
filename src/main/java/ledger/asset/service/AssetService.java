package ledger.asset.service;

import java.util.List;
import java.util.Map;

// 자산 DAO 연결. 로직은 AssetApiController 에 있다.
public interface AssetService {

    List<Map<String, Object>> selectAssetList(long userId);

    int insertAsset(Map<String, Object> param);

    int updateAsset(Map<String, Object> param);

    int deleteAsset(Map<String, Object> param);
}
