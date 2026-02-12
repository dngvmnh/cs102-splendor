package splendor.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a pool of tokens (either on the board or owned by a player).
 */
public class TokenPool {

    private final EnumMap<GemType, Integer> tokens = new EnumMap<>(GemType.class);

    public TokenPool() {
        for (GemType type : GemType.values()) {
            tokens.put(type, 0);
        }
    }

    public TokenPool(Map<GemType, Integer> initial) {
        this();
        if (initial != null) {
            initial.forEach(this::set);
        }
    }

    public int get(GemType type) {
        return tokens.getOrDefault(type, 0);
    }

    public void set(GemType type, int amount) {
        tokens.put(type, Math.max(0, amount));
    }

    public void add(GemType type, int delta) {
        set(type, get(type) + delta);
    }

    public boolean canRemove(GemType type, int amount) {
        return amount >= 0 && get(type) >= amount;
    }

    public void remove(GemType type, int amount) {
        if (!canRemove(type, amount)) {
            throw new IllegalArgumentException("Not enough tokens of type " + type);
        }
        set(type, get(type) - amount);
    }

    public int totalTokens() {
        return tokens.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<GemType, Integer> asUnmodifiableMap() {
        return Collections.unmodifiableMap(tokens);
    }

    public TokenPool copy() {
        return new TokenPool(tokens);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (GemType type : GemType.values()) {
            sb.append(type.name().charAt(0))
              .append(":")
              .append(get(type))
              .append(" ");
        }
        return sb.toString().trim();
    }
}

