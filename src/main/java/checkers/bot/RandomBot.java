package checkers.bot;

import checkers.model.Game;
import checkers.model.Move;
import java.util.List;
import java.util.Random;

/** Бот, выбирающий случайный допустимый ход */
public final class RandomBot implements Bot {

    @Override
    public String name() {
        return "random";
    }

    @Override
    public Move choose(Game game, Random rng) {
        List<Move> moves = game.legalMoves();
        if (moves.isEmpty()) {
            throw new IllegalStateException("Нет допустимых ходов");
        }
        return moves.get(rng.nextInt(moves.size()));
    }
}
