package thrones.game;

import ch.aplu.jcardgame.CardGame;
import ch.aplu.jcardgame.Deck;
import ch.aplu.jcardgame.Hand;
import ch.aplu.jcardgame.RowLayout;
import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import ch.aplu.jgamegrid.TextActor;

import java.awt.*;



public class DisplayManager extends CardGame {
    private static DisplayManager instance = new DisplayManager();
    private final String version = "1.0";


    private DisplayManager(){
        super(700, 700, 30);
        setTitle("Game of Thrones (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");
    }
    public static DisplayManager getInstance(){
        return instance;
    }

    private Actor[] pileTextActors = { null, null };
    private Actor[] scoreActors = {null, null, null, null};
    Font bigFont = new Font("Arial", Font.BOLD, 36);
    Font smallFont = new Font("Arial", Font.PLAIN, 10);
    private final int handWidth = 400;
    private final int pileWidth = 40;
    private final String[] playerTeams = { "[Players 0 & 2]", "[Players 1 & 3]"};




    private final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };

    private final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(25, 25),
            new Location(575, 125)
    };
    private final Location[] pileLocations = {
            new Location(350, 280),
            new Location(350, 430)
    };
    private final Location[] pileStatusLocations = {
            new Location(250, 200),
            new Location(250, 520)
    };




//    public void setStatusText(String s){
//        setStatusText(s);
//    }


    public void printResult(int[] scores){
        String text;
        if (scores[0] > scores[1]) {
            text = "Players 0 and 2 won.";
        } else if (scores[0] == scores[1]) {
            text = "All players drew.";
        } else {
            text = "Players 1 and 3 won.";
        }
        System.out.println("Result: " + text);
        this.setStatusText(text);
    }

    public void printScore(int i){
        String text = "P" + i + "-0";
        scoreActors[i] = new TextActor(text, Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[i], scoreLocations[i]);
    }

    public void printPileText(){
        String text = "Attack: 0 - Defence: 0";
        for (int i = 0; i < pileTextActors.length; i++) {
            pileTextActors[i] = new TextActor(text, Color.WHITE, bgColor, smallFont);
            addActor(pileTextActors[i], pileStatusLocations[i]);
        }
    }

    public void updateScore(int player, int[] scores) {
        removeActor(scoreActors[player]);
        String text = "P" + player + "-" + scores[player];
        scoreActors[player] = new TextActor(text, Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[player], scoreLocations[player]);
    }

    public void displayGraphic(RowLayout[] layouts, Hand[] hands,int i){
        layouts[i] = new RowLayout(handLocations[i], handWidth);
        layouts[i].setRotationAngle(90 * i);
        hands[i].setView(this, layouts[i]);
        hands[i].draw();
    }

    public void displayPile(Hand pile,int i){
        pile.setView(this, new RowLayout(pileLocations[i], 8 * pileWidth));
        pile.draw();
    }

    public void updatePileRankState(int pileIndex, int attackRank, int defenceRank) {


        TextActor currentPile = (TextActor) pileTextActors[pileIndex];
        removeActor(currentPile);
        String text = playerTeams[pileIndex] + " Attack: " + attackRank + " - Defence: " + defenceRank;
        pileTextActors[pileIndex] = new TextActor(text, Color.WHITE, bgColor, smallFont);
        addActor(pileTextActors[pileIndex], pileStatusLocations[pileIndex]);
    }


}
