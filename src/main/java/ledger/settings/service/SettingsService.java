package ledger.settings.service;

import java.util.List;
import java.util.Map;

// 설정(주기·카테고리·결제수단) DAO 연결. 로직은 SettingsApiController 에 있다.
public interface SettingsService {

    // 주기 설정(급여일·보정)과 시작 잔액
    Map<String, Object> selectUserSetting(long userId);

    // 주기 시작일·주말 보정 저장
    int updateUserSetting(Map<String, Object> param);

    // 시작 잔액 저장. 기준일이 null 이면 해제
    int updateOpening(Map<String, Object> param);

    // 카테고리 전체(지출 먼저, 표시 순서대로, 숨김 포함)
    List<Map<String, Object>> selectCategoryList(long userId);

    // 내 카테고리 하나
    Map<String, Object> selectCategory(Map<String, Object> param);

    // 같은 구분·이름 카테고리 수(수정 중인 자기 자신 제외)
    int countCategoryName(Map<String, Object> param);

    // 이 카테고리를 쓰는 거래·고정 항목 수
    int countCategoryUsage(Map<String, Object> param);

    // 구분 안 가장 큰 표시 순서(새 항목은 그 뒤)
    int selectMaxCategoryOrder(Map<String, Object> param);

    // 구분 안 카테고리 id(표시 순서대로)
    List<Long> selectCategoryIds(Map<String, Object> param);

    // 카테고리 추가
    int insertCategory(Map<String, Object> param);

    // 카테고리 수정
    int updateCategory(Map<String, Object> param);

    // 카테고리 표시 순서 변경
    int updateCategoryOrder(Map<String, Object> param);

    // 카테고리 삭제
    int deleteCategory(Map<String, Object> param);

    // 결제수단 전체(표시 순서대로, 숨김 포함)
    List<Map<String, Object>> selectPaymentList(long userId);

    // 내 결제수단 하나
    Map<String, Object> selectPayment(Map<String, Object> param);

    // 같은 이름 결제수단 수(수정 중인 자기 자신 제외)
    int countPaymentName(Map<String, Object> param);

    // 이 결제수단을 쓰는 거래·고정 항목 수
    int countPaymentUsage(Map<String, Object> param);

    // 가장 큰 표시 순서(새 항목은 그 뒤)
    int selectMaxPaymentOrder(long userId);

    // 결제수단 id(표시 순서대로)
    List<Long> selectPaymentIds(long userId);

    // 결제수단 추가
    int insertPayment(Map<String, Object> param);

    // 결제수단 수정
    int updatePayment(Map<String, Object> param);

    // 결제수단 표시 순서 변경
    int updatePaymentOrder(Map<String, Object> param);

    // 결제수단 삭제
    int deletePayment(Map<String, Object> param);
}
