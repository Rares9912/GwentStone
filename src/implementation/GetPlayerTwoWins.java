package implementation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;

public class GetPlayerTwoWins {
    public GetPlayerTwoWins(final Game game, final ActionsInput action, final ArrayNode output) {
        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());
        node.put("output", game.getPlayerTwoWins());
    }
}
