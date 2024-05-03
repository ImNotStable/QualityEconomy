package com.imnotstable.qualityeconomy.storage.importdata;

public interface ImportData<T> {
  
  boolean importData(T data);
  
}
