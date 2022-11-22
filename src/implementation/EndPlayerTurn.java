package implementation;

import fileio.CardInput;
import fileio.StartGameInput;

import java.util.ArrayList;

public class EndPlayerTurn {
    public EndPlayerTurn(final Game game,
                         final StartGameInput startGame, final CommandsImplementation commands) {
        if (startGame.getStartingPlayer() == 2) {
            startGame.setStartingPlayer(1);
            for (int k = 0; k < 2; k++) {
                for (int l = 0; l < game.getGameTable().get(k).size(); l++) {
                    if (game.getGameTable().get(k).get(l).isFrozen()) {
                        game.getGameTable().get(k).get(l).setFrozen(false);
                    }
                }
            }
        } else {
            startGame.setStartingPlayer(2);
            for (int k = 2; k < 4; k++) {
                for (int l = 0; l < game.getGameTable().get(k).size(); l++) {
                    if (game.getGameTable().get(k).get(l).isFrozen()) {
                        game.getGameTable().get(k).get(l).setFrozen(false);
                    }
                }
            }
        }
        game.setTurnNumber(game.getTurnNumber() + 1);

        if (game.getTurnNumber() == 2) {
            game.setTurnNumber(0);
            game.setRoundNumber(game.getRoundNumber() + 1);

            startGame.getPlayerOneHero().setHasAttacked(false);
            startGame.getPlayerTwoHero().setHasAttacked(false);

            for (ArrayList<CardInput> cardInputs : game.getGameTable()) {
                for (CardInput cardInput : cardInputs) {
                    if (cardInput.hasAttacked()) {
                        cardInput.setHasAttacked(false);
                    }
                }
            }

            if (game.getPlayerOneDeck().size() != 0) {
                game.getPlayerOneHand().add(game.getPlayerOneDeck().get(0));
                game.getPlayerOneDeck().remove(0);
            }

            if (game.getPlayerTwoDeck().size() != 0) {
                game.getPlayerTwoHand().add(game.getPlayerTwoDeck().get(0));
                game.getPlayerTwoDeck().remove(0);
            }

            if (game.getRoundNumber() < 10) {
                game.setPlayerOneMana(commands.addPlayerMana(game.getRoundNumber(),
                        game.getPlayerOneMana()));
                game.setPlayerTwoMana(commands.addPlayerMana(game.getRoundNumber(),
                        game.getPlayerTwoMana()));
            } else {
                game.setPlayerTwoMana(commands.addPlayerMana(10,
                        game.getPlayerOneMana()));
                game.setPlayerOneMana(commands.addPlayerMana(10,
                        game.getPlayerOneMana()));
            }
        }
    }
}
