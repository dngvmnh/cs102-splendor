package splendor.game.engine;

import splendor.game.actions.DiscardTokensAction;
import splendor.game.actions.GameAction;
import splendor.game.actions.ActionType;
import splendor.model.Noble;
import splendor.model.Player;

import java.util.List;

/**
 * High-level game engine facade.
 *
 * This class exposes game-logic operations without any dependency
 * on console or networking concerns.
 */
public class Game {

    private final GameState state;
    private final TurnManager turnManager;
    private final ActionValidator validator;
    private final ActionExecutor executor;
    private final EndGameManager endGameManager;

    public Game(GameState state) {
        this.state = state;
        this.turnManager = new TurnManager(state.getPlayers().size());
        this.validator = new ActionValidator();
        this.executor = new ActionExecutor();
        this.endGameManager = new EndGameManager(turnManager.getFirstPlayerIndex());
    }

    public GameState getState() {
        return state;
    }

    public Player getCurrentPlayer() {
        return state.getPlayers().get(turnManager.getCurrentPlayerIndex());
    }

    public int getCurrentPlayerIndex() {
        return turnManager.getCurrentPlayerIndex();
    }

    public boolean isGameOver() {
        return endGameManager.isGameOver();
    }

    public ValidationResult validateAction(GameAction action) {
        return validator.validate(state, getCurrentPlayerIndex(), action);
    }

    public void applyAction(GameAction action) {
        executor.execute(state, getCurrentPlayerIndex(), action);
    }

    public boolean isTokenLimitExceededForCurrentPlayer() {
        return getCurrentPlayer().getTotalTokens() > Player.MAX_TOKENS;
    }

    public void applyDiscard(DiscardTokensAction discardAction) {
        executor.execute(state, getCurrentPlayerIndex(), discardAction);
    }

    public List<Noble> getClaimableNoblesForCurrentPlayer() {
        return executor.findClaimableNobles(state.getBoard(), getCurrentPlayer());
    }

    public void claimNoble(Noble noble) {
        executor.claimNoble(state.getBoard(), getCurrentPlayer(), noble);
    }

    /**
     * Should be called once after the current player has completed their
     * main action (and any required discards/noble decisions).
     */
    public void endTurn() {
        endGameManager.checkEndTriggered(state.getPlayers(), getCurrentPlayerIndex());
        int newIndex = turnManager.advanceToNextPlayer();
        endGameManager.onTurnAdvanced(newIndex);
    }

    public Player determineWinner() {
        return endGameManager.determineWinner(state.getPlayers());
    }
}

