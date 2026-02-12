package splendor.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Noble tile in Splendor.
 *
 * Nobles provide 3 prestige points and have requirements in permanent bonuses.
 */
public class Noble {

    private final String name;
    private final EnumMap<GemType, Integer> requirements;
    private final int prestigePoints;

    public Noble(String name, Map<GemType, Integer> requirements) {
        this.name = name;
        this.requirements = new EnumMap<>(GemType.class);
        if (requirements != null) {
            requirements.forEach((k, v) -> {
                if (k.isStandard() && v > 0) {
                    this.requirements.put(k, v);
                }
            });
        }
        this.prestigePoints = 3;
    }

    public String getName() {
        return name;
    }

    public Map<GemType, Integer> getRequirements() {
        return Collections.unmodifiableMap(requirements);
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    @Override
    public String toString() {
        return name + " (+" + prestigePoints + "P) req " + requirements;
    }
}

