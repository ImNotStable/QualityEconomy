package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.QualityEconomy;
import lombok.Getter;

public class Version {
  
  private static final Version pluginVersion = new Version(QualityEconomy.getInstance().getDescription().getVersion());
  
  public static boolean requiresUpdate(Version version) {
    if (pluginVersion.getMajor() < version.getMajor())
      return true;
    if (pluginVersion.getMajor() < version.getMinor())
      return true;
    return pluginVersion.getPatch() < version.getPatch();
  }
  
  private final @Getter int major;
  private final @Getter int minor;
  private final @Getter int patch;
  
  public Version(String version) {
    if (version.contains("-"))
      version = version.split("-")[0];
    String[] split = version.split("\\.");
    this.major = Integer.parseInt(split[0]);
    this.minor = Integer.parseInt(split[1]);
    this.patch = Integer.parseInt(split[2]);
  }
  
  public Version(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }
  
}
