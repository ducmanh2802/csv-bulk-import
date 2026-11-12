package dev.manhd.csvimport;

import org.springframework.boot.SpringApplication;

public class TestCsvBulkImportApplication {

    public static void main(String[] args) {
        SpringApplication.from(CsvBulkImportApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
