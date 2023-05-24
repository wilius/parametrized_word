package org.syndrome.parametrizedword.structuralfile;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public abstract class StructuralFileRow {
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^[1-9][0-9]*(\\.[0-9]+)?$");

    final RowMeta rowMeta;
    final long line;

    protected StructuralFileRow(RowMeta rowMeta,
                                long line) {
        this.rowMeta = rowMeta;
        this.line = line;
    }

    final CellAdapter getCell(String column) {
        return getCell(rowMeta.indexOf(column));
    }

    abstract CellAdapter getCell(int index);

    public final boolean isEmpty() {
        for (int i = 0; i < rowMeta.length(); i++) {
            CellAdapter cell = getCell(i);
            Object value = cell.getValue();

            if (value != null && !StringUtils.isEmpty(value.toString())) {
                return false;
            }
        }

        return true;
    }

    public BigDecimal getRequiredBigDecimal(String column) {
        CellAdapter cell = getCell(column);
        if (cell == null) {
            throw new RuntimeException(String.format("Column '%s' not found", column));
        }

        Object value = cell.getValue();
        if (value == null) {
            throw new RuntimeException(String.format("Column '%s' required but it is null on line %s", column, line));
        }

        if (value instanceof Double) {
            return new BigDecimal(value.toString());
        }

        String valueText = StringUtils.trim(value.toString());
        if (valueText == null) {
            throw new RuntimeException(String.format("Column '%s' required but it is empty on line %s", column, line));
        }

        if (!AMOUNT_PATTERN.matcher(valueText).matches()) {
            throw new RuntimeException(String.format("Column '%s' is not in a valid decimal format on line %s. it should be in format like '10.32'", column, line));
        }

        return new BigDecimal(valueText);
    }

    public String getRequiredTextValue(String column) {
        CellAdapter cell = getCell(column);
        if (cell == null) {
            throw new RuntimeException(String.format("Column '%s' not found", column));
        }

        // cell.castToText();
        Object value = cell.getValue();
        if (value == null) {
            throw new RuntimeException(String.format("Column '%s' required but it is null on line %s", column, line));
        }

        String result = StringUtils.trim(value.toString());
        if (StringUtils.isEmpty(result)) {
            throw new RuntimeException(String.format("Column '%s' required but it is empty on line %s", column, line));
        }

        return result;
    }

    public String getOptionalTextValue(String column) {
        CellAdapter cell = getCell(column);
        if (cell == null) {
            return null;
        }

        // cell.castToText();
        Object value = cell.getValue();
        if (value == null) {
            return null;
        }

        return StringUtils.trim(value.toString());
    }

    public long getLine() {
        return line;
    }
}
