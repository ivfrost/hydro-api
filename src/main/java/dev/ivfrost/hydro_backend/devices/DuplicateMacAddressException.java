package dev.ivfrost.hydro_backend.devices;

public class DuplicateMacAddressException extends RuntimeException {

  public DuplicateMacAddressException(String macAddress) {
    super("Device with MAC address " + macAddress + " already exists.");
  }

  public DuplicateMacAddressException(String message, Throwable cause) {
    super(message, cause);
  }

}
