package splendor.game.engine;

/**
 * Manages turn order and round progression.
 */
public class TurnManager {

    private final int playerCount;
    private final int firstPlayerIndex;
    private int currentPlayerIndex;

    public TurnManager(int playerCount) {
        this(playerCount, 0);
    }

    public TurnManager(int playerCount, int firstPlayerIndex) {
        if (playerCount < 2) {
            throw new IllegalArgumentException("At least 2 players required");
        }
        this.playerCount = playerCount;
        this.firstPlayerIndex = firstPlayerIndex;
        this.currentPlayerIndex = firstPlayerIndex;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getFirstPlayerIndex() {
        return firstPlayerIndex;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int advanceToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerCount;
        return currentPlayerIndex;
    }
}

