package dev.ivfrost.hydro_backend.tokens;

public class ExpiredVerificationToken extends RuntimeException {

  public ExpiredVerificationToken(String message) {
    super(message);
  }

}
