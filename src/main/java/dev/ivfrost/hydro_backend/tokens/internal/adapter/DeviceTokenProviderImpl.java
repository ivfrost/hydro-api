package dev.ivfrost.hydro_backend.tokens.internal.adapter;

import dev.ivfrost.hydro_backend.tokens.DeviceTokenProvider;
import dev.ivfrost.hydro_backend.tokens.DeviceMqttTokenPayload;
import dev.ivfrost.hydro_backend.tokens.DeviceTokenResponse;
import dev.ivfrost.hydro_backend.tokens.internal.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DeviceTokenProviderImpl implements DeviceTokenProvider {

  private final TokenService tokenService;

  @Override
  public DeviceTokenResponse generateMqttToken(DeviceMqttTokenPayload payload) {
    return tokenService.generateDeviceMqttToken(payload);
  }
}
