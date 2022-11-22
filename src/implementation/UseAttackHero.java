package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.CardInput;
import fileio.Coordinates;
import fileio.StartGameInput;

public class UseAttackHero {
    public UseAttackHero(final Game game, final StartGameInput startGame, final ActionsInput action,
                         final ArrayNode output, final CommandsImplementation commands,
                         final ObjectMapper mapper) {
        int playerTurn = startGame.getStartingPlayer();
        Coordinates cardAttackerCoord = action.getCardAttacker();
        CardInput cardAttacker = game.getGameTable()
                .get(cardAttackerCoord.getX()).get(cardAttackerCoord.getY());

        if (cardAttacker.isFrozen()) {
            ObjectNode node = output.addObject();
            node.put("command", action.getCommand());
            ObjectNode result = mapper.createObjectNode();
            commands.writeCoordinates(cardAttackerCoord, result);
            node.put("cardAttacker", result);
            node.put("error", "Attacker card is frozen.");
        } else if (cardAttacker.hasAttacked()) {
            ObjectNode node = output.addObject();
            node.put("command", action.getCommand());
            ObjectNode result = mapper.createObjectNode();
            commands.writeCoordinates(cardAttackerCoord, result);
            node.put("cardAttacker", result);
            node.put("error", "Attacker card has already attacked this turn.");
        } else {
            switch (playerTurn) {
                case 1 -> {
                    if (commands.isTankOnRow(game.getGameTable().get(1))) {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        ObjectNode result = mapper.createObjectNode();
                        commands.writeCoordinates(cardAttackerCoord, result);
                        node.put("cardAttacker", result);
                        node.put("error", "Attacked card is not of type 'Tank'.");
                        break;
                    }
                    startGame.getPlayerTwoHero()
                            .setHealth(startGame.getPlayerTwoHero().getHealth()
                                    - cardAttacker.getAttackDamage());
                    if (startGame.getPlayerTwoHero().getHealth() <= 0) {
                        ObjectNode node = output.addObject();
                        node.put("gameEnded", "Player one killed the enemy hero.");
                        game.setPlayerOneWins(game.getPlayerOneWins() + 1);
                        game.setGameEnded(true);
                    }
                    cardAttacker.setHasAttacked(true);
                }
                case 2 -> {
                    if (commands.isTankOnRow(game.getGameTable().get(2))) {
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        ObjectNode result = mapper.createObjectNode();
                        commands.writeCoordinates(cardAttackerCoord, result);
                        node.put("cardAttacker", result);
                        node.put("error", "Attacked card is not of type 'Tank'.");
                        break;
                    }
                    startGame.getPlayerOneHero().setHealth(startGame.getPlayerOneHero().getHealth()
                            - cardAttacker.getAttackDamage());
                    if (startGame.getPlayerOneHero().getHealth() <= 0) {
                        ObjectNode node = output.addObject();
                        node.put("gameEnded", "Player two killed the enemy hero.");
                        game.setPlayerTwoWins(game.getPlayerTwoWins() + 1);
                        game.setGameEnded(true);
                    }
                    cardAttacker.setHasAttacked(true);
                }
            }
        }
    }
}
