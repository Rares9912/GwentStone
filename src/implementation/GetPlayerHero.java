package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.StartGameInput;

public class GetPlayerHero {

    public GetPlayerHero(final StartGameInput startGame, final ActionsInput action,
                         final ArrayNode output, final CommandsImplementation commands,
                         final ObjectMapper mapper) {

        ObjectNode node = output.addObject();
        node.put("command", action.getCommand());
        node.put("playerIdx", action.getPlayerIdx());
        ObjectNode result = mapper.createObjectNode();

        if (action.getPlayerIdx() == 2) {
            commands.writeCard(startGame.getPlayerTwoHero(), result, mapper);
            node.put("output", result);
        } else {
            commands.writeCard(startGame.getPlayerOneHero(), result, mapper);
            node.put("output", result);
        }
    }
}
