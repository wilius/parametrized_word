package org.syndrome.parametrizedword;

import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

abstract class WordFinder implements OnWordFoundCallback {

    private static final int DEFAULT_POS = 0;

    private String bookmark;

    void findWordsInTable(XWPFDocument doc, String word) {
        this.bookmark = word;
        for (XWPFTable t : doc.getTables()) {
            checkTable(t);
        }

        findWordsInText(doc, bookmark);
    }

    void findWordsInText(XWPFDocument doc, String word) {
        this.bookmark = word;
        for (XWPFParagraph p : doc.getParagraphs()) {
            if (paragraphNotNullAndHasRuns(p)) {
                checkInParagraph(p);
            }
        }
    }

    private void checkTable(XWPFTable t) {
        if (t.getRows() == null) return;
        for (XWPFTableRow r : t.getRows()) {
            checkRow(r);
        }
    }

    private void checkRow(XWPFTableRow r) {
        List<XWPFTableCell> tableCells = r.getTableCells();
        if (tableCells == null) {
            return;
        }

        for (XWPFTableCell cell : tableCells) {
            checkCell(cell);
        }
    }

    private void checkCell(XWPFTableCell cell) {
        List<XWPFParagraph> paragraphs = cell.getParagraphs();
        if (paragraphs == null) {
            return;
        }
        for (XWPFParagraph p : paragraphs) {
            if (paragraphNotNullAndHasRuns(p)) {
                checkInParagraph(p);
            }
        }
    }

    private void checkInParagraph(XWPFParagraph p) {
        List<XWPFRun> runs = p.getRuns();
        int lastUsedRun = -1;
        for (int runIndex = 0; runIndex < runs.size(); runIndex++) {
            XWPFRun run = p.getRuns().get(runIndex);
            if (isRunNotNullAndNotEmpty(run)) {
                String text = run.getText(DEFAULT_POS);
                if (text.contains(bookmark)) {
                    onWordFoundInRun(run);
                    lastUsedRun = runIndex;
                } else if (nextRunHasText(runs, runIndex)
                        && !nextRunsText(runs, runIndex).contains(bookmark)
                        && isWordInPreviousCurrentNextRuns(runs, lastUsedRun, runIndex)) {
                    onWordFoundInPreviousCurrentNextRun(runs, runIndex);
                }
            }
        }
    }

    private boolean isWordInPreviousCurrentNextRuns(List<XWPFRun> runs, int lastUsedRun, int runIndex) {
        return isNotFirstRun(runIndex)
                && previousRunHasText(runs, runIndex)
                && previousRunWasNotUsed(lastUsedRun, runIndex)
                && lastThisNextRunText(runs, runIndex).contains(bookmark);
    }

    private boolean previousRunWasNotUsed(int lastUsedRun, int runIndex) {
        return lastUsedRun != runIndex - 1;
    }

    private boolean paragraphNotNullAndHasRuns(XWPFParagraph p) {
        return p != null && !p.getRuns().isEmpty();
    }

    private String lastThisNextRunText(List<XWPFRun> runs, int runIndex) {
        String text = runs.get(runIndex).getText(DEFAULT_POS);
        return lastAndCurrentRunsText(runs, runIndex, text) + nextRunsText(runs, runIndex);
    }

    private boolean nextRunHasText(List<XWPFRun> runs, int runIndex) {
        return runs.size() > runIndex + 1
                && runs.get(runIndex + 1).getText(DEFAULT_POS) != null
                && !runs.get(runIndex + 1).getText(DEFAULT_POS).isEmpty();
    }

    private String nextRunsText(List<XWPFRun> runs, int i) {
        return runs.get(i + 1).getText(DEFAULT_POS);
    }

    private String lastAndCurrentRunsText(List<XWPFRun> runs, int runIndex, String text) {
        return runs.get(runIndex - 1).getText(DEFAULT_POS) + text;
    }

    private boolean previousRunHasText(List<XWPFRun> runs, int runIndex) {
        return runs.get(runIndex - 1).getText(DEFAULT_POS) != null
                && !runs.get(runIndex - 1).getText(DEFAULT_POS).isEmpty();
    }

    private boolean isNotFirstRun(int runIndex) {
        return runIndex > 0;
    }

    private boolean isRunNotNullAndNotEmpty(XWPFRun run) {
        return run != null && run.getText(DEFAULT_POS) != null;
    }
}
