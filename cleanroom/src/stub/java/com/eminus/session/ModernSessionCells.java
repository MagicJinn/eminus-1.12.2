package com.eminus.session;

import com.eminus.cell.CellStates;
import com.eminus.cell.ColumnCoverage;
import com.eminus.cell.Dictionary;
import com.eminus.cell.DictionaryStateTable;
import com.eminus.ingest.CellChangeListener;
import com.eminus.ingest.CellMerger;
import com.eminus.ingest.SectionPyramid;
import com.eminus.work.WorkService;

public final class ModernSessionCells implements SessionCells {
    public static final ModernSessionCells INSTANCE = new ModernSessionCells();

    private ModernSessionCells() {
    }

    @Override
    public CellStates states(Dictionary<String> stateIds) {
        return new DictionaryStateTable(stateIds);
    }

    @Override
    public DimensionIngest ingest(WorkService<SectionPyramid> ingestService, CellStates states,
            Dictionary<String> biomes, CellMerger merger, ColumnCoverage coverage, CellChangeListener listener) {
        return null;
    }
}