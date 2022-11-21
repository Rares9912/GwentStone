package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.CardInput;
import fileio.Coordinates;
import fileio.Input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Game {


    public Game(Input input, ArrayNode output) {

        int playerOneWins = 0;
        int playerTwoWins = 0;
        int totalGamesPlayed = 0;
        for (int i = 0; i < input.getGames().size(); i++) {
            ObjectMapper mapper = new ObjectMapper();
            CommandsImplementation commands = new CommandsImplementation();

            int playerOneMana = 1;
            int playerTwoMana = 1;
            int turnNumber = 0;
            int roundNumber = 1;
            boolean gameEnded = false;
            int deckIndex1 = input.getGames().get(i).getStartGame().getPlayerOneDeckIdx();
            int deckIndex2 = input.getGames().get(i).getStartGame().getPlayerTwoDeckIdx();
            int shuffleSeed = input.getGames().get(i).getStartGame().getShuffleSeed();
            
            ArrayList<CardInput> playerOneDeck = new ArrayList<>();
            ArrayList<CardInput> playerTwoDeck = new ArrayList<>();

            for (int j = 0; j < input.getPlayerOneDecks().getNrCardsInDeck(); j++) {
                playerOneDeck.add(new CardInput(input.getPlayerOneDecks().getDecks()
                        .get(deckIndex1).get(j)));
            }

            for (int j = 0; j < input.getPlayerTwoDecks().getNrCardsInDeck(); j++) {
                playerTwoDeck.add(new CardInput(input.getPlayerTwoDecks().getDecks()
                        .get(deckIndex2).get(j)));
            }

            
            Collections.shuffle(playerOneDeck, new Random(shuffleSeed));
            Collections.shuffle(playerTwoDeck, new Random(shuffleSeed));

            ArrayList<CardInput> playerOneHand = new ArrayList<>();
            ArrayList<CardInput> playerTwoHand = new ArrayList<>();

            playerOneHand.add(playerOneDeck.get(0));
            playerOneDeck.remove(0);

            playerTwoHand.add(playerTwoDeck.get(0));
            playerTwoDeck.remove(0);

            ArrayList<ArrayList<CardInput>> gameTable = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                gameTable.add(new ArrayList<>());
            }

            CardInput playerOneHero = input.getGames().get(i).getStartGame().getPlayerOneHero();
            CardInput playerTwoHero = input.getGames().get(i).getStartGame().getPlayerTwoHero();

            playerOneHero.setHealth(30);
            playerTwoHero.setHealth(30);

            for (int j = 0; j < input.getGames().get(i).getActions().size(); j++) {
                ActionsInput action = input.getGames().get(i).getActions().get(j);
                if (gameEnded && !commands.isGetCommand(action.getCommand())) {
                    break;
                }
                switch (action.getCommand()) {
                    case "getPlayerDeck" -> {
                        new getPlayerDeck(action, output, commands, playerOneDeck, playerTwoDeck,
                                mapper);
                    }

                    case "getPlayerHero" -> {
                        int playerIdx = input.getGames().get(i).getActions().get(j).getPlayerIdx();
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("playerIdx", playerIdx);
                        ObjectNode result = mapper.createObjectNode();

                        if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            commands.writeCard(playerTwoHero, result, mapper);
                            node.put("output", result);
                        } else {
                            commands.writeCard(playerOneHero, result, mapper);
                            node.put("output", result);
                        }
                    }

                    case "getPlayerTurn" -> {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("output", input.getGames().get(i).getStartGame().getStartingPlayer());
                    }

                    case "endPlayerTurn" -> {
                        if (input.getGames().get(i).getStartGame().getStartingPlayer() == 2) {
                            input.getGames().get(i).getStartGame().setStartingPlayer(1);
                            for (int k = 0; k < 2; k++) {
                                for (int l = 0; l < gameTable.get(k).size(); l++) {
                                    if (gameTable.get(k).get(l).isFrozen()) {
                                        gameTable.get(k).get(l).setFrozen(false);
                                    }
                                }
                            }
                        } else {
                            input.getGames().get(i).getStartGame().setStartingPlayer(2);
                            for (int k = 2; k < 4; k++) {
                                for (int l = 0; l < gameTable.get(k).size(); l++) {
                                    if (gameTable.get(k).get(l).isFrozen()) {
                                        gameTable.get(k).get(l).setFrozen(false);
                                    }
                                }
                            }
                        }
                        turnNumber++;

                        if (turnNumber == 2) {
                            turnNumber = 0;
                            roundNumber++;

                            playerOneHero.setHasAttacked(false);
                            playerTwoHero.setHasAttacked(false);

                            for (ArrayList<CardInput> cardInputs : gameTable) {
                                for (CardInput cardInput : cardInputs) {
                                    if (cardInput.hasAttacked()) {
                                        cardInput.setHasAttacked(false);
                                    }
                                }
                            }

                            if (playerOneDeck.size() != 0) {
                                playerOneHand.add(playerOneDeck.get(0));
                                playerOneDeck.remove(0);
                            }

                            if (playerTwoDeck.size() != 0) {
                                playerTwoHand.add(playerTwoDeck.get(0));
                                playerTwoDeck.remove(0);
                            }

                            if (roundNumber < 10) {
                                playerOneMana = commands.addPlayerMana(roundNumber, playerOneMana);
                                playerTwoMana = commands.addPlayerMana(roundNumber, playerTwoMana);
                            } else {
                                playerOneMana = commands.addPlayerMana(10, playerOneMana);
                                playerTwoMana = commands.addPlayerMana(10, playerTwoMana);
                            }

                        }
                    }
                    case "placeCard" -> {
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();
                        int handIdx = input.getGames().get(i).getActions().get(j).getHandIdx();
                        switch (playerTurn) {
                            case 1 -> {
                                if (playerOneHand.size() <= handIdx)
                                    break;

                                String cardNamePlayerOne = playerOneHand.get(handIdx).getName();

                                boolean isEnvironment = cardNamePlayerOne.equals("Winterfell") ||
                                        cardNamePlayerOne.equals("Firestorm") ||
                                        cardNamePlayerOne.equals("Heart Hound");

                                if (isEnvironment) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", action.getCommand());
                                    node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                    node.put("error", "Cannot place environment card on table.");
                                    break;
                                }

                                if (cardNamePlayerOne.equals("The Ripper") || cardNamePlayerOne.equals("Miraj") || cardNamePlayerOne.equals("Goliath") || cardNamePlayerOne.equals("Warden")) {
                                    if (playerOneMana < playerOneHand.get(handIdx).getMana()) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Not enough mana to place card on table.");
                                        break;
                                    }

                                    if (gameTable.get(2).size() == 5) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Cannot place card on table since row is full.");
                                        break;
                                    }

                                    gameTable.set(2, commands.addCardOnRow(gameTable.get(2), playerOneHand.get(handIdx)));
                                    playerOneMana = playerOneMana - playerOneHand.get(handIdx).getMana();
                                    playerOneHand.remove(handIdx);
                                }
                                if (cardNamePlayerOne.equals("Sentinel") || cardNamePlayerOne.equals("Berserker") || cardNamePlayerOne.equals("The Cursed One") || cardNamePlayerOne.equals("Disciple")) {
                                    if (playerOneMana < playerOneHand.get(handIdx).getMana()) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Not enough mana to place card on table.");
                                        break;
                                    }

                                    if (gameTable.get(3).size() == 5) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Cannot place card on table since row is full.");
                                        break;
                                    }

                                    gameTable.set(3, commands.addCardOnRow(gameTable.get(3), playerOneHand.get(handIdx)));
                                    playerOneMana = playerOneMana - playerOneHand.get(handIdx).getMana();
                                    playerOneHand.remove(handIdx);
                                }
                            }
                            case 2 -> {
                                if (playerTwoHand.size() <= handIdx)
                                    break;

                                String cardNamePlayerTwo = playerTwoHand.get(handIdx).getName();

                                boolean isEnvironment = cardNamePlayerTwo.equals("Winterfell") ||
                                        cardNamePlayerTwo.equals("Firestorm") ||
                                        cardNamePlayerTwo.equals("Heart Hound");

                                if (isEnvironment) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", action.getCommand());
                                    node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                    node.put("error", "Cannot place environment card on table.");
                                    break;
                                }

                                if (cardNamePlayerTwo.equals("The Ripper") || cardNamePlayerTwo.equals("Miraj") || cardNamePlayerTwo.equals("Goliath") || cardNamePlayerTwo.equals("Warden")) {
                                    if (playerTwoMana < playerTwoHand.get(handIdx).getMana()) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Not enough mana to place card on table.");
                                        break;
                                    }

                                    if (gameTable.get(1).size() == 5) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Cannot place card on table since row is full.");
                                        break;
                                    }

                                    gameTable.set(1, commands.addCardOnRow(gameTable.get(1), playerTwoHand.get(handIdx)));
                                    playerTwoMana = playerTwoMana - playerTwoHand.get(handIdx).getMana();
                                    playerTwoHand.remove(handIdx);
                                }
                                if (cardNamePlayerTwo.equals("Sentinel") || cardNamePlayerTwo.equals("Berserker") || cardNamePlayerTwo.equals("The Cursed One") || cardNamePlayerTwo.equals("Disciple")) {
                                    if (playerTwoMana < playerTwoHand.get(handIdx).getMana()) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Not enough mana to place card on table.");
                                        break;
                                    }

                                    if (gameTable.get(0).size() == 5) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Cannot place card on table since row is full.");
                                        break;
                                    }

                                    gameTable.set(0, commands.addCardOnRow(gameTable.get(0), playerTwoHand.get(handIdx)));
                                    playerTwoMana = playerTwoMana - playerTwoHand.get(handIdx).getMana();
                                    playerTwoHand.remove(handIdx);
                                }
                            }
                        }
                    }
                    case "getCardsInHand" -> {
                        ObjectNode node = output.addObject();

                        node.put("command", action.getCommand());
                        node.put("playerIdx", input.getGames().get(i).getActions().get(j).getPlayerIdx());
                        ArrayNode result = node.putArray("output");

                        if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 1) {
                            commands.writeDeck(playerOneHand, result, mapper);
                        } else if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            commands.writeDeck(playerTwoHand, result, mapper);
                        }
                    }
                    case "getPlayerMana" -> {
                        ObjectNode node = output.addObject();

                        node.put("command", action.getCommand());
                        node.put("playerIdx", input.getGames().get(i).getActions().get(j).getPlayerIdx());

                        if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 1) {
                            node.put("output", playerOneMana);
                        } else if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            node.put("output", playerTwoMana);
                        }

                    }
                    case "getCardsOnTable" -> {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        ArrayNode result = node.putArray("output");

                        for (ArrayList<CardInput> cardInputs : gameTable) {
                            commands.writeTable(cardInputs, result, mapper);
                        }
                    }
                    case "getEnvironmentCardsInHand" -> {
                        int playerIdx = input.getGames().get(i).getActions().get(j).getPlayerIdx();
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("playerIdx", playerIdx);

                        ArrayNode result = node.putArray("output");
                        ArrayList<CardInput> environmentCards = new ArrayList<>();

                        switch (playerIdx) {
                            case 1 -> {
                                for (CardInput cardInput : playerOneHand) {
                                    if (commands.isEnvironment(cardInput)) {
                                        environmentCards.add(cardInput);
                                    }
                                }
                                commands.writeDeck(environmentCards, result, mapper);
                            }
                            case 2 -> {
                                for (CardInput cardInput : playerTwoHand) {
                                    if (commands.isEnvironment(cardInput)) {
                                        environmentCards.add(cardInput);
                                    }
                                }
                                commands.writeDeck(environmentCards, result, mapper);
                            }
                        }
                    }
                    case "useEnvironmentCard" -> {
                        int handIdx = input.getGames().get(i).getActions().get(j).getHandIdx();
                        int affectedRow = input.getGames().get(i).getActions().get(j).getAffectedRow();
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();

                        switch (playerTurn) {
                            case 1 -> {
                                if (!commands.isEnvironment(playerOneHand.get(handIdx))) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("handIdx", handIdx);
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Chosen card is not of type environment.");
                                    break;
                                }

                                if (playerOneMana < playerOneHand.get(handIdx).getMana()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("handIdx", handIdx);
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Not enough mana to use environment card.");
                                    break;
                                }

                                if (affectedRow == 2 || affectedRow == 3) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("handIdx", handIdx);
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Chosen row does not belong to the enemy.");
                                    break;
                                }

                                String environmentCardName = playerOneHand.get(handIdx).getName();

                                switch (environmentCardName) {

                                    case "Firestorm" -> {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setHealth(gameTable.get(affectedRow).get(k).getHealth() - 1);
                                        }
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            boolean everyCardDead = true;
                                            if (gameTable.get(affectedRow).get(k).getHealth() == 0) {
                                                gameTable.set(affectedRow, commands.removeCardFromRow(gameTable.get(affectedRow), k));
                                                everyCardDead = false;
                                            }

                                            if (!everyCardDead) {
                                                k = 0;
                                            }
                                        }
                                        playerOneMana = playerOneMana - playerOneHand.get(handIdx).getMana();
                                        playerOneHand.remove(handIdx);
                                    }
                                    case "Winterfell" -> {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setFrozen(true);
                                        }
                                        playerOneMana = playerOneMana - playerOneHand.get(handIdx).getMana();
                                        playerOneHand.remove(handIdx);
                                    }
                                    case "Heart Hound" -> {
                                        int maxHealth = 0;
                                        int minionIndex = 0;
                                        CardInput minionMaxHealth = new CardInput();

                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            if (gameTable.get(affectedRow).get(k).getHealth() > maxHealth) {
                                                maxHealth = gameTable.get(affectedRow).get(k).getHealth();
                                                minionMaxHealth = gameTable.get(affectedRow).get(k);
                                                minionIndex = k;
                                            }
                                        }
                                        if (affectedRow == 0) {
                                            if (gameTable.get(3).size() == 5) {
                                                ObjectNode node = output.addObject();
                                                node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                                node.put("handIdx", handIdx);
                                                node.put("affectedRow", affectedRow);
                                                node.put("error", "Cannot steal enemy card since the player's row is full.");
                                                break;
                                            }
                                            gameTable.set(3, commands.addCardOnRow(gameTable.get(3), minionMaxHealth));
                                            gameTable.set(0, commands.removeCardFromRow(gameTable.get(0), minionIndex));
                                        } else if (affectedRow == 1) {
                                            if (gameTable.get(2).size() == 5) {
                                                ObjectNode node = output.addObject();
                                                node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                                node.put("handIdx", handIdx);
                                                node.put("affectedRow", affectedRow);
                                                node.put("error", "Cannot steal enemy card since the player's row is full.");
                                                break;
                                            }
                                            gameTable.set(2, commands.addCardOnRow(gameTable.get(2), minionMaxHealth));
                                            gameTable.set(1, commands.removeCardFromRow(gameTable.get(1), minionIndex));
                                        }
                                        playerOneMana = playerOneMana - playerOneHand.get(handIdx).getMana();
                                        playerOneHand.remove(handIdx);
                                    }
                                }
                            }
                            case 2 -> {
                                if (!commands.isEnvironment(playerTwoHand.get(handIdx))) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("handIdx", handIdx);
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Chosen card is not of type environment.");
                                    break;
                                }

                                if (playerTwoMana < playerTwoHand.get(handIdx).getMana()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("handIdx", handIdx);
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Not enough mana to use environment card.");
                                    break;
                                }

                                if (affectedRow == 0 || affectedRow == 1) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("handIdx", handIdx);
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Chosen row does not belong to the enemy.");
                                    break;
                                }

                                String environmentCardName = playerTwoHand.get(handIdx).getName();

                                switch (environmentCardName) {
                                    case "Firestorm" -> {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setHealth(gameTable.get(affectedRow).get(k).getHealth() - 1);
                                        }
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            boolean everyCardDead = true;
                                            if (gameTable.get(affectedRow).get(k).getHealth() == 0) {
                                                gameTable.set(affectedRow, commands.removeCardFromRow(gameTable.get(affectedRow), k));
                                                everyCardDead = false;
                                            }
                                            if (!everyCardDead) {
                                                k = 0;
                                            }
                                        }
                                        playerTwoMana = playerTwoMana - playerTwoHand.get(handIdx).getMana();
                                        playerTwoHand.remove(handIdx);
                                    }
                                    case "Winterfell" -> {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setFrozen(true);
                                        }
                                        playerTwoMana = playerTwoMana - playerTwoHand.get(handIdx).getMana();
                                        playerTwoHand.remove(handIdx);
                                    }
                                    case "Heart Hound" -> {
                                        int maxHealth = 0;
                                        int minionIndex = 0;
                                        CardInput minionMaxHealth = new CardInput();

                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            if (gameTable.get(affectedRow).get(k).getHealth() > maxHealth) {
                                                maxHealth = gameTable.get(affectedRow).get(k).getHealth();
                                                minionMaxHealth = gameTable.get(affectedRow).get(k);
                                                minionIndex = k;
                                            }
                                        }
                                        if (affectedRow == 3) {
                                            if (gameTable.get(0).size() == 5) {
                                                ObjectNode node = output.addObject();
                                                node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                                node.put("handIdx", handIdx);
                                                node.put("affectedRow", affectedRow);
                                                node.put("error", "Cannot steal enemy card since the player's row is full.");
                                                break;
                                            }
                                            gameTable.set(0, commands.addCardOnRow(gameTable.get(0), minionMaxHealth));
                                            gameTable.set(3, commands.removeCardFromRow(gameTable.get(3), minionIndex));


                                        } else if (affectedRow == 2) {
                                            if (gameTable.get(1).size() == 5) {
                                                ObjectNode node = output.addObject();
                                                node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                                node.put("handIdx", handIdx);
                                                node.put("affectedRow", affectedRow);
                                                node.put("error", "Cannot steal enemy card since the player's row is full.");
                                                break;
                                            }
                                            gameTable.set(1, commands.addCardOnRow(gameTable.get(1), minionMaxHealth));
                                            gameTable.set(2, commands.removeCardFromRow(gameTable.get(2), minionIndex));

                                        }
                                        playerTwoMana = playerTwoMana - playerTwoHand.get(handIdx).getMana();
                                        playerTwoHand.remove(handIdx);
                                    }
                                }
                            }
                        }
                    }
                    case "getCardAtPosition" -> {
                        int x = input.getGames().get(i).getActions().get(j).getX();
                        int y = input.getGames().get(i).getActions().get(j).getY();
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        node.put("x", x);
                        node.put("y", y);

                        ObjectNode result = mapper.createObjectNode();

                        if (gameTable.get(x).size() <= y) {
                            node.put("output", "No card available at that position.");
                        } else {
                            commands.writeCard(gameTable.get(x).get(y), result, mapper);
                            node.put("output", result);
                        }
                    }
                    case "getFrozenCardsOnTable" -> {
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());

                        ArrayNode result = node.putArray("output");
                        ArrayList<CardInput> frozenCards = new ArrayList<>();

                        for (ArrayList<CardInput> cardInputs : gameTable) {
                            for (CardInput cardInput : cardInputs) {
                                if (cardInput.isFrozen()) {
                                    frozenCards.add(cardInput);
                                }
                            }
                        }
                        commands.writeDeck(frozenCards, result, mapper);
                    }
                    case "cardUsesAttack" -> {
                        Coordinates cardAttackerCoord = input.getGames().get(i).getActions().get(j).getCardAttacker();
                        Coordinates cardAttackedCoord = input.getGames().get(i).getActions().get(j).getCardAttacked();
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();

                        switch (playerTurn) {
                            case 1 -> {
                                if (cardAttackedCoord.getX() == 2 || cardAttackedCoord.getX() == 3) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacked card does not belong to the enemy.");
                                    break;
                                }
                                CardInput cardAttacker = gameTable.get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY());
                                CardInput cardAttacked = gameTable.get(cardAttackedCoord.getX()).get(cardAttackedCoord.getY());

                                if (cardAttacker.hasAttacked()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacker card has already attacked this turn.");
                                    break;
                                }

                                if (cardAttacker.isFrozen()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacker card is frozen.");
                                    break;
                                }

                                if (commands.isTankOnRow(gameTable.get(1))) {
                                    if (!commands.isTank(cardAttacked)) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        ObjectNode result1 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackerCoord, result1);
                                        node.put("cardAttacker", result1);
                                        ObjectNode result2 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackedCoord, result2);
                                        node.put("cardAttacked", result2);
                                        node.put("error", "Attacked card is not of type 'Tank'.");
                                        break;
                                    }
                                }
                                gameTable.get(cardAttackedCoord.getX()).set(cardAttackedCoord.getY(), commands.attackCard(cardAttacker, cardAttacked));

                                if (gameTable.get(cardAttackedCoord.getX()).get(cardAttackedCoord.getY()).getHealth() <= 0) {
                                    gameTable.set(cardAttackedCoord.getX(), commands.removeCardFromRow(gameTable.get(cardAttackedCoord.getX()),
                                            cardAttackedCoord.getY()));
                                }
                                gameTable.get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY()).setHasAttacked(true);
                            }
                            case 2 -> {
                                if (cardAttackedCoord.getX() == 0 || cardAttackedCoord.getX() == 1) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacked card does not belong to the enemy.");
                                    break;
                                }
                                CardInput cardAttacker = gameTable.get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY());
                                CardInput cardAttacked = gameTable.get(cardAttackedCoord.getX()).get(cardAttackedCoord.getY());

                                if (cardAttacker.hasAttacked()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacker card has already attacked this turn.");
                                    break;
                                }

                                if (cardAttacker.isFrozen()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacker card is frozen.");
                                    break;
                                }

                                if (commands.isTankOnRow(gameTable.get(2))) {
                                    if (!commands.isTank(cardAttacked)) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        ObjectNode result1 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackerCoord, result1);
                                        node.put("cardAttacker", result1);
                                        ObjectNode result2 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackedCoord, result2);
                                        node.put("cardAttacked", result2);
                                        node.put("error", "Attacked card is not of type 'Tank'.");
                                        break;
                                    }
                                }

                                gameTable.get(cardAttackedCoord.getX()).set(cardAttackedCoord.getY(), commands.attackCard(cardAttacker, cardAttacked));

                                if (gameTable.get(cardAttackedCoord.getX()).get(cardAttackedCoord.getY()).getHealth() <= 0) {
                                    gameTable.set(cardAttackedCoord.getX(), commands.removeCardFromRow(gameTable.get(cardAttackedCoord.getX()),
                                            cardAttackedCoord.getY()));
                                }
                                gameTable.get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY()).setHasAttacked(true);
                            }
                        }
                    }
                    case "cardUsesAbility" -> {
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();
                        Coordinates cardAttackerCoord = input.getGames().get(i).getActions().get(j).getCardAttacker();
                        Coordinates cardAttackedCoord = input.getGames().get(i).getActions().get(j).getCardAttacked();
                        CardInput cardAttacker = gameTable.get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY());
                        CardInput cardAttacked = gameTable.get(cardAttackedCoord.getX()).get(cardAttackedCoord.getY());
                        String cardAttackerName = cardAttacker.getName();

                        if (cardAttacker.isFrozen()) {
                            ObjectNode node = output.addObject();
                            node.put("command", action.getCommand());
                            ObjectNode result1 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result1);
                            node.put("cardAttacker", result1);
                            ObjectNode result2 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackedCoord, result2);
                            node.put("cardAttacked", result2);
                            node.put("error", "Attacker card is frozen.");
                            break;
                        } else if (cardAttacker.hasAttacked()) {
                            ObjectNode node = output.addObject();
                            node.put("command", action.getCommand());
                            ObjectNode result1 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result1);
                            node.put("cardAttacker", result1);
                            ObjectNode result2 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackedCoord, result2);
                            node.put("cardAttacked", result2);
                            node.put("error", "Attacker card has already attacked this turn.");
                            break;
                        }

                        switch (playerTurn) {
                            case 1 -> {
                                if (commands.isAttackingCard(cardAttacker)) {
                                    if (cardAttackedCoord.getX() == 2 || cardAttackedCoord.getX() == 3) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        ObjectNode result1 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackerCoord, result1);
                                        node.put("cardAttacker", result1);
                                        ObjectNode result2 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackedCoord, result2);
                                        node.put("cardAttacked", result2);
                                        node.put("error", "Attacked card does not belong to the enemy.");
                                        break;
                                    }
                                    if (commands.isTankOnRow(gameTable.get(1))) {
                                        if (!commands.isTank(cardAttacked)) {
                                            ObjectNode node = output.addObject();
                                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                            ObjectNode result1 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackerCoord, result1);
                                            node.put("cardAttacker", result1);
                                            ObjectNode result2 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackedCoord, result2);
                                            node.put("cardAttacked", result2);
                                            node.put("error", "Attacked card is not of type 'Tank'.");
                                            break;
                                        }
                                    }
                                }

                                switch (cardAttackerName) {
                                    case "Disciple" -> {
                                        if (cardAttackedCoord.getX() == 0 || cardAttackedCoord.getX() == 1) {
                                            ObjectNode node = output.addObject();
                                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                            ObjectNode result1 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackerCoord, result1);
                                            node.put("cardAttacker", result1);
                                            ObjectNode result2 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackedCoord, result2);
                                            node.put("cardAttacked", result2);
                                            node.put("error", "Attacked card does not belong to the current player.");
                                            break;
                                        }
                                        cardAttacked.setHealth(cardAttacked.getHealth() + 2);
                                    }
                                    case "The Ripper" -> {
                                        if (cardAttacked.getAttackDamage() <= 2) {
                                            cardAttacked.setAttackDamage(0);
                                        } else
                                            cardAttacked.setAttackDamage(cardAttacked.getAttackDamage() - 2);
                                    }
                                    case "Miraj" -> {
                                        int enemyHealth = cardAttacked.getHealth();
                                        cardAttacked.setHealth(cardAttacker.getHealth());
                                        cardAttacker.setHealth(enemyHealth);
                                    }
                                    case "The Cursed One" -> {
                                        int enemyAttack = cardAttacked.getAttackDamage();
                                        if (enemyAttack == 0) {
                                            gameTable.set(cardAttackedCoord.getX(), commands.removeCardFromRow(gameTable.get(cardAttackedCoord.getX()),
                                                    cardAttackedCoord.getY()));
                                        } else {
                                            cardAttacked.setAttackDamage(cardAttacked.getHealth());
                                            cardAttacked.setHealth(enemyAttack);
                                        }
                                    }
                                }
                                cardAttacker.setHasAttacked(true);
                            }
                            case 2 -> {
                                if (commands.isAttackingCard(cardAttacker)) {
                                    if (cardAttackedCoord.getX() == 0 || cardAttackedCoord.getX() == 1) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        ObjectNode result1 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackerCoord, result1);
                                        node.put("cardAttacker", result1);
                                        ObjectNode result2 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackedCoord, result2);
                                        node.put("cardAttacked", result2);
                                        node.put("error", "Attacked card does not belong to the enemy.");
                                        break;
                                    }
                                    if (commands.isTankOnRow(gameTable.get(2))) {
                                        if (!commands.isTank(cardAttacked)) {
                                            ObjectNode node = output.addObject();
                                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                            ObjectNode result1 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackerCoord, result1);
                                            node.put("cardAttacker", result1);
                                            ObjectNode result2 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackedCoord, result2);
                                            node.put("cardAttacked", result2);
                                            node.put("error", "Attacked card is not of type 'Tank'.");
                                            break;
                                        }
                                    }
                                }

                                switch (cardAttackerName) {
                                    case "Disciple" -> {
                                        if (cardAttackedCoord.getX() == 2 || cardAttackedCoord.getX() == 3) {
                                            ObjectNode node = output.addObject();
                                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                            ObjectNode result1 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackerCoord, result1);
                                            node.put("cardAttacker", result1);
                                            ObjectNode result2 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackedCoord, result2);
                                            node.put("cardAttacked", result2);
                                            node.put("error", "Attacked card does not belong to the current player.");
                                            break;
                                        }
                                        cardAttacked.setHealth(cardAttacked.getHealth() + 2);
                                    }
                                    case "The Ripper" -> {
                                        if (cardAttacked.getAttackDamage() <= 2) {
                                            cardAttacked.setAttackDamage(0);
                                        } else
                                            cardAttacked.setAttackDamage(cardAttacked.getAttackDamage() - 2);
                                    }
                                    case "Miraj" -> {
                                        int enemyHealth = cardAttacked.getHealth();
                                        cardAttacked.setHealth(cardAttacker.getHealth());
                                        cardAttacker.setHealth(enemyHealth);
                                    }
                                    case "The Cursed One" -> {
                                        int enemyAttack = cardAttacked.getAttackDamage();
                                        if (enemyAttack == 0) {
                                            gameTable.set(cardAttackedCoord.getX(), commands.removeCardFromRow(gameTable.get(cardAttackedCoord.getX()),
                                                    cardAttackedCoord.getY()));
                                        } else {
                                            cardAttacked.setAttackDamage(cardAttacked.getHealth());
                                            cardAttacked.setHealth(enemyAttack);
                                        }
                                    }
                                }
                                cardAttacker.setHasAttacked(true);
                            }
                        }
                    }
                    case "useAttackHero" -> {
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();
                        Coordinates cardAttackerCoord = input.getGames().get(i).getActions().get(j).getCardAttacker();
                        CardInput cardAttacker = gameTable.get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY());

                        if (cardAttacker.isFrozen()) {
                            ObjectNode node = output.addObject();
                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                            ObjectNode result = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result);
                            node.put("cardAttacker", result);
                            node.put("error", "Attacker card is frozen.");
                            break;
                        } else if (cardAttacker.hasAttacked()) {
                            ObjectNode node = output.addObject();
                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                            ObjectNode result = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result);
                            node.put("cardAttacker", result);
                            node.put("error", "Attacker card has already attacked this turn.");
                            break;
                        }

                        switch (playerTurn) {
                            case 1 -> {
                                if (commands.isTankOnRow(gameTable.get(1))) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result);
                                    node.put("cardAttacker", result);
                                    node.put("error", "Attacked card is not of type 'Tank'.");
                                    break;
                                }
                                playerTwoHero.setHealth(playerTwoHero.getHealth() - cardAttacker.getAttackDamage());
                                if (playerTwoHero.getHealth() <= 0) {
                                    ObjectNode node = output.addObject();
                                    node.put("gameEnded", "Player one killed the enemy hero.");
                                    playerOneWins++;
                                    totalGamesPlayed++;
                                    gameEnded = true;
                                }
                                cardAttacker.setHasAttacked(true);
                            }
                            case 2 -> {
                                if (commands.isTankOnRow(gameTable.get(2))) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result);
                                    node.put("cardAttacker", result);
                                    node.put("error", "Attacked card is not of type 'Tank'.");
                                    break;
                                }
                                playerOneHero.setHealth(playerOneHero.getHealth() - cardAttacker.getAttackDamage());
                                if (playerOneHero.getHealth() <= 0) {
                                    ObjectNode node = output.addObject();
                                    node.put("gameEnded", "Player two killed the enemy hero.");
                                    playerTwoWins++;
                                    totalGamesPlayed++;
                                    gameEnded = true;
                                }
                                cardAttacker.setHasAttacked(true);
                            }
                        }
                    }
                    case "useHeroAbility" -> {
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();
                        int affectedRow = input.getGames().get(i).getActions().get(j).getAffectedRow();

                        switch (playerTurn) {
                            case 1 -> {
                                if (playerOneMana < playerOneHero.getMana()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", action.getCommand());
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Not enough mana to use hero's ability.");
                                    break;
                                } else if (playerOneHero.hasAttacked()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", action.getCommand());
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Hero has already attacked this turn.");
                                    break;
                                }

                                String heroName = playerOneHero.getName();

                                if (heroName.equals("Lord Royce") || heroName.equals("Empress Thorina")) {
                                    if (affectedRow == 2 || affectedRow == 3) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", action.getCommand());
                                        node.put("affectedRow", affectedRow);
                                        node.put("error", "Selected row does not belong to the enemy.");
                                        break;
                                    }
                                } else if (affectedRow == 0 || affectedRow == 1) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", action.getCommand());
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Selected row does not belong to the current player.");
                                    break;
                                }

                                playerOneMana = playerOneMana - playerOneHero.getMana();
                                playerOneHero.setHasAttacked(true);

                                switch (heroName) {
                                    case "Lord Royce" -> {
                                        int maxAttack = 0;
                                        int maxAttackIndex = 0;
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            if (gameTable.get(affectedRow).get(k).getAttackDamage() > maxAttack) {
                                                maxAttack = gameTable.get(affectedRow).get(k).getAttackDamage();
                                                maxAttackIndex = k;
                                            }
                                        }
                                        if (!gameTable.get(affectedRow).get(maxAttackIndex).isFrozen()) {
                                            gameTable.get(affectedRow).get(maxAttackIndex).setFrozen(true);
                                        }
                                    }
                                    case "Empress Thorina" -> {
                                        int maxHealth = 0;
                                        int maxHealthIndex = 0;
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            if (gameTable.get(affectedRow).get(k).getHealth() > maxHealth) {
                                                maxHealth = gameTable.get(affectedRow).get(k).getHealth();
                                                maxHealthIndex = k;
                                            }
                                        }

                                        gameTable.set(affectedRow,
                                                commands.removeCardFromRow(
                                                        gameTable.get(affectedRow),
                                                        maxHealthIndex));
                                    }
                                    case "King Mudface" -> {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setHealth
                                                    (gameTable.get(affectedRow).get(k).getHealth() + 1);
                                        }
                                    }
                                    case "General Kocioraw" -> {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setAttackDamage
                                                    (gameTable.get(affectedRow).get(k).getAttackDamage() + 1);
                                        }
                                    }
                                }
                            }
                            case 2 -> {
                                if (playerTwoMana < playerTwoHero.getMana()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Not enough mana to use hero's ability.");
                                    break;
                                } else if (playerTwoHero.hasAttacked()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Hero has already attacked this turn.");
                                    break;
                                }

                                String heroName = playerTwoHero.getName();

                                if (heroName.equals("Lord Royce") || heroName.equals("Empress Thorina")) {
                                    if (affectedRow == 0 || affectedRow == 1) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        node.put("affectedRow", affectedRow);
                                        node.put("error", "Selected row does not belong to the enemy.");
                                        break;
                                    }
                                } else if (affectedRow == 2 || affectedRow == 3) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("affectedRow", affectedRow);
                                    node.put("error", "Selected row does not belong to the current player.");
                                    break;
                                }

                                playerTwoMana = playerTwoMana - playerTwoHero.getMana();
                                playerTwoHero.setHasAttacked(true);

                                switch (heroName) {
                                    case "Lord Royce" -> {
                                        int maxAttack = 0;
                                        int maxAttackIndex = 0;
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            if (gameTable.get(affectedRow).get(k).getAttackDamage() > maxAttack) {
                                                maxAttack = gameTable.get(affectedRow).get(k).getAttackDamage();
                                                maxAttackIndex = k;
                                            }
                                        }
                                        if (!gameTable.get(affectedRow).get(maxAttackIndex).isFrozen()) {
                                            gameTable.get(affectedRow).get(maxAttackIndex).setFrozen(true);
                                        }
                                    }
                                    case "Empress Thorina" -> {
                                        int maxHealth = 0;
                                        int maxHealthIndex = 0;
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            if (gameTable.get(affectedRow).get(k).getHealth() > maxHealth) {
                                                maxHealth = gameTable.get(affectedRow).get(k).getHealth();
                                                maxHealthIndex = k;
                                            }
                                        }

                                        gameTable.set(affectedRow,
                                                commands.removeCardFromRow(
                                                        gameTable.get(affectedRow),
                                                        maxHealthIndex));
                                    }
                                    case "King Mudface" -> {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setHealth
                                                    (gameTable.get(affectedRow).get(k).getHealth() + 1);
                                        }
                                    }
                                    case "General Kocioraw" -> {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setAttackDamage
                                                    (gameTable.get(affectedRow).get(k).getAttackDamage() + 1);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    case "getPlayerOneWins" -> {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("output", playerOneWins);
                    }

                    case "getPlayerTwoWins" -> {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("output", playerTwoWins);
                    }

                    case "getTotalGamesPlayed" -> {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("output", totalGamesPlayed);
                    }

                }
            }
        }
    }
}
