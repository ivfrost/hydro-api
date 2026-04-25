package dev.ivfrost.hydro_backend.users;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserAuthRequest(

    @NotBlank
    @Email
    @Schema(requiredMode = RequiredMode.REQUIRED)
    String email,

    @NotBlank
    @Schema(requiredMode = RequiredMode.REQUIRED)
    String password) {

}
