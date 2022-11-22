package implementation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.StartGameInput;

public class PlaceCard {
    public PlaceCard(final Game game, final StartGameInput startGame, final ActionsInput action,
                     final CommandsImplementation commands, final ArrayNode output) {
        int playerTurn = startGame.getStartingPlayer();
        int handIdx = action.getHandIdx();
        final int rowNumber = 3;
        final int maxSize = 5;

        switch (playerTurn) {
            case 1 -> {
                if (game.getPlayerOneHand().size() <= handIdx) {
                    break;
                }
                String cardNamePlayerOne = game.getPlayerOneHand().get(handIdx).getName();

                if (commands.isEnvironment(game.getPlayerOneHand().get(handIdx))) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("error", "Cannot place environment card on table.");
                    break;
                }
                if (game.getPlayerOneMana() < game.getPlayerOneHand().get(handIdx).getMana()) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("error", "Not enough mana to place card on table.");
                    break;
                }

                if (cardNamePlayerOne.equals("The Ripper") || cardNamePlayerOne.equals("Miraj")
                        || cardNamePlayerOne.equals("Goliath")
                        || cardNamePlayerOne.equals("Warden")) {
                    if (game.getGameTable().get(2).size() == maxSize) {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("handIdx", handIdx);
                        node.put("error", "Cannot place card on "
                                + "table since row is full.");
                        break;
                    }
                    game.getGameTable().set(2, commands.addCardOnRow(game.getGameTable().get(2),
                            game.getPlayerOneHand().get(handIdx)));
                    game.setPlayerOneMana(game.getPlayerOneMana()
                            - game.getPlayerOneHand().get(handIdx).getMana());
                    game.getPlayerOneHand().remove(handIdx);
                    break;
                }

                if (cardNamePlayerOne.equals("Sentinel") || cardNamePlayerOne.equals("Berserker")
                        || cardNamePlayerOne.equals("The Cursed One")
                        || cardNamePlayerOne.equals("Disciple")) {
                    if (game.getGameTable().get(rowNumber).size() == maxSize) {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("handIdx", handIdx);
                        node.put("error", "Cannot place card on "
                                + "table since row is full.");
                        break;
                    }
                    game.getGameTable().set(rowNumber, commands.addCardOnRow(game
                                    .getGameTable().get(rowNumber),
                            game.getPlayerOneHand().get(handIdx)));
                    game.setPlayerOneMana(game.getPlayerOneMana()
                            - game.getPlayerOneHand().get(handIdx).getMana());
                    game.getPlayerOneHand().remove(handIdx);
                }
            }
            case 2 -> {
                if (game.getPlayerTwoHand().size() <= handIdx) {
                    break;
                }

                String cardNamePlayerTwo = game.getPlayerTwoHand().get(handIdx).getName();

                if (commands.isEnvironment(game.getPlayerTwoHand().get(handIdx))) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("error", "Cannot place environment card on table.");
                    break;
                }
                if (game.getPlayerTwoMana() < game.getPlayerTwoHand().get(handIdx).getMana()) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("error", "Not enough mana to place card on table.");
                    break;
                }

                if (cardNamePlayerTwo.equals("The Ripper") || cardNamePlayerTwo.equals("Miraj")
                        || cardNamePlayerTwo.equals("Goliath")
                        || cardNamePlayerTwo.equals("Warden")) {
                    if (game.getGameTable().get(1).size() == maxSize) {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("handIdx", handIdx);
                        node.put("error", "Cannot place card on "
                                + "table since row is full.");
                        break;
                    }

                    game.getGameTable().set(1, commands.addCardOnRow(game.getGameTable().get(1),
                            game.getPlayerTwoHand().get(handIdx)));
                    game.setPlayerTwoMana(game.getPlayerTwoMana()
                            - game.getPlayerTwoHand().get(handIdx).getMana());
                    game.getPlayerTwoHand().remove(handIdx);
                    break;
                }
                if (cardNamePlayerTwo.equals("Sentinel") || cardNamePlayerTwo.equals("Berserker")
                        || cardNamePlayerTwo.equals("The Cursed One")
                        || cardNamePlayerTwo.equals("Disciple")) {
                    if (game.getGameTable().get(0).size() == maxSize) {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("handIdx", handIdx);
                        node.put("error", "Cannot place card on "
                               + "table since row is full.");
                        break;
                    }

                    game.getGameTable().set(0, commands.addCardOnRow(game.getGameTable().get(0),
                            game.getPlayerTwoHand().get(handIdx)));
                    game.setPlayerTwoMana(game.getPlayerTwoMana()
                            - game.getPlayerTwoHand().get(handIdx).getMana());
                    game.getPlayerTwoHand().remove(handIdx);
                }
            }
        }
    }
}
