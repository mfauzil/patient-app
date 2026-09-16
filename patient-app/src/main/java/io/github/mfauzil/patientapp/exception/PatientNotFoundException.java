package io.github.mfauzil.patientapp.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Long id) {
        super("Patient not found with id " + id);
    }
}