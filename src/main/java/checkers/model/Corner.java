package checkers.model;

/**
 * Один из шести лучей звезды. Константы перечислены по кругу, поэтому
 * противоположный луч всегда отстоёт на три позиции.
 */
public enum Corner {
    Q_POS(new Hex(8, -4)), S_NEG(new Hex(4, 4)), R_POS(new Hex(-4, 8)),
    Q_NEG(new Hex(-8, 4)), S_POS(new Hex(-4, -4)), R_NEG(new Hex(4, -8));

    private final Hex apex;

    Corner(Hex apex) {
        this.apex = apex;
    }

    //вершина луча (самая дальняя клетка)
    public Hex apex() {
        return apex;
    }

    //Принадлежит ли клетка лучу (10 клеток: ряды из 1, 2, 3 и 4 клеток)
    public boolean contains(Hex h) {
        return switch (this) {
            case Q_POS -> h.q() >= 5;
            case S_NEG -> h.s() <= -5;
            case R_POS -> h.r() >= 5;
            case Q_NEG -> h.q() <= -5;
            case S_POS -> h.s() >= 5;
            case R_NEG -> h.r() <= -5;
        };
    }

   //смотрим противоположный лучн
    public Corner opposite() {
        return values()[(ordinal() + 3) % 6];
    }
}
