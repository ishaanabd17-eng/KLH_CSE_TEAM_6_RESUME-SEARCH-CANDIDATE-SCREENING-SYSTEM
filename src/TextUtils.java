import java.util.Locale;

public final class TextUtils {
    private TextUtils() {}
    public static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
