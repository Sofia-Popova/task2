package checkers.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Поле-звезда (121 клетка) как неизменяемый граф соседства
 * Звезда — объединение двух больших треугольников
 */
public final class Board {

    private static final Board STANDARD = new Board();

    private final List<Hex> cells;
    private final Set<Hex> cellSet;
    private final Map<Hex, List<Hex>> graph = new LinkedHashMap<>();

    //Строим стандартную звезду
    public Board() {
        TreeSet<Hex> sorted = new TreeSet<>();
        for (int q = -8; q <= 8; q++) {
            for (int r = -8; r <= 8; r++) {
                int s = -q - r;
                boolean up = q >= -4 && r >= -4 && s >= -4;
                boolean down = q <= 4 && r <= 4 && s <= 4;
                if (up || down) {
                    sorted.add(new Hex(q, r));
                }
            }
        }
        cells = List.copyOf(sorted);
        cellSet = Set.copyOf(sorted);
        for (Hex c : cells) {
            graph.put(c, Hex.DIRECTIONS.stream().map(c::plus).filter(cellSet::contains).toList());
        }
    }

    public static Board standard() {
        return STANDARD;
    }

    public List<Hex> cells() {
        return cells;
    }

    public boolean contains(Hex h) {
        return cellSet.contains(h);
    }

    public List<Hex> neighbors(Hex h) {
        List<Hex> list = graph.get(h);
        if (list == null) {
            throw new IllegalArgumentException("Клетки " + h + " нет на поле");
        }
        return list;
    }

    public List<Hex> cornerCells(Corner corner) {
        return cells.stream().filter(corner::contains).toList();
    }

    /**
     * Все поля, достижимые цепочкой прыжков (обход графа в ширину), прыжок — через соседнюю
     * занятую клетку на свободную клетку сразу за ней. Исходная клетка считается пустой.
     */
    public Set<Hex> reachableByJumps(Hex start, Predicate<Hex> occupied) {
        Set<Hex> result = new LinkedHashSet<>(jumpTree(start, occupied).keySet());
        result.remove(start);
        return result;
    }

    //Кратчайшая (по числу прыжков) цепочка клеток от старта до цели.

    public List<Hex> jumpPath(Hex start, Hex target, Predicate<Hex> occupied) {
        Map<Hex, Hex> parent = jumpTree(start, occupied);
        if (target.equals(start) || !parent.containsKey(target)) {
            return List.of();
        }
        LinkedList<Hex> path = new LinkedList<>();
        for (Hex c = target; c != null; c = parent.get(c)) {
            path.addFirst(c);
        }
        return List.copyOf(path);
    }

    //BFS: отображение «клетка -> откуда в неё прыгнули», у старта родитель null
    private Map<Hex, Hex> jumpTree(Hex start, Predicate<Hex> occupied) {
        neighbors(start); // проверка, что старт на поле
        Predicate<Hex> blocked = c -> !c.equals(start) && occupied.test(c);
        Map<Hex, Hex> parent = new LinkedHashMap<>();
        parent.put(start, null);
        Deque<Hex> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Hex cur = queue.poll();
            for (Hex over : neighbors(cur)) {
                if (!blocked.test(over)) {
                    continue;
                }
                Hex landing = over.plus(over.minus(cur));
                if (cellSet.contains(landing) && !blocked.test(landing) && !parent.containsKey(landing)) {
                    parent.put(landing, cur);
                    queue.add(landing);
                }
            }
        }
        return parent;
    }
}
