package dev.manhd.csvimport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CsvBulkImportApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsvBulkImportApplication.class, args);
    }
}
