package checkers.bot;

import checkers.model.Game;
import checkers.model.Outcome;
import java.util.List;
import java.util.Random;

/** Прогон партии между ботами */
public final class GameRunner {

    private GameRunner() { }

    public static Outcome play(Game game, List<Bot> bots, Random rng) {
        while (!game.isOver()) {
            game.apply(bots.get(game.currentPlayer()).choose(game, rng));
        }
        return game.outcome().orElseThrow();
    }
}
