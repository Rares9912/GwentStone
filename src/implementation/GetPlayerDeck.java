package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;

public class GetPlayerDeck {
    public GetPlayerDeck(final Game game, final ActionsInput action, final ArrayNode output,
                         final CommandsImplementation commands, final ObjectMapper mapper) {

        String command = action.getCommand();
        int playerIdx = action.getPlayerIdx();
        ObjectNode node = output.addObject();
        node.put("command", command);
        node.put("playerIdx", playerIdx);
        ArrayNode result = node.putArray("output");

        if (playerIdx == 2) {
            commands.writeDeck(game.getPlayerTwoDeck(), result, mapper);
        } else {
            commands.writeDeck(game.getPlayerOneDeck(), result, mapper);
        }
    }
}
