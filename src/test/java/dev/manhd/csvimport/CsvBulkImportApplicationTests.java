package dev.manhd.csvimport;

import dev.manhd.csvimport.bench.CsvGenerator;
import dev.manhd.csvimport.importer.CsvImporter;
import dev.manhd.csvimport.importer.ImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** End-to-end against a real PostgreSQL in Docker (skipped when Docker is not available). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class CsvBulkImportApplicationTests {

    @Autowired
    List<CsvImporter> importers;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanTable() {
        jdbc.execute("TRUNCATE customer RESTART IDENTITY");
    }

    @Test
    void bothStrategiesProduceTheSameResult(@TempDir Path dir) throws Exception {
        Path file = CsvGenerator.generate(dir.resolve("customers.csv"), 5_000);
        for (CsvImporter importer : importers) {
            cleanTable();
            ImportResult r;
            try (InputStream in = Files.newInputStream(file)) {
                r = importer.importCsv(in);
            }
            assertThat(r.totalRows()).as(importer.name()).isEqualTo(5_000);
            assertThat(r.inserted() + r.duplicates() + r.rejected()).as(importer.name()).isEqualTo(5_000);
            assertThat(count()).as(importer.name()).isEqualTo(r.inserted());
        }
    }

    @Test
    void reportsInvalidRowsWithLineNumbersAndSkipsDuplicates() throws Exception {
        String csv = """
                email,full_name,phone,city
                an@example.com,Nguyen Van An,0901234567,Ha Noi
                broken-email,No Email,,
                AN@example.com,"Nguyen, An",,Da Nang
                binh@example.com,,,
                """;
        ImportResult r = streaming().importCsv(stream(csv));

        assertThat(r.inserted()).isEqualTo(1);
        assertThat(r.duplicates()).isEqualTo(1); // same email, different case
        assertThat(r.rejected()).isEqualTo(2);
        assertThat(r.errors()).extracting(ImportResult.RowError::line).containsExactly(3L, 5L);
    }

    @Test
    void rejectsWrongHeader() {
        assertThatThrownBy(() -> streaming().importCsv(stream("mail,name\nx@y.z,X\n")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("header");
    }

    private CsvImporter streaming() {
        return importers.stream().filter(i -> i.name().equals("streaming")).findFirst().orElseThrow();
    }

    private long count() {
        return jdbc.queryForObject("SELECT count(*) FROM customer", Long.class);
    }

    private static InputStream stream(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }
}
