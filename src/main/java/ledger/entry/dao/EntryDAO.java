package ledger.entry.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("entryDAO")
public class EntryDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.entry.dao.EntryDAO";

    public List<Map<String, Object>> selectEntryList(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectEntryList", param);
    }

    public List<Map<String, Object>> selectRecentEntries(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectRecentEntries", param);
    }

    public Map<String, Object> selectEntry(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectEntry", param);
    }

    // 실행 후 param.id 에 생성된 키
    public int insertEntry(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertEntry", param);
    }

    public int updateEntry(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateEntry", param);
    }

    public int deleteEntry(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteEntry", param);
    }
}
