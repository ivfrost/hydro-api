package dev.ivfrost.hydro_backend.users;

public class UserNotAuthenticatedException extends RuntimeException {

  public UserNotAuthenticatedException(String message) {
    super(message);
  }

}
