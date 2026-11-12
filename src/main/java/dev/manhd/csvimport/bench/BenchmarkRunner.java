package dev.manhd.csvimport.bench;

import dev.manhd.csvimport.importer.CsvImporter;
import dev.manhd.csvimport.importer.ImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Runs every importer on the same generated file and prints a Markdown table for the README.
 * <pre>./mvnw spring-boot:run -Dspring-boot.run.arguments="--bench.rows=100000"</pre>
 */
@Component
@ConditionalOnProperty("bench.rows")
class BenchmarkRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkRunner.class);

    private final List<CsvImporter> importers;
    private final JdbcTemplate jdbc;
    private final int rows;

    BenchmarkRunner(List<CsvImporter> importers, JdbcTemplate jdbc, @Value("${bench.rows}") int rows) {
        this.importers = importers;
        this.jdbc = jdbc;
        this.rows = rows;
    }

    @Override
    public void run(String... args) throws Exception {
        Path file = CsvGenerator.generate(Files.createTempFile("customers-", ".csv"), rows);
        log.info("Generated {} rows ({} MB)", rows, Files.size(file) / (1024 * 1024));
        StringBuilder table = new StringBuilder("""

                | Strategy | Rows | Inserted | Duplicates | Rejected | Time | Rows/s |
                |---|---:|---:|---:|---:|---:|---:|
                """);
        for (CsvImporter importer : importers) {
            jdbc.execute("TRUNCATE customer RESTART IDENTITY");
            ImportResult r;
            try (InputStream in = Files.newInputStream(file)) {
                r = importer.importCsv(in);
            }
            table.append("| %s | %,d | %,d | %,d | %,d | %.1f s | %,d |%n".formatted(r.strategy(), r.totalRows(),
                    r.inserted(), r.duplicates(), r.rejected(), r.millis() / 1000.0, r.rowsPerSecond()));
        }
        log.info("Benchmark result:{}", table);
        Files.deleteIfExists(file);
    }
}
