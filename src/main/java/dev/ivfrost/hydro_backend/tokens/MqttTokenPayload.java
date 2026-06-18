package dev.ivfrost.hydro_backend.tokens;

import java.util.List;

public record MqttTokenPayload(Long userId, Long deviceId, List<String> topics) {

}
