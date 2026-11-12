package dev.manhd.csvimport.bench;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/** Writes a realistic test file: ~1% invalid rows and ~0.5% duplicate emails, fixed seed so runs are comparable. */
public final class CsvGenerator {

    private static final String[] CITIES = {"Ha Noi", "Ho Chi Minh", "Da Nang", "Hai Phong", "Can Tho"};

    private CsvGenerator() {
    }

    public static Path generate(Path file, int rows) throws IOException {
        Random random = new Random(42);
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write("email,full_name,phone,city\n");
            for (int i = 0; i < rows; i++) {
                int r = random.nextInt(1000);
                if (r < 10) {
                    out.write("not-an-email-" + i + ",Broken Row,,\n");                  // invalid
                } else if (r < 15 && i > 0) {
                    out.write("user" + random.nextInt(i) + "@example.com,Again,,Ha Noi\n"); // duplicate
                } else {
                    out.write("user" + i + "@example.com,\"Nguyen, Van " + i + "\",09" + (10000000 + i % 90000000)
                            + "," + CITIES[i % CITIES.length] + "\n");
                }
            }
        }
        return file;
    }
}
