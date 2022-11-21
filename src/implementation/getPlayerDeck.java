package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;
import fileio.CardInput;

import java.util.ArrayList;

public class getPlayerDeck {
    public getPlayerDeck(ActionsInput action, ArrayNode output, CommandsImplementation commands,
                         ArrayList<CardInput> playerOneDeck, ArrayList<CardInput> playerTwoDeck,
                         ObjectMapper mapper) {
        String command = action.getCommand();
        int playerIdx = action.getPlayerIdx();
        ObjectNode node = output.addObject();
        node.put("command", command);
        node.put("playerIdx", playerIdx);
        ArrayNode result = node.putArray("output");

        if (playerIdx == 2) {
            commands.writeDeck(playerTwoDeck, result, mapper);
        } else {
            commands.writeDeck(playerOneDeck, result, mapper);
        }
    }
}
