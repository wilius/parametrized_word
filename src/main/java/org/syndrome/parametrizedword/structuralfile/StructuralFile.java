package org.syndrome.parametrizedword.structuralfile;

import java.util.ArrayList;

public class StructuralFile {
    private final RowMeta rowMeta;
    private final ArrayList<? extends StructuralFileRow> records;

    public StructuralFile(RowMeta rowMeta, ArrayList<? extends StructuralFileRow> records) {
        this.rowMeta = rowMeta;
        this.records = records;
    }

    public RowMeta getRowMeta() {
        return rowMeta;
    }

    public ArrayList<? extends StructuralFileRow> getRecords() {
        return records;
    }
}
