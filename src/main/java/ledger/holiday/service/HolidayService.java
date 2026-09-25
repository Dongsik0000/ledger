package ledger.holiday.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 공휴일 DAO 연결.
public interface HolidayService {

    List<LocalDate> selectHolidayDates(Map<String, Object> param);
}
