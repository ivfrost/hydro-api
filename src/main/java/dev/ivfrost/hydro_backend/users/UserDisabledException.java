package dev.ivfrost.hydro_backend.users;

public class UserDisabledException extends RuntimeException {

  public UserDisabledException(Long userId) {
    super("User with ID " + userId + " is disabled.");
  }

}
