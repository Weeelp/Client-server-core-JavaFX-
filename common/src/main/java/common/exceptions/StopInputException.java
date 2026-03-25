package common.exceptions;

public class StopInputException extends Exception {
    public StopInputException() {
        super("Ввод остановлен пользователем");
    }
}