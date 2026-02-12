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
        // Support network modes:
        // - server-bg [port] [playerCount] : start server in background (blocks until stopped)
        // - client <host> <port> <name> : start network client
        if (args.length > 0) {
            String mode = args[0];
            if (mode.equalsIgnoreCase("server-bg")) {
                int port = 4000;
                int players = -1;
                if (args.length > 1) port = Integer.parseInt(args[1]);
                if (args.length > 2) players = Integer.parseInt(args[2]);
                splendor.network.Server server = new splendor.network.Server(port, players);
                Thread t = new Thread(() -> {
                    try {
                        server.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                t.setDaemon(false);
                t.start();
                System.out.println("Server started in background on port " + port + ", waiting for " + (players>0?players:"players") );
                System.out.println("Press Enter to stop server.");
                try {
                    System.in.read();
                } catch (Exception ignored) {}
                System.exit(0);
            } else if (mode.equalsIgnoreCase("client")) {
                if (args.length < 4) {
                    System.out.println("Usage: client <host> <port> <name>");
                    return;
                }
                String host = args[1];
                String port = args[2];
                String name = args[3];
                try {
                    splendor.network.Client.main(new String[]{host, port, name});
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        ConsoleView view = new ConsoleView();
        InputHandler input = new InputHandler();
        GameController controller = new GameController(view, input);
        controller.start();
    }
}

