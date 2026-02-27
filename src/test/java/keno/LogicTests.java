package keno;


import keno.logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;


public class LogicTests {
    private BetCard card;
    private DrawingEngine engine;

    @BeforeEach
    void setup() {
        card = new BetCard();
        engine = new DrawingEngine();
    }

    @Test
    void bc_setValidSpots_1() { card.setSpots(1); assertEquals(1, card.getSpots()); }

    @Test
    void bc_setValidSpots_4() { card.setSpots(4); assertEquals(4, card.getSpots()); }

    @Test
    void bc_setValidSpots_8() { card.setSpots(8); assertEquals(8, card.getSpots()); }

    @Test
    void bc_setValidSpots_10() { card.setSpots(10); assertEquals(10, card.getSpots()); }

    @Test
    void bc_setSpots_clearsPicks() {
        card.setSpots(4);
        card.togglePick(1);
        card.togglePick(2);
        assertEquals(2, card.getPicks().size());
        card.setSpots(8);
        assertEquals(0, card.getPicks().size());
    }

    @Test
    void bc_togglePick_adds() {
        card.setSpots(4);
        assertTrue(card.togglePick(10));
        assertTrue(card.getPicks().contains(10));
    }

    @Test
    void bc_togglePick_removesIfAlreadyPicked() {
        card.setSpots(4);
        assertTrue(card.togglePick(7));
        assertFalse(card.togglePick(7)); // remove
        assertFalse(card.getPicks().contains(7));
    }

    @Test
    void bc_togglePick_refusesWhenFull() {
        card.setSpots(1);
        assertTrue(card.togglePick(1));
        assertFalse(card.togglePick(2));
        assertEquals(Set.of(1), card.getPicks());
    }

    @Test
    void bc_quickFill_completesToSpots() {
        card.setSpots(4);
        card.quickFill();
        assertEquals(4, card.getPicks().size());
        assertTrue(card.isComplete());
    }

    @Test
    void bc_quickFill_doesNothingIfSpotsZero() {
        card.quickFill();
        assertEquals(0, card.getPicks().size());
        assertFalse(card.isComplete());
    }

    @Test
    void bc_quickFill_numbersInRange() {
        card.setSpots(10);
        card.quickFill();
        assertTrue(card.getPicks().stream().allMatch(n -> n >= 1 && n <= 80));
    }

    @Test
    void bc_isComplete_falseInitially() {
        assertFalse(card.isComplete());
    }

    @Test
    void bc_isComplete_trueWhenAllPicked() {
        card.setSpots(4);
        for (int i = 1; i <= 4; i++) card.togglePick(i);
        assertTrue(card.isComplete());
    }

    @Test
    void bc_isComplete_falseWhenUnderfilled() {
        card.setSpots(4);
        card.togglePick(1);
        card.togglePick(2);
        assertFalse(card.isComplete());
    }

    @Test
    void bc_getPicks_unmodifiable() {
        card.setSpots(4);
        card.togglePick(5);
        Set<Integer> picks = card.getPicks();
        assertThrows(UnsupportedOperationException.class, () -> picks.add(99));
    }

    @Test
    void bc_clear_emptiesAndResetsSpots() {
        card.setSpots(4);
        card.togglePick(1);
        card.clear();
        assertEquals(0, card.getSpots());
        assertTrue(card.getPicks().isEmpty());
        assertFalse(card.isComplete());
    }

    @Test
    void bc_clear_thenReuse() {
        card.setSpots(4);
        card.quickFill();
        card.clear();
        assertEquals(0, card.getSpots());
        card.setSpots(1);
        card.quickFill();
        assertEquals(1, card.getPicks().size());
        assertTrue(card.isComplete());
    }

    @Test
    void bc_quickFill_uniqueNumbers() {
        card.setSpots(8);
        card.quickFill();
        assertEquals(card.getPicks().size(), new HashSet<>(card.getPicks()).size());
    }

    @Test
    void pt_asTextTable_containsHeadersAndSource() {
        String s = PayoutTable.asTextTable();
        assertTrue(s.contains("Official North Carolina Keno Payouts"));
        assertTrue(s.contains("Spot 1"));
        assertTrue(s.contains("Spot 4"));
        assertTrue(s.contains("Spot 8"));
        assertTrue(s.contains("Spot 10"));
        assertTrue(s.contains("Source: North Carolina Education Lottery"));
    }

    @Test
    void mr_accessors_basic() {
        Set<Integer> hits = new TreeSet<>(Set.of(2,5,9));
        MatchResult mr = new MatchResult(1, hits, 3, 12);
        assertEquals(1, mr.drawIndex());
        assertEquals(hits, mr.hits());
        assertEquals(3, mr.k());
        assertEquals(12, mr.win());
    }

    @Test
    void mr_hits_isNotNull() {
        MatchResult mr = new MatchResult(1, new TreeSet<>(), 0, 0);
        assertNotNull(mr.hits());
        assertEquals(0, mr.k());
        assertEquals(0, mr.win());
    }

