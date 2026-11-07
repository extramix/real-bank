package dev.extramix;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OpenAccountRequest(
        @NotBlank(message = "id cannot be empty")
        String id,
        @NotNull(message = "currency cannot be empty")
        Currency currency) {
}
