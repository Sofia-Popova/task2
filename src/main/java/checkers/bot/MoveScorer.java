package checkers.bot;

import checkers.model.Game;
import checkers.model.Move;

/** Оценка хода числом: больше — лучше */
@FunctionalInterface
public interface MoveScorer {

    int score(Game game, Move move);
}
