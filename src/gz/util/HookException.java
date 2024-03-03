package gz.util;

public class HookException extends Exception {
    private final Exception originalException;

    public HookException(Exception originalException) {
        super("Radar exception");
        this.originalException = originalException;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("\nOriginal Exception: ");
        stringBuilder.append(originalException.toString());
        return stringBuilder.toString();
    }

    public static void printStackTrace(Exception e) {
        new HookException(e).printStackTrace();
    }
}
