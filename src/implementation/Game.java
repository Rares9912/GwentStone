package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.Coordinates;
import fileio.Input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static java.util.Collections.swap;

public class Game {


    public Game(Input input, ArrayNode output) {

        for (int i = 0; i < input.getGames().size(); i++) {
            Random rand = new Random(input.getGames().get(i).getStartGame().getShuffleSeed());
            ObjectMapper mapper = new ObjectMapper();
            CommandsImplementation commands = new CommandsImplementation();

            int playerOneMana = 1;
            int playerTwoMana = 1;
            int turnNumber = 0;
            int roundNumber = 1;

            int deckIndex1 = input.getGames().get(i).getStartGame().getPlayerOneDeckIdx();
            Collections.shuffle(input.getPlayerOneDecks().getDecks().get(deckIndex1), rand);

            rand = new Random(input.getGames().get(i).getStartGame().getShuffleSeed());

            int deckIndex2 = input.getGames().get(i).getStartGame().getPlayerTwoDeckIdx();
            Collections.shuffle(input.getPlayerTwoDecks().getDecks().get(deckIndex2), rand);

            ArrayList<CardInput> playerOneHand = new ArrayList<>();
            ArrayList<CardInput> playerTwoHand = new ArrayList<>();

            playerOneHand.add(input.getPlayerOneDecks().getDecks().get(deckIndex1).get(0));
            input.getPlayerOneDecks().getDecks().get(deckIndex1).remove(0);

            playerTwoHand.add(input.getPlayerTwoDecks().getDecks().get(deckIndex2).get(0));
            input.getPlayerTwoDecks().getDecks().get(deckIndex2).remove(0);

            ArrayList<ArrayList<CardInput>> gameTable = new ArrayList<>();

            CardInput playerOneHero = input.getGames().get(i).getStartGame().getPlayerOneHero();
            CardInput playerTwoHero = input.getGames().get(i).getStartGame().getPlayerTwoHero();

            playerOneHero.setHealth(30);
            playerTwoHero.setHealth(30);

            for (int j = 0; j < 4; j++) {
                gameTable.add(new ArrayList<>());
            }

            for (int j = 0; j < input.getGames().get(i).getActions().size(); j++) {

                switch (input.getGames().get(i).getActions().get(j).getCommand()) {
                    case "getPlayerDeck": {
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        node.put("playerIdx", input.getGames().get(i).getActions().get(j).getPlayerIdx());
                        ArrayNode result = node.putArray("output");

                        if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            commands.writeDeck(input.getPlayerTwoDecks().getDecks().get(deckIndex2), result, mapper);
                        } else {
                            commands.writeDeck(input.getPlayerOneDecks().getDecks().get(deckIndex1), result, mapper);
                        }

                        break;
                    }

                    case "getPlayerHero": {
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        node.put("playerIdx", input.getGames().get(i).getActions().get(j).getPlayerIdx());
                        ObjectNode result = mapper.createObjectNode();

                        if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            commands.writeCard(playerTwoHero, result, mapper);
                            node.put("output", result);
                        } else {
                            commands.writeCard(playerOneHero, result, mapper);
                            node.put("output", result);
                        }
                        break;
                    }

                    case "getPlayerTurn": {
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        node.put("output", input.getGames().get(i).getStartGame().getStartingPlayer());
                        break;
                    }

                    case "endPlayerTurn": {
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

                            for (int k = 0; k < gameTable.size(); k++) {
                                for (int l = 0; l < gameTable.get(k).size(); l++) {
                                    if (gameTable.get(k).get(l).hasAttacked()) {
                                        gameTable.get(k).get(l).setHasAttacked(false);
                                    }
                                }
                            }

                            if (input.getPlayerOneDecks().getDecks().get(deckIndex1).size() != 0) {
                                playerOneHand.add(input.getPlayerOneDecks().getDecks().get(deckIndex1).get(0));
                                input.getPlayerOneDecks().getDecks().get(deckIndex1).remove(0);
                            }

                            if (input.getPlayerTwoDecks().getDecks().get(deckIndex2).size() != 0) {
                                playerTwoHand.add(input.getPlayerTwoDecks().getDecks().get(deckIndex2).get(0));
                                input.getPlayerTwoDecks().getDecks().get(deckIndex2).remove(0);
                            }

                            if (roundNumber < 10) {
                                playerOneMana = commands.addPlayerMana(roundNumber, playerOneMana);
                                playerTwoMana = commands.addPlayerMana(roundNumber, playerTwoMana);
                            } else {
                                playerOneMana = commands.addPlayerMana(10, playerOneMana);
                                playerTwoMana = commands.addPlayerMana(10, playerTwoMana);
                            }

                        }
                        break;
                    }

                    case "placeCard": {
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();
                        int handIdx = input.getGames().get(i).getActions().get(j).getHandIdx();
                        switch (playerTurn) {
                            case 1: {
                                if (playerOneHand.size() <= handIdx)
                                    break;

                                String cardNamePlayerOne = playerOneHand.get(handIdx).getName();

                                boolean isEnvironment = cardNamePlayerOne.equals("Winterfell") ||
                                        cardNamePlayerOne.equals("Firestorm") ||
                                        cardNamePlayerOne.equals("Heart Hound");

                                if (isEnvironment) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                    node.put("error", "Cannot place environment card on table.");
                                    break;
                                }

                                if (cardNamePlayerOne.equals("The Ripper") || cardNamePlayerOne.equals("Miraj") || cardNamePlayerOne.equals("Goliath") || cardNamePlayerOne.equals("Warden")) {
                                    if (playerOneMana < playerOneHand.get(handIdx).getMana()) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Not enough mana to place card on table.");
                                        break;
                                    }

                                    if (gameTable.get(2).size() == 5) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
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
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Not enough mana to place card on table.");
                                        break;
                                    }

                                    if (gameTable.get(3).size() == 5) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Cannot place card on table since row is full.");
                                        break;
                                    }

                                    gameTable.set(3, commands.addCardOnRow(gameTable.get(3), playerOneHand.get(handIdx)));
                                    playerOneMana = playerOneMana - playerOneHand.get(handIdx).getMana();
                                    playerOneHand.remove(handIdx);
                                }
                                break;
                            }

                            case 2: {
                                if (playerTwoHand.size() <= handIdx)
                                    break;

                                String cardNamePlayerTwo = playerTwoHand.get(handIdx).getName();

                                boolean isEnvironment = cardNamePlayerTwo.equals("Winterfell") ||
                                        cardNamePlayerTwo.equals("Firestorm") ||
                                        cardNamePlayerTwo.equals("Heart Hound");

                                if (isEnvironment) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                    node.put("error", "Cannot place environment card on table.");
                                    break;
                                }

                                if (cardNamePlayerTwo.equals("The Ripper") || cardNamePlayerTwo.equals("Miraj") || cardNamePlayerTwo.equals("Goliath") || cardNamePlayerTwo.equals("Warden")) {
                                    if (playerTwoMana < playerTwoHand.get(handIdx).getMana()) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Not enough mana to place card on table.");
                                        break;
                                    }

                                    if (gameTable.get(1).size() == 5) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
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
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Not enough mana to place card on table.");
                                        break;
                                    }

                                    if (gameTable.get(0).size() == 5) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        node.put("handIdx", input.getGames().get(i).getActions().get(j).getHandIdx());
                                        node.put("error", "Cannot place card on table since row is full.");
                                        break;
                                    }

                                    gameTable.set(0, commands.addCardOnRow(gameTable.get(0), playerTwoHand.get(handIdx)));
                                    playerTwoMana = playerTwoMana - playerTwoHand.get(handIdx).getMana();
                                    playerTwoHand.remove(handIdx);
                                }
                                break;
                            }
                        }
                        break;
                    }

                    case "getCardsInHand": {
                        ObjectNode node = output.addObject();

                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        node.put("playerIdx", input.getGames().get(i).getActions().get(j).getPlayerIdx());
                        ArrayNode result = node.putArray("output");

                        if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 1) {
                            commands.writeDeck(playerOneHand, result, mapper);
                        } else if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            commands.writeDeck(playerTwoHand, result, mapper);
                        }
                        break;
                    }

                    case "getPlayerMana": {
                        ObjectNode node = output.addObject();

                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        node.put("playerIdx", input.getGames().get(i).getActions().get(j).getPlayerIdx());

                        if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 1) {
                            node.put("output", playerOneMana);
                        } else if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            node.put("output", playerTwoMana);
                        }
                        break;

                    }

                    case "getCardsOnTable": {
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        ArrayNode result = node.putArray("output");

                        for (int k = 0; k < gameTable.size(); k++) {
                            commands.writeTable(gameTable.get(k), result, mapper);
                        }
                        break;
                    }

                    case "getEnvironmentCardsInHand": {
                        int playerIdx = input.getGames().get(i).getActions().get(j).getPlayerIdx();
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        node.put("playerIdx", playerIdx);

                        ArrayNode result = node.putArray("output");
                        ArrayList<CardInput> environmentCards = new ArrayList<>();

                        switch (playerIdx) {
                            case 1: {
                                for (int k = 0; k < playerOneHand.size(); k++) {
                                    if (commands.isEnvironment(playerOneHand.get(k))) {
                                        environmentCards.add(playerOneHand.get(k));
                                    }
                                }
                                commands.writeDeck(environmentCards, result, mapper);
                                break;
                            }

                            case 2: {
                                for (int k = 0; k < playerTwoHand.size(); k++) {
                                    if (commands.isEnvironment(playerTwoHand.get(k))) {
                                        environmentCards.add(playerTwoHand.get(k));
                                    }
                                }
                                commands.writeDeck(environmentCards, result, mapper);
                                break;
                            }
                        }
                        break;
                    }

                    case "useEnvironmentCard": {
                        int handIdx = input.getGames().get(i).getActions().get(j).getHandIdx();
                        int affectedRow = input.getGames().get(i).getActions().get(j).getAffectedRow();
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();

                        switch (playerTurn) {
                            case 1: {
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
                                    case "Firestorm": {
                                        int rowSize = gameTable.get(affectedRow).size();
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setHealth(gameTable.get(affectedRow).get(k).getHealth() - 1);
                                        }
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            boolean everyCardDead = true;
                                            if (gameTable.get(affectedRow).get(k).getHealth() == 0) {
                                                gameTable.set(affectedRow, commands.removeCardFromRow(gameTable.get(affectedRow), k));
                                                everyCardDead = false;
                                            }

                                            if(!everyCardDead) {
                                                k = 0;
                                            }
                                        }
                                        playerOneMana = playerOneMana - playerOneHand.get(handIdx).getMana();
                                        playerOneHand.remove(handIdx);
                                        break;
                                    }

                                    case "Winterfell": {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setFrozen(true);
                                        }
                                        playerOneMana = playerOneMana - playerOneHand.get(handIdx).getMana();
                                        playerOneHand.remove(handIdx);
                                        break;
                                    }

                                    case "Heart Hound": {
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
                                        break;
                                    }
                                }
                                break;
                            }

                            case 2: {
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
                                    case "Firestorm": {
                                        int rowSize = gameTable.get(affectedRow).size();
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setHealth(gameTable.get(affectedRow).get(k).getHealth() - 1);
                                        }
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            boolean everyCardDead = true;
                                            if (gameTable.get(affectedRow).get(k).getHealth() == 0) {
                                                gameTable.set(affectedRow, commands.removeCardFromRow(gameTable.get(affectedRow), k));
                                                everyCardDead = false;
                                            }
                                            if(!everyCardDead) {
                                                k = 0;
                                            }
                                        }
                                        playerTwoMana = playerTwoMana - playerTwoHand.get(handIdx).getMana();
                                        playerTwoHand.remove(handIdx);
                                        break;
                                    }

                                    case "Winterfell": {
                                        for (int k = 0; k < gameTable.get(affectedRow).size(); k++) {
                                            gameTable.get(affectedRow).get(k).setFrozen(true);
                                        }
                                        playerTwoMana = playerTwoMana - playerTwoHand.get(handIdx).getMana();
                                        playerTwoHand.remove(handIdx);
                                        break;
                                    }

                                    case "Heart Hound": {
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
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }

                    case "getCardAtPosition": {
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
                        break;
                    }

                    case "getFrozenCardsOnTable": {
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());

                        ArrayNode result = node.putArray("output");
                        ArrayList<CardInput> frozenCards = new ArrayList<>();

                        for (int k = 0; k < gameTable.size(); k++) {
                            for (int l = 0; l < gameTable.get(k).size(); l++) {
                                if (gameTable.get(k).get(l).isFrozen()) {
                                    frozenCards.add(gameTable.get(k).get(l));
                                }
                            }
                        }
                        commands.writeDeck(frozenCards, result, mapper);
                        break;
                    }

                    case "cardUsesAttack": {
                        Coordinates cardAttackerCoord = input.getGames().get(i).getActions().get(j).getCardAttacker();
                        Coordinates cardAttackedCoord = input.getGames().get(i).getActions().get(j).getCardAttacked();
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();

                        switch (playerTurn) {
                            case 1: {
                                if (cardAttackedCoord.getX() == 2 || cardAttackedCoord.getX() == 3) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2, mapper);
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
                                    commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacker card has already attacked this turn.");
                                    break;
                                }

                                if (cardAttacker.isFrozen()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacker card is frozen.");
                                    break;
                                }

                                if (commands.isTankOnRow(gameTable.get(1))) {
                                    if (!commands.isTank(cardAttacked)) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        ObjectNode result1 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                        node.put("cardAttacker", result1);
                                        ObjectNode result2 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackedCoord, result2, mapper);
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
                                break;
                            }

                            case 2: {
                                if (cardAttackedCoord.getX() == 0 || cardAttackedCoord.getX() == 1) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2, mapper);
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
                                    commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacker card has already attacked this turn.");
                                    break;
                                }

                                if (cardAttacker.isFrozen()) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result1 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                    node.put("cardAttacker", result1);
                                    ObjectNode result2 = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                    node.put("cardAttacked", result2);
                                    node.put("error", "Attacker card is frozen.");
                                    break;
                                }

                                if (commands.isTankOnRow(gameTable.get(2))) {
                                    if (!commands.isTank(cardAttacked)) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        ObjectNode result1 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                        node.put("cardAttacker", result1);
                                        ObjectNode result2 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackedCoord, result2, mapper);
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
                                break;
                            }
                        }
                        break;
                    }

                    case "cardUsesAbility": {
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();
                        Coordinates cardAttackerCoord = input.getGames().get(i).getActions().get(j).getCardAttacker();
                        Coordinates cardAttackedCoord = input.getGames().get(i).getActions().get(j).getCardAttacked();
                        CardInput cardAttacker = gameTable.get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY());
                        CardInput cardAttacked = gameTable.get(cardAttackedCoord.getX()).get(cardAttackedCoord.getY());
                        String cardAttackerName = cardAttacker.getName();

                        if (cardAttacker.isFrozen()) {
                            ObjectNode node = output.addObject();
                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                            ObjectNode result1 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                            node.put("cardAttacker", result1);
                            ObjectNode result2 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                            node.put("cardAttacked", result2);
                            node.put("error", "Attacker card is frozen.");
                            break;
                        } else if (cardAttacker.hasAttacked()) {
                            ObjectNode node = output.addObject();
                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                            ObjectNode result1 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                            node.put("cardAttacker", result1);
                            ObjectNode result2 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                            node.put("cardAttacked", result2);
                            node.put("error", "Attacker card has already attacked this turn.");
                            break;
                        }

                        switch (playerTurn) {
                            case 1: {
                                if (commands.isAttackingCard(cardAttacker)) {
                                    if (cardAttackedCoord.getX() == 2 || cardAttackedCoord.getX() == 3) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        ObjectNode result1 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                        node.put("cardAttacker", result1);
                                        ObjectNode result2 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                        node.put("cardAttacked", result2);
                                        node.put("error", "Attacked card does not belong to the enemy.");
                                        break;
                                    }
                                    if (commands.isTankOnRow(gameTable.get(1))) {
                                        if (!commands.isTank(cardAttacked)) {
                                            ObjectNode node = output.addObject();
                                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                            ObjectNode result1 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                            node.put("cardAttacker", result1);
                                            ObjectNode result2 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                            node.put("cardAttacked", result2);
                                            node.put("error", "Attacked card is not of type 'Tank'.");
                                            break;
                                        }
                                    }
                                }

                                switch (cardAttackerName) {
                                    case "Disciple": {
                                        if (cardAttackedCoord.getX() == 0 || cardAttackedCoord.getX() == 1) {
                                            ObjectNode node = output.addObject();
                                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                            ObjectNode result1 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                            node.put("cardAttacker", result1);
                                            ObjectNode result2 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                            node.put("cardAttacked", result2);
                                            node.put("error", "Attacked card does not belong to the current player.");
                                            break;
                                        }
                                        cardAttacked.setHealth(cardAttacked.getHealth() + 2);
                                        break;
                                    }

                                    case "The Ripper": {
                                        if (cardAttacked.getAttackDamage() <= 2) {
                                            cardAttacked.setAttackDamage(0);
                                        } else cardAttacked.setAttackDamage(cardAttacked.getAttackDamage() - 2);
                                        break;
                                    }

                                    case "Miraj": {
                                        int enemyHealth = cardAttacked.getHealth();
                                        cardAttacked.setHealth(cardAttacker.getHealth());
                                        cardAttacker.setHealth(enemyHealth);
                                        break;
                                    }

                                    case "The Cursed One": {
                                        int enemyAttack = cardAttacked.getAttackDamage();
                                        if (enemyAttack == 0) {
                                            gameTable.set(cardAttackedCoord.getX(), commands.removeCardFromRow(gameTable.get(cardAttackedCoord.getX()),
                                                    cardAttackedCoord.getY()));
                                        } else {
                                            cardAttacked.setAttackDamage(cardAttacked.getHealth());
                                            cardAttacked.setHealth(enemyAttack);
                                        }
                                        break;
                                    }
                                }
                                cardAttacker.setHasAttacked(true);
                                break;
                            }
                            case 2: {
                                if (commands.isAttackingCard(cardAttacker)) {
                                    if (cardAttackedCoord.getX() == 0 || cardAttackedCoord.getX() == 1) {
                                        ObjectNode node = output.addObject();
                                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                        ObjectNode result1 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                        node.put("cardAttacker", result1);
                                        ObjectNode result2 = mapper.createObjectNode();
                                        commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                        node.put("cardAttacked", result2);
                                        node.put("error", "Attacked card does not belong to the enemy.");
                                        break;
                                    }
                                    if (commands.isTankOnRow(gameTable.get(2))) {
                                        if (!commands.isTank(cardAttacked)) {
                                            ObjectNode node = output.addObject();
                                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                            ObjectNode result1 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                            node.put("cardAttacker", result1);
                                            ObjectNode result2 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                            node.put("cardAttacked", result2);
                                            node.put("error", "Attacked card is not of type 'Tank'.");
                                            break;
                                        }
                                    }
                                }

                                switch (cardAttackerName) {
                                    case "Disciple": {
                                        if (cardAttackedCoord.getX() == 2 || cardAttackedCoord.getX() == 3) {
                                            ObjectNode node = output.addObject();
                                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                            ObjectNode result1 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackerCoord, result1, mapper);
                                            node.put("cardAttacker", result1);
                                            ObjectNode result2 = mapper.createObjectNode();
                                            commands.writeCoordinates(cardAttackedCoord, result2, mapper);
                                            node.put("cardAttacked", result2);
                                            node.put("error", "Attacked card does not belong to the current player.");
                                            break;
                                        }
                                        cardAttacked.setHealth(cardAttacked.getHealth() + 2);
                                        break;
                                    }

                                    case "The Ripper": {
                                        if (cardAttacked.getAttackDamage() <= 2) {
                                            cardAttacked.setAttackDamage(0);
                                        } else cardAttacked.setAttackDamage(cardAttacked.getAttackDamage() - 2);
                                        break;
                                    }

                                    case "Miraj": {
                                        int enemyHealth = cardAttacked.getHealth();
                                        cardAttacked.setHealth(cardAttacker.getHealth());
                                        cardAttacker.setHealth(enemyHealth);
                                        break;
                                    }

                                    case "The Cursed One": {
                                        int enemyAttack = cardAttacked.getAttackDamage();
                                        if (enemyAttack == 0) {
                                            gameTable.set(cardAttackedCoord.getX(), commands.removeCardFromRow(gameTable.get(cardAttackedCoord.getX()),
                                                    cardAttackedCoord.getY()));
                                        } else {
                                            cardAttacked.setAttackDamage(cardAttacked.getHealth());
                                            cardAttacked.setHealth(enemyAttack);
                                        }
                                        break;
                                    }
                                }
                                cardAttacker.setHasAttacked(true);
                                break;
                            }
                        }
                        break;
                    }

                    case "useAttackHero": {
                        int playerTurn = input.getGames().get(i).getStartGame().getStartingPlayer();
                        Coordinates cardAttackerCoord = input.getGames().get(i).getActions().get(j).getCardAttacker();
                        CardInput cardAttacker = gameTable.get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY());

                        if (cardAttacker.isFrozen()) {
                            ObjectNode node = output.addObject();
                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                            ObjectNode result = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result, mapper);
                            node.put("cardAttacker", result);
                            node.put("error", "Attacker card is frozen.");
                            break;
                        } else if (cardAttacker.hasAttacked()) {
                            ObjectNode node = output.addObject();
                            node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                            ObjectNode result = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result, mapper);
                            node.put("cardAttacker", result);
                            node.put("error", "Attacker card has already attacked this turn.");
                            break;
                        }

                        switch (playerTurn) {
                            case 1: {
                                if (commands.isTankOnRow(gameTable.get(1))) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result, mapper);
                                    node.put("cardAttacker", result);
                                    node.put("error", "Attacked card is not of type 'Tank'.");
                                    break;
                                }
                                playerTwoHero.setHealth(playerTwoHero.getHealth() - cardAttacker.getAttackDamage());
                                if (playerTwoHero.getHealth() <= 0) {
                                    ObjectNode node = output.addObject();
                                    node.put("gameEnded", "Player one killed the enemy hero.");
                                }
                                cardAttacker.setHasAttacked(true);
                                break;
                            }

                            case 2: {
                                if (commands.isTankOnRow(gameTable.get(2))) {
                                    ObjectNode node = output.addObject();
                                    node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                                    ObjectNode result = mapper.createObjectNode();
                                    commands.writeCoordinates(cardAttackerCoord, result, mapper);
                                    node.put("cardAttacker", result);
                                    node.put("error", "Attacked card is not of type 'Tank'.");
                                    break;
                                }
                                playerOneHero.setHealth(playerOneHero.getHealth() - cardAttacker.getAttackDamage());
                                if (playerOneHero.getHealth() <= 0) {
                                    ObjectNode node = output.addObject();
                                    node.put("gameEnded", "Player two killed the enemy hero.");
                                }
                                cardAttacker.setHasAttacked(true);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
}
