package checkers.bot;

import checkers.model.Game;
import checkers.model.Hex;
import checkers.model.Move;

/** Оценка продвижения к цели, расстояние до вершины целевого луча. */
public final class Evaluator {

    /** Расстояние между противоположными вершинами звезды. */
    public static final int MAX_DISTANCE = 16;

    private Evaluator() { }

    public static int distanceToGoal(Game game, int player, Hex cell) {
        return cell.distanceTo(game.targetOf(player).apex());
    }

    //Выигрыш хода, на сколько он приближает шашку к цели (может быть отрицательным)
    public static int gain(Game game, Move move) {
        int p = game.currentPlayer();
        return distanceToGoal(game, p, move.from()) - distanceToGoal(game, p, move.to());
    }

    //Суммарное продвижение игрока: чем больше, тем ближе все его шашки к цели.
    public static int progress(Game game, int player) {
        return game.piecesOf(player).stream()
                .mapToInt(c -> MAX_DISTANCE - distanceToGoal(game, player, c))
                .sum();
    }
}
