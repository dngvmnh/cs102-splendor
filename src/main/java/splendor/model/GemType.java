package splendor.model;

/**
 * Types of gems in Splendor.
 *
 * Five standard gem colors plus GOLD, which represents wild joker tokens.
 */
public enum GemType {
    WHITE,
    BLUE,
    GREEN,
    RED,
    BLACK,
    GOLD; // Joker / wild token

    /**
     * Returns true if this gem type is a standard colored gem (not gold).
     */
    public boolean isStandard() {
        return this != GOLD;
    }
}

