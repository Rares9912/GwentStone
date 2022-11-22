package implementation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.StartGameInput;

public class GetPlayerTurn {
    public GetPlayerTurn(final ActionsInput action, final ArrayNode output,
                         final StartGameInput startGame) {
        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());
        node.put("output", startGame.getStartingPlayer());
    }
}
