package splendor.game.engine;

import splendor.game.actions.*;
import splendor.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Applies validated actions to the game state.
 */
public class ActionExecutor {

    public void execute(GameState state, int currentPlayerIndex, GameAction action) {
        Player player = state.getPlayers().get(currentPlayerIndex);
        Board board = state.getBoard();

        switch (action.getType()) {
            case TAKE_TOKENS -> executeTakeTokens(board, player, (TakeTokensAction) action);
            case BUY_CARD -> executeBuyCard(board, player, (BuyCardAction) action);
            case RESERVE_CARD -> executeReserve(board, player, (ReserveCardAction) action);
            case DISCARD_TOKENS -> executeDiscard(board, player, (DiscardTokensAction) action);
        }
    }

    private void executeTakeTokens(Board board, Player player, TakeTokensAction action) {
        for (Map.Entry<GemType, Integer> e : action.getTaken().entrySet()) {
            GemType type = e.getKey();
            int amount = e.getValue();
            board.getSupplyTokens().remove(type, amount);
            player.getTokens().add(type, amount);
        }
    }

    private void executeBuyCard(Board board, Player player, BuyCardAction action) {
        Card card;
        if (action.isFromReserved()) {
            card = player.getReservedCards().remove(action.getCardIndex());
        } else {
            int level = action.getLevel();
            card = board.takeFaceUpCard(level, action.getCardIndex());
            board.refillLevel(level);
        }

        // Determine token payment using minimal gold usage.
        TokenPool payment = new TokenPool();
        int goldAvailable = player.getTokens().get(GemType.GOLD);
        int goldUsed = 0;

        for (Map.Entry<GemType, Integer> e : card.getCost().entrySet()) {
            GemType color = e.getKey();
            int cost = e.getValue();
            int bonus = player.getBonus(color);
            int effectiveCost = Math.max(0, cost - bonus);
            int playerTokens = player.getTokens().get(color);
            if (playerTokens >= effectiveCost) {
                if (effectiveCost > 0) {
                    payment.add(color, effectiveCost);
                }
            } else {
                int missing = effectiveCost - playerTokens;
                if (playerTokens > 0) {
                    payment.add(color, playerTokens);
                }
                goldUsed += missing;
            }
        }

        if (goldUsed > 0) {
            payment.add(GemType.GOLD, goldUsed);
        }

        // Remove payment from player, return to supply.
        for (Map.Entry<GemType, Integer> e : payment.asUnmodifiableMap().entrySet()) {
            GemType type = e.getKey();
            int amount = e.getValue();
            if (amount <= 0) continue;
            player.getTokens().remove(type, amount);
            board.getSupplyTokens().add(type, amount);
        }

        // Finally, give the card to the player.
        player.purchaseCard(card);
    }

    private void executeReserve(Board board, Player player, ReserveCardAction action) {
        Card reservedCard;
        if (action.isFromTopOfDeck()) {
            reservedCard = board.drawFromDeck(action.getLevel());
        } else {
            reservedCard = board.takeFaceUpCard(action.getLevel(), action.getCardIndex());
            board.refillLevel(action.getLevel());
        }

        if (reservedCard != null) {
            player.getReservedCards().add(reservedCard);
        }

        // Take one gold token if available.
        if (board.getSupplyTokens().get(GemType.GOLD) > 0) {
            board.getSupplyTokens().remove(GemType.GOLD, 1);
            player.getTokens().add(GemType.GOLD, 1);
        }
    }

    private void executeDiscard(Board board, Player player, DiscardTokensAction action) {
        for (Map.Entry<GemType, Integer> e : action.getDiscards().entrySet()) {
            GemType type = e.getKey();
            int amount = e.getValue();
            player.getTokens().remove(type, amount);
            board.getSupplyTokens().add(type, amount);
        }
    }

    /**
     * Determine which nobles the player qualifies for.
     */
    public List<Noble> findClaimableNobles(Board board, Player player) {
        List<Noble> result = new ArrayList<>();
        for (Noble noble : board.getNobles()) {
            boolean ok = true;
            for (Map.Entry<GemType, Integer> req : noble.getRequirements().entrySet()) {
                if (player.getBonus(req.getKey()) < req.getValue()) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                result.add(noble);
            }
        }
        return result;
    }

    public void claimNoble(Board board, Player player, Noble noble) {
        player.addPrestigePoints(noble.getPrestigePoints());
        board.removeNoble(noble);
    }
}

