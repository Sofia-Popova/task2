package checkers.bot;

import checkers.model.Game;
import checkers.model.Move;
import java.util.Random;

//паттерн «Стратегия»
public interface Bot {
    String name();

    /**
     * Выбирает ход за текущего игрока.
     * game партия (только чтение)
     * rng единый генератор случайных чисел
     * допустимый ход
     * IllegalStateException если ходов нет
     */
    Move choose(Game game, Random rng);
}
