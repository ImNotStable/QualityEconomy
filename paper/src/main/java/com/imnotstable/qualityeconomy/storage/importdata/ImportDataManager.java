package com.imnotstable.qualityeconomy.storage.importdata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

public class ImportDataManager {
  
  private static final ImportData<JsonObject> LEGACY = new Legacy();
  
  private static final ImportData<JsonObject> V1_5 = new V1_5();
  
  public static boolean importData(File file) {
    if (file.getName().endsWith(".json")) {
      JsonObject data = getFormattedData(file);
      if (data == null)
        return false;
      if (!data.has("VERSION"))
        return LEGACY.importData(data);
      if (data.get("VERSION").getAsString().equalsIgnoreCase("1.5.0"))
        return V1_5.importData(data);
    }
    throw new IllegalArgumentException("Invalid file format: " + file.getName());
  }
  
  private static @Nullable JsonObject getFormattedData(File file) {
    if (!file.exists())
      throw new IllegalArgumentException("File does not exist: " + file);
    try {
      String rawJSON = new String(Files.readAllBytes(file.toPath()));
      return new Gson().fromJson(rawJSON, JsonObject.class);
    } catch (IOException exception) {
      Logger.logError("Error while importing playerdata", exception);
    } catch (InvalidPathException exception) {
      Logger.logError("Invalid Path found", exception);
    }
    return null;
  }
  
}
