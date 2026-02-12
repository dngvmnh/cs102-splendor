package splendor.game.actions;

import splendor.model.GemType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Action: discard tokens down to the maximum hand size.
 */
public class DiscardTokensAction implements GameAction {

    private final EnumMap<GemType, Integer> discards = new EnumMap<>(GemType.class);

    public DiscardTokensAction(Map<GemType, Integer> discards) {
        if (discards != null) {
            discards.forEach((type, amount) -> {
                if (type != null && amount > 0) {
                    this.discards.put(type, amount);
                }
            });
        }
    }

    @Override
    public ActionType getType() {
        return ActionType.DISCARD_TOKENS;
    }

    public Map<GemType, Integer> getDiscards() {
        return Collections.unmodifiableMap(discards);
    }
}

