package ledger.entry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// 수정·삭제 때 기존 이체를 되돌리고 새 이체를 반영한다. 같은 자산이면 차액만 적용한다.
public final class TransferAdjustment {
    private TransferAdjustment() {}

    public static List<Delta> between(Long oldAssetId, long oldAmount, Long newAssetId, long newAmount) {
        List<Delta> changes = new ArrayList<>(2);
        if (oldAssetId != null && oldAssetId.equals(newAssetId)) {
            if (newAmount != oldAmount) changes.add(new Delta(oldAssetId, newAmount - oldAmount));
        } else {
            if (oldAssetId != null) changes.add(new Delta(oldAssetId, -oldAmount));
            if (newAssetId != null) changes.add(new Delta(newAssetId, newAmount));
        }
        changes.sort(Comparator.comparingLong(Delta::assetId));
        return changes;
    }

    public record Delta(long assetId, long amount) {}
}
