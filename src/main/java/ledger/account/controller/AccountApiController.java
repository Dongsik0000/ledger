package ledger.account.controller;

import jakarta.servlet.http.HttpSession;
import ledger.account.TransferGenerator;
import ledger.account.service.AccountService;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.cycle.OpeningBalance;
import ledger.cycle.PayCycle;
import ledger.dashboard.service.DashboardService;
import ledger.entry.controller.EntryApiController;
import ledger.holiday.service.HolidayService;
import ledger.recurring.controller.RecurringApiController;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 통장: 통장별 잔액(기준 잔액 + 기준일 이후 이 통장을 고른 거래 + 이체)·통장 CRUD·이체 CRUD·정기 이체 CRUD.
// 이체는 수입·지출이 아니라 ledger_entry 와 가계부 잔액·통계에 영향을 주지 않는다.
// 기본 통장은 기준 잔액을 쓰지 않고 "가계부 잔액 − 다른 통장" 으로 구해(accountsWithBalance), 통장 합계가 가계부 잔액과 항상 같다.
@Controller
@RequestMapping("/ledger/account")
public class AccountApiController {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int NAME_MAX = 50;     // bank_account.name VARCHAR(50)
    private static final int MEMO_MAX = 500;
    private static final int TRANSFER_LIMIT = 100;
    private static final Set<String> ADJUSTS = Set.of("NONE", "PREV_BIZ", "NEXT_BIZ");

    @Autowired
    private AccountService accountService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private TransferGenerator transferGenerator;

    @Autowired
    private DashboardService dashboardService;

    // 통장 화면
    @GetMapping
    public String account() {
        return "account/accountMain";
    }

