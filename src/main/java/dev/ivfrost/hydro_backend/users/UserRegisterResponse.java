package dev.ivfrost.hydro_backend.users;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserRegisterResponse(
    @NotNull
    List<String> recoveryCodes) {

}
