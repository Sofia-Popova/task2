package checkers.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class BoardTest {

    private final Board board = Board.standard();

    private static Predicate<Hex> occ(Hex... cells) {
        Set<Hex> set = Set.of(cells);
        return set::contains;
    }

    @Test
    void starHas121Cells() {
        assertEquals(121, board.cells().size());
    }

    @Test
    void everyCornerHasTenCellsAndTheyDoNotOverlap() {
        for (Corner c : Corner.values()) {
            assertEquals(10, board.cornerCells(c).size());
            assertTrue(board.contains(c.apex()) && c.contains(c.apex()));
        }
        long distinct = Arrays.stream(Corner.values()).flatMap(c -> board.cornerCells(c).stream()).distinct().count();
        assertEquals(60, distinct);
    }

    @Test
    void oppositeCornerIsInvolution() {
        assertEquals(Corner.Q_NEG, Corner.Q_POS.opposite());
        for (Corner c : Corner.values()) {
            assertEquals(c, c.opposite().opposite());
        }
    }

    @Test
    void adjacencyIsSymmetric() {
        assertTrue(board.cells().stream()
                .allMatch(c -> board.neighbors(c).stream().allMatch(n -> board.neighbors(n).contains(c))));
    }

    @Test
    void degreesOfCenterAndApex() {
        assertEquals(6, board.neighbors(new Hex(0, 0)).size());
        assertEquals(2, board.neighbors(Corner.Q_POS.apex()).size());
    }

    @Test
    void unknownCellThrows() {
        assertThrows(IllegalArgumentException.class, () -> board.neighbors(new Hex(20, 20)));
    }

    @Test
    void noJumpsOnEmptyBoard() {
        assertTrue(board.reachableByJumps(new Hex(0, 0), occ()).isEmpty());
    }

    @Test
    void chainOfJumpsIsFound() {
        Set<Hex> reach = board.reachableByJumps(new Hex(0, 0), occ(new Hex(1, 0), new Hex(3, 0)));
        assertEquals(Set.of(new Hex(2, 0), new Hex(4, 0)), reach);
    }

    @Test
    void jumpPathListsLandingCells() {
        List<Hex> path = board.jumpPath(new Hex(0, 0), new Hex(4, 0), occ(new Hex(1, 0), new Hex(3, 0)));
        assertEquals(List.of(new Hex(0, 0), new Hex(2, 0), new Hex(4, 0)), path);
        assertTrue(board.jumpPath(new Hex(0, 0), new Hex(0, 2), occ(new Hex(1, 0))).isEmpty());
    }

    @Test
    void bfsTerminatesOnCyclesAndExcludesStart() {
        Hex start = new Hex(0, 0);
        Predicate<Hex> ring = c -> start.distanceTo(c) == 1 || start.distanceTo(c) == 3;
        Set<Hex> reach = board.reachableByJumps(start, ring);
        assertFalse(reach.contains(start));
        assertTrue(reach.size() >= 6);
    }

    @Test
    void cannotJumpOffTheBoard() {
        Hex apex = Corner.Q_POS.apex();
        Hex before = new Hex(apex.q() - 1, apex.r());
        assertTrue(board.reachableByJumps(before, occ(apex)).isEmpty());
    }
}
