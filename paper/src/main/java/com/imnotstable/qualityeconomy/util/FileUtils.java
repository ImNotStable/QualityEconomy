package com.imnotstable.qualityeconomy.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {
  
  public static String compress(File file) {
    String compressedFileName = file.getName() + ".zip";
    Path path = file.toPath();
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path.resolveSibling(compressedFileName)))) {
      ZipEntry zipEntry = new ZipEntry(path.getFileName().toString());
      zos.putNextEntry(zipEntry);
      Files.copy(path, zos);
      zos.closeEntry();
      Files.delete(path);
    } catch (IOException exception) {
      throw new RuntimeException("Error while compressing file", exception);
    }
    return compressedFileName;
  }
  
  public static ByteArrayOutputStream decompress(File file) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file.toPath()))) {
      ZipEntry entry = zis.getNextEntry();
      if (entry != null) {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
          byteArrayOutputStream.write(buffer, 0, len);
        }
        zis.closeEntry();
      }
    } catch (IOException exception) {
      throw new RuntimeException("Error while decompressing file", exception);
    }
    return byteArrayOutputStream;
  }
  
}
