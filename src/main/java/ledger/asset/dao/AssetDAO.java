package ledger.asset.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("assetDAO")
public class AssetDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.asset.dao.AssetDAO";

    public List<Map<String, Object>> selectAssetList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectAssetList", userId);
    }

    public int insertAsset(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertAsset", param);
    }

    public int updateAsset(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateAsset", param);
    }

    public int deleteAsset(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteAsset", param);
    }
}
