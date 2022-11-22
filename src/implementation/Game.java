package implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.ActionsInput;
import fileio.CardInput;
import fileio.Input;
import fileio.StartGameInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public final class Game {
    private int playerOneWins;
    private int playerTwoWins;
    private int playerOneMana;
    private int playerTwoMana;
    private int turnNumber;
    private int roundNumber;
    private boolean gameEnded;
    private ArrayList<CardInput> playerOneDeck;
    private ArrayList<CardInput> playerTwoDeck;
    private ArrayList<CardInput> playerOneHand;
    private ArrayList<CardInput> playerTwoHand;
    private ArrayList<ArrayList<CardInput>> gameTable;

    public ArrayList<ArrayList<CardInput>> getGameTable() {
        return gameTable;
    }

    public void setGameTable(final ArrayList<ArrayList<CardInput>> gameTable) {
        this.gameTable = gameTable;
    }

    public ArrayList<CardInput> getPlayerOneHand() {
        return playerOneHand;
    }

    public void setPlayerOneHand(final ArrayList<CardInput> playerOneHand) {
        this.playerOneHand = playerOneHand;
    }

    public ArrayList<CardInput> getPlayerTwoHand() {
        return playerTwoHand;
    }

    public void setPlayerTwoHand(final ArrayList<CardInput> playerTwoHand) {
        this.playerTwoHand = playerTwoHand;
    }

    public ArrayList<CardInput> getPlayerOneDeck() {
        return playerOneDeck;
    }

    public void setPlayerOneDeck(final ArrayList<CardInput> playerOneDeck) {
        this.playerOneDeck = playerOneDeck;
    }

    public ArrayList<CardInput> getPlayerTwoDeck() {
        return playerTwoDeck;
    }

    public void setPlayerTwoDeck(final ArrayList<CardInput> playerTwoDeck) {
        this.playerTwoDeck = playerTwoDeck;
    }

    public int getPlayerOneWins() {
        return playerOneWins;
    }

    public void setPlayerOneWins(final int playerOneWins) {
        this.playerOneWins = playerOneWins;
    }

    public int getPlayerTwoWins() {
        return playerTwoWins;
    }

    public void setPlayerTwoWins(final int playerTwoWins) {
        this.playerTwoWins = playerTwoWins;
    }

    public int getPlayerOneMana() {
        return playerOneMana;
    }

    public void setPlayerOneMana(final int playerOneMana) {
        this.playerOneMana = playerOneMana;
    }

    public int getPlayerTwoMana() {
        return playerTwoMana;
    }

    public void setPlayerTwoMana(final int playerTwoMana) {
        this.playerTwoMana = playerTwoMana;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(final int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(final int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void setGameEnded(final boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    public Game(final Input input, final ArrayNode output) {
        playerOneWins = 0;
        playerTwoWins = 0;
        for (int i = 0; i < input.getGames().size(); i++) {
            ObjectMapper mapper = new ObjectMapper();
            CommandsImplementation commands = new CommandsImplementation();
            StartGameInput startGame = input.getGames().get(i).getStartGame();
            playerOneMana = 1;
            playerTwoMana = 1;
            turnNumber = 0;
            roundNumber = 1;
            gameEnded = false;
            int deckIndex1 = input.getGames().get(i).getStartGame().getPlayerOneDeckIdx();
            int deckIndex2 = input.getGames().get(i).getStartGame().getPlayerTwoDeckIdx();
            int shuffleSeed = input.getGames().get(i).getStartGame().getShuffleSeed();
            playerOneDeck = new ArrayList<>();
            playerTwoDeck = new ArrayList<>();

            for (int j = 0; j < input.getPlayerOneDecks().getNrCardsInDeck(); j++) {
                playerOneDeck.add(new CardInput(input.getPlayerOneDecks().getDecks()
                        .get(deckIndex1).get(j)));
            }

            for (int j = 0; j < input.getPlayerTwoDecks().getNrCardsInDeck(); j++) {
                playerTwoDeck.add(new CardInput(input.getPlayerTwoDecks().getDecks()
                        .get(deckIndex2).get(j)));
            }
            Collections.shuffle(playerOneDeck, new Random(shuffleSeed));
            Collections.shuffle(playerTwoDeck, new Random(shuffleSeed));

            playerOneHand = new ArrayList<>();
            playerTwoHand = new ArrayList<>();

            playerOneHand.add(playerOneDeck.get(0));
            playerOneDeck.remove(0);

            playerTwoHand.add(playerTwoDeck.get(0));
            playerTwoDeck.remove(0);

            gameTable = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                gameTable.add(new ArrayList<>());
            }

            startGame.getPlayerOneHero().setHealth(30);
            startGame.getPlayerTwoHero().setHealth(30);

            for (int j = 0; j < input.getGames().get(i).getActions().size(); j++) {
                ActionsInput action = input.getGames().get(i).getActions().get(j);
                if (this.isGameEnded() && !commands.isGetCommand(action.getCommand())) {
                    break;
                }
                switch (action.getCommand()) {
                    case "getPlayerDeck" -> {
                        new GetPlayerDeck(this, action, output, commands, mapper);
                    }

                    case "getPlayerHero" -> {
                        new GetPlayerHero(startGame, action, output, commands, mapper);
                    }

                    case "getPlayerTurn" -> {
                        new GetPlayerTurn(action, output, startGame);
                    }

                    case "endPlayerTurn" -> {
                        new EndPlayerTurn(this, startGame, commands);
                    }

                    case "placeCard" -> {
                        new PlaceCard(this, startGame, action, commands, output);
                    }

                    case "getCardsInHand" -> {
                        new GetCardsInHand(this, action, commands, output, mapper);
                    }

                    case "getPlayerMana" -> {
                        new GetPlayerMana(this, action, output);
                    }

                    case "getCardsOnTable" -> {
                        new GetCardsOnTable(this, action, output, commands, mapper);

                    }

                    case "getEnvironmentCardsInHand" -> {
                        new GetEnvironmentCardsInHand(this, action, output, commands, mapper);

                    }

                    case "useEnvironmentCard" -> {
                        new UseEnvironmentCard(this, startGame, action, output, commands);

                    }

                    case "getCardAtPosition" -> {
                        new GetCardAtPosition(this, action, output, commands, mapper);
                    }

                    case "getFrozenCardsOnTable" -> {
                        new GetFrozenCardsOnTable(this, action, output, commands, mapper);
                    }

                    case "cardUsesAttack" -> {
                        new CardUsesAttack(this, startGame, action, output, commands, mapper);
                    }

                    case "cardUsesAbility" -> {
                        new CardUsesAbility(this, startGame, action, output, commands,
                                mapper);
                    }

                    case "useAttackHero" -> {
                        new UseAttackHero(this, startGame, action, output, commands, mapper);
                    }

                    case "useHeroAbility" -> {
                        new UseHeroAbility(this, startGame, action, output, commands);

                    }

                    case "getPlayerOneWins" -> {
                        new GetPlayerOneWins(this, action, output);
                    }

                    case "getPlayerTwoWins" -> {
                        new GetPlayerTwoWins(this, action, output);

                    }

                    case "getTotalGamesPlayed" -> {
                        new GetTotalGamesPlayed(this, action, output);
                    }
                }
            }
        }
    }
}
