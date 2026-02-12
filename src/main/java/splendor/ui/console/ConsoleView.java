package splendor.ui.console;

import splendor.game.engine.GameState;
import splendor.model.*;

import java.util.List;
import java.util.Map;

/**
 * Responsible for displaying game information on the console.
 */
public class ConsoleView {

    public void showWelcome() {
        System.out.println("========================================");
        System.out.println("       Splendor (Console Edition)       ");
        System.out.println("========================================");
        System.out.println();
    }

    public void showTurnHeader(Player currentPlayer) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("It's " + currentPlayer.getName() + "'s turn.");
        System.out.println("Prestige: " + currentPlayer.getPrestigePoints()
                + " | Tokens: " + currentPlayer.getTokens()
                + " | Bonuses: " + currentPlayer.getBonuses());
        System.out.println("Reserved cards: " + currentPlayer.getReservedCards().size());
        System.out.println("========================================");
        System.out.println();
    }

    public void showGameState(GameState state) {
        Board board = state.getBoard();

        System.out.println("---- Board ----");
        System.out.println("Supply tokens: " + board.getSupplyTokens());
        System.out.println();

        System.out.println("Nobles:");
        List<Noble> nobles = board.getNobles();
        if (nobles.isEmpty()) {
            System.out.println("  (none left)");
        } else {
            for (int i = 0; i < nobles.size(); i++) {
                Noble n = nobles.get(i);
                System.out.println("  [" + i + "] " + n);
            }
        }
        System.out.println();

        printLevelCards(1, board.getLevel1FaceUp());
        printLevelCards(2, board.getLevel2FaceUp());
        printLevelCards(3, board.getLevel3FaceUp());
    }

    private void printLevelCards(int level, List<Card> cards) {
        System.out.println("Level " + level + " cards:");
        if (cards.isEmpty()) {
            System.out.println("  (no cards showing)");
        } else {
            for (int i = 0; i < cards.size(); i++) {
                Card c = cards.get(i);
                System.out.println("  [" + i + "] " + cardSummary(c));
            }
        }
        System.out.println();
    }

    private String cardSummary(Card c) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID ").append(c.getId())
          .append(" | P=").append(c.getPrestigePoints())
          .append(" | Bonus=").append(c.getBonus())
          .append(" | Cost=");
        Map<GemType, Integer> cost = c.getCost();
        if (cost.isEmpty()) {
            sb.append("free");
        } else {
            sb.append(cost);
        }
        return sb.toString();
    }

    public void showPlayers(List<Player> players) {
        System.out.println("---- Players ----");
        for (Player p : players) {
            System.out.println(" - " + p.getName()
                    + " | P=" + p.getPrestigePoints()
                    + " | tokens=" + p.getTokens()
                    + " | bonuses=" + p.getBonuses()
                    + " | purchased=" + p.getPurchasedCards().size());
        }
        System.out.println();
    }

    public void showError(String message) {
        System.out.println("[Error] " + message);
    }

    public void showInfo(String message) {
        System.out.println("[Info] " + message);
    }

    public void showNobleChoices(List<Noble> nobles) {
        System.out.println("You qualify for a noble. Choose one (or -1 to skip):");
        for (int i = 0; i < nobles.size(); i++) {
            System.out.println("  [" + i + "] " + nobles.get(i));
        }
    }

    public void showWinner(Player winner, List<Player> players) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("Game over!");
        System.out.println();
        showPlayers(players);
        if (winner != null) {
            System.out.println("Winner: " + winner.getName()
                    + " with " + winner.getPrestigePoints() + " prestige.");
        } else {
            System.out.println("No winner could be determined.");
        }
        System.out.println("========================================");
    }
}

