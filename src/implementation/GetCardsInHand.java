package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;

public class GetCardsInHand {
    public GetCardsInHand(final Game game, final ActionsInput action,
                          final CommandsImplementation commands, final ArrayNode output,
                          final ObjectMapper mapper) {
        int playerIdx = action.getPlayerIdx();
        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());
        node.put("playerIdx", playerIdx);
        ArrayNode result = node.putArray("output");
        if (playerIdx == 1) {
            commands.writeDeck(game.getPlayerOneHand(), result, mapper);
        } else if (playerIdx == 2) {
            commands.writeDeck(game.getPlayerTwoHand(), result, mapper);
        }
    }
}
