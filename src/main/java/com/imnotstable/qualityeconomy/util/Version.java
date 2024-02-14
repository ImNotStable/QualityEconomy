package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.QualityEconomy;
import lombok.Getter;
import org.jetbrains.annotations.Range;

public class Version {
  
  private static final @Getter Version pluginVersion = new Version(QualityEconomy.getInstance().getDescription().getVersion());
  private final int[] version = new int[3];
  
  public Version(String version) {
    if (version.contains("-"))
      version = version.split("-")[0];
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
    if (this.version[0] > other.version[0])
      return 1;
    else if (this.version[0] < other.version[0])
      return -1;
    if (this.version[1] > other.version[1])
      return 1;
    else if (this.version[1] < other.version[1])
      return -1;
    return Integer.compare(this.version[2], other.version[2]);
  }
  
  @Override
  public String toString() {
    return this.version[0] + "." + this.version[1] + "." + this.version[2];
  }
  
}
