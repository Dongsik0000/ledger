package ledger.settings.controller;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 순서 변경 요청의 id 목록 검사 테스트
class SettingsApiControllerTest {

    // JSON 숫자 배열을 id 목록으로
    @Test
    void idsReadsJsonNumbers() {
        assertEquals(List.of(3L, 1L, 2L), SettingsApiController.ids(List.of(3, 1L, 2)));
    }

    // 배열이 아니거나 정수가 아니면 null
    @Test
    void idsRejectsNonArrayOrNonInteger() {
        assertNull(SettingsApiController.ids("1,2"));
        assertNull(SettingsApiController.ids(null));
        assertNull(SettingsApiController.ids(List.of(1, "2")));
        assertNull(SettingsApiController.ids(List.of(1, 2.5)));
    }

    // 순서만 바뀐 같은 목록은 허용
    @Test
    void sameIdsAcceptsReorderedList() {
        assertTrue(SettingsApiController.sameIds(List.of(10L, 20L, 30L), List.of(30L, 10L, 20L)));
    }

    // 빠지거나 더해지거나 중복이면 거부
    @Test
    void sameIdsRejectsMissingExtraOrDuplicate() {
        List<Long> saved = List.of(10L, 20L, 30L);
        assertFalse(SettingsApiController.sameIds(saved, List.of(10L, 20L)));
        assertFalse(SettingsApiController.sameIds(saved, List.of(10L, 20L, 30L, 40L)));
        assertFalse(SettingsApiController.sameIds(saved, List.of(10L, 20L, 99L)));
        assertFalse(SettingsApiController.sameIds(saved, List.of(10L, 10L, 20L)));
    }
}
