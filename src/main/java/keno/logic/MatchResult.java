package keno.logic;


import java.util.Set;


public record MatchResult(int drawIndex, Set<Integer> hits, int k, int win) {}