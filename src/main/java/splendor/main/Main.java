package splendor.main;

import splendor.game.controller.GameController;
import splendor.ui.console.ConsoleView;
import splendor.ui.console.InputHandler;

/**
 * Entry point for the console-based Splendor game.
 *
 * This class wires together the console UI and the game controller.
 */
public class Main {

    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        InputHandler input = new InputHandler();
        GameController controller = new GameController(view, input);
        controller.start();
    }
}

