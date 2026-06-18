package dev.ivfrost.hydro_backend.devices;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceAuthRequest(
    @NotNull(message = "Device key is required")
    String key,

    @NotBlank(message = "Device secret is required")
    String secret
) {

}
