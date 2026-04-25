package dev.ivfrost.hydro_backend.tokens;

import java.util.List;

public record MqttTokenPayload(Long userId, List<String> topics) {

}
