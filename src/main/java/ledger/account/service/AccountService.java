package ledger.account.service;

import java.util.List;
import java.util.Map;

// 통장·이체·정기 이체 DAO 연결. 로직은 AccountApiController 와 TransferGenerator 에 있다.
public interface AccountService {

    List<Map<String, Object>> selectAccountList(Map<String, Object> param);

    List<Map<String, Object>> selectAccountOptions(long userId);

    Map<String, Object> selectAccount(Map<String, Object> param);

    int countAccountName(Map<String, Object> param);

    int countAccountUsage(Map<String, Object> param);

    int insertAccount(Map<String, Object> param);

    int updateAccount(Map<String, Object> param);

    int clearDefault(Map<String, Object> param);

    int deleteAccount(Map<String, Object> param);

    List<Map<String, Object>> selectTransferList(Map<String, Object> param);

    int insertTransfer(Map<String, Object> param);

    int updateTransfer(Map<String, Object> param);

    int deleteTransfer(Map<String, Object> param);

    List<Map<String, Object>> selectRecurringList(long userId);

    Map<String, Object> selectRecurring(Map<String, Object> param);

    List<Map<String, Object>> selectActiveRecurring();

    int insertRecurring(Map<String, Object> param);

    int updateRecurring(Map<String, Object> param);

    int deleteRecurring(Map<String, Object> param);

    int insertRecurringRun(Map<String, Object> param);

    int updateRecurringRunTransfer(Map<String, Object> param);

    List<String> selectRecurringRunKeys(Map<String, Object> param);
}
