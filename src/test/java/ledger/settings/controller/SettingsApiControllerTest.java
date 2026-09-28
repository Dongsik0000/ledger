package ledger.settings.controller;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsApiControllerTest {

    @Test
    void idsReadsJsonNumbers() {
        assertEquals(List.of(3L, 1L, 2L), SettingsApiController.ids(List.of(3, 1L, 2)));
    }

    @Test
    void idsRejectsNonArrayOrNonInteger() {
        assertNull(SettingsApiController.ids("1,2"));
        assertNull(SettingsApiController.ids(null));
        assertNull(SettingsApiController.ids(List.of(1, "2")));
        assertNull(SettingsApiController.ids(List.of(1, 2.5)));
    }

    @Test
    void sameIdsAcceptsReorderedList() {
        assertTrue(SettingsApiController.sameIds(List.of(10L, 20L, 30L), List.of(30L, 10L, 20L)));
    }

    @Test
    void sameIdsRejectsMissingExtraOrDuplicate() {
        List<Long> saved = List.of(10L, 20L, 30L);
        assertFalse(SettingsApiController.sameIds(saved, List.of(10L, 20L)));
        assertFalse(SettingsApiController.sameIds(saved, List.of(10L, 20L, 30L, 40L)));
        assertFalse(SettingsApiController.sameIds(saved, List.of(10L, 20L, 99L)));
        assertFalse(SettingsApiController.sameIds(saved, List.of(10L, 10L, 20L)));
    }
}
