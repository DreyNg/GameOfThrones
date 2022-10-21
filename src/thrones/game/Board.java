package thrones.game;

import ch.aplu.jcardgame.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static thrones.game.GameOfThrones.random;
import static thrones.game.GameOfThrones.randomCard;

public class Board {

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

    private Hand[] hands;
    private final int nbPlayers;
    private Deck deck;
    public final int nbStartCards = 9;
    public final int nbPlays = 6;
    public final int nbRounds = 3;
    private DisplayManager dm;
    private Optional<Card> selected;




    public Board(int nbStartCards, int nbPlayers, Deck deck, DisplayManager dm, Optional<Card> selected){
        this.nbPlayers = nbPlayers;
        this.deck = deck;
        this.dm = dm;
        this.selected = selected;
        this.setupGame();
    }




    public Hand[] setupGame() {
        hands = new Hand[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            hands[i] = new Hand(deck);
        }
        dealingOut(hands, nbPlayers, nbStartCards);

        for (int i = 0; i < nbPlayers; i++) {
            hands[i].sort(Hand.SortType.SUITPRIORITY, true);
            System.out.println("hands[" + i + "]: " + canonical(hands[i]));
        }

        for (final Hand currentHand : hands) {
            // Set up human player for interaction
            currentHand.addCardListener(new CardAdapter() {
                public void leftDoubleClicked(Card card) {
                    selected = Optional.of(card);
                    currentHand.setTouchEnabled(false);
                }
                public void rightClicked(Card card) {
                    selected = Optional.empty(); // Don't care which card we right-clicked for player to pass
                    currentHand.setTouchEnabled(false);
                }
            });
        }
        // graphics
        RowLayout[] layouts = new RowLayout[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            dm.displayGraphic(layouts, hands,i);
        }
        // End graphics
        return hands;
    }

    private void dealingOut(Hand[] hands, int nbPlayers, int nbCardsPerPlayer) {
        Hand pack = deck.toHand(false);
        assert pack.getNumberOfCards() == 52 : " Starting pack is not 52 cards.";
        // Remove 4 Aces
        List<Card> aceCards = pack.getCardsWithRank(GoTCard.Rank.ACE);
        for (Card card : aceCards) {
            card.removeFromHand(false);
        }
        assert pack.getNumberOfCards() == 48 : " Pack without aces is not 48 cards.";
        // Give each player 3 heart cards
        for (int i = 0; i < nbPlayers; i++) {
            for (int j = 0; j < 3; j++) {
                List<Card> heartCards = pack.getCardsWithSuit(GoTCard.Suit.HEARTS);
                int x = random.nextInt(heartCards.size());
                Card randomCard = heartCards.get(x);
                randomCard.removeFromHand(false);
                hands[i].insert(randomCard, false);
            }
        }
        assert pack.getNumberOfCards() == 36 : " Pack without aces and hearts is not 36 cards.";
        // Give each player 9 of the remaining cards
        for (int i = 0; i < nbCardsPerPlayer; i++) {
            for (int j = 0; j < nbPlayers; j++) {
                assert !pack.isEmpty() : " Pack has prematurely run out of cards.";
                Card dealt = randomCard(pack);
                dealt.removeFromHand(false);
                hands[j].insert(dealt, false);
            }
        }
        for (int j = 0; j < nbPlayers; j++) {
            assert hands[j].getNumberOfCards() == 12 : " Hand does not have twelve cards.";
        }
    }
}
