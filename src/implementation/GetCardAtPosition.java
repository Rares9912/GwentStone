package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;

public class GetCardAtPosition {
    public GetCardAtPosition(final Game game, final ActionsInput action, final ArrayNode output,
                             final CommandsImplementation commands, final ObjectMapper mapper) {
        int x = action.getX();
        int y = action.getY();
        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());
        node.put("x", x);
        node.put("y", y);

        ObjectNode result = mapper.createObjectNode();

        if (game.getGameTable().get(x).size() <= y) {
            node.put("output", "No card available at that position.");
        } else {
            commands.writeCard(game.getGameTable().get(x).get(y), result, mapper);
            node.put("output", result);
        }
    }
}
