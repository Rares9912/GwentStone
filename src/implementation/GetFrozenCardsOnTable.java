package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.CardInput;

import java.util.ArrayList;

public class GetFrozenCardsOnTable {
    public GetFrozenCardsOnTable(final Game game, final ActionsInput action, final ArrayNode output,
                                 final CommandsImplementation commands, final ObjectMapper mapper) {
        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());

        ArrayNode result = node.putArray("output");
        ArrayList<CardInput> frozenCards = new ArrayList<>();

        for (ArrayList<CardInput> cardInputs : game.getGameTable()) {
            for (CardInput cardInput : cardInputs) {
                if (cardInput.isFrozen()) {
                    frozenCards.add(cardInput);
                }
            }
        }
        commands.writeDeck(frozenCards, result, mapper);
    }
}
