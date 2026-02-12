package splendor.game.engine;

import splendor.model.Player;

import java.util.Comparator;
import java.util.List;

/**
 * Handles end-of-game trigger and winner determination.
 */
public class EndGameManager {

    private final int firstPlayerIndex;
    private boolean finalRoundTriggered = false;
    private boolean gameOver = false;

    public EndGameManager(int firstPlayerIndex) {
        this.firstPlayerIndex = firstPlayerIndex;
    }

    /**
     * Check after a player's turn if the end-game condition has been reached
     * (15 or more prestige points).
     */
    public void checkEndTriggered(List<Player> players, int currentPlayerIndex) {
        if (finalRoundTriggered || gameOver) {
            return;
        }
        Player player = players.get(currentPlayerIndex);
        if (player.getPrestigePoints() >= 15) {
            finalRoundTriggered = true;
        }
    }

    /**
     * Called after the current player index has advanced.
     * If the final round was triggered and we loop back to the first player,
     * the game ends.
     */
    public void onTurnAdvanced(int newCurrentPlayerIndex) {
        if (finalRoundTriggered && newCurrentPlayerIndex == firstPlayerIndex) {
            gameOver = true;
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Determine winner following Splendor rules:
     * - Highest prestige points
     * - Ties broken by fewest purchased development cards
     */
    public Player determineWinner(List<Player> players) {
        return players.stream().max((a, b) -> {
            int cmp = Integer.compare(a.getPrestigePoints(), b.getPrestigePoints());
            if (cmp != 0) {
                return cmp;
            }
            // Fewer purchased cards wins tie
            return Integer.compare(b.getPurchasedCards().size(), a.getPurchasedCards().size());
        }).orElse(null);
    }
}

