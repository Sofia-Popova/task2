package checkers.bot;

import checkers.model.Hex;
import java.util.List;

//паттерн «Фабричный метод»
public final class Bots {

    private Bots() { }

    //имена доступных стратегий
    public static List<String> names() {
        return List.of("random", "greedy", "balanced");
    }

   //сщздаем бота
    public static Bot create(String name) {
        return switch (name) {
            case "random" -> new RandomBot();
            // жадный: максимальное приближение к цели
            case "greedy" -> new ScoringBot("greedy", Evaluator::gain);
            // сбалансированный: при равном приближении двигает самую отставшую шашку
            case "balanced" -> new ScoringBot("balanced", (g, m) -> {
                Hex from = m.from();
                return Evaluator.gain(g, m) * 100 + Evaluator.distanceToGoal(g, g.currentPlayer(), from);
            });
            default -> throw new IllegalArgumentException("Неизвестный бот: " + name);
        };
    }
}
