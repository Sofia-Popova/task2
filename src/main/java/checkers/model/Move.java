package checkers.model;

/**
 * Ход одной фигуры, тут либо шаг на соседнюю клетку, либо цепочка прыжков (одним ходом).
 *  from откуда
 *  to куда
 */
public record Move(Hex from, Hex to) implements Comparable<Move> {

    public boolean isJump() {
        return from.distanceTo(to) > 1;
    }

    @Override
    public int compareTo(Move o) {
        int c = from.compareTo(o.from);
        return c != 0 ? c : to.compareTo(o.to);
    }

    @Override
    public String toString() {
        return from + "->" + to;
    }
}
