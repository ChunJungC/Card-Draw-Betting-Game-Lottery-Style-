package keno.logic;


import java.util.*;


public class BetCard {
    private int spots = 0;
    private final Set<Integer> picks = new TreeSet<>();
    private final Random rnd = new Random();


    public void setSpots(int s) {
        if (!(s==1||s==4||s==8||s==10)) throw new IllegalArgumentException("Spots must be 1,4,8,10");
        spots = s; picks.clear();
    }


    public boolean togglePick(int n) {
        if (n<1||n>80) throw new IllegalArgumentException("n");
        if (picks.contains(n)) { picks.remove(n); return false; }
        if (picks.size()>=spots) return false;
        picks.add(n); return true;
    }


    public void quickFill() {
        if (spots==0) return;
        while (picks.size()<spots) {
            picks.add(1 + rnd.nextInt(80));
        }
    }


    public boolean isComplete() { return spots>0 && picks.size()==spots; }
    public int getSpots() { return spots; }
    public Set<Integer> getPicks() { return Collections.unmodifiableSet(picks); }
    public void clear() { picks.clear(); spots = 0; }
}