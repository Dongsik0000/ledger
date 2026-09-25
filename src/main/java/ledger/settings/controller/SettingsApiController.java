package ledger.settings.controller;

import jakarta.servlet.http.HttpSession;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 설정: 주기, 카테고리·결제수단 추가·수정·삭제·순서. 로직은 여기, SettingsServiceImpl 은 DAO 연결만.
@Controller
@RequestMapping("/ledger/settings")
public class SettingsApiController {

    private static final int NAME_MAX = 50;    // category.name, payment_method.name VARCHAR(50)
    private static final int ORDER_MAX = 9999;
    private static final Set<String> TYPES = Set.of("EXPENSE", "INCOME");
    private static final Set<String> DIRECTIONS = Set.of("UP", "DOWN");
    private static final String MSG_IN_USE_DELETE = "거래나 고정 항목에서 쓰고 있어 삭제할 수 없어요.";

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public String settings() {
        return "settings/settingsMain";
    }

    // 여러 화면이 쓰는 선택지: 카테고리·결제수단 전체(비활성 포함, active 로 구분)
    @ResponseBody
    @PostMapping("/master")
    public Response master(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Map<String, Object> data = new HashMap<>();
        data.put("categories", settingsService.selectCategoryList(userId));
        data.put("paymentMethods", settingsService.selectPaymentList(userId));
        return Response.of(Constants.SUCCESS, data);
    }

    @ResponseBody
    @PostMapping("/load")
    public Response load(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Map<String, Object> data = new HashMap<>(settingsService.selectUserSetting(userId));
        data.put("categories", settingsService.selectCategoryList(userId));
        data.put("paymentMethods", settingsService.selectPaymentList(userId));
        return Response.of(Constants.SUCCESS, data);
    }

    @ResponseBody
    @PostMapping("/cycle/save")
    public Response saveCycle(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Integer payDay = ParamUtil.integer(param, "payDay");
        Boolean adjust = ParamUtil.bool(param, "payDayAdjust");
        if (payDay == null || payDay < 1 || payDay > 31 || adjust == null) {
            return Response.invalid("주기 시작일은 1~31일 중에서 골라 주세요.");
        }
        settingsService.updateUserSetting(ParamUtil.map(
                "userId", SessionUtil.getUserId(session), "payDay", payDay, "payDayAdjust", adjust));
        return Response.of(Constants.SUCCESS);
    }

    @ResponseBody
    @PostMapping("/category/save")
    public Response saveCategory(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        String type = ParamUtil.str(param, "type");
        String name = ParamUtil.str(param, "name");
        String groupName = ParamUtil.str(param, "groupName");
        Integer sortOrder = ParamUtil.integer(param, "sortOrder");
        Boolean active = ParamUtil.has(param, "active") ? ParamUtil.bool(param, "active") : Boolean.TRUE;

        if ((ParamUtil.has(param, "id") && id == null) || !TYPES.contains(type)
                || name.isEmpty() || name.length() > NAME_MAX || groupName.length() > NAME_MAX
                || (ParamUtil.has(param, "sortOrder") && !isOrder(sortOrder)) || active == null) {
            return Response.invalid("이름·그룹명은 50자 이하, 순서는 0~9999 로 입력해 주세요.");
        }

        Map<String, Object> category = ParamUtil.map("userId", userId, "id", id, "type", type, "name", name,
                "groupName", groupName.isEmpty() ? null : groupName, "sortOrder", sortOrder, "active", active);

        if (id != null) {
            Map<String, Object> key = ParamUtil.map("id", id, "userId", userId);
            Map<String, Object> saved = settingsService.selectCategory(key);
            if (saved == null) {
                return Response.of(Constants.NOT_FOUND);
            }
            // 이미 기록된 거래와 구분이 어긋나지 않게, 쓰는 곳이 있으면 구분을 바꾸지 않는다
            if (!type.equals(saved.get("type")) && settingsService.countCategoryUsage(key) > 0) {
                return Response.of(Constants.IN_USE, "거래나 고정 항목에서 쓰고 있어 구분을 바꿀 수 없어요.", null);
            }
            if (sortOrder == null) {
                category.put("sortOrder", saved.get("sortOrder"));
            }
        }
        if (settingsService.countCategoryName(category) > 0) {
            return Response.of(Constants.DUPLICATE, "같은 이름의 카테고리가 있어요.", null);
        }

        if (id == null) {
            if (sortOrder == null) {
                category.put("sortOrder", settingsService.selectMaxCategoryOrder(category) + 1);
            }
            settingsService.insertCategory(category);
        } else {
            settingsService.updateCategory(category);
        }
        return Response.of(Constants.SUCCESS);
    }

