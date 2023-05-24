package org.syndrome.parametrizedword.structuralfile;

import org.apache.poi.ss.usermodel.CellType;

public abstract class CellAdapter {
    private final long line;

    protected CellAdapter(long line) {
        this.line = line;
    }

    protected abstract CellType getCellTypeEnum();

    protected abstract String getStringCellValue();

    protected abstract Number getNumericCellValue();

    protected abstract String getNumericCellValueFormatted();

    protected abstract CellType getCachedFormulaResultTypeEnum();

    protected abstract String getRichStringCellValue();

    public abstract void castToText();

    protected final Object getValue() {
        CellType cellType = getCellTypeEnum();
        if (CellType.BLANK.equals(cellType) || CellType._NONE.equals(cellType)) {
            return null;
        }

        if (CellType.STRING.equals(cellType)) {
            return getStringCellValue();
        }

        if (CellType.NUMERIC.equals(cellType)) {
            return getNumericCellValueFormatted();
        }

        if (CellType.FORMULA.equals(cellType)) {
            switch (getCachedFormulaResultTypeEnum()) {
                case NUMERIC:
                    return getNumericCellValue();
                case STRING:
                    return getRichStringCellValue();
                default:
                    throw new RuntimeException(String.format("Not supported formula cell type %s on line %s", getCachedFormulaResultTypeEnum(), line));
            }
        }

        throw new RuntimeException(String.format("Not supported cell type %s on line %s", cellType, line));
    }
}
