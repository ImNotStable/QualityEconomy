package com.imnotstable.qualityeconomy.util;

import java.util.regex.Pattern;

public class Misc {
  
  private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
  
  public static boolean isValidUUID(String uuid) {
    return UUID_PATTERN.matcher(uuid).matches();
  }
  
}
