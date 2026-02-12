package splendor.ui.console;

import splendor.game.actions.BuyCardAction;
import splendor.game.actions.ReserveCardAction;
import splendor.game.actions.TakeTokensAction;
import splendor.model.Board;
import splendor.model.GemType;
import splendor.model.Player;
import splendor.model.TokenPool;

import java.util.EnumMap;
import java.util.Map;

/**
 * Presents menus to the user and constructs action objects based on choices.
 */
public class MenuSystem {

    private final InputHandler input;

    public MenuSystem(InputHandler input) {
        this.input = input;
    }

    public int chooseMainAction() {
        System.out.println("Choose your action:");
        System.out.println("  1) Take tokens");
        System.out.println("  2) Buy a card");
        System.out.println("  3) Reserve a card");
        return input.readIntInRange("Enter choice (1-3): ", 1, 3);
    }

    public TakeTokensAction buildTakeTokensAction(Board board, Player player) {
        System.out.println("Take tokens:");
        System.out.println("  1) Take 3 different colors (1 each)");
        System.out.println("  2) Take 2 of the same color (requires 4+ in supply)");
        int choice = input.readIntInRange("Enter choice (1-2): ", 1, 2);

        EnumMap<GemType, Integer> taken = new EnumMap<>(GemType.class);

        if (choice == 1) {
            int remaining = 3;
            while (remaining > 0) {
                printSupply(board);
                System.out.println("You must select " + remaining + " more different colors.");
                GemType type = askColor();
                if (!type.isStandard()) {
                    System.out.println("You may not take gold tokens.");
                    continue;
                }
                if (taken.containsKey(type)) {
                    System.out.println("You already chose that color; pick another.");
                    continue;
                }
                if (board.getSupplyTokens().get(type) < 1) {
                    System.out.println("Not enough of that color in the supply.");
                    continue;
                }
                taken.put(type, 1);
                remaining--;
            }
        } else {
            printSupply(board);
            while (true) {
                GemType type = askColor();
                if (!type.isStandard()) {
                    System.out.println("You may not take gold tokens.");
                    continue;
                }
                if (board.getSupplyTokens().get(type) < 4) {
                    System.out.println("You may take two of a color only if at least 4 are in supply.");
                    continue;
                }
                taken.put(type, 2);
                break;
            }
        }

        return new TakeTokensAction(taken);
    }

    public BuyCardAction buildBuyCardAction(Board board, Player player) {
        System.out.println("Buy card from:");
        System.out.println("  1) Board (face-up)");
        System.out.println("  2) Your reserved cards");
        int src = input.readIntInRange("Enter choice (1-2): ", 1, 2);

        if (src == 2) {
            if (player.getReservedCards().isEmpty()) {
                System.out.println("You have no reserved cards.");
                return null;
            }
            for (int i = 0; i < player.getReservedCards().size(); i++) {
                System.out.println("  [" + i + "] " + player.getReservedCards().get(i));
            }
            int idx = input.readIntInRange("Choose reserved card index: ", 0, player.getReservedCards().size() - 1);
            return BuyCardAction.fromReserved(idx);
        } else {
            int level = input.readIntInRange("Choose level (1-3): ", 1, 3);
            int maxIndex = switch (level) {
                case 1 -> board.getLevel1FaceUp().size() - 1;
                case 2 -> board.getLevel2FaceUp().size() - 1;
                case 3 -> board.getLevel3FaceUp().size() - 1;
                default -> -1;
            };
            if (maxIndex < 0) {
                System.out.println("No cards available at that level.");
                return null;
            }
            int idx = input.readIntInRange("Choose card index: ", 0, maxIndex);
            return BuyCardAction.fromMarket(level, idx);
        }
    }

    public ReserveCardAction buildReserveCardAction(Board board, Player player) {
        System.out.println("Reserve from:");
        System.out.println("  1) Board (face-up)");
        System.out.println("  2) Top of a deck (blind)");
        int src = input.readIntInRange("Enter choice (1-2): ", 1, 2);

        if (src == 2) {
            int level = input.readIntInRange("Choose deck level (1-3): ", 1, 3);
            return ReserveCardAction.fromTopOfDeck(level);
        } else {
            int level = input.readIntInRange("Choose level (1-3): ", 1, 3);
            int maxIndex = switch (level) {
                case 1 -> board.getLevel1FaceUp().size() - 1;
                case 2 -> board.getLevel2FaceUp().size() - 1;
                case 3 -> board.getLevel3FaceUp().size() - 1;
                default -> -1;
            };
            if (maxIndex < 0) {
                System.out.println("No cards available at that level.");
                return null;
            }
            int idx = input.readIntInRange("Choose card index to reserve: ", 0, maxIndex);
            return ReserveCardAction.fromMarket(level, idx);
        }
    }

    public Map<GemType, Integer> buildDiscardMap(TokenPool playerTokens, int mustDiscardAtLeast) {
        EnumMap<GemType, Integer> discards = new EnumMap<>(GemType.class);
        System.out.println("You must discard down to " + splendor.model.Player.MAX_TOKENS + " tokens.");
        System.out.println("Your tokens: " + playerTokens.asUnmodifiableMap());

        int discarded = 0;
        while (discarded < mustDiscardAtLeast) {
            GemType type = askColorAllowGold();
            int owned = playerTokens.get(type);
            if (owned <= 0) {
                System.out.println("You have no tokens of that type.");
                continue;
            }
            int max = owned;
            int amount = input.readIntInRange("Discard how many (" + type + ", max " + max + ")? ", 1, max);
            discards.merge(type, amount, Integer::sum);
            discarded += amount;
            System.out.println("Planned discards so far: " + discards);
            if (discarded < mustDiscardAtLeast) {
                System.out.println("You still need to discard at least " + (mustDiscardAtLeast - discarded) + " more.");
            }
        }
        return discards;
    }

    private GemType askColor() {
        while (true) {
            System.out.println("Choose color:");
            System.out.println("  1) WHITE");
            System.out.println("  2) BLUE");
            System.out.println("  3) GREEN");
            System.out.println("  4) RED");
            System.out.println("  5) BLACK");
            int choice = input.readIntInRange("Enter color (1-5): ", 1, 5);
            return switch (choice) {
                case 1 -> GemType.WHITE;
                case 2 -> GemType.BLUE;
                case 3 -> GemType.GREEN;
                case 4 -> GemType.RED;
                case 5 -> GemType.BLACK;
                default -> GemType.WHITE;
            };
        }
    }

    private GemType askColorAllowGold() {
        while (true) {
            System.out.println("Choose color:");
            System.out.println("  1) WHITE");
            System.out.println("  2) BLUE");
            System.out.println("  3) GREEN");
            System.out.println("  4) RED");
            System.out.println("  5) BLACK");
            System.out.println("  6) GOLD");
            int choice = input.readIntInRange("Enter color (1-6): ", 1, 6);
            return switch (choice) {
                case 1 -> GemType.WHITE;
                case 2 -> GemType.BLUE;
                case 3 -> GemType.GREEN;
                case 4 -> GemType.RED;
                case 5 -> GemType.BLACK;
                case 6 -> GemType.GOLD;
                default -> GemType.WHITE;
            };
        }
    }

    private void printSupply(Board board) {
        System.out.println("Supply tokens: " + board.getSupplyTokens().asUnmodifiableMap());
    }
}

