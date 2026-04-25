package dev.ivfrost.hydro_backend.devices;

import dev.ivfrost.hydro_backend.tokens.DeviceMqttTokenPayload;
import dev.ivfrost.hydro_backend.tokens.DeviceTokenResponse;

public interface DeviceTokenProvider {

  DeviceTokenResponse generateMqttToken(DeviceMqttTokenPayload payload);
}
