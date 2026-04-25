package dev.ivfrost.hydro_backend.devices;

public class DeviceFetchException extends RuntimeException {

  public DeviceFetchException(String message) {
    super(message);
  }

  public DeviceFetchException(String message, Throwable cause) {
    super(message, cause);
  }

}
