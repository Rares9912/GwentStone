package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.Input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Game {



    public Game(Input input, ArrayNode output) {

        for(int i = 0; i < input.getGames().size(); i++) {
            Random rand = new Random(input.getGames().get(i).getStartGame().getShuffleSeed());
            ObjectMapper mapper = new ObjectMapper();
            CommandsImplementation commands = new CommandsImplementation();

            int playerOneMana = 1;
            int playerTwoMana = 1;

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

            for (int j = 0; j < input.getGames().get(i).getActions().size(); j++){
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
                        } else for (int k = 0; k < input.getPlayerOneDecks().getDecks().get(deckIndex1).size(); k++) {
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

                           if(input.getGames().get(i).getActions().get(j).getPlayerIdx() == 2) {
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
                           if(input.getGames().get(i).getStartGame().getStartingPlayer() == 2){
                               input.getGames().get(i).getStartGame().setStartingPlayer(1);
                           } else {
                               input.getGames().get(i).getStartGame().setStartingPlayer(2);
                           }
                           break;
                       }

                       case "placeCard": {

                       }
                }

            }


        }


    }

}
