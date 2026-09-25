package ledger.summary;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// 요약 화면의 월별 표(카테고리·결제수단 × 월)를 만드는 순수 계산. DB 결과(Map)를 받아 행 목록을 돌려준다.
public final class SummaryTables {

    // kind: item(항목) / subtotal(그룹 소계) / total(합계)
    public record Row(String label, List<Long> values, long total, String kind) {
    }

    private SummaryTables() {
    }

    // from ~ to (양 끝 포함) "YYYY-MM" 목록
    public static List<String> months(YearMonth from, YearMonth to) {
        List<String> months = new ArrayList<>();
        for (YearMonth m = from; !m.isAfter(to); m = m.plusMonths(1)) {
            months.add(m.toString());
        }
        return months;
    }

    // categories: 지출 카테고리(표시 순서대로) {id, name, groupName, active}
    // amounts: {id(카테고리), month, amount}
    // 같은 그룹은 첫 멤버 자리에 모으고, 표시되는 멤버가 2개 이상이면 소계 행. 비활성은 기록이 있을 때만 표시
    public static List<Row> categoryRows(List<Map<String, Object>> categories, List<Map<String, Object>> amounts, List<String> months) {
        Map<Object, long[]> byId = index(amounts, months);
        Set<Object> emitted = new HashSet<>();
        List<Row> rows = new ArrayList<>();
        long[] total = new long[months.size()];

        for (Map<String, Object> category : categories) {
            if (emitted.contains(key(category.get("id")))) {
                continue;
            }
            String group = (String) category.get("groupName");
            List<Map<String, Object>> members = new ArrayList<>();
            if (group == null) {
                members.add(category);
            } else {
                for (Map<String, Object> c : categories) {
                    if (group.equals(c.get("groupName"))) {
                        members.add(c);
                    }
                }
            }

            long[] groupSum = new long[months.size()];
            int shown = 0;
            for (Map<String, Object> m : members) {
                emitted.add(key(m.get("id")));
                long[] values = byId.getOrDefault(key(m.get("id")), new long[months.size()]);
                if (Boolean.TRUE.equals(m.get("active")) || sum(values) != 0) {
                    rows.add(row((String) m.get("name"), values, "item"));
                    shown++;
                }
                add(groupSum, values);
                add(total, values);
            }
            if (group != null && shown >= 2) {
                rows.add(row(group + " 그룹 소계", groupSum, "subtotal"));
            }
        }
        rows.add(row("합계", total, "total"));
        return rows;
    }

    // payments: {id, name, active}, amounts: {id(결제수단, 없으면 null), month, amount}
    public static List<Row> paymentRows(List<Map<String, Object>> payments, List<Map<String, Object>> amounts, List<String> months) {
        Map<Object, long[]> byId = index(amounts, months);
        List<Row> rows = new ArrayList<>();
        long[] total = new long[months.size()];
        for (Map<String, Object> p : payments) {
            long[] values = byId.getOrDefault(key(p.get("id")), new long[months.size()]);
            if (Boolean.TRUE.equals(p.get("active")) || sum(values) != 0) {
                rows.add(row((String) p.get("name"), values, "item"));
            }
            add(total, values);
        }
        long[] none = byId.get(null);
        if (none != null && sum(none) != 0) {
            rows.add(row("없음", none, "item"));
            add(total, none);
        }
        rows.add(row("합계", total, "total"));
        return rows;
    }

    private static Map<Object, long[]> index(List<Map<String, Object>> amounts, List<String> months) {
        Map<String, Integer> col = new HashMap<>();
        for (int i = 0; i < months.size(); i++) {
            col.put(months.get(i), i);
        }
        Map<Object, long[]> byId = new HashMap<>();
        for (Map<String, Object> a : amounts) {
            Integer i = col.get(Objects.toString(a.get("month")));
            if (i == null) {
                continue;
            }
            byId.computeIfAbsent(key(a.get("id")), k -> new long[months.size()])[i] += ((Number) a.get("amount")).longValue();
        }
        return byId;
    }

    // DB 는 Long, 테스트·다른 경로는 Integer 일 수 있어 키를 Long 으로 맞춘다
    private static Object key(Object id) {
        return id == null ? null : ((Number) id).longValue();
    }

    private static Row row(String label, long[] values, String kind) {
        List<Long> list = new ArrayList<>();
        for (long v : values) {
            list.add(v);
        }
        return new Row(label, list, sum(values), kind);
    }

    private static long sum(long[] values) {
        long s = 0;
        for (long v : values) {
            s += v;
        }
        return s;
    }

    private static void add(long[] target, long[] values) {
        for (int i = 0; i < target.length; i++) {
            target[i] += values[i];
        }
    }
}
