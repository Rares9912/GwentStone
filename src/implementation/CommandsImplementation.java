package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;

import java.util.ArrayList;

public class CommandsImplementation {
    public void writeCard (CardInput card, ArrayNode output, ObjectMapper objectMapper) {
        ObjectNode cardInfo = objectMapper.createObjectNode();
        cardInfo.put("mana", card.getMana());

        if (card.getHealth() != 0) {
            cardInfo.put("attackDamage", card.getAttackDamage());
            cardInfo.put("health", card.getHealth());
        }
        cardInfo.put("description", card.getDescription());

        ArrayNode colours = cardInfo.putArray("colors");
        for (String colour : card.getColors()) {
            colours.add(colour);
        }
        cardInfo.put("name", card.getName());
        output.add(cardInfo);
    }

    public void writeHero (CardInput card, ObjectNode output, ObjectMapper objectMapper) {
        output.put("mana", card.getMana());
        output.put("description", card.getDescription());

        ArrayNode colours = output.putArray("colors");

        for (String colour : card.getColors()) {
            colours.add(colour);
        }
        output.put("name", card.getName());
        output.put("health", 30);
    }

    public void writeTable (ArrayList<CardInput> cardRow, ArrayNode output, ObjectMapper objectMapper) {
        ArrayNode tableInfo = objectMapper.createArrayNode();

        for (int i = 0; i < cardRow.size(); i++) {
            writeCard(cardRow.get(i), tableInfo, objectMapper);
        }
        output.add(tableInfo);
    }

    public int addPlayerMana(int manaGiven, int playerMana) {
        return playerMana + manaGiven;
    }

    public ArrayList<CardInput> addCardOnRow(ArrayList<CardInput> row, CardInput element) {
        if(row.size() < 5) {
            row.add(element);
        }
        return row;
    }
}
