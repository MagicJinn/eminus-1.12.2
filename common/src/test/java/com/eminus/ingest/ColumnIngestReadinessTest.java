package com.eminus.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.eminus.ingest.port.ColumnIngestReadiness;

import org.junit.jupiter.api.Test;

class ColumnIngestReadinessTest {
    @Test
    void emptySlotArrayIsNotReady() {
        assertFalse(ColumnIngestReadiness.hasAllocatedSection(new ColumnIngestReadiness.SectionSlot[0]));
        assertFalse(ColumnIngestReadiness.hasAllocatedSection(null));
    }

    @Test
    void nullSlotsMatchPrePacketClientChunk() {
        ColumnIngestReadiness.SectionSlot[] slots = new ColumnIngestReadiness.SectionSlot[4];
        assertFalse(ColumnIngestReadiness.hasAllocatedSection(slots));
        assertEquals(0, ColumnIngestReadiness.countNonemptySections(slots));
    }

    @Test
    void allocatedAirSectionCountsAsReadyButNotNonempty() {
        ColumnIngestReadiness.SectionSlot[] slots = {allocatedEmpty(), null, null, null};
        assertTrue(ColumnIngestReadiness.hasAllocatedSection(slots));
        assertEquals(0, ColumnIngestReadiness.countNonemptySections(slots));
    }

    @Test
    void nonemptySectionsTypicalOverworld() {
        ColumnIngestReadiness.SectionSlot[] slots = {null, withBlocks(), withBlocks(), withBlocks()};
        assertTrue(ColumnIngestReadiness.hasAllocatedSection(slots));
        assertEquals(3, ColumnIngestReadiness.countNonemptySections(slots));
    }

    private static ColumnIngestReadiness.SectionSlot allocatedEmpty() {
        return new ColumnIngestReadiness.SectionSlot() {
            @Override
            public boolean allocated() {
                return true;
            }

            @Override
            public boolean empty() {
                return true;
            }
        };
    }

    private static ColumnIngestReadiness.SectionSlot withBlocks() {
        return new ColumnIngestReadiness.SectionSlot() {
            @Override
            public boolean allocated() {
                return true;
            }

            @Override
            public boolean empty() {
                return false;
            }
        };
    }
}