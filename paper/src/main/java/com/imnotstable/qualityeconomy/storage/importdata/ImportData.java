package com.imnotstable.qualityeconomy.storage.importdata;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface ImportData<T> {
  
  boolean importData(T data);
  
}
