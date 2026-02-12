# Splendor Console (Java 17)

Console-based implementation of the board game **Splendor**, written in Java 17 with a layered architecture suitable for academic evaluation and future extensions (GUI, networking, AI).

## Features

- 2–4 human players
- Full game loop playable via console
- Separation of concerns:
  - **Model layer**: core data classes (`Player`, `Card`, `Board`, `Noble`, `TokenPool`, etc.)
  - **Game logic layer**: rules and state management (`Game`, `GameState`, `TurnManager`, `ActionValidator`, `ActionExecutor`, `EndGameManager`)
  - **Controller layer**: `GameController` mediates between UI and logic
  - **UI layer (console)**: `ConsoleView`, `InputHandler`, `MenuSystem`
  - **Optional network layer**: designed to be added later without changing the game engine
- Follows official Splendor rules for:
  - Token setup and taking rules
  - Card purchasing and reserving (including gold/joker tokens)
  - Nobles and end-game scoring
  - End-of-game trigger at 15+ prestige points

## Running the Game

Requirements:

- Java 17+
- Maven 3.8+

Build and run:

```bash
mvn compile
mvn exec:java -Dexec.mainClass="splendor.main.Main"
```

Or build a runnable JAR:

```bash
mvn package
java -jar target/splendor-console-1.0.0-SNAPSHOT.jar
```

## Architecture Overview

The code is organized under `src/main/java`:

- `splendor.model`  
  Core domain objects: gems, tokens, cards, nobles, decks, board, players.

- `splendor.game.engine`  
  Game state and rules: `Game`, `GameState`, `TurnManager`, `ActionValidator`, `ActionExecutor`, `EndGameManager`, setup utilities.

- `splendor.game.actions`  
  Action objects (`TakeTokensAction`, `BuyCardAction`, `ReserveCardAction`, `DiscardTokensAction`) plus `GameAction` interface and `ActionType`.

- `splendor.game.controller`  
  `GameController` which coordinates between engine and any UI or network front-end.

- `splendor.ui.console`  
  Console-specific input/output and menus: `ConsoleView`, `InputHandler`, `MenuSystem`.

- `splendor.util`  
  Small utilities (logging, formatting) shared across layers.

This layering ensures the **game logic does not depend on console or networking code**, making it straightforward to add a GUI or networked client/server later.

## Extensibility

The design anticipates:

- Alternative UIs (Swing/JavaFX, web, etc.) driven by `GameController`
- AI opponents implemented using the same `GameAction` types and validator/executor
- Optional networking layer that sends serialized actions between client and server
- Save/load, undo, statistics, and replay features (hook into `EndGameManager` and action history)

## License

This project is intended for educational and academic use.

