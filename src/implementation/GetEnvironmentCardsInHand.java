package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.CardInput;

import java.util.ArrayList;

public class GetEnvironmentCardsInHand {
    public GetEnvironmentCardsInHand(final Game game, final ActionsInput action,
                                     final ArrayNode output, final CommandsImplementation commands,
                                     final ObjectMapper mapper) {
        int playerIdx = action.getPlayerIdx();
        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());
        node.put("playerIdx", playerIdx);

        ArrayNode result = node.putArray("output");
        ArrayList<CardInput> environmentCards = new ArrayList<>();

        switch (playerIdx) {
            case 1 -> {
                for (CardInput cardInput : game.getPlayerOneHand()) {
                    if (commands.isEnvironment(cardInput)) {
                        environmentCards.add(cardInput);
                    }
                }
                commands.writeDeck(environmentCards, result, mapper);
            }
            case 2 -> {
                for (CardInput cardInput : game.getPlayerTwoHand()) {
                    if (commands.isEnvironment(cardInput)) {
                        environmentCards.add(cardInput);
                    }
                }
                commands.writeDeck(environmentCards, result, mapper);
            }
        }
    }
}
