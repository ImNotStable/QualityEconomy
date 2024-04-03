package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.QualityEconomy;
import lombok.AllArgsConstructor;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Number {
  
  private static final List<String> SUFFIXES = List.of("", "k", "M", "B", "T", "Q", "Qt", "Sx", "Sp", "O", "N", "D");
  private static final List<String> SUFFIXES_UPPER = List.of("", "k", "M", "B", "T", "Q", "QI", "SX", "SP", "O", "N", "D");
  private static final DecimalFormat NORMAL_FORMAT = new DecimalFormat("#");
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
  private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###.##");
  private static final Pattern SUFFIX_PATTERN = Pattern.compile("\\d+(?:\\.\\d*)?[a-zA-Z]{1,2}");
  private static final Pattern COMMA_PATTERN = Pattern.compile("^(?:[1-9][0-9]{0,2}|1000)(?:,\\d{3})*(?:\\.\\d*)?$");
  
  public static String format(double value, FormatType formatType) {
    return formatType.format(value);
  }
  
  public static double unformat(String value) throws NumberFormatException {
    if (SUFFIX_PATTERN.matcher(value).matches()) {
      String number = value.replaceAll("[^\\d.]", "");
      String suffix = value.replaceAll("[\\d.]", "").toUpperCase();
      int index = SUFFIXES_UPPER.indexOf(suffix);
      if (index == -1)
        throw new NumberFormatException("Invalid suffix");
      double multiplier = Math.pow(1000, index);
      return Double.parseDouble(number) * multiplier;
    }
    if (COMMA_PATTERN.matcher(value).matches())
      return Double.parseDouble(value.replaceAll(",", ""));
    return Double.parseDouble(value);
  }
  
  public static double roundObj(Object obj) {
    return (obj instanceof Double n) ? round(n) : 0.0;
  }
  
  public static double round(double n) {
    if (QualityEconomy.getQualityConfig().DECIMAL_PLACES == -1)
      return n;
    double multiplier = Math.pow(10, QualityEconomy.getQualityConfig().DECIMAL_PLACES);
    return Math.floor(n * multiplier) / multiplier;
  }
  
  public static double getMinimumValue() {
    if (QualityEconomy.getQualityConfig().DECIMAL_PLACES <= 0)
      return 0.0;
    return Math.pow(10, -QualityEconomy.getQualityConfig().DECIMAL_PLACES);
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
    COMMAS(COMMA_FORMAT::format);
    
    private final Function<Double, String> formatter;
    
    public String format(double value) {
      return formatter.apply(value);
    }
  }
  
}