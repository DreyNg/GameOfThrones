package thrones.game;

// Oh_Heaven.java

import ch.aplu.jcardgame.*;

import java.util.*;
import java.util.stream.Collectors;


@SuppressWarnings("serial")
public class GameOfThrones {




    static public int seed;
    static Random random;
    private DisplayManager dm;
    private ScoreManager sm;
    private Board board;


    public String canonical(GoTCard.Suit s) { return s.toString().substring(0, 1); }

    public String canonical(GoTCard.Rank r) {
        switch (r) {
            case ACE: case KING: case QUEEN: case JACK: case TEN:
                return r.toString().substring(0, 1);
            default:
                return String.valueOf(r.getRankValue());
        }
    }

    public String canonical(Card c) {
        return canonical((GoTCard.Rank) c.getRank()) + canonical((GoTCard.Suit) c.getSuit());
    }

    public String canonical(Hand h) {
        return "[" + h.getCardList().stream().map(this::canonical).collect(Collectors.joining(",")) + "]";
    }
    // return random Card from Hand
    public static Card randomCard(Hand hand) {
        assert !hand.isEmpty() : " random card from empty hand.";
        int x = random.nextInt(hand.getNumberOfCards());
        return hand.get(x);
    }

//    private void dealingOut(Hand[] hands, int nbPlayers, int nbCardsPerPlayer) {
//        Hand pack = deck.toHand(false);
//        assert pack.getNumberOfCards() == 52 : " Starting pack is not 52 cards.";
//        // Remove 4 Aces
//        List<Card> aceCards = pack.getCardsWithRank(GoTCard.Rank.ACE);
//        for (Card card : aceCards) {
//            card.removeFromHand(false);
//        }
//        assert pack.getNumberOfCards() == 48 : " Pack without aces is not 48 cards.";
//        // Give each player 3 heart cards
//        for (int i = 0; i < nbPlayers; i++) {
//            for (int j = 0; j < 3; j++) {
//                List<Card> heartCards = pack.getCardsWithSuit(GoTCard.Suit.HEARTS);
//                int x = random.nextInt(heartCards.size());
//                Card randomCard = heartCards.get(x);
//                randomCard.removeFromHand(false);
//                hands[i].insert(randomCard, false);
//            }
//        }
//        assert pack.getNumberOfCards() == 36 : " Pack without aces and hearts is not 36 cards.";
//        // Give each player 9 of the remaining cards
//        for (int i = 0; i < nbCardsPerPlayer; i++) {
//            for (int j = 0; j < nbPlayers; j++) {
//                assert !pack.isEmpty() : " Pack has prematurely run out of cards.";
//                Card dealt = randomCard(pack);
//                dealt.removeFromHand(false);
//                hands[j].insert(dealt, false);
//            }
//        }
//        for (int j = 0; j < nbPlayers; j++) {
//            assert hands[j].getNumberOfCards() == 12 : " Hand does not have twelve cards.";
//        }
//    }

    public final int nbPlayers = 4;
    public final int nbStartCards = 9;
	public final int nbPlays = 6;
	public final int nbRounds = 3;

    private Deck deck = new Deck(GoTCard.Suit.values(), GoTCard.Rank.values(), "cover");


    private final int watchingTime = 5000;
    private Hand[] hands;
    private Hand[] piles;
    private int nextStartingPlayer = random.nextInt(nbPlayers);


    private int[] scores = new int[nbPlayers];



    // boolean[] humanPlayers = { true, false, false, false};
    boolean[] humanPlayers = { false, false, false, false};


    private Optional<Card> selected;
    private final int NON_SELECTION_VALUE = -1;
    private int selectedPileIndex = NON_SELECTION_VALUE;
    private final int UNDEFINED_INDEX = -1;
    private final int ATTACK_RANK_INDEX = 0;
    private final int DEFENCE_RANK_INDEX = 1;



    private void pickACorrectSuit(int playerIndex, boolean isCharacter) {
        Hand currentHand = hands[playerIndex];
        List<Card> shortListCards = new ArrayList<>();
        for (int i = 0; i < currentHand.getCardList().size(); i++) {
            Card card = currentHand.getCardList().get(i);
            GoTCard.Suit suit = (GoTCard.Suit) card.getSuit();
            if (suit.isCharacter() == isCharacter) {
                shortListCards.add(card);
            }
        }
        if (shortListCards.isEmpty() || !isCharacter && random.nextInt(3) == 0) {
            selected = Optional.empty();
        } else {
            selected = Optional.of(shortListCards.get(random.nextInt(shortListCards.size())));
        }
    }

    private void selectRandomPile() {
        selectedPileIndex = random.nextInt(2);
    }

    private void waitForCorrectSuit(int playerIndex, boolean isCharacter) {
        if (hands[playerIndex].isEmpty()) {
            selected = Optional.empty();
        } else {
            selected = null;
            hands[playerIndex].setTouchEnabled(true);
            do {
                if (selected == null) {
                    dm.delay(100);
                    continue;
                }
                GoTCard.Suit suit = selected.isPresent() ? (GoTCard.Suit) selected.get().getSuit() : null;
                if (isCharacter && suit != null && suit.isCharacter() ||         // If we want character, can't pass and suit must be right
                        !isCharacter && (suit == null || !suit.isCharacter())) { // If we don't want character, can pass or suit must not be character
                    // if (suit != null && suit.isCharacter() == isCharacter) {
                    break;
                } else {
                    selected = null;
                    hands[playerIndex].setTouchEnabled(true);
                }
                dm.delay(100);
            } while (true);
        }
    }

