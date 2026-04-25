package dev.ivfrost.hydro_backend.devices;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record DeviceResponse(
    @NotNull Long id,
    @NotNull String name,
    @NotNull String location,
    @NotNull String firmware,
    @NotNull String technicalName,
    @NotNull String ip,
    @NotNull Instant createdAt,
    @NotNull Instant updatedAt,
    @NotNull Instant linkedAt,
    @NotNull Instant lastSeen,
    @NotNull Long userId,
    @NotNull Long order) {

}
