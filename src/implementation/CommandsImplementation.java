package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.Coordinates;

import java.util.ArrayList;

public final class CommandsImplementation {

    /** Writes the card Deck in the output file */

    public void writeDeck(final ArrayList<CardInput> deck, final ArrayNode output,
                           final ObjectMapper objectMapper) {

        for (CardInput cardInput : deck) {
            ObjectNode cardInfo = objectMapper.createObjectNode();
            writeCard(cardInput, cardInfo, objectMapper);
            output.add(cardInfo);
        }

    }

    /** Writes the Card in the output file */

    public void writeCard(final CardInput card, final ObjectNode output,
                           final ObjectMapper objectMapper) {

        output.put("mana", card.getMana());

        if (card.getHealth() != 0 && !this.isHero(card)) {
            output.put("attackDamage", card.getAttackDamage());
            output.put("health", card.getHealth());

        }
        output.put("description", card.getDescription());

        ArrayNode colours = output.putArray("colors");

        for (String colour : card.getColors()) {
            colours.add(colour);
        }
        output.put("name", card.getName());

        if (this.isHero(card)) {
            output.put("health", card.getHealth());
        }

    }

    /** Writes the game Table in the output file */

    public void writeTable(final ArrayList<CardInput> cardRow, final ArrayNode output,
                            final ObjectMapper objectMapper) {

        ArrayNode tableInfo = objectMapper.createArrayNode();
        writeDeck(cardRow, tableInfo, objectMapper);
        output.add(tableInfo);

    }

    /** Writes the coordinates in the output file */

    public void writeCoordinates(final Coordinates coordinates, final ObjectNode output) {

        output.put("x", coordinates.getX());
        output.put("y", coordinates.getY());

    }

    /** Gives the player mana according to the round number */

    public int addPlayerMana(final int manaGiven, final int playerMana) {

        return playerMana + manaGiven;

    }

    /** Adds a card on the table */

    public ArrayList<CardInput> addCardOnRow(final ArrayList<CardInput> row,
                                             final CardInput card) {
        final int maxSize = 5;
        if (row.size() < maxSize) {
            row.add(card);
        }
        return row;
    }

    /** Checks if a card is an Environment one */

    public boolean isEnvironment(final CardInput card) {
        return card.getName().equals("Firestorm")
                || card.getName().equals("Winterfell")
                || card.getName().equals("Heart Hound");
    }

    /** Checks if a card is a hero */

    public boolean isHero(final CardInput card) {
        return card.getName().equals("Lord Royce")
                || card.getName().equals("Empress Thorina")
                || card.getName().equals("King Mudface")
                || card.getName().equals("General Kocioraw");
    }

    /** Checks if a card is a tank */

    public boolean isTank(final CardInput card) {
        return card.getName().equals("Warden") || card.getName().equals("Goliath");
    }

    /** Removes a given card from a given row */

    public ArrayList<CardInput> removeCardFromRow(final ArrayList<CardInput> row,
                                                  final int cardIndex) {
        row.remove(cardIndex);
        return row;
    }

    /** Attacks a card by decreasing its health with the attacker's damage */

    public CardInput attackCard(final CardInput cardAttacker, final CardInput cardAttacked) {

        int attackerDamage = cardAttacker.getAttackDamage();
        int damage = cardAttacked.getHealth() - attackerDamage;
        cardAttacked.setHealth(damage);
        return cardAttacked;

    }

    /** Checks if there is a tank on the specified row */

    public boolean isTankOnRow(final ArrayList<CardInput> cardRow) {
        for (CardInput cardInput : cardRow) {
            String cardName = cardInput.getName();
            if (cardName.equals("Goliath") || cardName.equals("Warden")) {
                return true;
            }
        }
        return false;
    }

    /** Checks if the given card attacks */

    public boolean isAttackingCard(final CardInput card) {
        return card.getName().equals("The Ripper")
                || card.getName().equals("Miraj")
                || card.getName().equals("The Cursed One");
    }

    /** Checks if the given command is a getter */

    public boolean isGetCommand(final String command) {
        return command.equals("getPlayerDeck") || command.equals("getPlayerHero")
                || command.equals("getPlayerTurn") || command.equals("getCardsInHand")
                || command.equals("getPlayerMana") || command.equals("getCardsOnTable")
                || command.equals("getEnvironmentCardsInHand")
                || command.equals("getCardAtPosition") || command.equals("getFrozenCardsOnTable")
                || command.equals("getPlayerOneWins") || command.equals("getPlayerTwoWins")
                || command.equals("getTotalGamesPlayed");
    }
}