    @Test
    void mr_emptyHitsKZeroWinZero() {
        MatchResult mr = new MatchResult(2, Set.of(), 0, 0);
        assertTrue(mr.hits().isEmpty());
        assertEquals(0, mr.k());
        assertEquals(0, mr.win());
    }

    @Test
    void mr_nonEmptyConsistency() {
        Set<Integer> h = Set.of(1,2);
        MatchResult mr = new MatchResult(3, h, 2, 5);
        assertEquals(h.size(), mr.k());
        assertTrue(mr.win() >= 0);
    }

    @Test
    void mr_orderIndependentHits() {
        MatchResult a = new MatchResult(1, new TreeSet<>(Set.of(2,3)), 2, 5);
        MatchResult b = new MatchResult(1, new TreeSet<>(Set.of(3,2)), 2, 5);
        assertEquals(a.hits(), b.hits());
    }

    @Test
    void mr_drawIndex_incrementsAcrossRounds() {
        MatchResult m1 = new MatchResult(1, Set.of(), 0, 0);
        MatchResult m2 = new MatchResult(2, Set.of(), 0, 0);
        assertTrue(m2.drawIndex() > m1.drawIndex());
    }

    @Test
    void kg_configure_validRanges() {
        KenoGame g = new KenoGame();
        g.configure(4, 2);
        assertEquals(2, g.getDrawings());
        assertEquals(0, g.getCurrentDraw());
        assertFalse(g.isRunning());
    }

    @Test
    void kg_start_setsRunningAndResetsIndex() {
        KenoGame g = new KenoGame();
        g.configure(4, 2);
        g.start();
        assertTrue(g.isRunning());
        assertEquals(0, g.getCurrentDraw());
    }

    @Test
    void kg_computeResult_incrementsDrawIndex() {
        KenoGame g = new KenoGame();
        g.configure(4, 3);
        g.start();
        MatchResult r1 = g.computeResult(Set.of(1,2,3,4), Set.of(1,20,30));
        assertEquals(1, r1.drawIndex());
        MatchResult r2 = g.computeResult(Set.of(1,2,3,4), Set.of(2,40,50));
        assertEquals(2, r2.drawIndex());
        assertTrue(g.hasNext());
    }

    @Test
    void kg_computeResult_setsRunningFalseOnLast() {
        KenoGame g = new KenoGame();
        g.configure(4, 2);
        g.start();
        g.computeResult(Set.of(1,2,3,4), Set.of(1,2));
        assertTrue(g.isRunning());
        g.computeResult(Set.of(1,2,3,4), Set.of(3,4));
        assertFalse(g.isRunning());
        assertFalse(g.hasNext());
    }

    @Test
    void kg_payoutIntegration_spot1_match1_pays2() {
        KenoGame g = new KenoGame();
        g.configure(1, 1);
        g.start();
        MatchResult r = g.computeResult(Set.of(7), Set.of(7, 10, 20));
        assertEquals(2, r.win());
        assertEquals(1, r.k());
    }

    @Test
    void kg_payoutIntegration_spot4_match4_pays75() {
        KenoGame g = new KenoGame();
        g.configure(4, 1);
        g.start();
        MatchResult r = g.computeResult(Set.of(1,2,3,4), Set.of(1,2,3,4,10,11,12));
        assertEquals(75, r.win());
        assertEquals(4, r.k());
    }

    @Test
    void kg_payoutIntegration_spot8_match7_pays500() {
        KenoGame g = new KenoGame();
        g.configure(8, 1);
        g.start();
        MatchResult r = g.computeResult(
                Set.of(1,2,3,4,5,6,7,8),
                Set.of(1,2,3,4,5,6,7,20,21,22,23,24,25,26,27,28,29,30,31,32));
        assertEquals(500, r.win());
        assertEquals(7, r.k());
    }

    @Test
    void kg_payoutIntegration_spot10_match10_pays100k() {
        KenoGame g = new KenoGame();
        g.configure(10, 1);
        g.start();
        MatchResult r = g.computeResult(
                Set.of(1,2,3,4,5,6,7,8,9,10),
                Set.of(1,2,3,4,5,6,7,8,9,10,30,31,32,33,34,35,36,37,38,39));
        assertEquals(100000, r.win());
        assertEquals(10, r.k());
    }

    @Test
    void kg_hasNext_trueWhileUnderDrawCount() {
        KenoGame g = new KenoGame();
        g.configure(4, 3);
        g.start();
        assertTrue(g.hasNext());
        g.computeResult(Set.of(1,2,3,4), Set.of(1));
        assertTrue(g.hasNext());
        g.computeResult(Set.of(1,2,3,4), Set.of(2));
        assertTrue(g.hasNext());
        g.computeResult(Set.of(1,2,3,4), Set.of(3));
        assertFalse(g.hasNext());
    }

    @Test
    void kg_computeResult_hitCountIsIntersectionSize() {
        KenoGame g = new KenoGame();
        g.configure(4, 1);
        g.start();
        MatchResult r = g.computeResult(Set.of(1,2,3,4), Set.of(2,3,99));
        assertEquals(2, r.k());
        assertEquals(Set.of(2,3), r.hits());
    }

