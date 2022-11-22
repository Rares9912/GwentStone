package implementation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;

public class GetPlayerMana {
    public GetPlayerMana(final Game game, final ActionsInput action, final ArrayNode output) {
        int playerIdx = action.getPlayerIdx();
        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());
        node.put("playerIdx", playerIdx);

        if (playerIdx == 1) {
            node.put("output", game.getPlayerOneMana());
        } else if (playerIdx == 2) {
            node.put("output", game.getPlayerTwoMana());
        }
    }
}
