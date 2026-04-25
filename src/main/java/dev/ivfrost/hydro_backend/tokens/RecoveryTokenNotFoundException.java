package dev.ivfrost.hydro_backend.tokens;

public class RecoveryTokenNotFoundException extends RuntimeException {

  public RecoveryTokenNotFoundException(String message) {
    super(message);
  }

}
