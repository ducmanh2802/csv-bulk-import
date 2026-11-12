package dev.manhd.csvimport.importer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerRowTest {

    @Test
    void normalizesEmailAndEmptyOptionalFields() {
        CustomerRow row = CustomerRow.from(List.of("An@Example.COM", "Nguyen Van An", "", ""));
        assertThat(row).isEqualTo(new CustomerRow("an@example.com", "Nguyen Van An", null, null));
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> CustomerRow.from(List.of("no-at-sign", "An", "", "")))
                .hasMessageContaining("invalid email");
    }

    @Test
    void rejectsMissingName() {
        assertThatThrownBy(() -> CustomerRow.from(List.of("a@b.com", " ", "", "")))
                .hasMessageContaining("full_name");
    }

    @Test
    void rejectsWrongColumnCount() {
        assertThatThrownBy(() -> CustomerRow.from(List.of("a@b.com", "An")))
                .hasMessageContaining("expected 4 columns");
    }
}
