package org.syndrome.parametrizedword.structuralfile;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StructuralFileFactory {
    private StructuralFileFactory() {
    }

    public static StructuralFile createFromFile(File file) throws IOException {
        int[] lines = {1};
        try (FileInputStream stream = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(stream);
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            RowMeta rowMeta = createRowMeta(headerRow, Cell::getStringCellValue, Cell::getColumnIndex);

            var formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter dataFormatter = new DataFormatter(Locale.getDefault());
            ArrayList<ExcelRowAdapter> data = StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(x -> new ExcelRowAdapter(
                            rowMeta,
                            lines[0]++,
                            x,
                            formulaEvaluator,
                            dataFormatter))
                    .collect(Collectors.toCollection(ArrayList::new));

            return new StructuralFile(rowMeta, data);
        } catch (NotOfficeXmlFileException e) {
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .build();

            try (FileReader reader = new FileReader(file)) {
                CSVReader csvReader = new CSVReaderBuilder(reader)
                        .withCSVParser(parser)
                        .build();

                String[] headers;
                try {
                    headers = csvReader.readNext();
                } catch (Exception exception) {
                    throw new RuntimeException("Couldn't read headers from csv file", e);
                }

                int[] indexes = {0};
                RowMeta rowMeta = createRowMeta(Arrays.asList(headers), x -> x, x -> indexes[0]++);

                ArrayList<CSVRowAdapter> data = StreamSupport.stream(csvReader.spliterator(), false)
                        .map(x -> new CSVRowAdapter(rowMeta, lines[0]++, x))
                        .collect(Collectors.toCollection(ArrayList::new));

                return new StructuralFile(rowMeta, data);
            }
        }
    }

    private static <T> RowMeta createRowMeta(Iterable<T> headerRow,
                                             Function<T, String> stringHeaderGettingAdapter,
                                             Function<T, Integer> lineNumberGettingAdapter) {
        Map<String, Integer> columns = new HashMap<>();
        for (T t : headerRow) {
            String headerValue = StringUtils.normalize(stringHeaderGettingAdapter.apply(t));
            Integer index = lineNumberGettingAdapter.apply(t);
            if (!StringUtils.isEmpty(headerValue)) {
                columns.put(headerValue, index);
            }
        }

        return new RowMeta(columns);
    }
}
