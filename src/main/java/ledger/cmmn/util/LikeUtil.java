package ledger.cmmn.util;

// LIKE/ILIKE 부분 일치 패턴. 사용자가 넣은 % _ \ 는 글자로 찾도록 이스케이프한다.
// 매퍼에서는 반드시 ESCAPE '\' 와 함께 쓴다.
public class LikeUtil {

    private LikeUtil() {
    }

    // "커피" -> "%커피%". 빈 값은 null(조건 생략)
    public static String contains(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        String escaped = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
