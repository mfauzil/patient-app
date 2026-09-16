package io.github.mfauzil.patientapp.dto;

import io.github.mfauzil.patientapp.entity.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PatientDto(
        Long id,

        @NotBlank @Size(max = 20) String pid,
        @NotBlank @Size(max = 60) String firstName,
        @NotBlank @Size(max = 60) String lastName,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull Gender gender,

        @NotBlank
        @Pattern(regexp = "^[0-9 +()-]{6,20}$", message = "invalid phone number format")
        String phoneNo,

        @Valid @NotNull AddressDto address
) {}