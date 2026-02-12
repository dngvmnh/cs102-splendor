package splendor.game.actions;

/**
 * Action: reserve a development card.
 *
 * The player may reserve a visible face-up card or the top card of a deck.
 */
public class ReserveCardAction implements GameAction {

    private final boolean fromTopOfDeck;
    private final int level;      // card level (1, 2, or 3)
    private final int cardIndex;  // index in face-up list if fromTopOfDeck == false

    public static ReserveCardAction fromMarket(int level, int cardIndex) {
        return new ReserveCardAction(false, level, cardIndex);
    }

    public static ReserveCardAction fromTopOfDeck(int level) {
        return new ReserveCardAction(true, level, -1);
    }

    private ReserveCardAction(boolean fromTopOfDeck, int level, int cardIndex) {
        this.fromTopOfDeck = fromTopOfDeck;
        this.level = level;
        this.cardIndex = cardIndex;
    }

    @Override
    public ActionType getType() {
        return ActionType.RESERVE_CARD;
    }

    public boolean isFromTopOfDeck() {
        return fromTopOfDeck;
    }

    public int getLevel() {
        return level;
    }

    public int getCardIndex() {
        return cardIndex;
    }
}

