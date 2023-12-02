package com.imnotstable.qualityeconomy.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class Pair<K, V> {
  
  private K key;
  private V value;
  
  @Override
  public String toString() {
    return "(" + key + ", " + value + ")";
  }
  
}
