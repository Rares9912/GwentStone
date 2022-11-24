package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.CardInput;
import fileio.Coordinates;
import fileio.StartGameInput;

public class CardUsesAttack {
    public CardUsesAttack(final Game game, final StartGameInput startGame,
                          final ActionsInput action, final ArrayNode output,
                          final CommandsImplementation commands, final ObjectMapper mapper) {
        Coordinates cardAttackerCoord = action.getCardAttacker();
        Coordinates cardAttackedCoord = action.getCardAttacked();
        CardInput cardAttacker = game.getGameTable().get(cardAttackerCoord.getX())
                .get(cardAttackerCoord.getY());
        CardInput cardAttacked = game.getGameTable().get(cardAttackedCoord.getX())
                .get(cardAttackedCoord.getY());
        int playerTurn = startGame.getStartingPlayer();
        boolean isError = false;

        if (cardAttacker.hasAttacked()) {
            ObjectNode node = output.addObject();
            node.put("command", action.getCommand());
            ObjectNode result1 = mapper.createObjectNode();
            commands.writeCoordinates(cardAttackerCoord, result1);
            node.put("cardAttacker", result1);
            ObjectNode result2 = mapper.createObjectNode();
            commands.writeCoordinates(cardAttackedCoord, result2);
            node.put("cardAttacked", result2);
            node.put("error", "Attacker card has already attacked this turn.");
        } else if (cardAttacker.isFrozen()) {
            ObjectNode node = output.addObject();
            node.put("command", action.getCommand());
            ObjectNode result1 = mapper.createObjectNode();
            commands.writeCoordinates(cardAttackerCoord, result1);
            node.put("cardAttacker", result1);
            ObjectNode result2 = mapper.createObjectNode();
            commands.writeCoordinates(cardAttackedCoord, result2);
            node.put("cardAttacked", result2);
            node.put("error", "Attacker card is frozen.");
        } else {
            switch (playerTurn) {
                case 1 -> {
                    if (cardAttackedCoord.getX() == 2 || cardAttackedCoord.getX() == 3) {
                        isError = true;
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        ObjectNode result1 = mapper.createObjectNode();
                        commands.writeCoordinates(cardAttackerCoord, result1);
                        node.put("cardAttacker", result1);
                        ObjectNode result2 = mapper.createObjectNode();
                        commands.writeCoordinates(cardAttackedCoord, result2);
                        node.put("cardAttacked", result2);
                        node.put("error", "Attacked card does not belong "
                                + "to the enemy.");
                        break;
                    }

                    if (commands.isTankOnRow(game.getGameTable().get(1))) {
                        if (!commands.isTank(cardAttacked)) {
                            isError = true;
                            ObjectNode node = output.addObject();
                            node.put("command", action.getCommand());
                            ObjectNode result1 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result1);
                            node.put("cardAttacker", result1);
                            ObjectNode result2 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackedCoord, result2);
                            node.put("cardAttacked", result2);
                            node.put("error", "Attacked card is not of type 'Tank'.");
                        }
                    }
                }
                case 2 -> {
                    if (cardAttackedCoord.getX() == 0 || cardAttackedCoord.getX() == 1) {
                        isError = true;
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        ObjectNode result1 = mapper.createObjectNode();
                        commands.writeCoordinates(cardAttackerCoord, result1);
                        node.put("cardAttacker", result1);
                        ObjectNode result2 = mapper.createObjectNode();
                        commands.writeCoordinates(cardAttackedCoord, result2);
                        node.put("cardAttacked", result2);
                        node.put("error", "Attacked card does not belong to "
                                + "the enemy.");
                        break;
                    }

                    if (commands.isTankOnRow(game.getGameTable().get(2))) {
                        if (!commands.isTank(cardAttacked)) {
                            isError = true;
                            ObjectNode node = output.addObject();
                            node.put("command", action.getCommand());
                            ObjectNode result1 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackerCoord, result1);
                            node.put("cardAttacker", result1);
                            ObjectNode result2 = mapper.createObjectNode();
                            commands.writeCoordinates(cardAttackedCoord, result2);
                            node.put("cardAttacked", result2);
                            node.put("error", "Attacked card is not of type 'Tank'.");
                        }
                    }
                }
            }

            if (!isError) {
                game.getGameTable().get(cardAttackedCoord.getX()).set(cardAttackedCoord.getY(),
                        commands.attackCard(cardAttacker, cardAttacked));

                if (game.getGameTable().get(cardAttackedCoord.getX())
                        .get(cardAttackedCoord.getY()).getHealth() <= 0) {
                    game.getGameTable().set(cardAttackedCoord.getX(),
                            commands.removeCardFromRow(game.getGameTable()
                                            .get(cardAttackedCoord.getX()),
                                    cardAttackedCoord.getY()));
                }
                game.getGameTable().get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY())
                        .setHasAttacked(true);
            }
        }
    }
}
