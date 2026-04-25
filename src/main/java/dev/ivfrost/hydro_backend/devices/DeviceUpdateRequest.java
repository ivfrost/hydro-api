package dev.ivfrost.hydro_backend.devices;

public record DeviceUpdateRequest(
    String friendlyName,
    String technicalName,
    String firmware,
    Long userId,
    Long displayOrder
) {

}
