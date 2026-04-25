package dev.ivfrost.hydro_backend.tokens;

import dev.ivfrost.hydro_backend.tokens.internal.Token;

public class TokenNotFoundException extends RuntimeException {

  public TokenNotFoundException(Token.TokenType type) {
    super("Token of type " + type + " not found or invalid.");
  }

  public TokenNotFoundException(String message) {
    super(message);
  }

}
