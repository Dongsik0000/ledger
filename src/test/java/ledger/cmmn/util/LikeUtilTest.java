package ledger.cmmn.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LikeUtilTest {

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

    @Test
    void blankIsNull() {
        assertNull(LikeUtil.contains(""));
        assertNull(LikeUtil.contains(null));
    }
}
