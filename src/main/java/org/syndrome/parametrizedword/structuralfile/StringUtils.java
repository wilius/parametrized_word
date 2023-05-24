package org.syndrome.parametrizedword.structuralfile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.stripAccents;

public class StringUtils {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    private StringUtils() {
    }

    public static String normalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return toLowerCase(stripAccents(toUpperCase(trim(str))));
    }

    public static boolean isLetterOrDigit(int codePoint) {
        return ((((1 << Character.UPPERCASE_LETTER) |
                (1 << Character.LOWERCASE_LETTER) |
                (1 << Character.DECIMAL_DIGIT_NUMBER)) >> Character.getType(codePoint)) & 1)
                != 0;
    }

    public static String trim(String string) {
        if (string == null) {
            return null;
        }

        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }

        return string;
    }

    public static String toUpperCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.toUpperCase(Locale.ENGLISH);
    }

    public static String toLowerCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.toLowerCase(Locale.ENGLISH);
    }

    public static String toString(Object object) {
        if (object != null) {
            return object.toString();
        }

        return null;
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        } else {
            int index = indexOfExtension(filename);
            return index == -1 ? "" : filename.substring(index + 1);
        }
    }

    private static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int extensionPos = filename.lastIndexOf(46);
            int lastSeparator = indexOfLastSeparator(filename);
            return lastSeparator > extensionPos ? -1 : extensionPos;
        }
    }

    private static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int lastUnixPos = filename.lastIndexOf(47);
            int lastWindowsPos = filename.lastIndexOf(92);
            return Math.max(lastUnixPos, lastWindowsPos);
        }
    }

    public static String createBasicAuthenticationHeader(String username, String password) {
        String authorizationHeader = String.format("%s:%s", username, password);
        authorizationHeader = Base64.getEncoder().encodeToString(authorizationHeader.getBytes());
        return String.format("Basic %s", authorizationHeader);
    }

    public static String printStackTrace(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        t.printStackTrace(printWriter);
        return writer.toString();
    }

    public static String concatUrl(String base, String segment) {
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        if (segment.startsWith("/")) {
            segment = segment.substring(1);
        }

        return base + "/" + segment;
    }

    public static String byteCountToDisplaySize(long value) {
        final long[] dividers = new long[]{T, G, M, K, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
        if (value < 1) {
            throw new IllegalArgumentException("Invalid file size: " + value);
        }

        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }

        return result;
    }

    private static String format(final long value,
                                 final long divider,
                                 final String unit) {
        double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Invalid length to generate a random string");
        }

        StringBuilder builder = new StringBuilder(length);
        int alphabetLength = ALPHABET.length();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.abs(Math.log(Math.random() * Integer.MAX_VALUE) * Integer.MAX_VALUE) % alphabetLength);
            builder.append(ALPHABET.charAt(index));
        }

        return builder.toString();
    }

    public static String trimAllWhitespace(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        int len = str.length();
        StringBuilder sb = new StringBuilder(str.length());

        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
