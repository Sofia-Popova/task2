package checkers.ui;

import checkers.bot.Bot;
import checkers.bot.Bots;
import checkers.bot.GameRunner;
import checkers.model.Game;
import checkers.model.GameEvent;
import checkers.model.Hex;
import checkers.model.IllegalMoveException;
import checkers.model.Move;
import checkers.tournament.MatchResult;
import checkers.tournament.Tournament;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//консоль игра
public final class Main {

    private Main() { }

    public static void main(String[] args) throws IOException {
        Map<String, String> opt = parse(args);
        long seed = Long.parseLong(opt.getOrDefault("seed", "42"));
        switch (opt.getOrDefault("mode", "sim")) {
            case "sim" -> simulate(opt, seed);
            case "play" -> play(opt, seed);
            case "tournament" -> tournament(opt, seed);
            default -> System.out.println("Режимы: sim | play | tournament. Боты: " + Bots.names());
        }
    }

    private static Map<String, String> parse(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                boolean hasValue = i + 1 < args.length && !args[i + 1].startsWith("--");
                map.put(args[i].substring(2), hasValue ? args[++i] : "true");
            }
        }
        return map;
    }

    private static List<Bot> seatBots(String spec, int players) {
        String[] names = spec.split(",");
        List<Bot> bots = new ArrayList<>();
        for (int i = 0; i < players; i++) {
            bots.add(Bots.create(names[i % names.length].trim()));
        }
        return bots;
    }

    private static void simulate(Map<String, String> opt, long seed) {
        int players = Integer.parseInt(opt.getOrDefault("players", "2"));
        Game game = new Game(players, Integer.parseInt(opt.getOrDefault("rounds", "300")));
        ConsoleView view = new ConsoleView(System.out);
        if (!opt.containsKey("quiet")) {
            game.subscribe(view::print);
        }
        System.out.println("Зерно: " + seed);
        view.render(game);
        GameRunner.play(game, seatBots(opt.getOrDefault("bots", "greedy"), players), new Random(seed));
        view.render(game);
        System.out.println(view.summary(game.outcome().orElseThrow()) + ", кругов: " + game.round());
    }

    private static void tournament(Map<String, String> opt, long seed) {
        List<Bot> bots = new ArrayList<>();
        Bots.names().forEach(n -> bots.add(Bots.create(n)));
        Tournament t = new Tournament(bots, Integer.parseInt(opt.getOrDefault("games", "4")), seed,
                Integer.parseInt(opt.getOrDefault("rounds", "300")));
        List<MatchResult> results = t.run();
        results.forEach(r -> System.out.println(r.seat0() + " vs " + r.seat1() + ": "
                + r.outcome().reason() + " " + r.outcome().winners() + ", кругов " + r.rounds()));
        System.out.println("Таблица:");
        t.standings(results).ranking(Comparator.naturalOrder())
                .forEach(e -> System.out.println("  " + e.getKey() + " — " + e.getValue()));
    }

    private static void play(Map<String, String> opt, long seed) throws IOException {
        int players = Integer.parseInt(opt.getOrDefault("players", "2"));
        Game game = new Game(players, Integer.parseInt(opt.getOrDefault("rounds", "300")));
        ConsoleView view = new ConsoleView(System.out);
        game.subscribe(view::print);
        List<Bot> bots = seatBots(opt.getOrDefault("bots", "greedy"), players);
        Random rng = new Random(seed);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (!game.isOver()) {
            if (game.currentPlayer() != 0) {
                game.apply(bots.get(game.currentPlayer()).choose(game, rng));
                continue;
            }
            view.render(game);
            Move move = readMove(game, in);
            if (move == null) {
                return;
            }
            try {
                game.apply(move);
            } catch (IllegalMoveException ex) {
                System.out.println("Нельзя: " + ex.getMessage());
            }
        }
        view.render(game);
    }

    /** Читает ход «q r q r», номер из списка «moves» или «quit» */
    private static Move readMove(Game game, BufferedReader in) throws IOException {
        List<Move> listed = List.of();
        Pattern num = Pattern.compile("-?\\d+");
        while (true) {
            System.out.print("Ваш ход (q r q r | moves | номер | quit): ");
            String line = in.readLine();
            if (line == null || line.trim().equals("quit")) {
                return null;
            }
            line = line.trim();
            if (line.equals("moves")) {
                listed = game.legalMoves();
                for (int i = 0; i < listed.size(); i++) {
                    System.out.println("  " + i + ": " + listed.get(i));
                }
                continue;
            }
            List<Integer> ints = new ArrayList<>();
            Matcher m = num.matcher(line);
            while (m.find()) {
                ints.add(Integer.parseInt(m.group()));
            }
            if (ints.size() == 1 && ints.get(0) >= 0 && ints.get(0) < listed.size()) {
                return listed.get(ints.get(0));
            }
            if (ints.size() == 4) {
                return new Move(new Hex(ints.get(0), ints.get(1)), new Hex(ints.get(2), ints.get(3)));
            }
            System.out.println("Не понял ввод.");
        }
    }
}
