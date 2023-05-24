package org.syndrome.parametrizedword.structuralfile;

import org.apache.poi.ss.usermodel.*;

public class ExcelRowAdapter extends StructuralFileRow {
    private final Row row;
    private final FormulaEvaluator formulaEvaluator;
    private final DataFormatter dataFormatter;

    public ExcelRowAdapter(RowMeta rowMeta,
                           long line,
                           Row row,
                           FormulaEvaluator formulaEvaluator,
                           DataFormatter dataFormatter) {
        super(rowMeta, line);
        this.row = row;
        this.formulaEvaluator = formulaEvaluator;
        this.dataFormatter = dataFormatter;
    }

    @Override
    public CellAdapter getCell(int index) {
        if (index < 0) {
            return null;
        }

        Cell cell = row.getCell(index);
        return new CellAdapter(line) {
            @Override
            public CellType getCellTypeEnum() {
                if (cell == null) {
                    return CellType.BLANK;
                }

                return cell.getCellTypeEnum();
            }

            @Override
            public String getStringCellValue() {
                if (cell == null) {
                    return null;
                }

                return cell.getStringCellValue();
            }

            @Override
            public Number getNumericCellValue() {
                if (cell == null) {
                    return null;
                }

                return cell.getNumericCellValue();
            }

            @Override
            public String getNumericCellValueFormatted() {
                if (cell == null) {
                    return null;
                }

                return dataFormatter.formatCellValue(cell, formulaEvaluator);
            }

            @Override
            public CellType getCachedFormulaResultTypeEnum() {
                if (cell == null) {
                    return null;
                }

                //noinspection deprecation
                return cell.getCachedFormulaResultTypeEnum();
            }

            @Override
            public String getRichStringCellValue() {
                if (cell == null) {
                    return null;
                }

                return cell.getRichStringCellValue().toString();
            }

            @Override
            public void castToText() {
                if (cell != null) {
                    cell.setCellType(CellType.STRING);
                }
            }
        };
    }
}
