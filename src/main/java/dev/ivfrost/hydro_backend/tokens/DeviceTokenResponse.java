package dev.ivfrost.hydro_backend.tokens;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record DeviceTokenResponse(
    @NotNull
    String value,
    @NotNull
    String type,
    @NotNull
    Instant expiryDate,
    @NotNull
    Long userId) {

}