package dev.manhd.csvimport.importer;

import dev.manhd.csvimport.csv.CsvLineParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The "first version" many projects start with, kept as a baseline for the benchmark:
 * reads the whole file into memory, then checks and saves rows one by one through JPA
 * (one SELECT + one INSERT + one transaction per row). Correct, but slow and memory hungry on big files.
 */
@Component
class NaiveCsvImporter implements CsvImporter {

    private final CustomerRepository repository;
    private final ImportProperties props;

    NaiveCsvImporter(CustomerRepository repository, ImportProperties props) {
        this.repository = repository;
        this.props = props;
    }

    @Override
    public String name() {
        return "naive";
    }

    @Override
    public ImportResult importCsv(InputStream csv) throws IOException {
        long start = System.nanoTime();
        ImportReport report = new ImportReport(props.maxReportedErrors());
        // whole file as one String, then as a list of lines: memory grows with the file size
        List<String> lines = new String(csv.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        for (int i = 1; i < lines.size(); i++) { // line 0 is the header
            if (lines.get(i).isBlank()) {
                continue;
            }
            report.total++;
            try {
                CustomerRow row = CustomerRow.from(CsvLineParser.parse(lines.get(i)));
                if (repository.existsByEmail(row.email())) {
                    report.duplicates++;
                } else {
                    repository.save(new Customer(row));
                    report.inserted++;
                }
            } catch (IllegalArgumentException e) {
                report.reject(i + 1, e.getMessage());
            }
        }
        return report.toResult(name(), start);
    }
}
