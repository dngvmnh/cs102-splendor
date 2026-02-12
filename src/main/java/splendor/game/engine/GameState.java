package splendor.game.engine;

import splendor.model.Board;
import splendor.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable-style holder for the current game state.
 * The collections returned are read-only views over mutable lists.
 */
public class GameState {

    private final Board board;
    private final List<Player> players;

    public GameState(Board board, List<Player> players) {
        this.board = board;
        this.players = new ArrayList<>(players);
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
}

