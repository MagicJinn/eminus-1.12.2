package com.eminus.session;

import com.eminus.cell.CellFrame;
import com.eminus.cell.CellStates;
import com.eminus.cell.Dictionary;
import com.eminus.cell.DictionaryStateTable;
import com.eminus.ingest.CellChangeListener;
import com.eminus.ingest.CellMerger;
import com.eminus.ingest.SectionPyramid;
import com.eminus.work.WorkService;

import org.jspecify.annotations.Nullable;

/**
 * Loader-specific dimension wiring (modern block ingest vs vetus column handoff).
 */
public interface SessionCells {
    DimensionIngest NO_INGEST = new DimensionIngest() { };

    SessionCells DICTIONARY = new SessionCells() {
        @Override
        public CellStates states(Dictionary<String> stateIds) {
            return new DictionaryStateTable(stateIds);
        }

        @Override
        public @Nullable DimensionIngest ingest(WorkService<SectionPyramid> ingestService, CellStates states,
                Dictionary<String> biomes, CellMerger merger, com.eminus.cell.ColumnCoverage coverage,
                CellChangeListener listener) {
            return null;
        }
    };

    CellStates states(Dictionary<String> stateIds);

    @Nullable
    DimensionIngest ingest(WorkService<SectionPyramid> ingestService, CellStates states, Dictionary<String> biomes,
            CellMerger merger, com.eminus.cell.ColumnCoverage coverage, CellChangeListener listener);

    /**
     * Modern chunk ingest service. Vetus uses {@linkplain SessionCells#DICTIONARY} and column {@code CellMerger.merge}.
     */
    interface DimensionIngest {
        default int queued() {
            return 0;
        }

        default int pendingBlockChanges() {
            return 0;
        }
    }
}