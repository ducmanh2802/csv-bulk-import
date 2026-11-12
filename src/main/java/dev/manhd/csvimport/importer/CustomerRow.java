package dev.manhd.csvimport.importer;

import java.util.List;
import java.util.regex.Pattern;

/** One validated CSV row: email,full_name,phone,city */
public record CustomerRow(String email, String fullName, String phone, String city) {

    public static final List<String> HEADER = List.of("email", "full_name", "phone", "city");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Returns the row, or throws IllegalArgumentException with a message for the error report. */
    public static CustomerRow from(List<String> f) {
        if (f.size() != HEADER.size()) {
            throw new IllegalArgumentException("expected " + HEADER.size() + " columns but got " + f.size());
        }
        String email = f.get(0).toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("invalid email: " + f.get(0));
        }
        if (f.get(1).isBlank()) {
            throw new IllegalArgumentException("full_name is required");
        }
        if (f.get(1).length() > 200 || f.get(2).length() > 30 || f.get(3).length() > 100) {
            throw new IllegalArgumentException("value too long");
        }
        return new CustomerRow(email, f.get(1), emptyToNull(f.get(2)), emptyToNull(f.get(3)));
    }

    private static String emptyToNull(String s) {
        return s.isBlank() ? null : s;
    }
}
