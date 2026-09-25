package ledger.holiday.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository("holidayDAO")
public class HolidayDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.holiday.dao.HolidayDAO";

    public List<LocalDate> selectHolidayDates(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectHolidayDates", param);
    }
}
