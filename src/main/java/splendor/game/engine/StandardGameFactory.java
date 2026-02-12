package splendor.game.engine;

import splendor.model.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating a standard Splendor game
 * with official-like decks, nobles, and initial token setup.
 *
 * For brevity, this sample includes a representative subset of cards
 * rather than the full published set, but the rules and engine are
 * compatible with the official card lists.
 */
public class StandardGameFactory {

    private static int nextCardId = 1;

    private static Card card(int level, int points, GemType bonus, int w, int u, int g, int r, int b) {
        EnumMap<GemType, Integer> cost = new EnumMap<>(GemType.class);
        if (w > 0) cost.put(GemType.WHITE, w);
        if (u > 0) cost.put(GemType.BLUE, u);
        if (g > 0) cost.put(GemType.GREEN, g);
        if (r > 0) cost.put(GemType.RED, r);
        if (b > 0) cost.put(GemType.BLACK, b);
        return new Card(nextCardId++, level, points, bonus, cost);
    }

    private static Noble noble(String name, int w, int u, int g, int r, int b) {
        EnumMap<GemType, Integer> req = new EnumMap<>(GemType.class);
        if (w > 0) req.put(GemType.WHITE, w);
        if (u > 0) req.put(GemType.BLUE, u);
        if (g > 0) req.put(GemType.GREEN, g);
        if (r > 0) req.put(GemType.RED, r);
        if (b > 0) req.put(GemType.BLACK, b);
        return new Noble(name, req);
    }

    private static List<Card> level1Cards() {
        List<Card> list = new ArrayList<>();
        // A small but varied subset of level 1 cards
        list.add(card(1, 0, GemType.WHITE, 0, 0, 1, 1, 1));
        list.add(card(1, 0, GemType.BLUE, 0, 1, 1, 1, 0));
        list.add(card(1, 0, GemType.GREEN, 1, 0, 0, 2, 0));
        list.add(card(1, 0, GemType.RED, 0, 2, 0, 0, 1));
        list.add(card(1, 0, GemType.BLACK, 1, 1, 1, 0, 0));
        list.add(card(1, 1, GemType.WHITE, 0, 0, 2, 2, 0));
        list.add(card(1, 1, GemType.BLUE, 2, 0, 0, 0, 2));
        list.add(card(1, 1, GemType.GREEN, 0, 2, 2, 0, 0));
        list.add(card(1, 1, GemType.RED, 0, 0, 0, 4, 0));
        list.add(card(1, 1, GemType.BLACK, 3, 0, 0, 0, 0));
        return list;
    }

    private static List<Card> level2Cards() {
        List<Card> list = new ArrayList<>();
        list.add(card(2, 2, GemType.WHITE, 0, 0, 3, 2, 2));
        list.add(card(2, 2, GemType.BLUE, 2, 0, 0, 3, 2));
        list.add(card(2, 2, GemType.GREEN, 2, 3, 0, 0, 2));
        list.add(card(2, 2, GemType.RED, 2, 2, 3, 0, 0));
        list.add(card(2, 3, GemType.BLACK, 0, 0, 6, 0, 0));
        list.add(card(2, 1, GemType.WHITE, 0, 2, 2, 3, 0));
        list.add(card(2, 1, GemType.BLUE, 3, 0, 2, 2, 0));
        list.add(card(2, 1, GemType.GREEN, 0, 3, 0, 2, 2));
        list.add(card(2, 1, GemType.RED, 2, 0, 3, 0, 2));
        list.add(card(2, 2, GemType.BLACK, 0, 5, 0, 0, 0));
        return list;
    }

    private static List<Card> level3Cards() {
        List<Card> list = new ArrayList<>();
        list.add(card(3, 4, GemType.WHITE, 0, 0, 7, 0, 0));
        list.add(card(3, 4, GemType.BLUE, 0, 7, 0, 0, 0));
        list.add(card(3, 4, GemType.GREEN, 0, 0, 0, 7, 0));
        list.add(card(3, 4, GemType.RED, 0, 0, 0, 0, 7));
        list.add(card(3, 5, GemType.BLACK, 3, 3, 3, 3, 0));
        list.add(card(3, 3, GemType.WHITE, 3, 0, 0, 5, 3));
        list.add(card(3, 3, GemType.BLUE, 3, 3, 0, 0, 5));
        list.add(card(3, 3, GemType.GREEN, 5, 3, 3, 0, 0));
        list.add(card(3, 3, GemType.RED, 0, 5, 3, 3, 0));
        list.add(card(3, 3, GemType.BLACK, 0, 0, 5, 3, 3));
        return list;
    }

    private static List<Noble> nobles() {
        List<Noble> list = new ArrayList<>();
        list.add(noble("Henry VIII", 3, 3, 0, 0, 0));
        list.add(noble("Isabella", 0, 3, 3, 0, 0));
        list.add(noble("Lorenzo", 0, 0, 3, 3, 0));
        list.add(noble("Catherine", 0, 0, 0, 3, 3));
        list.add(noble("Charles", 3, 0, 0, 0, 3));
        return list;
    }

    public static Game createGame(List<String> playerNames) {
        if (playerNames.size() < 2 || playerNames.size() > 4) {
            throw new IllegalArgumentException("Splendor supports 2–4 players.");
        }

        // Create decks and board
        Deck d1 = new Deck(1, level1Cards());
        Deck d2 = new Deck(2, level2Cards());
        Deck d3 = new Deck(3, level3Cards());
        d1.shuffle();
        d2.shuffle();
        d3.shuffle();

        // Choose nobles: players + 1
        List<Noble> noblePool = nobles();
        java.util.Collections.shuffle(noblePool);
        List<Noble> chosenNobles = noblePool.subList(0, playerNames.size() + 1);

        Board board = new Board(d1, d2, d3, chosenNobles);
        board.initialDeal();

        // Tokens by player count
        int perColor;
        switch (playerNames.size()) {
            case 2 -> perColor = 4;
            case 3 -> perColor = 5;
            case 4 -> perColor = 7;
            default -> throw new IllegalStateException("Unexpected player count.");
        }

        for (GemType type : GemType.values()) {
            if (type.isStandard()) {
                board.getSupplyTokens().set(type, perColor);
            }
        }
        // Always 5 gold
        board.getSupplyTokens().set(GemType.GOLD, 5);

        // Players
        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }

        GameState state = new GameState(board, players);
        return new Game(state);
    }
}

