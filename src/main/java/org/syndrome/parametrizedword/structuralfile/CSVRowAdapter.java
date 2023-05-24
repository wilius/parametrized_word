package org.syndrome.parametrizedword.structuralfile;

import org.apache.poi.ss.usermodel.CellType;

public class CSVRowAdapter extends StructuralFileRow {
    private final String[] cells;

    public CSVRowAdapter(RowMeta rowMeta,
                         long line,
                         String[] cells) {
        super(rowMeta, line);
        this.cells = cells;
    }

    @Override
    CellAdapter getCell(int index) {
        if (index < 0) {
            return null;
        }

        String item = cells[index];
        return new CellAdapter(line) {
            @Override
            public CellType getCellTypeEnum() {
                return CellType.STRING;
            }

            @Override
            public String getStringCellValue() {
                return item;
            }

            @Override
            public Number getNumericCellValue() {
                throw new RuntimeException("Not supported functionality");
            }


            @Override
            public String getNumericCellValueFormatted() {
                throw new RuntimeException("Not supported functionality");
            }

            @Override
            public CellType getCachedFormulaResultTypeEnum() {
                throw new RuntimeException("Not supported functionality");
            }

            @Override
            public String getRichStringCellValue() {
                throw new RuntimeException("Not supported functionality");
            }

            @Override
            public void castToText() {

            }
        };
    }
}
