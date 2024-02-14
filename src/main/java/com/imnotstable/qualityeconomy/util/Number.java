package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.configuration.Configuration;

import java.text.DecimalFormat;
import java.util.List;

public class Number {
  
  private static final List<String> SUFFIXES = List.of("", "k", "M", "B", "T", "Q", "Qt", "Sx", "Sp", "O", "N", "D");
  private static final DecimalFormat NORMAL_FORMAT = new DecimalFormat("#");
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
  private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###.##");
  
  public static String format(double value, FormatType formatType) {
    switch (formatType) {
      case NORMAL -> {
        return NORMAL_FORMAT.format(value);
      }
      case SUFFIX -> {
        int index;
        for (index = 0; index < SUFFIXES.size() - 1 && value >= 1000; index++) {
          value /= 1000;
        }
        return DECIMAL_FORMAT.format(value) + SUFFIXES.get(index);
      }
      case COMMAS -> {
        return COMMA_FORMAT.format(value);
      }
      default -> throw new IllegalArgumentException("Invalid format type");
    }
  }
  
  public static double unformatSuffix(String value) throws NumberFormatException {
    String number = value.replaceAll("[^\\d.]", "");
    String suffix = value.replaceAll("[\\d.]", "");
    int index = SUFFIXES.indexOf(suffix);
    if (index == -1)
      throw new NumberFormatException("Invalid suffix");
    double multiplier = Math.pow(1000, index);
    return Double.parseDouble(number) * multiplier;
  }
  
  public static double roundObj(Object obj) {
    if (obj instanceof Double n)
      return round(n);
    return 0.0;
  }
  
  public static double round(double n) {
    if (Configuration.getDecimalPlaces() == -1)
      return n;
    double multiplier = Math.pow(10, Configuration.getDecimalPlaces());
    return Math.floor(n * multiplier) / multiplier;
  }
  
  public static double getMinimumValue() {
    if (Configuration.getDecimalPlaces() <= 0)
      return 0.0;
    else
      return Math.pow(10, -Configuration.getDecimalPlaces());
  }
  
  public enum FormatType {
    NORMAL, SUFFIX, COMMAS
  }
  
}