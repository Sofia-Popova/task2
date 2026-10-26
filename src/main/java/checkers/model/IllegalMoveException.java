package checkers.model;

/** показывается при попытке сделать недопустимый ход */
public class IllegalMoveException extends RuntimeException {

    public IllegalMoveException(String message) {
        super(message);
    }
}
