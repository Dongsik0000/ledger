package ledger.asset.service.impl;

import ledger.asset.dao.AssetDAO;
import ledger.asset.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("assetService")
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetDAO assetDAO;

    @Override
    public List<Map<String, Object>> selectAssetList(long userId) {
        return assetDAO.selectAssetList(userId);
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
