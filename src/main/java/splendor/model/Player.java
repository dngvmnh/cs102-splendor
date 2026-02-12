package splendor.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a player in the game.
 */
public class Player {

    public static final int MAX_RESERVED_CARDS = 3;
    public static final int MAX_TOKENS = 10;

    private final String name;
    private final TokenPool tokens = new TokenPool();
    private final EnumMap<GemType, Integer> bonuses = new EnumMap<>(GemType.class);
    private final List<Card> purchasedCards = new ArrayList<>();
    private final List<Card> reservedCards = new ArrayList<>();
    private int prestigePoints;

    public Player(String name) {
        this.name = name;
        for (GemType type : GemType.values()) {
            if (type.isStandard()) {
                bonuses.put(type, 0);
            }
        }
    }

    public String getName() {
        return name;
    }

    public TokenPool getTokens() {
        return tokens;
    }

    public Map<GemType, Integer> getBonuses() {
        return bonuses;
    }

    public List<Card> getPurchasedCards() {
        return purchasedCards;
    }

    public List<Card> getReservedCards() {
        return reservedCards;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public void addPrestigePoints(int delta) {
        prestigePoints += delta;
    }

    public int getBonus(GemType type) {
        return bonuses.getOrDefault(type, 0);
    }

    public void addBonus(GemType type, int delta) {
        if (!type.isStandard()) {
            throw new IllegalArgumentException("Bonuses must be standard gem types");
        }
        bonuses.put(type, getBonus(type) + delta);
    }

    public int getTotalTokens() {
        return tokens.totalTokens();
    }

    public boolean canReserveMore() {
        return reservedCards.size() < MAX_RESERVED_CARDS;
    }

    public void purchaseCard(Card card) {
        purchasedCards.add(card);
        addPrestigePoints(card.getPrestigePoints());
        addBonus(card.getBonus(), 1);
    }
}

