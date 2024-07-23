package com.imnotstable.qualityeconomy.storage.importdata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.util.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ImportDataManager {
  
  private static final ImportData<JsonObject> LEGACY = new Legacy();
  
  private static final ImportData<JsonObject> V1_5_2 = new V1_5_2();
  
  private static final ImportData<JsonObject> V1_5_4 = new V1_5_4();
  
  public static boolean importData(File file) {
    if (!file.getName().endsWith(".json"))
      throw new IllegalArgumentException("Invalid file format: " + file.getName());
    JsonObject data = getFormattedData(file);
    if (data == null)
      return false;
    if (!data.has("VERSION"))
      return LEGACY.importData(data);
    return switch (data.get("VERSION").getAsString()) {
      case "1.5.1", "1.5.2" -> V1_5_2.importData(data);
      case "1.5.3", "1.5.4" -> V1_5_4.importData(data);
      default -> throw new IllegalStateException("Unexpected value: " + data.get("VERSION").getAsString());
    };
  }
  
  private static @Nullable JsonObject getFormattedData(File file) {
    if (!file.exists())
      throw new IllegalArgumentException("File does not exist: " + file);
    String rawJSON = FileUtils.decompress(file).toString();
    return new Gson().fromJson(rawJSON, JsonObject.class);
  }
  
}
