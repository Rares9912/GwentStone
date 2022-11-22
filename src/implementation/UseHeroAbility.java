package implementation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.StartGameInput;

public class UseHeroAbility {
    public UseHeroAbility(final Game game, final StartGameInput startGame,
                          final ActionsInput action, final ArrayNode output,
                          final CommandsImplementation commands) {
        int playerTurn = startGame.getStartingPlayer();
        int affectedRow = action.getAffectedRow();
        boolean isError = false;
        String heroName = "";

        switch (playerTurn) {
            case 1 -> {
                if (game.getPlayerOneMana() < startGame.getPlayerOneHero().getMana()) {
                    isError = true;
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Not enough mana to use hero's ability.");
                    break;
                } else if (startGame.getPlayerOneHero().hasAttacked()) {
                    isError = true;
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Hero has already attacked this turn.");
                    break;
                }
                heroName = startGame.getPlayerOneHero().getName();

                if (heroName.equals("Lord Royce") || heroName.equals("Empress Thorina")) {
                    if (affectedRow == 2 || affectedRow == 3) {
                        isError = true;
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("affectedRow", affectedRow);
                        node.put("error", "Selected row does not belong to the enemy.");
                        break;
                    }
                } else if (affectedRow == 0 || affectedRow == 1) {
                    isError = true;
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Selected row does not belong "
                            + "to the current player.");
                    break;
                }
                game.setPlayerOneMana(game.getPlayerOneMana()
                        - startGame.getPlayerOneHero().getMana());
                startGame.getPlayerOneHero().setHasAttacked(true);
            }

            case 2 -> {
                if (game.getPlayerTwoMana() < startGame.getPlayerTwoHero().getMana()) {
                    isError = true;
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Not enough mana to use hero's ability.");
                    break;
                } else if (startGame.getPlayerTwoHero().hasAttacked()) {
                    isError = true;
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Hero has already attacked this turn.");
                    break;
                }
                heroName = startGame.getPlayerTwoHero().getName();

                if (heroName.equals("Lord Royce") || heroName.equals("Empress Thorina")) {
                    if (affectedRow == 0 || affectedRow == 1) {
                        isError = true;
                        ObjectNode node = output.addObject();
                        node.put("command", action.getCommand());
                        node.put("affectedRow", affectedRow);
                        node.put("error", "Selected row does not belong to the enemy.");
                        break;
                    }
                } else if (affectedRow == 2 || affectedRow == 3) {
                    isError = true;
                    ObjectNode node = output.addObject();
                    node.put("command", action.getCommand());
                    node.put("affectedRow", affectedRow);
                    node.put("error", "Selected row does not belong "
                           + "to the current player.");
                    break;
                }
                game.setPlayerTwoMana(game.getPlayerTwoMana()
                        - startGame.getPlayerTwoHero().getMana());
                startGame.getPlayerTwoHero().setHasAttacked(true);
            }
        }
        if (!isError) {
            switch (heroName) {
                case "Lord Royce" -> {
                    int maxA = 0;
                    int maxAttackIndex = 0;
                    for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                        if (game.getGameTable().get(affectedRow).get(k).getAttackDamage() > maxA) {
                            maxA = game.getGameTable()
                                    .get(affectedRow).get(k).getAttackDamage();
                            maxAttackIndex = k;
                        }
                    }
                    if (!game.getGameTable().get(affectedRow).get(maxAttackIndex).isFrozen()) {
                        game.getGameTable().get(affectedRow).get(maxAttackIndex).setFrozen(true);
                    }
                }
                case "Empress Thorina" -> {
                    int maxHealth = 0;
                    int maxHealthIndex = 0;

                    for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                        if (game.getGameTable().get(affectedRow).get(k).getHealth() > maxHealth) {
                            maxHealth = game.getGameTable().get(affectedRow).get(k).getHealth();
                            maxHealthIndex = k;
                        }
                    }
                    game.getGameTable().set(affectedRow,
                            commands.removeCardFromRow(
                                    game.getGameTable().get(affectedRow),
                                    maxHealthIndex));
                }
                case "King Mudface" -> {
                    for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                        game.getGameTable().get(affectedRow).get(k).setHealth(
                                game.getGameTable().get(affectedRow).get(k).getHealth() + 1);
                    }
                }
                case "General Kocioraw" -> {
                    for (int k = 0; k < game.getGameTable().get(affectedRow).size(); k++) {
                        game.getGameTable().get(affectedRow).get(k).setAttackDamage(
                                game.getGameTable().get(affectedRow).get(k).getAttackDamage() + 1);
                    }
                }
            }
        }
    }
}
