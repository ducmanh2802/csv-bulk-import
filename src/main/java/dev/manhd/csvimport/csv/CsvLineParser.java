package dev.manhd.csvimport.csv;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses one CSV line (RFC 4180 subset): comma separator, fields may be wrapped in double quotes,
 * a doubled quote ("") inside a quoted field is a literal quote. Multi-line quoted fields are not supported.
 */
public final class CsvLineParser {

    private CsvLineParser() {
    }

    public static List<String> parse(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    boolean escapedQuote = i + 1 < line.length() && line.charAt(i + 1) == '"';
                    if (escapedQuote) {
                        current.append('"');
                        i++; // skip the second quote of ""
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (inQuotes) {
            throw new IllegalArgumentException("unclosed quote");
        }
        fields.add(current.toString().trim());
        return fields;
    }
}
