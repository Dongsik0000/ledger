package ledger.entry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 거래 수정·삭제 때 연결 자산 증감 테스트
class TransferAdjustmentTest {
    // 새 연결은 더하고, 삭제는 되돌린다
    @Test
    void creditsNewDepositAndReversesDeletedDeposit() {
        assertEquals(List.of(new TransferAdjustment.Delta(3, 500_000)),
                TransferAdjustment.between(null, 0, 3L, 500_000));
        assertEquals(List.of(new TransferAdjustment.Delta(3, -500_000)),
                TransferAdjustment.between(3L, 500_000, null, 0));
    }

    // 같은 자산에서 금액만 바꾸면 차액만
    @Test
    void editingAmountOnlyChangesDifference() {
        assertEquals(List.of(new TransferAdjustment.Delta(3, 100_000)),
                TransferAdjustment.between(3L, 500_000, 3L, 600_000));
        assertEquals(List.of(), TransferAdjustment.between(3L, 500_000, 3L, 500_000));
    }

    // 자산을 바꾸면 이전 자산은 빼고 새 자산에 더한다
    @Test
    void movingDepositBetweenAssetsReversesOldAndCreditsNew() {
        assertEquals(List.of(new TransferAdjustment.Delta(3, -500_000), new TransferAdjustment.Delta(4, 600_000)),
                TransferAdjustment.between(3L, 500_000, 4L, 600_000));
    }
}
