package ledger.holiday.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 공휴일 DAO 연결. 로직은 HolidayApiController 에 있다.
public interface HolidayService {

    List<LocalDate> selectHolidayDates(Map<String, Object> param);

    List<Map<String, Object>> selectHolidayList(Map<String, Object> param);

    int countHoliday(LocalDate date);

    int insertHoliday(Map<String, Object> param);

    int upsertHoliday(Map<String, Object> param);

    int deleteHoliday(LocalDate date);
}