    // 통장(잔액 포함)·정기 이체(다음 이체일 포함)·최근 이체 TRANSFER_LIMIT 건.
    // 잔액은 대시보드 가계부 잔액과 같은 기간(이번 주기 마지막 날까지)으로 센다
    @ResponseBody
    @PostMapping("/list")
    public Response list(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        LocalDate today = LocalDate.now(SEOUL);
        Map<String, Object> setting = settingsService.selectUserSetting(userId);
        Set<LocalDate> holidays = new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                "from", today.minusMonths(3).withDayOfMonth(1), "to", today.plusMonths(3))));
        PayCycle.Cycle cycle = PayCycle.cycleOf(today, ((Number) setting.get("payDay")).intValue(),
                Boolean.TRUE.equals(setting.get("payDayAdjust")), holidays);

        List<Map<String, Object>> recurring = accountService.selectRecurringList(userId);
        if (!recurring.isEmpty()) {
            List<String> months = new ArrayList<>();
            for (int i = -1; i <= 2; i++) {
                months.add(YearMonth.from(today).plusMonths(i).toString());
            }
            Set<String> handled = new HashSet<>(accountService.selectRecurringRunKeys(
                    ParamUtil.map("userId", userId, "months", months)));
            for (Map<String, Object> item : recurring) {
                PayCycle.Due next = Boolean.TRUE.equals(item.get("active"))
                        ? RecurringApiController.nextDue(item, today, holidays, p -> handled.contains(item.get("id") + "|" + p))
                        : null;
                item.put("nextDue", next == null ? null : next.date().toString());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("accounts", accountsWithBalance(userId, setting, cycle.end()));
        data.put("recurring", recurring);
        data.put("transfers", accountService.selectTransferList(ParamUtil.map("userId", userId, "limit", TRANSFER_LIMIT)));
        data.put("today", today.toString());
        return Response.of(Constants.SUCCESS, data);
    }

    // 거래 입력 창의 통장 잔액 안내. 통장 화면과 같은 잔액과, 새 거래가 잔액에 들어가는 기간(통장별 countFrom ~ to)
    @ResponseBody
    @PostMapping("/balances")
    public Response balances(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        LocalDate today = LocalDate.now(SEOUL);
        Map<String, Object> setting = settingsService.selectUserSetting(userId);
        Set<LocalDate> holidays = new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                "from", today.minusMonths(3).withDayOfMonth(1), "to", today.plusMonths(3))));
        LocalDate to = PayCycle.cycleOf(today, ((Number) setting.get("payDay")).intValue(),
                Boolean.TRUE.equals(setting.get("payDayAdjust")), holidays).end();

        Map<String, Object> data = new HashMap<>();
        data.put("accounts", accountsWithBalance(userId, setting, to));
        data.put("to", to.toString());
        return Response.of(Constants.SUCCESS, data);
    }

    // 통장(잔액 포함). 기본 통장 = 가계부 잔액(대시보드 주기 잔액과 같은 값: 시작 잔액 + 기준일~to 거래) − 다른 통장.
    // countFrom: 새로 적는 거래가 이 통장 잔액에 들어가는 첫날. 다른 통장은 기준일, 기본 통장은 시작 잔액 기준일(없으면 null = 처음부터)
    private List<Map<String, Object>> accountsWithBalance(long userId, Map<String, Object> setting, LocalDate to) {
        List<Map<String, Object>> accounts = accountService.selectAccountList(ParamUtil.map("userId", userId, "to", to));
        OpeningBalance opening = OpeningBalance.of(setting);
        long rest = opening.balanceAt(dashboardService.selectBalanceBefore(
                ParamUtil.map("userId", userId, "date", to.plusDays(1), "from", opening.countFrom())));
        Map<String, Object> def = null;
        for (Map<String, Object> account : accounts) {
            if (Boolean.TRUE.equals(account.get("isDefault"))) {
                def = account;
            } else {
                rest -= ((Number) account.get("balance")).longValue();
                account.put("countFrom", account.get("baseDate"));
            }
        }
        if (def != null) {
            def.put("balance", rest);
            def.put("countFrom", opening.countFrom() == null ? null : opening.countFrom().toString());
        }
        return accounts;
    }

    // 통장 추가·수정. 기준 잔액은 음수 가능(마이너스 통장). 기본 통장은 기준 잔액을 쓰지 않아 0·오늘로 둔다
    @Transactional
    @ResponseBody
    @PostMapping("/save")
    public Response save(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        String name = ParamUtil.str(param, "name");
        Boolean isDefault = ParamUtil.has(param, "isDefault") ? ParamUtil.bool(param, "isDefault") : Boolean.FALSE;
        Long baseBalance = Boolean.TRUE.equals(isDefault) ? Long.valueOf(0) : ParamUtil.lng(param, "baseBalance");
        LocalDate baseDate = Boolean.TRUE.equals(isDefault) ? LocalDate.now(SEOUL) : ParamUtil.date(param, "baseDate");

        if ((ParamUtil.has(param, "id") && id == null) || name.isEmpty() || name.length() > NAME_MAX || isDefault == null
                || baseBalance == null || Math.abs(baseBalance) > EntryApiController.AMOUNT_MAX
                || baseDate == null || baseDate.isAfter(LocalDate.now(SEOUL))) {
            return Response.invalid("이름(50자 이하)·잔액(999억 원 미만)·기준일(오늘까지)을 확인해 주세요.");
        }
        if (id != null && accountService.selectAccount(ParamUtil.map("id", id, "userId", userId)) == null) {
            return Response.of(Constants.NOT_FOUND);
        }

        Map<String, Object> account = ParamUtil.map("userId", userId, "id", id, "name", name,
                "isDefault", isDefault, "baseBalance", baseBalance, "baseDate", baseDate);
        if (accountService.countAccountName(account) > 0) {
            return Response.of(Constants.DUPLICATE, "같은 이름의 통장이 있어요.", null);
        }
        if (isDefault) {
            accountService.clearDefault(account);
        }
        if (id == null) {
            accountService.insertAccount(account);
        } else {
            accountService.updateAccount(account);
        }
        return Response.of(Constants.SUCCESS);
    }

    // 거래·고정 항목·이체가 쓰고 있으면 삭제하지 않는다(다른 통장 잔액이 조용히 바뀌지 않게). 정기 이체는 함께 삭제된다
    @Transactional
    @ResponseBody
    @PostMapping("/delete")
    public Response delete(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 통장을 확인해 주세요.");
        }
        Map<String, Object> key = ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session));
        if (accountService.selectAccount(key) == null) {
            return Response.of(Constants.NOT_FOUND);
        }
        if (accountService.countAccountUsage(key) > 0) {
            return Response.of(Constants.IN_USE, "거래·고정 항목·이체에서 쓰고 있어 삭제할 수 없어요. 해당 기록의 통장을 먼저 바꿔 주세요.", null);
        }
        accountService.deleteAccount(key);
        return Response.of(Constants.SUCCESS);
    }

    // 이체 추가·수정. 보내는·받는 통장은 내 것이고 서로 달라야 한다
    @ResponseBody
    @PostMapping("/transfer/save")
    public Response saveTransfer(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        LocalDate transferDate = ParamUtil.date(param, "transferDate");
        Long fromAccountId = ParamUtil.lng(param, "fromAccountId");
        Long toAccountId = ParamUtil.lng(param, "toAccountId");
        Long amount = ParamUtil.lng(param, "amount");
        String memo = ParamUtil.str(param, "memo");

        if ((ParamUtil.has(param, "id") && id == null) || transferDate == null
                || amount == null || amount < 1 || amount > EntryApiController.AMOUNT_MAX || memo.length() > MEMO_MAX) {
            return Response.invalid("날짜·금액(1원~999억 원 미만)·메모(500자 이하)를 확인해 주세요.");
        }
        Response accountError = checkAccounts(userId, fromAccountId, toAccountId);
        if (accountError != null) {
            return accountError;
        }
        Map<String, Object> transfer = ParamUtil.map("userId", userId, "id", id, "transferDate", transferDate,
                "fromAccountId", fromAccountId, "toAccountId", toAccountId, "amount", amount,
                "memo", memo.isEmpty() ? null : memo);
        if (id == null) {
            accountService.insertTransfer(transfer);
        } else if (accountService.updateTransfer(transfer) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }

    // 이체 삭제. 정기 이체가 만든 것이면 그 달은 다시 만들지 않는다
    @ResponseBody
    @PostMapping("/transfer/delete")
    public Response deleteTransfer(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 이체를 확인해 주세요.");
        }
        if (accountService.deleteTransfer(ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session))) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }

    // 정기 이체 추가·수정. 저장 직후 지난 이체일은 건너뛰고(신규·이체일 변경·재활성화), 오늘이 이체일이면 바로 기록
    @Transactional
    @ResponseBody
    @PostMapping("/recurring/save")
    public Response saveRecurring(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        Long fromAccountId = ParamUtil.lng(param, "fromAccountId");
        Long toAccountId = ParamUtil.lng(param, "toAccountId");
        Long amount = ParamUtil.lng(param, "amount");
        Integer dayOfMonth = ParamUtil.integer(param, "dayOfMonth");
        String adjust = ParamUtil.has(param, "adjust") ? ParamUtil.str(param, "adjust") : "NONE";
        String memo = ParamUtil.str(param, "memo");
        Boolean active = ParamUtil.has(param, "active") ? ParamUtil.bool(param, "active") : Boolean.TRUE;

        if ((ParamUtil.has(param, "id") && id == null)
                || amount == null || amount < 1 || amount > EntryApiController.AMOUNT_MAX
                || dayOfMonth == null || dayOfMonth < 1 || dayOfMonth > 31 || !ADJUSTS.contains(adjust)
                || memo.length() > MEMO_MAX || active == null) {
            return Response.invalid("금액(1원~999억 원 미만)·이체일(1~31일)·메모(500자 이하)를 확인해 주세요.");
        }
        Response accountError = checkAccounts(userId, fromAccountId, toAccountId);
        if (accountError != null) {
            return accountError;
        }
        Map<String, Object> saved = null;
        if (id != null) {
            saved = accountService.selectRecurring(ParamUtil.map("id", id, "userId", userId));
            if (saved == null) {
                return Response.of(Constants.NOT_FOUND);
            }
        }

        Map<String, Object> item = ParamUtil.map("userId", userId, "id", id,
                "fromAccountId", fromAccountId, "toAccountId", toAccountId, "amount", amount,
                "dayOfMonth", dayOfMonth, "adjust", adjust, "active", active, "memo", memo.isEmpty() ? null : memo);
        if (id == null) {
            accountService.insertRecurring(item);          // item.id 에 새 키
        } else {
            accountService.updateRecurring(item);
        }

        if (active) {
            LocalDate today = LocalDate.now(SEOUL);
            if (RecurringApiController.shouldSkipPassed(saved, item)) {
                transferGenerator.skipPassed(item, today);
            }
            transferGenerator.generate(item, today);
        }
        return Response.of(Constants.SUCCESS);
    }

    // 정기 이체 삭제. 이미 기록된 이체는 남는다
    @ResponseBody
    @PostMapping("/recurring/delete")
    public Response deleteRecurring(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 정기 이체를 확인해 주세요.");
        }
        if (accountService.deleteRecurring(ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session))) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }

    // 보내는·받는 통장: 둘 다 있어야 하고, 같은 사용자 것이며, 서로 달라야 한다. 문제가 없으면 null
    private Response checkAccounts(long userId, Long fromAccountId, Long toAccountId) {
        if (fromAccountId == null || toAccountId == null) {
            return Response.invalid("보내는 통장과 받는 통장을 골라 주세요.");
        }
        if (fromAccountId.equals(toAccountId)) {
            return Response.invalid("보내는 통장과 받는 통장이 같아요.");
        }
        if (accountService.selectAccount(ParamUtil.map("id", fromAccountId, "userId", userId)) == null
                || accountService.selectAccount(ParamUtil.map("id", toAccountId, "userId", userId)) == null) {
            return Response.invalid("통장을 다시 골라 주세요.");
        }
        return null;
    }
}
