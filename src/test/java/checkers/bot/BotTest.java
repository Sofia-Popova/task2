package checkers.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import checkers.model.Game;
import checkers.model.Hex;
import checkers.model.Move;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BotTest {

    @Test
    void greedyPrefersLongestJump() {
        Game g = Game.withPieces(2, 10, Map.of(new Hex(0, 0), 0, new Hex(-1, 0), 1));
        Move m = Bots.create("greedy").choose(g, new Random(1));
        assertEquals(new Move(new Hex(0, 0), new Hex(-2, 0)), m);
        assertEquals(2, Evaluator.gain(g, m));
    }

    @Test
    void botChoicesAreAlwaysLegal() {
        Game g = new Game(2);
        Random rng = new Random(99);
        for (String name : Bots.names()) {
            assertTrue(g.legalMoves().contains(Bots.create(name).choose(g, rng)));
        }
    }

    @Test
    void sameSeedGivesSameChoice() {
        Game g = new Game(2);
        assertEquals(Bots.create("random").choose(g, new Random(5)), Bots.create("random").choose(g, new Random(5)));
    }

    @Test
    void progressGrowsAfterForwardMove() {
        Game g = new Game(2);
        int before = Evaluator.progress(g, 0);
        g.apply(Bots.create("greedy").choose(g, new Random(2)));
        assertTrue(Evaluator.progress(g, 0) > before);
    }

    @Test
    void unknownBotNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> Bots.create("nope"));
    }

    @Test
    void botWithoutMovesThrows() {
        Game g = Game.withPieces(2, 10, Map.of(new Hex(1, 1), 1));
        assertThrows(IllegalStateException.class, () -> Bots.create("random").choose(g, new Random(1)));
        assertEquals(List.of(), g.legalMoves());
    }
}
