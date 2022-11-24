I began the project by creating a class named "Game" where I wrote all the fields necessary
to implement the game successfully.
playerOne(Two)Wins - saves the wins of each player
playerOne(Two)Mana - saves the mana of each player
playerOne(Two)Deck - saves a deep copy of each player's deck
playerOne(Two)Hand - each player's hand of cards
gameTable - the table where the card will be placed
turnNumber - an integer where I keep account of the turn's number so that I know where a round has ended
roundNumber - an integer needed in order to give the proper number of mana to the players
gameEnded - a boolean that represents whether a hero has died or not in order to end the game and increment totalGamesPlayed

    The constructor's role is to go through every game and start it by setting every field to its 
default value, making deep copies of the two decks and then shuffling them, adding the first card 
in their hands and initiating the game table.
    I also created a class called "CommandsImplementation" where I implemented plenty of useful
methods that would help me in writing a cleaner and shorter code. This class also contains the
methods that write decks, cards, coordinates or the game table in the output file.
    After starting the game, I use a for to go through every command given in the input.
    I created a separate class for each command in order to write code more efficiently.
-GetPlayerDeck = writes a player's deck according to the turn
-GetPlayerHero = writes a player's hero according to the turn
-GetPlayerTurn = writes a player's turn
-EndPlayerTurn = changes the turn to the other player and checks if the round has ended
-placeCard = places a card on table according to the turn and checks the corner cases
-getCardsInHand = writes a player's hand according to the turn
-getPlayerMana = writes a player's mana according to the turn
-getCardsOnTable = writes the cards on the table
-getEnvironmentCardsInHand = writes the player's environment cards according to the turn
-useEnvironmentCard = uses the player's environment card on the given row and checks all the corner cases
-getCardAtPosition = writes the card situated at the given coordinates on the table
-getFrozenCardsOnTable = writes all the frozen cards on the table
-cardUsesAttack = the attacker card decreases the health of the attacked card by its attackDamage and checks all the corner cases
-cardUsesAbility = the attacker card uses its ability on the attacked card according to the card's name, checking all the corner cases
-useAttackHero = a minion card attacks the enemy's hero according to the player's turn, checking all the corner cases
-useHeroAbility = the player's hero uses its special ability on a specified row, enemy or ally, depending on the hero, checking all the corner cases
-getPlayerOneWins = writes the wins of player One
-getPlayerTwoWins = writes the wins of player Two
-getTotalGamesPlayed = writes the number of games played
