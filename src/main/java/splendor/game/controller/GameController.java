package splendor.game.controller;

import splendor.game.actions.BuyCardAction;
import splendor.game.actions.GameAction;
import splendor.game.actions.ReserveCardAction;
import splendor.game.actions.TakeTokensAction;
import splendor.game.engine.Game;
import splendor.game.engine.StandardGameFactory;
import splendor.game.engine.ValidationResult;
import splendor.model.Noble;
import splendor.model.Player;
import splendor.ui.console.ConsoleView;
import splendor.ui.console.InputHandler;
import splendor.ui.console.MenuSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates interaction between the console UI and the game engine.
 *
 * This layer knows about both UI and game logic, but the engine itself
 * remains independent of any console I/O.
 */
public class GameController {

    private final ConsoleView view;
    private final InputHandler input;
    private final MenuSystem menu;

    private Game game;

    public GameController(ConsoleView view, InputHandler input) {
        this.view = view;
        this.input = input;
        this.menu = new MenuSystem(input);
    }

    public void start() {
        view.showWelcome();

        int playerCount = input.readIntInRange("Enter number of players (2-4): ", 2, 4);
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            String name = input.readLine("Enter name for player " + i + ": ");
            if (name.isBlank()) {
                name = "Player " + i;
            }
            names.add(name);
        }

        this.game = StandardGameFactory.createGame(names);
        view.showInfo("Game created. First to reach 15 prestige triggers the final round.");

        gameLoop();
    }

    private void gameLoop() {
        while (!game.isGameOver()) {
            playTurn();
        }
        Player winner = game.determineWinner();
        view.showWinner(winner, game.getState().getPlayers());
    }

    private void playTurn() {
        Player current = game.getCurrentPlayer();
        view.showTurnHeader(current);
        view.showGameState(game.getState());

        // Main action
        GameAction action = null;
        while (action == null) {
            int choice = menu.chooseMainAction();
            switch (choice) {
                case 1 -> {
                    TakeTokensAction a = menu.buildTakeTokensAction(game.getState().getBoard(), current);
                    action = a;
                }
                case 2 -> {
                    BuyCardAction a = menu.buildBuyCardAction(game.getState().getBoard(), current);
                    action = a;
                }
                case 3 -> {
                    ReserveCardAction a = menu.buildReserveCardAction(game.getState().getBoard(), current);
                    action = a;
                }
                default -> view.showError("Unknown choice.");
            }

            if (action == null) {
                continue;
            }

            ValidationResult result = game.validateAction(action);
            if (!result.isValid()) {
                view.showError(result.getMessage());
                action = null; // re-prompt
            }
        }

        game.applyAction(action);

        // If player exceeded token limit, enforce discard.
        if (game.isTokenLimitExceededForCurrentPlayer()) {
            int excess = game.getCurrentPlayer().getTotalTokens() - Player.MAX_TOKENS;
            Map<splendor.model.GemType, Integer> discards =
                    menu.buildDiscardMap(game.getCurrentPlayer().getTokens(), excess);
            splendor.game.actions.DiscardTokensAction discardAction =
                    new splendor.game.actions.DiscardTokensAction(discards);
            ValidationResult discardResult = game.validateAction(discardAction);
            if (!discardResult.isValid()) {
                // As a safeguard, if discard map is invalid, keep asking until valid.
                view.showError(discardResult.getMessage());
                while (true) {
                    discards = menu.buildDiscardMap(game.getCurrentPlayer().getTokens(), excess);
                    discardAction = new splendor.game.actions.DiscardTokensAction(discards);
                    discardResult = game.validateAction(discardAction);
                    if (discardResult.isValid()) {
                        break;
                    }
                    view.showError(discardResult.getMessage());
                }
            }
            game.applyDiscard(discardAction);
            view.showInfo("You discarded tokens. You now have "
                    + game.getCurrentPlayer().getTotalTokens() + " tokens.");
        }

        // Noble check (after buying a card only, per rules)
        if (action instanceof BuyCardAction) {
            List<Noble> claimable = game.getClaimableNoblesForCurrentPlayer();
            if (!claimable.isEmpty()) {
                if (claimable.size() == 1) {
                    Noble noble = claimable.get(0);
                    game.claimNoble(noble);
                    view.showInfo("You gained noble " + noble.getName()
                            + " for +" + noble.getPrestigePoints() + " prestige!");
                } else {
                    view.showNobleChoices(claimable);
                    int idx = input.readIntInRange("Choose noble index (or -1 to skip): ", -1, claimable.size() - 1);
                    if (idx >= 0) {
                        Noble noble = claimable.get(idx);
                        game.claimNoble(noble);
                        view.showInfo("You gained noble " + noble.getName()
                                + " for +" + noble.getPrestigePoints() + " prestige!");
                    }
                }
            }
        }

        game.endTurn();
    }
}

