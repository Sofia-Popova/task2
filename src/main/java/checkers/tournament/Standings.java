package checkers.tournament;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Обобщённая турнирная таблица, очки по ключам произвольного типа
 */
public final class Standings<T> {

    private final Map<T, Integer> points = new LinkedHashMap<>();

    /**
     * Добавляет очки
     * key участник
     * pts очки
     */
    public void add(T key, int pts) {
        points.merge(key, pts, Integer::sum);
    }

    public int points(T key) {
        return points.getOrDefault(key, 0);
    }

    // @return сумма очков всех участников
    public int total() {
        return points.values().stream().mapToInt(Integer::intValue).sum();
    }

    // рейтинг
    public List<Map.Entry<T, Integer>> ranking(Comparator<? super T> tieBreaker) {
        Comparator<Map.Entry<T, Integer>> byPoints =
                Comparator.comparing((Map.Entry<T, Integer> e) -> e.getValue(), Comparator.reverseOrder());
        return points.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue()))
                .sorted(byPoints.thenComparing(Map.Entry::getKey, tieBreaker))
                .toList();
    }
}
