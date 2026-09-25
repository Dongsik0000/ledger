package ledger.settings.controller;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsApiControllerTest {

    private List<Long> ids(Long... values) {
        return new ArrayList<>(List.of(values));
    }

    @Test
    void moveUpSwapsWithPrevious() {
        List<Long> ids = ids(10L, 20L, 30L);
        assertTrue(SettingsApiController.reorder(ids, 20L, "UP"));
        assertEquals(List.of(20L, 10L, 30L), ids);
    }

    @Test
    void moveDownSwapsWithNext() {
        List<Long> ids = ids(10L, 20L, 30L);
        assertTrue(SettingsApiController.reorder(ids, 20L, "DOWN"));
        assertEquals(List.of(10L, 30L, 20L), ids);
    }

    @Test
    void cannotMovePastEnds() {
        List<Long> ids = ids(10L, 20L);
        assertFalse(SettingsApiController.reorder(ids, 10L, "UP"));
        assertFalse(SettingsApiController.reorder(ids, 20L, "DOWN"));
        assertEquals(List.of(10L, 20L), ids);
    }

    @Test
    void unknownIdDoesNothing() {
        List<Long> ids = ids(10L, 20L);
        assertFalse(SettingsApiController.reorder(ids, 99L, "UP"));
        assertEquals(List.of(10L, 20L), ids);
    }
}
