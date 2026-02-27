package keno.logic;


import java.util.*;


public class StatsTracker {
    private int totalWins = 0;
    private final List<MatchResult> history = new ArrayList<>();


    public void record(MatchResult mr) { history.add(mr); totalWins += mr.win(); }
    public int totalWins() { return totalWins; }
    public List<MatchResult> history() { return Collections.unmodifiableList(history); }
}