package dev.ivfrost.hydro_backend.devices;

import java.util.List;

public record DeviceOrderSaveRequest(List<Long> deviceIds) {

}
