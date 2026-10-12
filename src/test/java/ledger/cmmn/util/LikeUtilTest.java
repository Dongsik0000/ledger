package ledger.cmmn.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// LIKE 검색 패턴 만들기 테스트
class LikeUtilTest {

    // 앞뒤에 % 를 붙인다
    @Test
    void wrapsWithPercent() {
        assertEquals("%커피%", LikeUtil.contains("커피"));
    }

    // 사용자가 넣은 % _ \ 는 와일드카드가 아니라 글자로 찾는다
    @Test
    void escapesWildcards() {
        assertEquals("%100\\%%", LikeUtil.contains("100%"));
        assertEquals("%a\\_b%", LikeUtil.contains("a_b"));
        assertEquals("%c:\\\\d%", LikeUtil.contains("c:\\d"));
    }

    // 빈 검색어는 null(조건 없음)
    @Test
    void blankIsNull() {
        assertNull(LikeUtil.contains(""));
        assertNull(LikeUtil.contains(null));
    }
}
