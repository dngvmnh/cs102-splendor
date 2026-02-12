package splendor.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Development card in Splendor.
 *
 * Each card has:
 * - a level (1, 2, or 3)
 * - prestige points
 * - a permanent bonus (standard gem color)
 * - a cost in tokens
 */
public class Card {

    private final int id; // Unique identifier per game for UI reference
    private final int level;
    private final int prestigePoints;
    private final GemType bonus;
    private final EnumMap<GemType, Integer> cost;

    public Card(int id,
                int level,
                int prestigePoints,
                GemType bonus,
                Map<GemType, Integer> cost) {
        if (bonus == null || !bonus.isStandard()) {
            throw new IllegalArgumentException("Card bonus must be a standard gem type");
        }
        this.id = id;
        this.level = level;
        this.prestigePoints = prestigePoints;
        this.bonus = bonus;
        this.cost = new EnumMap<>(GemType.class);
        for (GemType type : GemType.values()) {
            if (type == GemType.GOLD) {
                continue;
            }
            int value = cost != null ? cost.getOrDefault(type, 0) : 0;
            if (value > 0) {
                this.cost.put(type, value);
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public GemType getBonus() {
        return bonus;
    }

    public Map<GemType, Integer> getCost() {
        return Collections.unmodifiableMap(cost);
    }

    @Override
    public String toString() {
        return "L" + level + " [" + prestigePoints + "P] " + bonus +
               " cost " + cost;
    }
}

