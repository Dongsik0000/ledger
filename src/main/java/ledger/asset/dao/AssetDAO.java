package ledger.asset.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// 자산 SQL 호출. 매퍼: sqlmap/mappers/ledger/asset/asset.xml
@Repository("assetDAO")
public class AssetDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.asset.dao.AssetDAO";

    // 자산 목록(주식 평가 정보 포함)
    public List<Map<String, Object>> selectAssetList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectAssetList", userId);
    }

    // 내 현금성 자산 하나(주식 제외). 없으면 null
    public Map<String, Object> selectCashAsset(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectCashAsset", param);
    }

    // 현금성 자산 잔액에 delta 를 더한다. 0원~999억 원을 벗어나면 0행
    public int adjustCashAsset(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".adjustCashAsset", param);
    }

    // 거래·고정 항목이 이 자산으로 이체하도록 연결돼 있는지
    public boolean hasTransfers(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".hasTransfers", param);
    }

    // 시세로 다시 평가할 주식 자산
    public List<Map<String, Object>> selectStockList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectStockList", userId);
    }

    // 주식 평가액·주가·환율·기준일 갱신
    public int updateStockValuation(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateStockValuation", param);
    }

    // 자산 추가
    public int insertAsset(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertAsset", param);
    }

    // 자산 수정
    public int updateAsset(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateAsset", param);
    }

    // 자산 삭제
    public int deleteAsset(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteAsset", param);
    }
}
