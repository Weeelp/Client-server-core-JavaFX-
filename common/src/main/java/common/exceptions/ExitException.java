package common.exceptions;

public class ExitException extends RuntimeException {
    public ExitException() {
        super("Программа завершена по команде exit");
    }
}