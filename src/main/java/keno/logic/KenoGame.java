package keno.logic;


import java.util.*;


public class KenoGame {
    private int spots; private int drawings; private int currentDraw = 0; private boolean running = false;


    public void configure(int spots, int drawings) {
        if (!(spots==1||spots==4||spots==8||spots==10)) throw new IllegalArgumentException("spots");
        if (drawings<1||drawings>4) throw new IllegalArgumentException("drawings");
        this.spots = spots; this.drawings = drawings; this.currentDraw = 0; this.running = false;
    }


    public void start() { running = true; currentDraw = 0; }


    public MatchResult computeResult(Set<Integer> picks, Set<Integer> drawn) {
        currentDraw++;
        Set<Integer> hits = new TreeSet<>(picks);
        hits.retainAll(drawn);
        int k = hits.size();
        int win = PayoutTable.payout(spots, k);
        if (currentDraw>=drawings) running = false;
        return new MatchResult(currentDraw, hits, k, win);
    }


    public boolean hasNext() { return running && currentDraw < drawings; }
    public boolean isRunning() { return running; }
    public int getCurrentDraw() { return currentDraw; }
    public int getDrawings() { return drawings; }
}