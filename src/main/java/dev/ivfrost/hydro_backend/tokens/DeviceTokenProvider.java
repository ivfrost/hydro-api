package dev.ivfrost.hydro_backend.tokens;

public interface DeviceTokenProvider {

   TokenResponse generateMqttToken(MqttTokenPayload payload);

   void validateMqttToken(String token);

   boolean validateMqttAcl(String token, String topic, int action);

}
