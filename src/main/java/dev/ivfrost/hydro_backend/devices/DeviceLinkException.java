package dev.ivfrost.hydro_backend.devices;

public class DeviceLinkException extends RuntimeException {

  public DeviceLinkException(String message) {
    super(message);
  }

  public DeviceLinkException(String message, Throwable cause) {
    super(message, cause);
  }

}
