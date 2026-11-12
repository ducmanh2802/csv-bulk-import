package dev.manhd.csvimport.importer;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param batchSize         rows sent to the database in one JDBC batch
 * @param maxReportedErrors how many row errors are returned to the caller (the rest are only counted)
 */
@ConfigurationProperties(prefix = "app.import")
public record ImportProperties(int batchSize, int maxReportedErrors) {

    public ImportProperties {
        if (batchSize <= 0) {
            batchSize = 1000;
        }
        if (maxReportedErrors <= 0) {
            maxReportedErrors = 100;
        }
    }
}
