package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.configuration.Configuration;

import java.text.DecimalFormat;

public class Number {
  
  private static final String[] SUFFIXES = {"", "k", "M", "B", "T", "Q", "Qt", "Sx", "Sp", "O", "N", "D"};
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
  private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###.##");
  
  public static String formatSuffix(double value) {
    int index;
    for (index = 0; index < SUFFIXES.length - 1 && value >= 1000; index++) {
      value /= 1000;
    }
    return DECIMAL_FORMAT.format(value) + SUFFIXES[index];
  }
  
  public static String formatCommas(double value) {
    return COMMA_FORMAT.format(value);
  }
  
  public static double round(Object obj) {
    if (!(obj instanceof Double n))
      return 0.0;
    double multiplier = Math.pow(10, (Configuration.getDecimalPlaces() + 1));
    return Math.round(n * multiplier) / multiplier;
  }
  
  public static double getMinimumValue() {
    if (Configuration.getDecimalPlaces() <= 0)
      return 0.0;
    else
      return Math.pow(10, -Configuration.getDecimalPlaces());
  }
  
}