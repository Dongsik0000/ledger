package ledger.entry;

import ledger.account.service.AccountService;
import ledger.asset.service.AssetService;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

// 거래·고정 항목이 가리키는 카테고리·결제수단·자산·통장이 그 사용자 것인지 확인한다(EntryApiController·RecurringApiController 공통)
@Component
public class EntryReferences {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AccountService accountService;

    // 문제가 없으면 null, 있으면 화면에 보일 입력 오류 응답. 카테고리는 필수이고 구분(type)과 같아야 한다
    public Response check(long userId, String type, long categoryId, Long paymentMethodId,
                          Long transferAssetId, Long accountId) {
        Map<String, Object> category = settingsService.selectCategory(ParamUtil.map("id", categoryId, "userId", userId));
        if (category == null || !type.equals(category.get("type"))) {
            return Response.invalid("카테고리를 다시 골라 주세요.");
        }
        if (paymentMethodId != null
                && settingsService.selectPayment(ParamUtil.map("id", paymentMethodId, "userId", userId)) == null) {
            return Response.invalid("결제수단을 다시 골라 주세요.");
        }
        if (transferAssetId != null
                && assetService.selectCashAsset(ParamUtil.map("id", transferAssetId, "userId", userId)) == null) {
            return Response.invalid("이체할 적금·예금 자산을 다시 골라 주세요.");
        }
        if (accountId != null
                && accountService.selectAccount(ParamUtil.map("id", accountId, "userId", userId)) == null) {
            return Response.invalid("통장을 다시 골라 주세요.");
        }
        return null;
    }
}
