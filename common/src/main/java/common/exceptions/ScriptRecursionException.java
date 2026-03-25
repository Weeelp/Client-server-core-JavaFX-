package common.exceptions;

public class ScriptRecursionException extends RuntimeException {
    public ScriptRecursionException(String message) {
        super(message);
    }
}