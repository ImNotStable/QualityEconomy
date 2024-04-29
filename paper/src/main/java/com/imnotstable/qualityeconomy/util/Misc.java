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
  
}
