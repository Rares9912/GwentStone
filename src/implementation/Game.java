package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.Input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

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
                            for (int k = 0; k < input.getPlayerTwoDecks().getDecks().get(deckIndex2).size(); k++) {
                                commands.writeCard(input.getPlayerTwoDecks().getDecks().get(deckIndex2).get(k), result, mapper);
                            }
                        } else
                            for (int k = 0; k < input.getPlayerOneDecks().getDecks().get(deckIndex1).size(); k++) {
                                commands.writeCard(input.getPlayerOneDecks().getDecks().get(deckIndex1).get(k), result, mapper);
                            }
                        break;
                    }

                    case "getPlayerHero": {
                        ObjectNode node = output.addObject();
                        node.put("command", input.getGames().get(i).getActions().get(j).getCommand());
                        node.put("playerIdx", input.getGames().get(i).getActions().get(j).getPlayerIdx());
                        ObjectNode result = mapper.createObjectNode();

                        CardInput playerOneHero = input.getGames().get(i).getStartGame().getPlayerOneHero();
                        CardInput playerTwoHero = input.getGames().get(i).getStartGame().getPlayerTwoHero();

                        if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            commands.writeHero(playerTwoHero, result, mapper);
                            node.put("output", result);
                        } else {
                            commands.writeHero(playerOneHero, result, mapper);
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
                        } else {
                            input.getGames().get(i).getStartGame().setStartingPlayer(2);
                        }
                        turnNumber++;

                        if (turnNumber == 2) {
                            turnNumber = 0;
                            roundNumber++;

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
                            for (CardInput cardInput : playerOneHand) {
                                commands.writeCard(cardInput, result, mapper);
                            }
                        } else if (input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
                            for (CardInput cardInput : playerTwoHand) {
                                commands.writeCard(cardInput, result, mapper);
                            }
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

                }


            }


        }

    }
}
