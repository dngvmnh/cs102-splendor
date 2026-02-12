package splendor.game.actions;

/**
 * Action: buy a development card.
 *
 * The card can be purchased either from the board (face-up market) or from the
 * player's reserved cards.
 */
public class BuyCardAction implements GameAction {

    private final boolean fromReserved;
    private final int level;      // used when fromReserved == false
    private final int cardIndex;  // index within face-up list or reserved list

    /**
     * Buy a card from the face-up market.
     *
     * @param level     card level (1, 2, or 3)
     * @param cardIndex index in the face-up list for that level
     */
    public static BuyCardAction fromMarket(int level, int cardIndex) {
        return new BuyCardAction(false, level, cardIndex);
    }

    /**
     * Buy a card from the player's reserved cards.
     *
     * @param reservedIndex index in the player's reserved list
     */
    public static BuyCardAction fromReserved(int reservedIndex) {
        return new BuyCardAction(true, 0, reservedIndex);
    }

    private BuyCardAction(boolean fromReserved, int level, int cardIndex) {
        this.fromReserved = fromReserved;
        this.level = level;
        this.cardIndex = cardIndex;
    }

    @Override
    public ActionType getType() {
        return ActionType.BUY_CARD;
    }

    public boolean isFromReserved() {
        return fromReserved;
    }

    public int getLevel() {
        return level;
    }

    public int getCardIndex() {
        return cardIndex;
    }
}

