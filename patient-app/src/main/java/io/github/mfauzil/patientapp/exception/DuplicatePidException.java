package io.github.mfauzil.patientapp.exception;

public class DuplicatePidException extends RuntimeException {
    public DuplicatePidException(String pid) {
        super("PID already exists: " + pid);
    }
}