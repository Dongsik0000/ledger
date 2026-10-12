package ledger.holiday.service.impl;

import ledger.holiday.dao.HolidayDAO;
import ledger.holiday.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 공휴일 서비스: DAO 위임
@Service("holidayService")
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayDAO holidayDAO;

    // 기간 안 공휴일 날짜(주기·결제일 보정용, 전 사용자 공용)
    @Override
    public List<LocalDate> selectHolidayDates(Map<String, Object> param) {
        return holidayDAO.selectHolidayDates(param);
    }

    // 연도별 공휴일 목록(설정 화면)
    @Override
    public List<Map<String, Object>> selectHolidayList(Map<String, Object> param) {
        return holidayDAO.selectHolidayList(param);
    }

    // 그 날짜 공휴일 수(이미 있는지 확인)
    @Override
    public int countHoliday(LocalDate date) {
        return holidayDAO.countHoliday(date);
    }

    // 공휴일 추가
    @Override
    public int insertHoliday(Map<String, Object> param) {
        return holidayDAO.insertHoliday(param);
    }

    // 공휴일 추가. 같은 날짜면 이름만 갱신(API 가져오기)
    @Override
    public int upsertHoliday(Map<String, Object> param) {
        return holidayDAO.upsertHoliday(param);
    }

    // 공휴일 삭제
    @Override
    public int deleteHoliday(LocalDate date) {
        return holidayDAO.deleteHoliday(date);
    }
}
