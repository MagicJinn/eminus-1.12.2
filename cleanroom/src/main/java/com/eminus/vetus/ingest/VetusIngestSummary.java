package com.eminus.vetus.ingest;

import com.eminus.Eminus;
import com.eminus.vetus.EminusVetus;

/**
 * Periodic INFO ingest summary. Per-column lines stay at DEBUG.
 */
public final class VetusIngestSummary {
    private static final long SUMMARY_INTERVAL_MS = 5_000L;

    private static long lastSummaryMs;
    private static int columnsSinceSummary;
    private static int sectionsSinceSummary;

    private VetusIngestSummary() {
    }

    public static void record(int sectionsConverted) {
        columnsSinceSummary++;
        sectionsSinceSummary += sectionsConverted;
        long now = System.currentTimeMillis();
        if (lastSummaryMs == 0L) {
            lastSummaryMs = now;
            return;
        }
        if (now - lastSummaryMs < SUMMARY_INTERVAL_MS) {
            return;
        }
        EminusVetus.LOGGER.info("[{}/ingest] last {}s: {} columns, {} sections converted",
                Eminus.MODID,
                (now - lastSummaryMs) / 1000L,
                columnsSinceSummary,
                sectionsSinceSummary);
        columnsSinceSummary = 0;
        sectionsSinceSummary = 0;
        lastSummaryMs = now;
    }
}