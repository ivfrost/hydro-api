package dev.ivfrost.hydro_backend.tokens;

public class RecoveryTokenMismatchException extends RuntimeException {

  public RecoveryTokenMismatchException(String message) {
    super(message);
  }

}
