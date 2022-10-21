package thrones.game;

import ch.aplu.jcardgame.Hand;

public class ScoreManager {

    public final int nbPlayers = 4;
    private int[] scores = new int[nbPlayers];
    private DisplayManager dm;
    private final String[] playerTeams = { "[Players 0 & 2]", "[Players 1 & 3]"};
    private final int ATTACK_RANK_INDEX = 0;
    private final int DEFENCE_RANK_INDEX = 1;

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
    }


    public int[] calculatePileRanks(int pileIndex, Hand[] piles) {
        Hand currentPile = piles[pileIndex];
        int i = currentPile.isEmpty() ? 0 : ((GoTCard.Rank) currentPile.get(0).getRank()).getRankValue();
        return new int[] { i, i };
    }


    public void updatePileRanks(Hand[] piles) {
        for (int j = 0; j <  piles.length; j++) {
            int[] ranks = calculatePileRanks(j, piles);
            dm.updatePileRankState(j, ranks[ATTACK_RANK_INDEX], ranks[DEFENCE_RANK_INDEX]);
        }
    }

    public void updateScores() {
        for (int i = 0; i < nbPlayers; i++) {
            dm.updateScore(i, scores);
        }
        System.out.println(playerTeams[0] + " score = " + scores[0] + "; " + playerTeams[1] + " score = " + scores[1]);
    }
}
