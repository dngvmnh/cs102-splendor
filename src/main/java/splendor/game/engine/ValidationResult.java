package splendor.game.engine;

/**
 * Result of validating a game action.
 */
public class ValidationResult {

    private final boolean valid;
    private final String message;

    private static final ValidationResult OK = new ValidationResult(true, "OK");

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResult ok() {
        return OK;
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}

