package net.mcclendo.arbiter;

public class ArbiterException extends RuntimeException {

    public ArbiterException(
            final String message) {
        super(message);
    }

    public ArbiterException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