    @Test
    void kg_computeResult_noHits_noPayout() {
        KenoGame g = new KenoGame();
        g.configure(8, 1);
        g.start();
        MatchResult r = g.computeResult(Set.of(1,2,3,4,5,6,7,8), Set.of(20,21,22,23));
        assertEquals(0, r.k());
        assertEquals(0, r.win());
        assertTrue(r.hits().isEmpty());
    }

    @Test
    void kg_computeResult_lastRound_noHits_stops() {
        KenoGame g = new KenoGame();
        g.configure(4, 1);
        g.start();
        g.computeResult(Set.of(1,2,3,4), Set.of(20,21,22));
        assertFalse(g.isRunning());
    }

    @Test
    void st_startsZero() {
        StatsTracker st = new StatsTracker();
        assertEquals(0, st.totalWins());
        assertTrue(st.history().isEmpty());
    }

    @Test
    void st_recordsSingleMatchResult() {
        StatsTracker st = new StatsTracker();
        st.record(new MatchResult(1, Set.of(1), 1, 2));
        assertEquals(2, st.totalWins());
        assertEquals(1, st.history().size());
    }

    @Test
    void st_accumulatesMultipleResults() {
        StatsTracker st = new StatsTracker();
        st.record(new MatchResult(1, Set.of(1), 1, 2));
        st.record(new MatchResult(2, Set.of(2,3), 2, 5));
        st.record(new MatchResult(3, Set.of(), 0, 0));
        assertEquals(7, st.totalWins());
        assertEquals(3, st.history().size());
    }

    @Test
    void st_history_isUnmodifiable() {
        StatsTracker st = new StatsTracker();
        st.record(new MatchResult(1, Set.of(1), 1, 2));
        List<MatchResult> h = st.history();
        assertThrows(UnsupportedOperationException.class, () -> h.add(new MatchResult(2, Set.of(2), 1, 2)));
    }

    @Test
    void st_history_orderPreserved() {
        StatsTracker st = new StatsTracker();
        MatchResult a = new MatchResult(1, Set.of(1), 1, 2);
        MatchResult b = new MatchResult(2, Set.of(2), 1, 2);
        st.record(a);
        st.record(b);
        assertEquals(a, st.history().get(0));
        assertEquals(b, st.history().get(1));
    }

    @Test
    void st_record_zeroWinStillRecorded() {
        StatsTracker st = new StatsTracker();
        st.record(new MatchResult(1, Set.of(), 0, 0));
        assertEquals(0, st.totalWins());
        assertEquals(1, st.history().size());
    }

    @Test
    void st_manyRecords_largeSum() {
        StatsTracker st = new StatsTracker();
        int total = 0;
        for (int i = 1; i <= 20; i++) {
            int w = i % 2 == 0 ? 5 : 0;
            total += w;
            st.record(new MatchResult(i, Set.of(), 0, w));
        }
        assertEquals(total, st.totalWins());
        assertEquals(20, st.history().size());
    }

    @Test
    void de_draw20_containsNoZeroOrOver80() {
        Set<Integer> draw = engine.draw20();
        assertFalse(draw.contains(0));
        assertFalse(draw.contains(81));
        assertTrue(draw.stream().allMatch(n -> n >= 1 && n <= 80));
    }

    @Test
    void de_draw20_sizeExactly20_evenAfterManyCalls() {
        for (int i = 0; i < 25; i++) {
            assertEquals(20, engine.draw20().size());
        }
    }

    @Test
    void de_draw20_uniqueWithinEachDraw() {
        for (int i = 0; i < 10; i++) {
            Set<Integer> d = engine.draw20();
            assertEquals(d.size(), new HashSet<>(d).size());
        }
    }

    @Test
    void de_multipleDraws_unionReasonable() {
        // Union after a few draws should be > 20 (not strict, but likely)
        Set<Integer> union = new HashSet<>();
        for (int i = 0; i < 5; i++) union.addAll(engine.draw20());
        assertTrue(union.size() >= 20); // very weak, deterministic
        assertTrue(union.stream().allMatch(n -> n >= 1 && n <= 80));
    }

    @Test void drawing_has_20_unique_in_range() {
        DrawingEngine e = new DrawingEngine();
        Set<Integer> s = e.draw20();
        assertEquals(20, s.size());
        assertTrue(s.stream().allMatch(v -> v>=1 && v<=80));
    }


    @Test void keno_result_intersection_and_payout() {
        KenoGame g = new KenoGame(); g.configure(4,1);
        Set<Integer> picks = Set.of(1,2,3,4);
        Set<Integer> drawn = Set.of(1,2,90,91,92,93,94,95,96,97,98,99,5,6,7,8,9,10,11,12);
        MatchResult mr = g.computeResult(picks, drawn);
        assertEquals(2, mr.k());
        assertTrue(mr.win() >= 0);
    }
}