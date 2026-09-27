package ledger.entry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferAdjustmentTest {
    @Test
    void creditsNewDepositAndReversesDeletedDeposit() {
        assertEquals(List.of(new TransferAdjustment.Delta(3, 500_000)),
                TransferAdjustment.between(null, 0, 3L, 500_000));
        assertEquals(List.of(new TransferAdjustment.Delta(3, -500_000)),
                TransferAdjustment.between(3L, 500_000, null, 0));
    }

    @Test
    void editingAmountOnlyChangesDifference() {
        assertEquals(List.of(new TransferAdjustment.Delta(3, 100_000)),
                TransferAdjustment.between(3L, 500_000, 3L, 600_000));
        assertEquals(List.of(), TransferAdjustment.between(3L, 500_000, 3L, 500_000));
    }

    @Test
    void movingDepositBetweenAssetsReversesOldAndCreditsNew() {
        assertEquals(List.of(new TransferAdjustment.Delta(3, -500_000), new TransferAdjustment.Delta(4, 600_000)),
                TransferAdjustment.between(3L, 500_000, 4L, 600_000));
    }
}
