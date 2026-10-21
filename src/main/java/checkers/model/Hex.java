package checkers.model;

import java.util.List;

/**
 * Каждая клетка на поле имеет шесть соседей.
 * Чтобы указать, где клетка находится, нужны два числа: q и r.
 * q столбец координата
 * r строка координата
 */
public record Hex(int q, int r) implements Comparable<Hex> {

    /** Шесть направлений к соседям,
     * противоположное направление отстоит на 3 позиции. */
    public static final List<Hex> DIRECTIONS = List.of(
            new Hex(1, 0), new Hex(1, -1), new Hex(0, -1),
            new Hex(-1, 0), new Hex(-1, 1), new Hex(0, 1));

    public int s() {
        return -q - r;
    }

    //Сумма координат (сдвиг клетки на вектор)
    public Hex plus(Hex other) {
        return new Hex(q + other.q, r + other.r);
    }

    //Разность координат
    public Hex minus(Hex other) {
        return new Hex(q - other.q, r - other.r);
    }

    //Расстояние по сетке в шагах между соседними клетками
    public int distanceTo(Hex other) {
        int dq = q - other.q;
        int dr = r - other.r;
        return Math.max(Math.abs(dq), Math.max(Math.abs(dr), Math.abs(dq + dr)));
    }

    @Override
    public int compareTo(Hex o) {
        return q != o.q ? Integer.compare(q, o.q) : Integer.compare(r, o.r);
    }

    @Override
    public String toString() {
        return "(" + q + "," + r + ")";
    }
}
