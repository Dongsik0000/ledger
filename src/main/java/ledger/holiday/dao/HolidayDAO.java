package ledger.holiday.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 공휴일 SQL 호출. 매퍼: sqlmap/mappers/ledger/holiday/holiday.xml
@Repository("holidayDAO")
public class HolidayDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.holiday.dao.HolidayDAO";

    // 기간 안 공휴일 날짜(주기·결제일 보정용, 전 사용자 공용)
    public List<LocalDate> selectHolidayDates(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectHolidayDates", param);
    }

    // 연도별 공휴일 목록(설정 화면)
    public List<Map<String, Object>> selectHolidayList(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectHolidayList", param);
    }

    // 그 날짜 공휴일 수(이미 있는지 확인)
    public int countHoliday(LocalDate date) {
        return sqlSession.selectOne(SQL_PATH + ".countHoliday", date);
    }

    // 공휴일 추가
    public int insertHoliday(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertHoliday", param);
    }

    // 공휴일 추가. 같은 날짜면 이름만 갱신(API 가져오기)
    public int upsertHoliday(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".upsertHoliday", param);
    }

    // 공휴일 삭제
    public int deleteHoliday(LocalDate date) {
        return sqlSession.delete(SQL_PATH + ".deleteHoliday", date);
    }
}
