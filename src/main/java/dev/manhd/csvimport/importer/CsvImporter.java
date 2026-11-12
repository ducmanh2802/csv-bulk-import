package dev.manhd.csvimport.importer;

import java.io.IOException;
import java.io.InputStream;

/** Imports customers from a CSV stream with header {@code email,full_name,phone,city}. */
public interface CsvImporter {

    String name();

    ImportResult importCsv(InputStream csv) throws IOException;
}
