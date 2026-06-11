package dev.ivfrost.hydro_backend.tokens;

public interface DeviceTokenProvider {

  DeviceTokenResponse generateMqttToken(DeviceMqttTokenPayload payload);
}
