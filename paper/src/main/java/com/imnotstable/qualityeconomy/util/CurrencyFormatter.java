package com.imnotstable.qualityeconomy.util;

import lombok.AllArgsConstructor;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class CurrencyFormatter {
  
  private static final List<String> SUFFIXES = List.of("", "k", "M", "B", "T", "Q", "Qi", "Sx", "Sp", "O", "N", "D");
  private static final List<String> UPPERCASE_SUFFIXES = List.of("", "K", "M", "B", "T", "Q", "QI", "SX", "SP", "O", "N", "D");
  private static final DecimalFormat NORMAL_FORMAT = new DecimalFormat("#");
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
  private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###.##");
  private static final Pattern SUFFIX_PATTERN = Pattern.compile("\\d{1,3}(?:\\.\\d{0,2})?[a-zA-Z]{1,2}");
  private static final Pattern COMMA_PATTERN = Pattern.compile("[1-9][0-9]{0,2}(?:,\\d{3})*(?:\\.\\d*)?");
  
  public static String format(double value, FormatType formatType) {
    return formatType.format(value);
  }
  
  public static double unformat(String value) throws NumberFormatException {
    if (SUFFIX_PATTERN.matcher(value).matches()) {
      String number = value.replaceAll("[^\\d.]", "");
      String suffix = value.replaceAll("[\\d.]", "").toUpperCase();
      int index = UPPERCASE_SUFFIXES.indexOf(suffix);
      if (index == -1)
        throw new NumberFormatException("Invalid suffix");
      double multiplier = Math.pow(1000, index);
      return Double.parseDouble(number) * multiplier;
    }
    if (COMMA_PATTERN.matcher(value).matches())
      return Double.parseDouble(value.replaceAll(",", ""));
    return Double.parseDouble(value);
  }
  
  @AllArgsConstructor
  public enum FormatType {
    NORMAL(NORMAL_FORMAT::format),
    SUFFIX(value -> {
      int index;
      for (index = 0; index < SUFFIXES.size() - 1 && value >= 1000; index++) {
        value /= 1000;
      }
      return DECIMAL_FORMAT.format(value) + SUFFIXES.get(index);
    }),
    UPPERCASE_SUFFIX(value -> {
      int index;
      for (index = 0; index < SUFFIXES.size() - 1 && value >= 1000; index++) {
        value /= 1000;
      }
      return DECIMAL_FORMAT.format(value) + UPPERCASE_SUFFIXES.get(index);
    }),
    COMMAS(COMMA_FORMAT::format),
    QUALITY(value -> {
      if (value < 1_000_000_000D)
        return DECIMAL_FORMAT.format(value);
      return SUFFIX.format(value);
    });
    
    private final Function<Double, String> formatter;
    
    public String format(double value) {
      return formatter.apply(value);
    }
  }
  
}