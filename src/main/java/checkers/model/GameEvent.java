package checkers.model;

public sealed interface GameEvent {

    /**
     * Сделан ход
     * player номер игрока
     * move ход
     * moveNumber порядковый номер хода этого игрока
     */
    record MoveMade(int player, Move move, int moveNumber) implements GameEvent { }

    //Игрок пропустил ход, так как у него нет допустимых ходов
    record Passed(int player) implements GameEvent { }

    //Игрок поставил все шашки в целевой луч.
    record PlayerFinished(int player, int moves) implements GameEvent { }

    //Партия завершена
    record GameOver(Outcome outcome) implements GameEvent { }
}
