package ledger.holiday.service.impl;

import ledger.holiday.dao.HolidayDAO;
import ledger.holiday.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service("holidayService")
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayDAO holidayDAO;

    @Override
    public List<LocalDate> selectHolidayDates(Map<String, Object> param) {
        return holidayDAO.selectHolidayDates(param);
    }

    @Override
    public List<Map<String, Object>> selectHolidayList(Map<String, Object> param) {
        return holidayDAO.selectHolidayList(param);
    }

    @Override
    public int countHoliday(LocalDate date) {
        return holidayDAO.countHoliday(date);
    }

    @Override
    public int insertHoliday(Map<String, Object> param) {
        return holidayDAO.insertHoliday(param);
    }

    @Override
    public int upsertHoliday(Map<String, Object> param) {
        return holidayDAO.upsertHoliday(param);
    }

    @Override
    public int deleteHoliday(LocalDate date) {
        return holidayDAO.deleteHoliday(date);
    }
}
