package splendor.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Minimal console client for the Splendor LAN server.
 *
 * Usage: `java splendor.network.Client <host> <port> <playerName>`
 */
public class Client {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: java splendor.network.Client <host> <port> <playerName>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String name = args[2];

        try (Socket sock = new Socket(host, port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            Scanner console = new Scanner(System.in);

            // Send join
            out.println("JOIN:" + name);

            // Reader thread to print server messages and set turn flags
            final java.util.concurrent.atomic.AtomicBoolean yourTurn = new java.util.concurrent.atomic.AtomicBoolean(false);
            final java.util.concurrent.atomic.AtomicBoolean discardNeeded = new java.util.concurrent.atomic.AtomicBoolean(false);
            final java.util.concurrent.atomic.AtomicBoolean nobleChoice = new java.util.concurrent.atomic.AtomicBoolean(false);
            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.equals("STATE")) {
                            StringBuilder sb = new StringBuilder();
                            while (true) {
                                String s = in.readLine();
                                if (s == null || s.equals("ENDSTATE")) break;
                                sb.append(s).append('\n');
                            }
                            System.out.println(sb.toString());
                        } else if (line.startsWith("WELCOME:")) {
                            System.out.println("Connected to server as " + name + " (slot " + line.substring(8) + ")");
                        } else if (line.equals("YOUR_TURN")) {
                            yourTurn.set(true);
                        } else if (line.startsWith("RESULT ")) {
                            System.out.println(line.substring(7));
                        } else if (line.startsWith("DISCARD_NEEDED ")) {
                            System.out.println("You must discard " + line.substring(15) + " tokens.");
                            discardNeeded.set(true);
                        } else if (line.startsWith("NOBLE_CHOICE ")) {
                            System.out.println("You may claim a noble. Choose index or -1 to skip.");
                            nobleChoice.set(true);
                        } else if (line.equals("GAME_OVER")) {
                            System.out.println("--- GAME OVER ---");
                        } else {
                            System.out.println(line);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Connection closed.");
                }
            });
            reader.setDaemon(true);
            reader.start();

            // Main input loop: wait for YOUR_TURN then present a clear interactive menu
            while (true) {
                // Wait for turn
                while (!yourTurn.get()) {
                    Thread.sleep(300);
                }
                System.out.println("\n=== YOUR TURN ===");
                System.out.println("Choose action:\n1) Take tokens\n2) Buy card\n3) Reserve card\n4) Show state (wait for server update)\nq) Quit");
                String choice = console.nextLine().trim();
                if (choice.equalsIgnoreCase("q") || choice.equalsIgnoreCase("quit") || choice.equalsIgnoreCase("exit")) break;
                switch (choice) {
                    case "1" -> {
                        System.out.print("Enter gems to take (e.g. WHITE,BLUE,RED or WHITE:2,BLUE:1): ");
                        String payload = console.nextLine().trim();
                        out.println("ACTION TAKE " + payload);
                    }
                    case "2" -> {
                        System.out.print("Buy from MARKET or RESERVED? (M/R): ");
                        String which = console.nextLine().trim();
                        if (which.equalsIgnoreCase("M")) {
                            System.out.print("Level (1-3): ");
                            String lvl = console.nextLine().trim();
                            System.out.print("Card index: ");
                            String idx = console.nextLine().trim();
                            out.println("ACTION BUY MARKET " + lvl + " " + idx);
                        } else {
                            System.out.print("Reserved index: ");
                            String idx = console.nextLine().trim();
                            out.println("ACTION BUY RESERVED " + idx);
                        }
                    }
                    case "3" -> {
                        System.out.print("Reserve from MARKET or TOP? (M/T): ");
                        String which = console.nextLine().trim();
                        if (which.equalsIgnoreCase("M")) {
                            System.out.print("Level (1-3): ");
                            String lvl = console.nextLine().trim();
                            System.out.print("Card index: ");
                            String idx = console.nextLine().trim();
                            out.println("ACTION RESERVE MARKET " + lvl + " " + idx);
                        } else {
                            System.out.print("Level (1-3): ");
                            String lvl = console.nextLine().trim();
                            out.println("ACTION RESERVE TOP " + lvl);
                        }
                    }
                    case "4" -> {
                        System.out.println("Waiting for server to send a state update...");
                    }
                    default -> System.out.println("Unknown choice.");
                }

                // After sending action, wait for potential discard/noble prompts or result
                yourTurn.set(false);
                while (!discardNeeded.get() && !nobleChoice.get()) {
                    Thread.sleep(200);
                    // break if turn assigned back to someone else
                    if (yourTurn.get()) break;
                }
                if (discardNeeded.get()) {
                    System.out.print("Enter discards (e.g. WHITE:1,BLUE:1): ");
                    String payload = console.nextLine().trim();
                    out.println("DISCARD " + payload);
                    discardNeeded.set(false);
                }
                if (nobleChoice.get()) {
                    System.out.print("Choose noble index (or -1 to skip): ");
                    String idx = console.nextLine().trim();
                    out.println("NOBLE " + idx);
                    nobleChoice.set(false);
                }
            }
        }
    }
}