    private void waitForPileSelection() {
        selectedPileIndex = NON_SELECTION_VALUE;
        for (Hand pile : piles) {
            pile.setTouchEnabled(true);
        }
        while(selectedPileIndex == NON_SELECTION_VALUE) {
            dm.delay(100);
        }
        for (Hand pile : piles) {
            pile.setTouchEnabled(false);
        }
    }





    private int getPlayerIndex(int index) {
        return index % nbPlayers;
    }

    void resetPile() {
        if (piles != null) {
            for (Hand pile : piles) {
                pile.removeAll(true);
            }
        }
        piles = new Hand[2];
        for (int i = 0; i < 2; i++) {
            piles[i] = new Hand(deck);

            dm.displayPile(piles[i],i);

            final Hand currentPile = piles[i];
            final int pileIndex = i;
            piles[i].addCardListener(new CardAdapter() {
                public void leftClicked(Card card) {
                    selectedPileIndex = pileIndex;
                    currentPile.setTouchEnabled(false);
                }
            });
        }

        sm.updatePileRanks(piles);
    }


    private void executeAPlay() {
        resetPile();

        nextStartingPlayer = getPlayerIndex(nextStartingPlayer);
        if (hands[nextStartingPlayer].getNumberOfCardsWithSuit(GoTCard.Suit.HEARTS) == 0)
            nextStartingPlayer = getPlayerIndex(nextStartingPlayer + 1);
        assert hands[nextStartingPlayer].getNumberOfCardsWithSuit(GoTCard.Suit.HEARTS) != 0 : " Starting player has no hearts.";

        // 1: play the first 2 hearts
        for (int i = 0; i < 2; i++) {
            int playerIndex = getPlayerIndex(nextStartingPlayer + i);
            dm.setStatusText("Player " + playerIndex + " select a Heart card to play");
            if (humanPlayers[playerIndex]) {
                waitForCorrectSuit(playerIndex, true);
            } else {
                pickACorrectSuit(playerIndex, true);
            }

            int pileIndex = playerIndex % 2;
            assert selected.isPresent() : " Pass returned on selection of character.";
            System.out.println("Player " + playerIndex + " plays " + canonical(selected.get()) + " on pile " + pileIndex);
            selected.get().setVerso(false);
            selected.get().transfer(piles[pileIndex], true); // transfer to pile (includes graphic effect)
            sm.updatePileRanks(piles);
        }

        // 2: play the remaining nbPlayers * nbRounds - 2
        int remainingTurns = nbPlayers * nbRounds - 2;
        int nextPlayer = nextStartingPlayer + 2;

        while(remainingTurns > 0) {
            nextPlayer = getPlayerIndex(nextPlayer);
            dm.setStatusText("Player" + nextPlayer + " select a non-Heart card to play.");
            if (humanPlayers[nextPlayer]) {
                waitForCorrectSuit(nextPlayer, false);
            } else {
                pickACorrectSuit(nextPlayer, false);
            }

            if (selected.isPresent()) {
                dm.setStatusText("Selected: " + canonical(selected.get()) + ". Player" + nextPlayer + " select a pile to play the card.");
                if (humanPlayers[nextPlayer]) {
                    waitForPileSelection();
                } else {
                    selectRandomPile();
                }
                System.out.println("Player " + nextPlayer + " plays " + canonical(selected.get()) + " on pile " + selectedPileIndex);
                selected.get().setVerso(false);
                selected.get().transfer(piles[selectedPileIndex], true); // transfer to pile (includes graphic effect)
                sm.updatePileRanks(piles);
            } else {
                dm.setStatusText("Pass.");
            }
            nextPlayer++;
            remainingTurns--;
        }

        // 3: calculate winning & update scores for players
        sm.checkWin(piles);


        // 5: discarded all cards on the piles
        nextStartingPlayer += 1;
        dm.delay(watchingTime);
    }

    public GameOfThrones() {

        dm =  DisplayManager.getInstance();
        sm = new ScoreManager(dm);
        board = new Board(nbStartCards, nbPlayers, deck, dm, selected);
        hands = board.setupGame();




        for (int i = 0; i < nbPlays; i++) {
            executeAPlay();
            sm.updateScores();
        }
        dm.printResult(scores);
        dm.refresh();
    }

    public static void main(String[] args) {
        // System.out.println("Working Directory = " + System.getProperty("user.dir"));
        // final Properties properties = new Properties();
        // properties.setProperty("watchingTime", "5000");
        /*
        if (args == null || args.length == 0) {
            //  properties = PropertiesLoader.loadPropertiesFile("cribbage.properties");
        } else {
            //  properties = PropertiesLoader.loadPropertiesFile(args[0]);
        }

        String seedProp = properties.getProperty("seed");  //Seed property
        if (seedProp != null) { // Use property seed
			  seed = Integer.parseInt(seedProp);
        } else { // and no property
			  seed = new Random().nextInt(); // so randomise
        }
        */
        GameOfThrones.seed = 130006;
        System.out.println("Seed = " + seed);
        GameOfThrones.random = new Random(seed);
        new GameOfThrones();
    }

}
