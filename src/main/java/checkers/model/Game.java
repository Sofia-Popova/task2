package checkers.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Партия в китайские шашки для 2–6 игроков. Класс хранит состояние, проверяет правила
 *
 * ходят по кругу. Ход — шаг на соседнюю свободную клетку либо цепочка прыжков
 * через соседние фигуры (свои или чужие), но не их смесь. Когда круг завершён и хотя бы один
 * игрок собрал все шашки в противоположном луче, партия заканчивается: побеждает тот, кто
 * сделал меньше ходов; если таких несколько — ничья. Игрок без допустимых ходов пропускает ход.
 */
public final class Game {

    // Лимит кругов по умолчанию (защита от бесконечных партий ботов)
    public static final int DEFAULT_MAX_ROUNDS = 300;

    // Какие лучи занимают игроки при данном числе участников
    private static final int[][] SEATS = {
            {0, 3}, {0, 2, 4}, {0, 1, 3, 4}, {0, 1, 2, 3, 4}, {0, 1, 2, 3, 4, 5}
    };

    private final Board board = Board.standard();
    private final int playerCount;
    private final int maxRounds;
    private final List<Corner> homes;
    private final Map<Hex, Integer> pieces = new HashMap<>();
    private final int[] moveCounts;
    private final int[] finishedAt;
    private final List<Move> history = new ArrayList<>();
    private final List<Consumer<GameEvent>> listeners = new ArrayList<>();
    private int current;
    private int round;
    private Outcome outcome;

    //Стандартная расстановка.

    public Game(int playerCount) {
        this(playerCount, DEFAULT_MAX_ROUNDS);
    }


    public Game(int playerCount, int maxRounds) {
        this(playerCount, maxRounds, null);
    }

    private Game(int playerCount, int maxRounds, Map<Hex, Integer> placement) {
        if (playerCount < 2 || playerCount > 6) {
            throw new IllegalArgumentException("Игроков должно быть от 2 до 6: " + playerCount);
        }
        if (maxRounds < 1) {
            throw new IllegalArgumentException("Лимит кругов должен быть положительным");
        }
        this.playerCount = playerCount;
        this.maxRounds = maxRounds;
        this.homes = Arrays.stream(SEATS[playerCount - 2]).mapToObj(i -> Corner.values()[i]).toList();
        this.moveCounts = new int[playerCount];
        this.finishedAt = new int[playerCount];
        if (placement == null) {
            for (int p = 0; p < playerCount; p++) {
                int owner = p;
                board.cornerCells(homes.get(p)).forEach(c -> pieces.put(c, owner));
            }
        } else {
            placement.forEach((cell, owner) -> {
                if (!board.contains(cell) || owner < 0 || owner >= playerCount) {
                    throw new IllegalArgumentException("Некорректная расстановка: " + cell + " -> " + owner);
                }
                pieces.put(cell, owner);
            });
        }
    }

    public static Game withPieces(int playerCount, int maxRounds, Map<Hex, Integer> placement) {
        return new Game(playerCount, maxRounds, Objects.requireNonNull(placement));
    }

    //Повтор партии из журнала ходов: даёт ту же позицию.
    public static Game fromHistory(int playerCount, int maxRounds, List<Move> moves) {
        Game g = new Game(playerCount, maxRounds);
        moves.forEach(g::apply);
        return g;
    }

    //наблюдатель
    public void subscribe(Consumer<GameEvent> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public Board board() {
        return board;
    }

    public int playerCount() {
        return playerCount;
    }

    public int currentPlayer() {
        return current;
    }

    public int round() {
        return round;
    }

    public int moveCount(int player) {
        return moveCounts[player];
    }

    public Corner homeOf(int player) {
        return homes.get(player);
    }

    public Corner targetOf(int player) {
        return homes.get(player).opposite();
    }

    public Optional<Integer> ownerAt(Hex cell) {
        return Optional.ofNullable(pieces.get(cell));
    }

    public List<Hex> piecesOf(int player) {
        return pieces.entrySet().stream()
                .filter(e -> e.getValue() == player)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    public boolean isInTarget(int player) {
        List<Hex> mine = piecesOf(player);
        return !mine.isEmpty() && mine.stream().allMatch(targetOf(player)::contains);
    }

    public boolean isOver() {
        return outcome != null;
    }

    public Optional<Outcome> outcome() {
        return Optional.ofNullable(outcome);
    }

    public List<Move> history() {
        return Collections.unmodifiableList(history);
    }

    public List<Move> legalMovesFrom(Hex from) {
        List<Move> result = new ArrayList<>();
        for (Hex n : board.neighbors(from)) {
            if (!pieces.containsKey(n)) {
                result.add(new Move(from, n));
            }
        }
        for (Hex t : board.reachableByJumps(from, pieces::containsKey)) {
            result.add(new Move(from, t));
        }
        Collections.sort(result);
        return result;
    }

    //все допустимые ходы текущего игрока (пусто, если партия окончена)
    public List<Move> legalMoves() {
        if (isOver()) {
            return List.of();
        }
        return piecesOf(current).stream().flatMap(c -> legalMovesFrom(c).stream()).sorted().toList();
    }

    public List<GameEvent> apply(Move move) {
        Objects.requireNonNull(move);
        if (isOver()) {
            throw new IllegalStateException("Партия окончена");
        }
        Integer owner = pieces.get(move.from());
        if (owner == null || owner != current) {
            throw new IllegalMoveException("На " + move.from() + " нет шашки игрока " + current);
        }
        if (!legalMovesFrom(move.from()).contains(move)) {
            throw new IllegalMoveException("Недопустимый ход " + move);
        }
        pieces.remove(move.from());
        pieces.put(move.to(), current);
        history.add(move);
        moveCounts[current]++;

        List<GameEvent> events = new ArrayList<>();
        events.add(new GameEvent.MoveMade(current, move, moveCounts[current]));
        if (finishedAt[current] == 0 && isInTarget(current)) {
            finishedAt[current] = moveCounts[current];
            events.add(new GameEvent.PlayerFinished(current, moveCounts[current]));
        }
        advance(events);
        events.forEach(e -> listeners.forEach(l -> l.accept(e)));
        return List.copyOf(events);
    }

    private void advance(List<GameEvent> events) {
        for (int attempt = 0; attempt < playerCount; attempt++) {
            current = (current + 1) % playerCount;
            if (current == 0) {
                round++;
                if (Arrays.stream(finishedAt).anyMatch(x -> x > 0) || round >= maxRounds) {
                    finish(events);
                    return;
                }
            }
            if (!piecesOf(current).stream().allMatch(c -> legalMovesFrom(c).isEmpty())) {
                return;
            }
            events.add(new GameEvent.Passed(current));
        }
        finish(events);
    }

    private void finish(List<GameEvent> events) {
        int best = Arrays.stream(finishedAt).filter(x -> x > 0).min().orElse(0);
        List<Integer> winners = new ArrayList<>();
        for (int p = 0; p < playerCount; p++) {
            if (best > 0 && finishedAt[p] == best) {
                winners.add(p);
            }
        }
        Outcome.Reason reason = winners.isEmpty() ? Outcome.Reason.UNFINISHED
                : winners.size() == 1 ? Outcome.Reason.WIN : Outcome.Reason.DRAW;
        outcome = new Outcome(reason, List.copyOf(winners));
        events.add(new GameEvent.GameOver(outcome));
    }
}
