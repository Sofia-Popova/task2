package checkers.ui;

import checkers.model.GameEvent;
import checkers.model.Game;
import checkers.model.Hex;
import checkers.model.Outcome;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

/** Консольный слой вывода: рисует поле и описывает события */
public final class ConsoleView {

    private final PrintStream out;

    public ConsoleView(PrintStream out) {
        this.out = out;
    }

    /**
     * Рисует поле: цифра — номер игрока (с 1), точка - пустая клетка.
     * Слева указаны строка r и координата q самой левой клетки строки.
     */
    public void render(Game game) {
        for (int r = -8; r <= 8; r++) {
            final int row = r;
            char[] line = new char[60];
            Arrays.fill(line, ' ');
            Hex first = game.board().cells().stream().filter(c -> c.r() == row)
                    .min(Comparator.naturalOrder()).orElse(null);
            if (first == null) {
                continue;
            }
            game.board().cells().stream().filter(c -> c.r() == row).forEach(c -> {
                int col = 2 * c.q() + c.r() + 30;
                line[col] = game.ownerAt(c).map(p -> (char) ('1' + p)).orElse('.');
            });
            out.printf("r=%2d q0=%2d %s%n", r, first.q(), new String(line).stripTrailing());
        }
    }

    //Текстовое описание события.

    public String describe(GameEvent e) {
        if (e instanceof GameEvent.MoveMade m) {
            return "Игрок " + (m.player() + 1) + " (ход " + m.moveNumber() + "): " + m.move()
                    + (m.move().isJump() ? " прыжок" : " шаг");
        } else if (e instanceof GameEvent.Passed p) {
            return "Игрок " + (p.player() + 1) + " пропускает ход";
        } else if (e instanceof GameEvent.PlayerFinished f) {
            return "Игрок " + (f.player() + 1) + " собрал все шашки за " + f.moves() + " ходов";
        } else if (e instanceof GameEvent.GameOver g) {
            return summary(g.outcome());
        }
        return e.toString();
    }

    public void print(GameEvent e) {
        out.println(describe(e));
    }

    public String summary(Outcome o) {
        String who = o.winners().stream().map(p -> String.valueOf(p + 1)).reduce((a, b) -> a + ", " + b).orElse("-");
        return switch (o.reason()) {
            case WIN -> "Партия окончена. Победил игрок " + who;
            case DRAW -> "Партия окончена. Ничья между игроками " + who;
            case UNFINISHED -> "Партия окончена без победителя (лимит кругов)";
        };
    }
}
