package checkers.tournament;

import static org.junit.jupiter.api.Assertions.assertEquals;

import checkers.bot.Bots;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TournamentTest {

    private static Tournament make(long seed) {
        return new Tournament(Bots.names().stream().map(Bots::create).toList(), 2, seed, 150);
    }

    @Test
    void everyGameAwardsTwoPointsInTotal() {
        Tournament t = make(3);
        List<MatchResult> results = t.run();
        assertEquals(6, results.size());
        assertEquals(12, t.standings(results).total());
    }

    @Test
    void sameSeedGivesSameTable() {
        Tournament a = make(11);
        Tournament b = make(11);
        assertEquals(a.run(), b.run());
    }

    @Test
    void standingsRankByPointsThenComparator() {
        Standings<String> s = new Standings<>();
        s.add("b", 3);
        s.add("a", 3);
        s.add("c", 5);
        List<Map.Entry<String, Integer>> r = s.ranking(Comparator.naturalOrder());
        assertEquals(List.of("c", "a", "b"), r.stream().map(Map.Entry::getKey).toList());
        assertEquals(11, s.total());
        assertEquals(0, s.points("zzz"));
    }
}
