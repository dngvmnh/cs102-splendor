package splendor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of development cards for a given level.
 */
public class Deck {

    private final int level;
    private final List<Card> cards;

    public Deck(int level, List<Card> cards) {
        this.level = level;
        this.cards = new ArrayList<>(cards);
    }

    public int getLevel() {
        return level;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public Card draw() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(cards.size() - 1);
    }

    public int size() {
        return cards.size();
    }
}

