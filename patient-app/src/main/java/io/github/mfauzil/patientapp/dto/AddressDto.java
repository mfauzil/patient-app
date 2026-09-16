package io.github.mfauzil.patientapp.dto;

import io.github.mfauzil.patientapp.entity.AustralianState;
import jakarta.validation.constraints.*;

public record AddressDto(
        @NotBlank @Size(max = 200) String addressLine,
        @NotBlank @Size(max = 100) String suburb,
        @NotNull AustralianState state,
        @NotBlank @Pattern(regexp = "^\\d{4}$", message = "postcode must be 4 digits") String postcode
) {}