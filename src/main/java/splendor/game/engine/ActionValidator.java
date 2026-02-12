package splendor.game.engine;

import splendor.game.actions.*;
import splendor.model.Board;
import splendor.model.GemType;
import splendor.model.Player;

import java.util.Map;

/**
 * Validates whether actions are legal according to Splendor rules.
 */
public class ActionValidator {

    public ValidationResult validate(GameState state, int currentPlayerIndex, GameAction action) {
        if (action == null) {
            return ValidationResult.error("No action selected.");
        }
        Player player = state.getPlayers().get(currentPlayerIndex);
        Board board = state.getBoard();

        return switch (action.getType()) {
            case TAKE_TOKENS -> validateTakeTokens(board, player, (TakeTokensAction) action);
            case BUY_CARD -> validateBuyCard(board, player, (BuyCardAction) action);
            case RESERVE_CARD -> validateReserve(board, player, (ReserveCardAction) action);
            case DISCARD_TOKENS -> validateDiscard(player, (DiscardTokensAction) action);
        };
    }

    private ValidationResult validateTakeTokens(Board board, Player player, TakeTokensAction action) {
        Map<GemType, Integer> taken = action.getTaken();
        if (taken.isEmpty()) {
            return ValidationResult.error("You must take some tokens.");
        }

        long nonZero = taken.values().stream().filter(v -> v > 0).count();
        if (nonZero == 3) {
            // 3 different colors, each exactly 1
            for (Map.Entry<GemType, Integer> e : taken.entrySet()) {
                GemType type = e.getKey();
                int amount = e.getValue();
                if (!type.isStandard()) {
                    return ValidationResult.error("You may not take gold tokens.");
                }
                if (amount != 1) {
                    return ValidationResult.error("To take three colors, you must take 1 of each.");
                }
                if (board.getSupplyTokens().get(type) < 1) {
                    return ValidationResult.error("Not enough tokens in supply for " + type + ".");
                }
            }
        } else if (nonZero == 1) {
            // 2 of the same color
            Map.Entry<GemType, Integer> entry = taken.entrySet().iterator().next();
            GemType type = entry.getKey();
            int amount = entry.getValue();
            if (!type.isStandard()) {
                return ValidationResult.error("You may not take gold tokens.");
            }
            if (amount != 2) {
                return ValidationResult.error("To take two of a color, you must take exactly 2.");
            }
            if (board.getSupplyTokens().get(type) < 4) {
                return ValidationResult.error("You may take two of a color only if at least 4 are in the supply.");
            }
        } else {
            return ValidationResult.error("You must either take 3 different colors or 2 of one color.");
        }

        // Token limit is enforced via discard action, not here.
        return ValidationResult.ok();
    }

    private ValidationResult validateBuyCard(Board board, Player player, BuyCardAction action) {
        if (action.isFromReserved()) {
            int idx = action.getCardIndex();
            if (idx < 0 || idx >= player.getReservedCards().size()) {
                return ValidationResult.error("Reserved card index is out of range.");
            }
            if (!canAfford(player, player.getReservedCards().get(idx))) {
                return ValidationResult.error("You cannot afford that reserved card.");
            }
        } else {
            int level = action.getLevel();
            int idx = action.getCardIndex();
            try {
                switch (level) {
                    case 1 -> board.getLevel1FaceUp().get(idx);
                    case 2 -> board.getLevel2FaceUp().get(idx);
                    case 3 -> board.getLevel3FaceUp().get(idx);
                    default -> {
                        return ValidationResult.error("Invalid card level.");
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                return ValidationResult.error("No card at that position.");
            }
            if (!canAfford(player, switch (level) {
                case 1 -> board.getLevel1FaceUp().get(idx);
                case 2 -> board.getLevel2FaceUp().get(idx);
                case 3 -> board.getLevel3FaceUp().get(idx);
                default -> null;
            })) {
                return ValidationResult.error("You cannot afford that card.");
            }
        }
        return ValidationResult.ok();
    }

    private boolean canAfford(Player player, splendor.model.Card card) {
        int gold = player.getTokens().get(GemType.GOLD);
        int requiredGold = 0;
        for (Map.Entry<GemType, Integer> e : card.getCost().entrySet()) {
            GemType color = e.getKey();
            int cost = e.getValue();
            int bonus = player.getBonus(color);
            int effectiveCost = Math.max(0, cost - bonus);
            int playerTokens = player.getTokens().get(color);
            if (playerTokens < effectiveCost) {
                requiredGold += (effectiveCost - playerTokens);
            }
        }
        return requiredGold <= gold;
    }

    private ValidationResult validateReserve(Board board, Player player, ReserveCardAction action) {
        if (!player.canReserveMore()) {
            return ValidationResult.error("You already have the maximum of 3 reserved cards.");
        }
        int level = action.getLevel();
        if (action.isFromTopOfDeck()) {
            // Deck may be empty, but official rules: cannot reserve from empty deck.
            if (!board.hasCardsInDeck(level)) {
                return ValidationResult.error("That deck is empty; you cannot reserve from it.");
            }
        } else {
            int idx = action.getCardIndex();
            try {
                switch (level) {
                    case 1 -> board.getLevel1FaceUp().get(idx);
                    case 2 -> board.getLevel2FaceUp().get(idx);
                    case 3 -> board.getLevel3FaceUp().get(idx);
                    default -> {
                        return ValidationResult.error("Invalid card level.");
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                return ValidationResult.error("No card at that position to reserve.");
            }
        }
        return ValidationResult.ok();
    }

    private ValidationResult validateDiscard(Player player, DiscardTokensAction action) {
        Map<GemType, Integer> discards = action.getDiscards();
        if (discards.isEmpty()) {
            return ValidationResult.error("You must discard at least one token.");
        }
        int totalDiscard = 0;
        for (Map.Entry<GemType, Integer> e : discards.entrySet()) {
            GemType type = e.getKey();
            int amount = e.getValue();
            if (amount <= 0) {
                return ValidationResult.error("Discard amounts must be positive.");
            }
            if (player.getTokens().get(type) < amount) {
                return ValidationResult.error("You do not have enough tokens of type " + type + " to discard.");
            }
            totalDiscard += amount;
        }
        int newTotal = player.getTotalTokens() - totalDiscard;
        if (newTotal > Player.MAX_TOKENS) {
            return ValidationResult.error("You must discard enough tokens to reach 10 or fewer.");
        }
        return ValidationResult.ok();
    }
}

