package dev.manhd.csvimport.csv;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvLineParserTest {

    @Test
    void splitsPlainFieldsAndTrims() {
        assertThat(CsvLineParser.parse("a@b.com, An ,0901,Ha Noi")).containsExactly("a@b.com", "An", "0901", "Ha Noi");
    }

    @Test
    void keepsCommasInsideQuotes() {
        assertThat(CsvLineParser.parse("a@b.com,\"Nguyen, Van An\",,Hue"))
                .containsExactly("a@b.com", "Nguyen, Van An", "", "Hue");
    }

    @Test
    void doubledQuoteIsALiteralQuote() {
        assertThat(CsvLineParser.parse("x,\"say \"\"hi\"\"\"")).containsExactly("x", "say \"hi\"");
    }

    @Test
    void emptyTrailingFieldIsKept() {
        assertThat(CsvLineParser.parse("a,b,")).containsExactly("a", "b", "");
    }

    @Test
    void unclosedQuoteIsAnError() {
        assertThatThrownBy(() -> CsvLineParser.parse("a,\"oops")).hasMessageContaining("unclosed quote");
    }
}
