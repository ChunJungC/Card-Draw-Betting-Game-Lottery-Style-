package keno.logic;


import java.util.*;


public class PayoutTable {
    // Official North Carolina Keno payouts (as of 2025)
    private static final Map<Integer, Map<Integer,Integer>> TABLE = new HashMap<>();
    static {
        // Spot 1
        TABLE.put(1, Map.of( 1, 2 ));
        // Spot 4
        TABLE.put(4, Map.of( 2, 1, 3, 5, 4, 75));
        // Spot 8
        TABLE.put(8, Map.of(4, 2, 5, 12, 6, 50, 7, 500, 8, 10000));
        // Spot 10
        TABLE.put(10, Map.of(5, 5, 6, 15, 7, 40, 8, 450, 9, 4250, 10, 100000));
    }


    public static int payout(int spots, int matches) {
        return TABLE.getOrDefault(spots, Map.of()).getOrDefault(matches, 0);
    }


    public static String asTextTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("Official North Carolina Keno Payouts\n\n");
        for (int s : List.of(1,4,8,10)) {
            sb.append("Spot ").append(s).append("\n");
            Map<Integer,Integer> m = TABLE.getOrDefault(s, Map.of());
            List<Integer> keys = new ArrayList<>(m.keySet());
            Collections.sort(keys);
            for (int k : keys)
                sb.append(String.format(" %d match%s → $%,d\n", k, (k==1?"":"es"), m.get(k)));
            sb.append("\n");
        }
        sb.append("Source: North Carolina Education Lottery (nclottery.com)");
        return sb.toString();
    }
}