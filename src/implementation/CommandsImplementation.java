package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.Coordinates;

import java.util.ArrayList;

public class CommandsImplementation {
    public void writeDeck (ArrayList<CardInput> deck, ArrayNode output, ObjectMapper objectMapper) {
        for (int i = 0; i < deck.size(); i++) {
            ObjectNode cardInfo = objectMapper.createObjectNode();
            writeCard(deck.get(i), cardInfo, objectMapper);
            output.add(cardInfo);
        }
    }

    public void writeCard (CardInput card, ObjectNode output, ObjectMapper objectMapper) {
        boolean isHero = card.getName().equals("Lord Royce") ||
                card.getName().equals("Empress Thorina")
                || card.getName().equals("King Mudface") ||
                card.getName().equals("General Kocioraw");

        output.put("mana", card.getMana());

        if (card.getHealth() != 0 && !isHero) {
            output.put("attackDamage", card.getAttackDamage());
            output.put("health", card.getHealth());
        }
        output.put("description", card.getDescription());

        ArrayNode colours = output.putArray("colors");

        for (String colour : card.getColors()) {
            colours.add(colour);
        }
        output.put("name", card.getName());

        if (isHero) {
            output.put("health", card.getHealth());
        }

    }

    public void writeTable (ArrayList<CardInput> cardRow, ArrayNode output, ObjectMapper objectMapper) {
        ArrayNode tableInfo = objectMapper.createArrayNode();
        writeDeck(cardRow, tableInfo, objectMapper);
        output.add(tableInfo);
    }

    public void writeCoordinates (Coordinates coordinates, ObjectNode output, ObjectMapper objectMapper) {
        output.put("x", coordinates.getX());
        output.put("y", coordinates.getY());
    }


    public int addPlayerMana(int manaGiven, int playerMana) {
        return playerMana + manaGiven;
    }

    public ArrayList<CardInput> addCardOnRow(ArrayList<CardInput> row, CardInput card) {
        if(row.size() < 5) {
            row.add(card);
        }
        return row;
    }

    public boolean isEnvironment (CardInput card) {
        return card.getName().equals("Firestorm")
                || card.getName().equals("Winterfell")
                || card.getName().equals("Heart Hound");
    }

    public boolean isHero (CardInput card) {
        return card.getName().equals("Lord Royce")
                || card.getName().equals("Empress Thorina")
                || card.getName().equals("King Mudface")
                || card.getName().equals("General Kocioraw");
    }

    public boolean isTank (CardInput card) {
        return card.getName().equals("Warden") || card.getName().equals("Goliath");
    }

    public ArrayList<CardInput> removeCardFromRow(ArrayList<CardInput> row, int cardIndex) {
        row.remove(cardIndex);
        return row;
    }

    public CardInput attackCard (CardInput cardAttacker, CardInput cardAttacked) {
        int attackerDamage = cardAttacker.getAttackDamage();
        int damage = cardAttacked.getHealth() - attackerDamage;
        cardAttacked.setHealth(damage);
        return cardAttacked;
    }

    public boolean isTankOnRow (ArrayList<CardInput> cardRow) {
        for (int i = 0; i < cardRow.size(); i++) {
            String cardName = cardRow.get(i).getName();
            if (cardName.equals("Goliath") || cardName.equals("Warden"))
                return true;
        }
        return false;
    }

    public boolean isAttackingCard (CardInput card) {
        return card.getName().equals("The Ripper")
                || card.getName().equals("Miraj")
                || card.getName().equals("The Cursed One");
    }
}
