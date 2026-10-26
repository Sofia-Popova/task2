package checkers.model;

import java.util.List;

/**
 * Итог партии
 * reason  причина завершения
 * winners номера игроков, финишировавших быстрее всех (при ничьей — несколько)
 */
public record Outcome(Reason reason, List<Integer> winners) {

    public enum Reason {
        //Единственный победитель
        WIN,
        //Несколько игроков закончили за одинаковое число ходов
        DRAW,
        // Никто не дошёл до цели (лимит кругов или все заблокированы)
        UNFINISHED
    }
}
