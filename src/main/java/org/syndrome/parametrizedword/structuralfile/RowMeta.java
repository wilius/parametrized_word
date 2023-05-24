package org.syndrome.parametrizedword.structuralfile;

import java.util.Map;
import java.util.Set;

public final class RowMeta {
    private final Map<String, Integer> columns;

    public RowMeta(Map<String, Integer> columns) {
        this.columns = columns;
    }

    public Set<String> getColumns() {
        return columns.keySet();
    }

    public int getColumnIndex(String key) {
        return columns.getOrDefault(key, -1);
    }

    public int indexOf(String column) {
        Integer index = columns.get(column);
        return index == null ? -1 : index;
    }

    public int length() {
        return columns.size();
    }
}
