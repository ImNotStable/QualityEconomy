package util;

import com.imnotstable.qualityeconomy.util.CurrencyFormatter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyFormatterTest {
    
    @ParameterizedTest(name = "{0} -> {1} -> {0}")
    @CsvSource(textBlock = """
        1;1
        1000;1,000
        1001;1,001
        100000;100,000
        1000000;1,000,000
        1000001;1,000,001
      """, delimiter = ';')
    public void testCommaFormat(double value, String expectedFormatedValue) {
        String formattedValue = CurrencyFormatter.format(value, CurrencyFormatter.FormatType.COMMAS);
        assertEquals(formattedValue, expectedFormatedValue, value + " should've been formatted to " + expectedFormatedValue + " found " + formattedValue);
        double unformattedValue = CurrencyFormatter.unformat(formattedValue);
        assertEquals(unformattedValue, value, formattedValue + " should've been unformatted to " + value + " found " + unformattedValue);
    }
    
    @ParameterizedTest(name = "{0} -> {1} -> {2}")
    @CsvSource(textBlock = """
        1;1;1
        1001;1k;1000
        1234;1.23k;1230
        102000;102k;102000
        1000000;1M;1000000
        1000001;1M;1000000
        123456789;123.46M;123460000
      """, delimiter = ';')
    public void testSuffixFormat(double value, String expectedFormatedValue, double expectedUnformattedValue) {
        String formattedValue = CurrencyFormatter.format(value, CurrencyFormatter.FormatType.SUFFIX);
        assertEquals(formattedValue, expectedFormatedValue, value + " should've been formatted to " + expectedFormatedValue + " found " + formattedValue);
        double unformattedValue = CurrencyFormatter.unformat(formattedValue);
        assertEquals(unformattedValue, expectedUnformattedValue, formattedValue + " should've been unformatted to " + value + " found " + unformattedValue);
    }
    
}
