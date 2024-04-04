package com.imnotstable.qualityeconomy.util;

import org.jetbrains.annotations.Range;

public class Version {
  
  private final int[] version = new int[3];
  private final PostFix postFix;
  
  public Version(String version) {
    if (version.contains("-")) {
      String[] split = version.split("-");
      version = split[0];
      this.postFix = PostFix.valueOf(split[1].toUpperCase());
    } else
      this.postFix = PostFix.RELEASE;
    String[] split = version.split("\\.");
    if (split.length != 3)
      throw new IllegalArgumentException("Invalid version format.");
    this.version[0] = Integer.parseInt(split[0]);
    this.version[1] = Integer.parseInt(split[1]);
    this.version[2] = Integer.parseInt(split[2]);
  }
  
  public static @Range(from = -1, to = 1) int compare(Version version1, Version version2) {
    return version1.compareTo(version2);
  }
  
  public int compareTo(Version other) {
    for (int i = 0; i < 3; i++) {
      if (this.version[i] > other.version[i])
        return 1;
      if (this.version[i] < other.version[i])
        return -1;
    }
    return Integer.compare(this.postFix.getPriority(), other.postFix.getPriority());
  }
  
  @Override
  public String toString() {
    return this.version[0] + "." + this.version[1] + "." + this.version[2] + (this.postFix == PostFix.RELEASE ? "" : "-" + this.postFix.name());
  }
  
  private enum PostFix {
    
    ALPHA, BETA, RELEASE, SPECIAL;
    
    private int getPriority() {
      return switch (this) {
        case ALPHA -> 0;
        case BETA -> 1;
        case RELEASE -> 2;
        case SPECIAL -> 3;
      };
    }
  }
  
}
