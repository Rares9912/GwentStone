package implementation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.CardInput;
import fileio.StartGameInput;

public class UseEnvironmentCard {
    public UseEnvironmentCard(final Game game, final StartGameInput startGame,
                              final ActionsInput action, final ArrayNode output,
                              final CommandsImplementation commands) {
        int handIdx = action.getHandIdx();
        int affectedRow = action.getAffectedRow();
        int playerTurn = startGame.getStartingPlayer();
        final int rowNumber = 3;
        final int maxSize = 5;

        switch (playerTurn) {
            case 1 -> {
                if (!commands.isEnvironment(game.getPlayerOneHand().get(handIdx))) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Chosen card is not of type environment.");
                    break;
                }

                if (game.getPlayerOneMana() < game.getPlayerOneHand().get(handIdx).getMana()) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Not enough mana to use environment card.");
                    break;
                }

                if (affectedRow == 2 || affectedRow == rowNumber) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Chosen row does not belong to the enemy.");
                    break;
                }

                String environmentCardName = game.getPlayerOneHand().get(handIdx).getName();

                switch (environmentCardName) {

                    case "Firestorm" -> {
                        for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                            game.getGameTable().get(affectedRow).get(k).setHealth(
                                    game.getGameTable().get(affectedRow).get(k).getHealth() - 1);
                        }
                        for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                            boolean everyCardDead = true;
                            if (game.getGameTable().get(affectedRow).get(k).getHealth() == 0) {
                                game.getGameTable().set(affectedRow,
                                        commands.removeCardFromRow(
                                                game.getGameTable().get(affectedRow), k));
                                everyCardDead = false;
                            }

                            if (!everyCardDead) {
                                k = 0;
                            }
                        }
                        game.setPlayerOneMana(game.getPlayerOneMana()
                                - game.getPlayerOneHand().get(handIdx).getMana());
                        game.getPlayerOneHand().remove(handIdx);
                    }
                    case "Winterfell" -> {
                        for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                            game.getGameTable().get(affectedRow).get(k).setFrozen(true);
                        }
                        game.setPlayerOneMana(game.getPlayerOneMana()
                                - game.getPlayerOneHand().get(handIdx).getMana());
                        game.getPlayerOneHand().remove(handIdx);
                    }
                    case "Heart Hound" -> {
                        int maxH = 0;
                        int minionIndex = 0;
                        CardInput minionMaxHealth = new CardInput();

                        for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                            if (game.getGameTable().get(affectedRow).get(k).getHealth() > maxH) {
                                maxH = game.getGameTable().get(affectedRow).get(k).getHealth();
                                minionMaxHealth = game.getGameTable().get(affectedRow).get(k);
                                minionIndex = k;
                            }
                        }
                        if (affectedRow == 0) {
                            if (game.getGameTable().get(rowNumber).size() == maxSize) {
                                ObjectNode node = output.addObject();
                                node.put("command", action.getCommand());
                                node.put("handIdx", handIdx);
                                node.put("affectedRow", affectedRow);
                                node.put("error", "Cannot steal enemy card since "
                                        + "the player's row is full.");
                                break;
                            }
                            game.getGameTable().set(rowNumber, commands.addCardOnRow(
                                    game.getGameTable().get(rowNumber), minionMaxHealth));
                            game.getGameTable().set(0, commands.removeCardFromRow(
                                    game.getGameTable().get(0), minionIndex));
                        } else if (affectedRow == 1) {
                            if (game.getGameTable().get(2).size() == maxSize) {
                                ObjectNode node = output.addObject();
                                node.put("command", action.getCommand());
                                node.put("handIdx", handIdx);
                                node.put("affectedRow", affectedRow);
                                node.put("error", "Cannot steal enemy card "
                                       + "since the player's row is full.");
                                break;
                            }
                            game.getGameTable().set(2, commands.addCardOnRow(
                                    game.getGameTable().get(2), minionMaxHealth));
                            game.getGameTable().set(1, commands.removeCardFromRow(
                                    game.getGameTable().get(1), minionIndex));
                        }
                        game.setPlayerOneMana(game.getPlayerOneMana()
                                - game.getPlayerOneHand().get(handIdx).getMana());
                        game.getPlayerOneHand().remove(handIdx);
                    }
                }
            }
            case 2 -> {
                if (!commands.isEnvironment(game.getPlayerTwoHand().get(handIdx))) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Chosen card is not of type environment.");
                    break;
                }

                if (game.getPlayerTwoMana() < game.getPlayerTwoHand().get(handIdx).getMana()) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Not enough mana to use environment card.");
                    break;
                }

                if (affectedRow == 0 || affectedRow == 1) {
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("handIdx", handIdx);
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Chosen row does not belong to the enemy.");
                    break;
                }

                String environmentCardName = game.getPlayerTwoHand().get(handIdx).getName();

                switch (environmentCardName) {
                    case "Firestorm" -> {
                        for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                            game.getGameTable().get(affectedRow).get(k).setHealth(
                                    game.getGameTable().get(affectedRow).get(k).getHealth() - 1);
                        }
                        for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                            boolean everyCardDead = true;
                            if (game.getGameTable().get(affectedRow).get(k).getHealth() == 0) {
                                game.getGameTable().set(affectedRow, commands.removeCardFromRow(
                                        game.getGameTable().get(affectedRow), k));
                                everyCardDead = false;
                            }
                            if (!everyCardDead) {
                                k = 0;
                            }
                        }
                        game.setPlayerTwoMana(game.getPlayerTwoMana()
                                - game.getPlayerTwoHand().get(handIdx).getMana());
                        game.getPlayerTwoHand().remove(handIdx);
                    }
                    case "Winterfell" -> {
                        for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                            game.getGameTable().get(affectedRow).get(k).setFrozen(true);
                        }
                        game.setPlayerTwoMana(game.getPlayerTwoMana()
                                - game.getPlayerTwoHand().get(handIdx).getMana());
                        game.getPlayerTwoHand().remove(handIdx);
                    }
                    case "Heart Hound" -> {
                        int maxH = 0;
                        int minionIndex = 0;
                        CardInput minionMaxHealth = new CardInput();

                        for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                            if (game.getGameTable().get(affectedRow).get(k).getHealth() > maxH) {
                                maxH = game.getGameTable().get(affectedRow).get(k).getHealth();
                                minionMaxHealth = game.getGameTable().get(affectedRow).get(k);
                                minionIndex = k;
                            }
                        }
                        if (affectedRow == rowNumber) {
                            if (game.getGameTable().get(0).size() == maxSize) {
                                ObjectNode node = output.addObject();
                                node.put("command", action.getCommand());
                                node.put("handIdx", handIdx);
                                node.put("affectedRow", affectedRow);
                                node.put("error", "Cannot steal enemy card since "
                                        + "the player's row is full.");
                                break;
                            }
                            game.getGameTable().set(0,
                                    commands.addCardOnRow(game.getGameTable().get(0),
                                            minionMaxHealth));
                            game.getGameTable().set(rowNumber,
                                    commands.removeCardFromRow(game.getGameTable().get(rowNumber),
                                            minionIndex));


                        } else if (affectedRow == 2) {
                            if (game.getGameTable().get(1).size() == maxSize) {
                                ObjectNode node = output.addObject();
                                node.put("command", action.getCommand());
                                node.put("handIdx", handIdx);
                                node.put("affectedRow", affectedRow);
                                node.put("error", "Cannot steal enemy card since "
                                        + "the player's row is full.");
                                break;
                            }
                            game.getGameTable().set(1, commands.addCardOnRow(game.getGameTable()
                                    .get(1), minionMaxHealth));
                            game.getGameTable().set(2,
                                    commands.removeCardFromRow(game.getGameTable()
                                    .get(2), minionIndex));

                        }
                        game.setPlayerTwoMana(game.getPlayerTwoMana()
                                - game.getPlayerTwoHand().get(handIdx).getMana());
                        game.getPlayerTwoHand().remove(handIdx);
                    }
                }
            }
        }
    }
}
