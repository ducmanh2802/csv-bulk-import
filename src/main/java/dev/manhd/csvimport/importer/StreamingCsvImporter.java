package dev.manhd.csvimport.importer;

import dev.manhd.csvimport.csv.CsvLineParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Optimized importer:
 * <ul>
 *   <li>streams the file line by line, so memory stays flat whatever the file size;</li>
 *   <li>sends rows to PostgreSQL in JDBC batches (one round trip per batch instead of per row);</li>
 *   <li>lets the unique index reject duplicates ({@code ON CONFLICT DO NOTHING}) instead of a SELECT per row;</li>
 *   <li>reports invalid rows with their line number and keeps going.</li>
 * </ul>
 * Each batch commits on its own, so a failure in the middle keeps the rows already imported
 * and the import can simply be re-run (duplicates are skipped).
 */
@Component
class StreamingCsvImporter implements CsvImporter {

    private static final String INSERT = """
            INSERT INTO customer (email, full_name, phone, city)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (email) DO NOTHING""";

    private final JdbcTemplate jdbc;
    private final ImportProperties props;

    StreamingCsvImporter(JdbcTemplate jdbc, ImportProperties props) {
        this.jdbc = jdbc;
        this.props = props;
    }

    @Override
    public String name() {
        return "streaming";
    }

    @Override
    public ImportResult importCsv(InputStream csv) throws IOException {
        long start = System.nanoTime();
        ImportReport report = new ImportReport(props.maxReportedErrors());
        List<CustomerRow> batch = new ArrayList<>(props.batchSize());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            checkHeader(reader.readLine());
            String line;
            long lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                report.total++;
                try {
                    batch.add(CustomerRow.from(CsvLineParser.parse(line)));
                } catch (IllegalArgumentException e) {
                    report.reject(lineNo, e.getMessage());
                }
                if (batch.size() == props.batchSize()) {
                    flush(batch, report);
                }
            }
        }
        flush(batch, report);
        return report.toResult(name(), start);
    }

    private void flush(List<CustomerRow> batch, ImportReport report) {
        if (batch.isEmpty()) {
            return;
        }
        int[][] counts = jdbc.batchUpdate(INSERT, batch, batch.size(), (ps, row) -> {
            ps.setString(1, row.email());
            ps.setString(2, row.fullName());
            ps.setString(3, row.phone());
            ps.setString(4, row.city());
        });
        for (int[] chunk : counts) {
            for (int c : chunk) {
                if (c == 1) {
                    report.inserted++;   // row inserted
                } else {
                    report.duplicates++; // 0 = email already existed, skipped by ON CONFLICT
                }
            }
        }
        batch.clear();
    }

    private static void checkHeader(String header) {
        if (header == null) {
            throw new IllegalArgumentException("file is empty");
        }
        List<String> columns = CsvLineParser.parse(header.replace("﻿", "")).stream()
                .map(String::toLowerCase).toList();
        if (!columns.equals(CustomerRow.HEADER)) {
            throw new IllegalArgumentException("header must be " + String.join(",", CustomerRow.HEADER));
        }
    }
}
