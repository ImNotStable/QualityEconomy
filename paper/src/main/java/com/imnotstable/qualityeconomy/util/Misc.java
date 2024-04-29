package com.imnotstable.qualityeconomy.util;

import java.util.Optional;
import java.util.UUID;

public class Misc {
  
  public static Optional<UUID> isUUID(String uuid) {
    try {
      return Optional.of(UUID.fromString(uuid));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
  
  public static boolean equals(Object object, Object... comparable) {
    for (Object o : comparable)
      if (object.equals(o)) return true;
    return false;
  }
  
}
