package checkers.tournament;

import checkers.bot.Bot;
import checkers.bot.Evaluator;
import checkers.bot.GameRunner;
import checkers.model.Game;
import checkers.model.Outcome;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Круговой турнир ботов: каждая пара играет заданное число партий вдвоём, места чередуются
 * Победа — 2 очка, ничья — по 1; если партия упёрлась в лимит кругов, побеждает больший прогресс
 */
public final class Tournament {

    private final List<Bot> bots;
    private final int gamesPerPair;
    private final long seed;
    private final int maxRounds;


    public Tournament(List<Bot> bots, int gamesPerPair, long seed, int maxRounds) {
        this.bots = List.copyOf(bots);
        this.gamesPerPair = gamesPerPair;
        this.seed = seed;
        this.maxRounds = maxRounds;
    }

    public List<MatchResult> run() {
        Random rng = new Random(seed);
        List<MatchResult> results = new ArrayList<>();
        for (int i = 0; i < bots.size(); i++) {
            for (int j = i + 1; j < bots.size(); j++) {
                for (int g = 0; g < gamesPerPair; g++) {
                    List<Bot> seats = g % 2 == 0 ? List.of(bots.get(i), bots.get(j))
                            : List.of(bots.get(j), bots.get(i));
                    Game game = new Game(2, maxRounds);
                    Outcome outcome = GameRunner.play(game, seats, rng);
                    results.add(new MatchResult(seats.get(0).name(), seats.get(1).name(), outcome, game.round(),
                            Evaluator.progress(game, 0), Evaluator.progress(game, 1)));
                }
            }
        }
        return results;
    }

    //Считает очки по результатам партий
    public Standings<String> standings(List<MatchResult> results) {
        Standings<String> table = new Standings<>();
        bots.forEach(b -> table.add(b.name(), 0));
        for (MatchResult r : results) {
            if (r.outcome().reason() == Outcome.Reason.WIN) {
                boolean firstWon = r.outcome().winners().get(0) == 0;
                table.add(firstWon ? r.seat0() : r.seat1(), 2);
            } else if (r.outcome().reason() == Outcome.Reason.UNFINISHED && r.progress0() != r.progress1()) {
                // лимит кругов: побеждает тот, кто продвинулся дальше
                table.add(r.progress0() > r.progress1() ? r.seat0() : r.seat1(), 2);
            } else {
                table.add(r.seat0(), 1);
                table.add(r.seat1(), 1);
            }
        }
        return table;
    }
}
