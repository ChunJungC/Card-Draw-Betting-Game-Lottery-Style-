package keno.logic;


import java.util.*;


public class DrawingEngine {
    private final Random rnd = new Random();


    public Set<Integer> draw20() {
        Set<Integer> s = new HashSet<>();
        while (s.size()<20) s.add(1 + rnd.nextInt(80));
        return s;
    }
}