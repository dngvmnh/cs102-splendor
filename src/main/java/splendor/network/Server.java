package splendor.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import splendor.game.actions.BuyCardAction;
import splendor.game.actions.DiscardTokensAction;
import splendor.game.actions.GameAction;
import splendor.game.actions.ReserveCardAction;
import splendor.game.actions.TakeTokensAction;
import splendor.game.engine.Game;
import splendor.game.engine.GameState;
import splendor.game.engine.StandardGameFactory;
import splendor.model.GemType;

/**
 * Simple authoritative game server for LAN play.
 *
 * - Accepts N client connections (players), each sending `JOIN:<name>` once connected.
 * - Hosts the `Game` instance locally and executes actions received from clients.
 * - Sends textual game state snapshots to all clients and prompts the current player.
 *
 * This implementation is intentionally minimal and line-oriented so it is
 * easy to run from laptops on the same local network. It keeps the core
 * game engine unchanged.
 */
public class Server {

    private static final int DEFAULT_PORT = 4000;

    private final int port;
    private final int autoPlayerCount; // if >0, use this instead of prompting

    public Server(int port) {
        this(port, -1);
    }

    public Server(int port, int autoPlayerCount) {
        this.port = port;
        this.autoPlayerCount = autoPlayerCount;
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            // Determine how many players to wait for (use autoPlayerCount if provided)
            Scanner console = new Scanner(System.in);
            int playerCount = this.autoPlayerCount > 0 ? this.autoPlayerCount : 0;
            while (playerCount < 2 || playerCount > 4) {
                System.out.print("Enter number of players to wait for (2-4): ");
                playerCount = Integer.parseInt(console.nextLine().trim());
            }

            List<ClientConn> clients = new ArrayList<>();

            // Accept connections until we have enough players
            while (clients.size() < playerCount) {
                System.out.println("Waiting for player " + (clients.size() + 1) + " of " + playerCount + " to connect...");
                Socket sock = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
                // Read join line
                String join = in.readLine();
                String name = "Player" + (clients.size() + 1);
                if (join != null && join.startsWith("JOIN:")) {
                    name = join.substring(5).trim();
                    if (name.isEmpty()) name = "Player" + (clients.size() + 1);
                }
                ClientConn cc = new ClientConn(name, sock, in, out);
                clients.add(cc);
                out.println("WELCOME:" + clients.size());
                System.out.println("Connected: " + name);
            }

            // Gather player names and create game
            List<String> names = new ArrayList<>();
            for (ClientConn c : clients) names.add(c.name);

            Game game = StandardGameFactory.createGame(names);

            // Main game loop
            while (!game.isGameOver()) {
                // Broadcast state
                String stateText = formatGameState(game.getState(), game.getCurrentPlayerIndex());
                broadcast(clients, "STATE");
                broadcast(clients, stateText);
                broadcast(clients, "ENDSTATE");

                int currentIndex = game.getCurrentPlayerIndex();
                ClientConn currentClient = clients.get(currentIndex);
                currentClient.out.println("YOUR_TURN");

                boolean actionApplied = false;
                while (!actionApplied) {
                    String line = currentClient.in.readLine();
                    if (line == null) throw new IllegalStateException("Client disconnected");
                    if (!line.startsWith("ACTION ")) {
                        currentClient.out.println("RESULT ERROR Expected ACTION message");
                        continue;
                    }
                    String cmd = line.substring(7).trim();
                    try {
                        GameAction action = parseAction(cmd);
                        // validate
                        var result = game.validateAction(action);
                        if (!result.isValid()) {
                            currentClient.out.println("RESULT ERROR " + result.getMessage());
                            continue;
                        }
                        game.applyAction(action);
                        currentClient.out.println("RESULT OK");
                        actionApplied = true;

                        // handle discard if needed
                        if (game.isTokenLimitExceededForCurrentPlayer()) {
                            int excess = game.getCurrentPlayer().getTotalTokens() - splendor.model.Player.MAX_TOKENS;
                            // Ask client to discard
                            currentClient.out.println("DISCARD_NEEDED " + excess);
                            boolean discarded = false;
                            while (!discarded) {
                                String dline = currentClient.in.readLine();
                                if (dline == null) throw new IllegalStateException("Client disconnected");
                                if (!dline.startsWith("DISCARD ")) {
                                    currentClient.out.println("RESULT ERROR Expected DISCARD message");
                                    continue;
                                }
                                String payload = dline.substring(8).trim();
                                var discards = parseGemMap(payload);
                                DiscardTokensAction discardAction = new DiscardTokensAction(discards);
                                var dres = game.validateAction(discardAction);
                                if (!dres.isValid()) {
                                    currentClient.out.println("RESULT ERROR " + dres.getMessage());
                                    continue;
                                }
                                game.applyDiscard(discardAction);
                                currentClient.out.println("RESULT OK");
                                discarded = true;
                            }
                        }

                        // Noble selection if applicable
                        if (action instanceof BuyCardAction) {
                            var claimable = game.getClaimableNoblesForCurrentPlayer();
                            if (!claimable.isEmpty()) {
                                if (claimable.size() == 1) {
                                    game.claimNoble(claimable.get(0));
                                } else {
                                    // ask client to choose or skip
                                    currentClient.out.println("NOBLE_CHOICE " + claimable.size());
                                    boolean chosen = false;
                                    while (!chosen) {
                                        String nline = currentClient.in.readLine();
                                        if (nline == null) throw new IllegalStateException("Client disconnected");
                                        if (!nline.startsWith("NOBLE ")) {
                                            currentClient.out.println("RESULT ERROR Expected NOBLE message");
                                            continue;
                                        }
                                        int idx = Integer.parseInt(nline.substring(6).trim());
                                        if (idx >= 0 && idx < claimable.size()) {
                                            game.claimNoble(claimable.get(idx));
                                        }
                                        currentClient.out.println("RESULT OK");
                                        chosen = true;
                                    }
                                }
                            }
                        }

                        game.endTurn();

                    } catch (IllegalArgumentException ex) {
                        currentClient.out.println("RESULT ERROR " + ex.getMessage());
                    }
                }
            }

            // Game over: broadcast final state and winner
            broadcast(clients, "GAME_OVER");
            broadcast(clients, formatGameState(game.getState(), -1));
            System.out.println("Game finished. Closing connections.");
            for (ClientConn c : clients) c.sock.close();
        }
    }

    private void broadcast(List<ClientConn> clients, String msg) {
        for (ClientConn c : clients) {
            c.out.println(msg);
        }
    }

    private static class ClientConn {
        final String name;
        final Socket sock;
        final BufferedReader in;
        final PrintWriter out;

        ClientConn(String name, Socket sock, BufferedReader in, PrintWriter out) {
            this.name = name;
            this.sock = sock;
            this.in = in;
            this.out = out;
        }
    }

    /**
     * Parse a simple command string into a GameAction.
     * Supported forms:
     *  - TAKE WHITE,BLUE,RED
     *  - TAKE WHITE:2,BLUE:1
     *  - BUY MARKET <level> <index>
     *  - BUY RESERVED <index>
     *  - RESERVE MARKET <level> <index>
     *  - RESERVE TOP <level>
     */
    private GameAction parseAction(String cmd) {
        String[] parts = cmd.split(" ", 2);
        String verb = parts[0].toUpperCase(Locale.ROOT);
        String rest = parts.length > 1 ? parts[1].trim() : "";
        switch (verb) {
            case "TAKE":
                Map<GemType, Integer> taken = parseGemMap(rest);
                return new TakeTokensAction(taken);
            case "BUY":
                String[] b = rest.split(" ");
                if (b[0].equalsIgnoreCase("MARKET")) {
                    int level = Integer.parseInt(b[1]);
                    int idx = Integer.parseInt(b[2]);
                    return BuyCardAction.fromMarket(level, idx);
                } else if (b[0].equalsIgnoreCase("RESERVED")) {
                    int idx = Integer.parseInt(b[1]);
                    return BuyCardAction.fromReserved(idx);
                }
                break;
            case "RESERVE":
                String[] r = rest.split(" ");
                if (r[0].equalsIgnoreCase("MARKET")) {
                    int level = Integer.parseInt(r[1]);
                    int idx = Integer.parseInt(r[2]);
                    return ReserveCardAction.fromMarket(level, idx);
                } else if (r[0].equalsIgnoreCase("TOP")) {
                    int level = Integer.parseInt(r[1]);
                    return ReserveCardAction.fromTopOfDeck(level);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown action verb: " + verb);
        }
        throw new IllegalArgumentException("Malformed action: " + cmd);
    }

    private static Map<GemType, Integer> parseGemMap(String s) {
        Map<GemType, Integer> map = new EnumMap<>(GemType.class);
        if (s.isBlank()) return map;
        String[] items = s.split(",");
        for (String it : items) {
            it = it.trim();
            if (it.isEmpty()) continue;
            if (it.contains(":")) {
                String[] kv = it.split(":");
                GemType t = GemType.valueOf(kv[0].toUpperCase(Locale.ROOT));
                int v = Integer.parseInt(kv[1]);
                map.put(t, v);
            } else {
                GemType t = GemType.valueOf(it.toUpperCase(Locale.ROOT));
                map.put(t, map.getOrDefault(t, 0) + 1);
            }
        }
        return map;
    }

    private String formatGameState(GameState state, int currentPlayerIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append("---- Board ----\n");
        sb.append("Supply tokens: ").append(state.getBoard().getSupplyTokens()).append("\n\n");

        sb.append("Nobles:\n");
        var nobles = state.getBoard().getNobles();
        if (nobles.isEmpty()) {
            sb.append("  (none left)\n");
        } else {
            for (int i = 0; i < nobles.size(); i++) {
                sb.append("  [").append(i).append("] ").append(nobles.get(i)).append("\n");
            }
        }
        sb.append("\n");

        appendLevel(sb, 1, state.getBoard().getLevel1FaceUp());
        appendLevel(sb, 2, state.getBoard().getLevel2FaceUp());
        appendLevel(sb, 3, state.getBoard().getLevel3FaceUp());

        sb.append("---- Players ----\n");
        var players = state.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            var p = players.get(i);
            sb.append(i == currentPlayerIndex ? "> " : "  ");
            sb.append(p.getName()).append(" | P=").append(p.getPrestigePoints())
                    .append(" | tokens=").append(p.getTokens())
                    .append(" | bonuses=").append(p.getBonuses())
                    .append(" | purchased=").append(p.getPurchasedCards().size()).append("\n");
        }
        return sb.toString();
    }

    private void appendLevel(StringBuilder sb, int level, java.util.List spl) {
        sb.append("Level ").append(level).append(" cards:\n");
        java.util.List cards = spl;
        if (cards.isEmpty()) {
            sb.append("  (no cards showing)\n\n");
            return;
        }
        for (int i = 0; i < cards.size(); i++) {
            Object c = cards.get(i);
            sb.append("  [").append(i).append("] ").append(c.toString()).append("\n");
        }
        sb.append("\n");
    }

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        int players = -1;
        if (args.length > 0) port = Integer.parseInt(args[0]);
        if (args.length > 1) players = Integer.parseInt(args[1]);
        Server s = new Server(port, players);
        s.start();
    }
}

