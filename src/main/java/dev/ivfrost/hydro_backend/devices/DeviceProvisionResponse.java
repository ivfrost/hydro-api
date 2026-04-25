package dev.ivfrost.hydro_backend.devices;

public record DeviceProvisionResponse(
    DeviceResponse device,
    String secret
) {

}