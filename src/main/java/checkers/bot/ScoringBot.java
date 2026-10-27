package checkers.bot;

import checkers.model.Game;
import checkers.model.Move;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Бот, выбирающий ход с максимальной оценкой, ничьи разрешает случайно */
public final class ScoringBot implements Bot {

    private final String name;
    private final MoveScorer scorer;

    public ScoringBot(String name, MoveScorer scorer) {
        this.name = Objects.requireNonNull(name);
        this.scorer = Objects.requireNonNull(scorer);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Move choose(Game game, Random rng) {
        List<Move> moves = game.legalMoves();
        if (moves.isEmpty()) {
            throw new IllegalStateException("Нет допустимых ходов");
        }
        int best = moves.stream().mapToInt(m -> scorer.score(game, m)).max().getAsInt();
        List<Move> top = moves.stream().filter(m -> scorer.score(game, m) == best).toList();
        return top.get(rng.nextInt(top.size()));
    }
}
