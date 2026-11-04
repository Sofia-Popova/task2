package checkers.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import checkers.bot.Bots;
import checkers.bot.GameRunner;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GameTest {

    private static Map<Hex, Integer> oneStepFromWin() {
        Map<Hex, Integer> m = new HashMap<>();
        Board b = Board.standard();
        for (Hex c : b.cornerCells(Corner.Q_NEG)) {
            if (!c.equals(new Hex(-5, 1))) {
                m.put(c, 0);
            }
        }
        m.put(new Hex(-4, 1), 0);
        return m;
    }

    @Test
    void playerCountOutsideRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Game(1));
        assertThrows(IllegalArgumentException.class, () -> new Game(7));
        assertThrows(IllegalArgumentException.class, () -> new Game(2, 0));
    }

    @Test
    void everyPlayerStartsWithTenPiecesInHomeCorner() {
        for (int n = 2; n <= 6; n++) {
            Game g = new Game(n);
            for (int p = 0; p < n; p++) {
                assertEquals(10, g.piecesOf(p).size());
                assertTrue(g.piecesOf(p).stream().allMatch(g.homeOf(p)::contains));
            }
        }
    }

    @Test
    void targetIsOppositeCornerAndNobodyStartsFinished() {
        Game g = new Game(2);
        assertEquals(g.homeOf(0).opposite(), g.targetOf(0));
        assertFalse(g.isInTarget(0));
        assertFalse(g.isOver());
    }

    @Test
    void initialLegalMovesAreSortedAndDistinct() {
        List<Move> moves = new Game(2).legalMoves();
        assertFalse(moves.isEmpty());
        assertEquals(moves.stream().sorted().distinct().toList(), moves);
    }

    @Test
    void movingForeignPieceThrows() {
        Game g = new Game(2);
        Hex foreign = g.piecesOf(1).get(0);
        Hex to = g.board().neighbors(foreign).get(0);
        assertThrows(IllegalMoveException.class, () -> g.apply(new Move(foreign, to)));
    }

    @Test
    void movingToOccupiedCellThrows() {
        Game g = new Game(2);
        Hex apex = g.homeOf(0).apex();
        assertThrows(IllegalMoveException.class, () -> g.apply(new Move(apex, new Hex(apex.q() - 1, apex.r()))));
    }

    @Test
    void movingToFarCellThrowsAndStateIsUnchanged() {
        Game g = new Game(2);
        Hex apex = g.homeOf(0).apex();
        assertThrows(IllegalMoveException.class, () -> g.apply(new Move(apex, new Hex(0, 0))));
        assertEquals(0, g.currentPlayer());
        assertTrue(g.history().isEmpty());
    }

    @Test
    void jumpOverForeignPieceIsAllowed() {
        Game g = Game.withPieces(2, 10, Map.of(new Hex(0, 0), 0, new Hex(1, 0), 1));
        assertTrue(g.legalMovesFrom(new Hex(0, 0)).contains(new Move(new Hex(0, 0), new Hex(2, 0))));
        assertTrue(new Move(new Hex(0, 0), new Hex(2, 0)).isJump());
    }

    @Test
    void jumpOverOwnPieceAllowedButNotOverEmptyCell() {
        Game g = Game.withPieces(2, 10, Map.of(new Hex(0, 0), 0, new Hex(1, 0), 0, new Hex(0, -3), 1));
        assertTrue(g.legalMovesFrom(new Hex(0, 0)).contains(new Move(new Hex(0, 0), new Hex(2, 0))));
        assertFalse(g.legalMovesFrom(new Hex(0, 0)).contains(new Move(new Hex(0, 0), new Hex(0, 2))));
    }

    @Test
    void stepAndJumpCannotBeCombined() {
        Game g = Game.withPieces(2, 10, Map.of(new Hex(0, 0), 0, new Hex(2, 0), 0, new Hex(3, 0), 1, new Hex(-4, 4), 1));
        // шаг на (1,0), потом прыжок через (2,0) на (3,0) нельзя; (3,0) занято, и цели (4,0) за один ход нет
        assertThrows(IllegalMoveException.class, () -> g.apply(new Move(new Hex(0, 0), new Hex(4, 0))));
    }

    @Test
    void multiJumpMovesPieceAlongChain() {
        Game g = Game.withPieces(2, 10, Map.of(new Hex(0, 0), 0, new Hex(1, 0), 1, new Hex(3, 0), 1));
        List<GameEvent> ev = g.apply(new Move(new Hex(0, 0), new Hex(4, 0)));
        assertEquals(Integer.valueOf(0), g.ownerAt(new Hex(4, 0)).orElseThrow());
        assertTrue(g.ownerAt(new Hex(0, 0)).isEmpty());
        assertTrue(ev.get(0) instanceof GameEvent.MoveMade);
    }

    @Test
    void turnPassesToNextPlayer() {
        Game g = new Game(3);
        g.apply(g.legalMoves().get(0));
        assertEquals(1, g.currentPlayer());
        assertEquals(1, g.moveCount(0));
    }

    @Test
    void winnerIsDeclaredAtEndOfRound() {
        Map<Hex, Integer> m = oneStepFromWin();
        m.put(new Hex(0, 0), 1);
        Game g = Game.withPieces(2, 50, m);
        List<GameEvent> first = g.apply(new Move(new Hex(-4, 1), new Hex(-5, 1)));
        assertTrue(first.stream().anyMatch(e -> e instanceof GameEvent.PlayerFinished));
        assertFalse(g.isOver());
        g.apply(new Move(new Hex(0, 0), new Hex(1, 0)));
        assertTrue(g.isOver());
        assertEquals(Outcome.Reason.WIN, g.outcome().orElseThrow().reason());
        assertEquals(List.of(0), g.outcome().orElseThrow().winners());
    }

    @Test
    void equalMoveCountsGiveDraw() {
        Map<Hex, Integer> m = oneStepFromWin();
        for (Hex c : Board.standard().cornerCells(Corner.Q_POS)) {
            if (!c.equals(new Hex(5, -1))) {
                m.put(c, 1);
            }
        }
        m.put(new Hex(4, -1), 1);
        Game g = Game.withPieces(2, 50, m);
        g.apply(new Move(new Hex(-4, 1), new Hex(-5, 1)));
        g.apply(new Move(new Hex(4, -1), new Hex(5, -1)));
        Outcome o = g.outcome().orElseThrow();
        assertEquals(Outcome.Reason.DRAW, o.reason());
        assertEquals(List.of(0, 1), o.winners());
    }

    @Test
    void moveAfterGameOverThrowsIllegalState() {
        Map<Hex, Integer> m = oneStepFromWin();
        m.put(new Hex(0, 0), 1);
        Game g = Game.withPieces(2, 50, m);
        g.apply(new Move(new Hex(-4, 1), new Hex(-5, 1)));
        g.apply(new Move(new Hex(0, 0), new Hex(1, 0)));
        assertThrows(IllegalStateException.class, () -> g.apply(new Move(new Hex(1, 0), new Hex(2, 0))));
        assertTrue(g.legalMoves().isEmpty());
    }

    @Test
    void roundLimitEndsGameUnfinished() {
        Game g = new Game(2, 1);
        g.apply(g.legalMoves().get(0));
        g.apply(g.legalMoves().get(0));
        assertEquals(Outcome.Reason.UNFINISHED, g.outcome().orElseThrow().reason());
    }

    @Test
    void blockedPlayerPasses() {
        // у игрока 1 нет шашек, значит ходов нет: он пропускает, ход возвращается игроку 0
        Game g = Game.withPieces(2, 50, Map.of(new Hex(0, 0), 0));
        List<GameEvent> ev = g.apply(new Move(new Hex(0, 0), new Hex(1, 0)));
        assertTrue(ev.stream().anyMatch(e -> e instanceof GameEvent.Passed p && p.player() == 1));
        assertEquals(0, g.currentPlayer());
    }

    @Test
    void listenersReceiveEvents() {
        Game g = new Game(2);
        List<GameEvent> seen = new ArrayList<>();
        g.subscribe(seen::add);
        g.apply(g.legalMoves().get(0));
        assertEquals(1, seen.size());
    }

    @Test
    void replayFromHistoryGivesSamePosition() {
        Game g = new Game(3);
        Random rng = new Random(7);
        for (int i = 0; i < 60; i++) {
            g.apply(Bots.create("random").choose(g, rng));
        }
        Game copy = Game.fromHistory(3, Game.DEFAULT_MAX_ROUNDS, g.history());
        for (int p = 0; p < 3; p++) {
            assertEquals(g.piecesOf(p), copy.piecesOf(p));
        }
        assertEquals(g.currentPlayer(), copy.currentPlayer());
    }

    @Test
    void sixPlayerGameRunsWithoutErrors() {
        Game g = new Game(6);
        Random rng = new Random(1);
        for (int i = 0; i < 120; i++) {
            g.apply(Bots.create("balanced").choose(g, rng));
        }
        for (int p = 0; p < 6; p++) {
            assertEquals(10, g.piecesOf(p).size());
        }
    }

    @Test
    void fullGameFromFixedSeedIsReproducibleAndSilent() {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        List<Move> first;
        List<Move> second;
        try {
            first = playGreedy(12345);
            second = playGreedy(12345);
        } finally {
            System.setOut(original);
        }
        assertEquals(0, captured.size(), "механика не должна ничего печатать");
        assertEquals(first, second);
        assertFalse(first.isEmpty());
    }

    private static List<Move> playGreedy(long seed) {
        Game g = new Game(2);
        GameRunner.play(g, List.of(Bots.create("greedy"), Bots.create("balanced")), new Random(seed));
        assertTrue(g.isOver());
        return g.history();
    }
}
