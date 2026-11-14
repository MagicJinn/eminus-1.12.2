package com.eminus.ingest.port;

/**
 * Loader-neutral checks for whether a column slice array is safe to ingest.
 */
public final class ColumnIngestReadiness {
    private ColumnIngestReadiness() {
    }

  /**
     * Returns true when at least one section slot is allocated (post {@code Chunk.read} on 1.12.2 client).
     * An empty column before packet apply has only null slots.
     */
    public static boolean hasAllocatedSection(SectionSlot[] sections) {
        if (sections == null) {
            return false;
        }
        for (SectionSlot section : sections) {
            if (section != null && section.allocated()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts sections that are allocated and not empty by the loader definition.
     */
    public static int countNonemptySections(SectionSlot[] sections) {
        if (sections == null) {
            return 0;
        }
        int count = 0;
        for (SectionSlot section : sections) {
            if (section != null && section.allocated() && !section.empty()) {
                count++;
            }
        }
        return count;
    }

    public interface SectionSlot {
        boolean allocated();

        boolean empty();
    }
}