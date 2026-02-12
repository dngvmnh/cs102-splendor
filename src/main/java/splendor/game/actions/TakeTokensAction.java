package splendor.game.actions;

import splendor.model.GemType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Action: take tokens from the central supply.
 *
 * The taken map should contain only standard gem types with positive amounts.
 */
public class TakeTokensAction implements GameAction {

    private final EnumMap<GemType, Integer> taken = new EnumMap<>(GemType.class);

    public TakeTokensAction(Map<GemType, Integer> taken) {
        if (taken != null) {
            taken.forEach((type, amount) -> {
                if (type != null && type.isStandard() && amount > 0) {
                    this.taken.put(type, amount);
                }
            });
        }
    }

    @Override
    public ActionType getType() {
        return ActionType.TAKE_TOKENS;
    }

    public Map<GemType, Integer> getTaken() {
        return Collections.unmodifiableMap(taken);
    }
}

