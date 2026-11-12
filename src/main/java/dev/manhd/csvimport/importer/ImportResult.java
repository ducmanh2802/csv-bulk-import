package dev.manhd.csvimport.importer;

import java.util.List;

public record ImportResult(String strategy, long totalRows, long inserted, long duplicates, long rejected,
                           List<RowError> errors, long millis) {

    public record RowError(long line, String message) {
    }

    public long rowsPerSecond() {
        return millis == 0 ? totalRows : totalRows * 1000 / millis;
    }
}
