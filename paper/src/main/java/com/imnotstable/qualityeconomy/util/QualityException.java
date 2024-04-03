package com.imnotstable.qualityeconomy.util;

public class QualityException extends Exception {
  
  
  public QualityException(String message, Exception subException, boolean useQualityError) {
    super(message, subException);
    if (useQualityError)
      new Debug.QualityError(message, subException).log();
  }
  
  public QualityException(String message, Exception subException) {
    this(message, subException, true);
  }
  
  public QualityException(String message, boolean useQualityError) {
    this(message, null, useQualityError);
  }
  
  public QualityException(String message) {
    this(message, true);
  }
  
  public QualityException() {
    super();
  }
  
}
