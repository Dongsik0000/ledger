package ledger.holiday.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 공휴일 DAO 연결. 로직은 HolidayApiController 에 있다.
public interface HolidayService {

    // 기간 안 공휴일 날짜(주기·결제일 보정용, 전 사용자 공용)
    List<LocalDate> selectHolidayDates(Map<String, Object> param);

    // 연도별 공휴일 목록(설정 화면)
    List<Map<String, Object>> selectHolidayList(Map<String, Object> param);

    // 그 날짜 공휴일 수(이미 있는지 확인)
    int countHoliday(LocalDate date);

    // 공휴일 추가
    int insertHoliday(Map<String, Object> param);

    // 공휴일 추가. 같은 날짜면 이름만 갱신(API 가져오기)
    int upsertHoliday(Map<String, Object> param);

    // 공휴일 삭제
    int deleteHoliday(LocalDate date);
}
