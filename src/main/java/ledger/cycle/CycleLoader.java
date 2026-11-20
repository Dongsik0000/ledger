package ledger.cycle;

import ledger.cmmn.util.ParamUtil;
import ledger.holiday.service.HolidayService;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// 사용자 주기 설정과 앞뒤 공휴일을 읽어 date 가 속한 주기를 구한다(대시보드·거래·통장·고정 항목 공통)
@Component
public class CycleLoader {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private HolidayService holidayService;

    // 보정이 앞뒤 달로 넘어갈 수 있어 공휴일은 date 기준 3개월 전 1일 ~ 3개월 뒤까지 읽는다
    public UserCycle load(long userId, LocalDate date) {
        Map<String, Object> setting = settingsService.selectUserSetting(userId);
        int payDay = ((Number) setting.get("payDay")).intValue();
        boolean adjust = Boolean.TRUE.equals(setting.get("payDayAdjust"));
        Set<LocalDate> holidays = new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                "from", date.minusMonths(3).withDayOfMonth(1), "to", date.plusMonths(3))));
        return new UserCycle(setting, payDay, adjust, holidays, PayCycle.cycleOf(date, payDay, adjust, holidays));
    }

    // setting: selectUserSetting 결과(시작 잔액 포함), cycle: date 가 속한 주기
    public record UserCycle(Map<String, Object> setting, int payDay, boolean adjust,
                            Set<LocalDate> holidays, PayCycle.Cycle cycle) {
    }
}
