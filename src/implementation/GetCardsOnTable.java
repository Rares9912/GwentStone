package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.CardInput;

import java.util.ArrayList;

public class GetCardsOnTable {
    public GetCardsOnTable(final Game game, final ActionsInput action, final ArrayNode output,
                           final CommandsImplementation commands, final ObjectMapper mapper) {
        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());
        ArrayNode result = node.putArray("output");

        for (ArrayList<CardInput> cardInputs : game.getGameTable()) {
            commands.writeTable(cardInputs, result, mapper);
        }
    }
}
