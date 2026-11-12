package dev.manhd.csvimport.importer;

import java.util.ArrayList;
import java.util.List;

/** Mutable counters while an import runs; keeps only the first N errors so memory stays bounded. */
class ImportReport {

    private final int maxErrors;
    private final List<ImportResult.RowError> errors = new ArrayList<>();
    long total;
    long inserted;
    long duplicates;
    long rejected;

    ImportReport(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    void reject(long line, String message) {
        rejected++;
        if (errors.size() < maxErrors) {
            errors.add(new ImportResult.RowError(line, message));
        }
    }

    ImportResult toResult(String strategy, long startNanos) {
        return new ImportResult(strategy, total, inserted, duplicates, rejected, List.copyOf(errors),
                (System.nanoTime() - startNanos) / 1_000_000);
    }
}
