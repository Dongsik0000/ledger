package ledger.entry.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// 거래 SQL 호출. 매퍼: sqlmap/mappers/ledger/entry/entry.xml
@Repository("entryDAO")
public class EntryDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.entry.dao.EntryDAO";

    // 조건별 거래 목록(날짜 내림차순)
    public List<Map<String, Object>> selectEntryList(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectEntryList", param);
    }

    // 같은 조건의 건수·수입·지출 합계
    public Map<String, Object> selectEntrySum(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectEntrySum", param);
    }

    // 오늘까지의 최근 거래
    public List<Map<String, Object>> selectRecentEntries(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectRecentEntries", param);
    }

    // 내용 자동 채우기: 내용별 가장 최근 구분·카테고리·결제수단·통장·금액
    public List<Map<String, Object>> selectTitleSuggestions(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectTitleSuggestions", param);
    }

    // 내 거래 하나
    public Map<String, Object> selectEntry(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectEntry", param);
    }

    // 수정·삭제 전 기존 거래(금액·연결 자산)를 행 잠금으로 읽기
    public Map<String, Object> selectEntryForUpdate(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectEntryForUpdate", param);
    }

    // 실행 후 param.id 에 생성된 키
    public int insertEntry(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertEntry", param);
    }

    // 거래 수정
    public int updateEntry(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateEntry", param);
    }

    // 거래 삭제
    public int deleteEntry(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteEntry", param);
    }
}
