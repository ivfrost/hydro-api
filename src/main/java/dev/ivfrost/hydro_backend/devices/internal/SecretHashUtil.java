package dev.ivfrost.hydro_backend.devices.internal;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class SecretHashUtil {

  public static String hash(String rawSecret) {
    return BCrypt.hashpw(rawSecret, BCrypt.gensalt());
  }

  public static boolean matches(String rawSecret, String storedHash) {
    return BCrypt.checkpw(rawSecret, storedHash);
  }
}
