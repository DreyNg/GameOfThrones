package thrones.game;

public class ScoreManager {

    public final int nbPlayers = 4;
    private int[] scores = new int[nbPlayers];
    private DisplayManager dm;
    private final String[] playerTeams = { "[Players 0 & 2]", "[Players 1 & 3]"};

    public ScoreManager(DisplayManager dm){
        this.dm = dm;
        this.initScore();
    }


    private void initScore() {
        for (int i = 0; i < nbPlayers; i++) {
            scores[i] = 0;
            dm.printScore(i);
        }
        dm.printPileText();

//        String text = "Attack: 0 - Defence: 0";
//        for (int i = 0; i < pileTextActors.length; i++) {
//            pileTextActors[i] = new TextActor(text, Color.WHITE, bgColor, smallFont);
//            addActor(pileTextActors[i], pileStatusLocations[i]);
//        }
    }


    public void updateScores() {
        for (int i = 0; i < nbPlayers; i++) {
            dm.updateScore(i, scores);
        }
        System.out.println(playerTeams[0] + " score = " + scores[0] + "; " + playerTeams[1] + " score = " + scores[1]);
    }
}
