package checkers.tournament;

import checkers.model.Outcome;

/**
 * Результат одной партии турнира.
 * seat0 имя бота на месте 0 (ходит первым)
 * seat1 имя бота на месте 1
 * outcome итог
 * rounds сколько кругов сыграно
 * progress0 продвижение игрока 0 в конце партии
 * progress1 продвижение игрока 1 в конце партии
 */
public record MatchResult(String seat0, String seat1, Outcome outcome, int rounds, int progress0, int progress1) { }