    // 쓰는 거래·고정 항목이 없을 때만 삭제(plan.md: 참조 중이면 삭제 불가 → 숨김)
    @Transactional
    @ResponseBody
    @PostMapping("/category/delete")
    public Response deleteCategory(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 항목을 확인해 주세요.");
        }
        Map<String, Object> key = ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session));
        if (settingsService.selectCategory(key) == null) {
            return Response.of(Constants.NOT_FOUND);
        }
        if (settingsService.countCategoryUsage(key) > 0) {
            return Response.of(Constants.IN_USE, MSG_IN_USE_DELETE, null);
        }
        settingsService.deleteCategory(key);
        return Response.of(Constants.SUCCESS);
    }

    // 같은 구분 안에서 한 칸 위·아래로. 순서를 1부터 다시 매긴다
    @Transactional
    @ResponseBody
    @PostMapping("/category/move")
    public Response moveCategory(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        String direction = ParamUtil.str(param, "direction");
        if (id == null || !DIRECTIONS.contains(direction)) {
            return Response.invalid("이동할 항목과 방향을 확인해 주세요.");
        }
        Map<String, Object> saved = settingsService.selectCategory(ParamUtil.map("id", id, "userId", userId));
        if (saved == null) {
            return Response.of(Constants.NOT_FOUND);
        }
        List<Long> ids = new ArrayList<>(settingsService.selectCategoryIds(
                ParamUtil.map("userId", userId, "type", saved.get("type"))));
        if (reorder(ids, id, direction)) {
            for (int i = 0; i < ids.size(); i++) {
                settingsService.updateCategoryOrder(ParamUtil.map("id", ids.get(i), "userId", userId, "sortOrder", i + 1));
            }
        }
        return Response.of(Constants.SUCCESS);
    }

    @ResponseBody
    @PostMapping("/payment/save")
    public Response savePayment(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        String name = ParamUtil.str(param, "name");
        Boolean active = ParamUtil.has(param, "active") ? ParamUtil.bool(param, "active") : Boolean.TRUE;

        if ((ParamUtil.has(param, "id") && id == null) || name.isEmpty() || name.length() > NAME_MAX || active == null) {
            return Response.invalid("결제수단 이름은 50자 이하로 입력해 주세요.");
        }

        Map<String, Object> payment = ParamUtil.map("userId", userId, "id", id, "name", name, "active", active);
        if (id != null && settingsService.selectPayment(ParamUtil.map("id", id, "userId", userId)) == null) {
            return Response.of(Constants.NOT_FOUND);
        }
        if (settingsService.countPaymentName(payment) > 0) {
            return Response.of(Constants.DUPLICATE, "같은 이름의 결제수단이 있어요.", null);
        }

        if (id == null) {
            payment.put("sortOrder", settingsService.selectMaxPaymentOrder(userId) + 1);
            settingsService.insertPayment(payment);
        } else {
            settingsService.updatePayment(payment);
        }
        return Response.of(Constants.SUCCESS);
    }

    @Transactional
    @ResponseBody
    @PostMapping("/payment/delete")
    public Response deletePayment(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 항목을 확인해 주세요.");
        }
        Map<String, Object> key = ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session));
        if (settingsService.selectPayment(key) == null) {
            return Response.of(Constants.NOT_FOUND);
        }
        if (settingsService.countPaymentUsage(key) > 0) {
            return Response.of(Constants.IN_USE, MSG_IN_USE_DELETE, null);
        }
        settingsService.deletePayment(key);
        return Response.of(Constants.SUCCESS);
    }

    @Transactional
    @ResponseBody
    @PostMapping("/payment/move")
    public Response movePayment(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        String direction = ParamUtil.str(param, "direction");
        if (id == null || !DIRECTIONS.contains(direction)) {
            return Response.invalid("이동할 항목과 방향을 확인해 주세요.");
        }
        if (settingsService.selectPayment(ParamUtil.map("id", id, "userId", userId)) == null) {
            return Response.of(Constants.NOT_FOUND);
        }
        List<Long> ids = new ArrayList<>(settingsService.selectPaymentIds(userId));
        if (reorder(ids, id, direction)) {
            for (int i = 0; i < ids.size(); i++) {
                settingsService.updatePaymentOrder(ParamUtil.map("id", ids.get(i), "userId", userId, "sortOrder", i + 1));
            }
        }
        return Response.of(Constants.SUCCESS);
    }

    // ids 안에서 id 를 한 칸 위(UP)·아래(DOWN)로 옮긴다. 끝이라 옮길 수 없거나 없는 id 면 false
    static boolean reorder(List<Long> ids, long id, String direction) {
        int from = ids.indexOf(id);
        int to = "UP".equals(direction) ? from - 1 : from + 1;
        if (from < 0 || to < 0 || to >= ids.size()) {
            return false;
        }
        Collections.swap(ids, from, to);
        return true;
    }

    private static boolean isOrder(Integer sortOrder) {
        return sortOrder != null && sortOrder >= 0 && sortOrder <= ORDER_MAX;
    }
}
