package ledger.asset.controller;

import jakarta.servlet.http.HttpSession;
import ledger.asset.service.AssetService;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.entry.controller.EntryApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 자산 잔액표(수동 입력). 총 잔액 = 가계부 잔액(대시보드 summary) + 자산 합계
@Controller
@RequestMapping("/ledger/asset")
public class AssetApiController {

    private static final int NAME_MAX = 50;   // asset.name VARCHAR(50)

    @Autowired
    private AssetService assetService;

    @GetMapping
    public String asset() {
        return "asset/assetMain";
    }

    @ResponseBody
    @PostMapping("/list")
    public Response list(HttpSession session) {
        List<Map<String, Object>> assets = assetService.selectAssetList(SessionUtil.getUserId(session));
        long total = 0;
        for (Map<String, Object> a : assets) {
            total += ((Number) a.get("amount")).longValue();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("assets", assets);
        data.put("total", total);
        return Response.of(Constants.SUCCESS, data);
    }

    @ResponseBody
    @PostMapping("/save")
    public Response save(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        String name = ParamUtil.str(param, "name");
        Long amount = ParamUtil.lng(param, "amount");
        if ((ParamUtil.has(param, "id") && id == null) || name.isEmpty() || name.length() > NAME_MAX
                || amount == null || amount < 0 || amount > EntryApiController.AMOUNT_MAX) {
            return Response.invalid("자산 이름(50자 이하)과 잔액(0원~999억 원 미만)을 확인해 주세요.");
        }
        Map<String, Object> asset = ParamUtil.map("userId", SessionUtil.getUserId(session), "id", id, "name", name, "amount", amount);
        if (id == null) {
            assetService.insertAsset(asset);
        } else if (assetService.updateAsset(asset) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }

    @ResponseBody
    @PostMapping("/delete")
    public Response delete(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 자산을 확인해 주세요.");
        }
        if (assetService.deleteAsset(ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session))) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }
}
