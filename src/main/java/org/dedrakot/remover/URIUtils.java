package org.dedrakot.remover;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

// borrowed from jdk.nashorn.internal.runtime;
public class URIUtils {

    public static String encodeUri(String string) {
        return encode(string, false);
    }

    public static String encodeUriComponent(String string) {
        return encode(string, true);
    }

    private static String encode(String string, boolean component) {
        if (string.isEmpty()) {
            return string;
        }

        final int len = string.length();
        final StringBuilder sb = new StringBuilder();

        for (int k = 0; k < len; k++) {
            final char c = string.charAt(k);
            if (isUnescaped(c, component)) {
                sb.append(c);
                continue;
            }

            if (c >= 0xDC00 && c <= 0xDFFF) {
                error(string, k);
            }

            int v;
            if (c < 0xD800 || c > 0xDBFF) {
                v = c;
            } else {
                k++;
                if (k == len) {
                    error(string, k);
                }

                final char kChar = string.charAt(k);
                if (kChar < 0xDC00 || kChar > 0xDFFF) {
                    error(string, k);
                }
                v = ((c - 0xD800) * 0x400 + (kChar - 0xDC00) + 0x10000);
            }

            try {
                sb.append(toHexEscape(v));
            } catch (final Exception e) {
                error(string, k, e.getMessage());
            }
        }

        return sb.toString();
    }

    private static String toHexEscape(final int u0) {
        int u = u0;
        int len;
        final byte[] b = new byte[6];

        if (u <= 0x7f) {
            b[0] = (byte) u;
            len = 1;
        } else {
            // > 0x7ff -> length 2
            // > 0xffff -> length 3
            // and so on. each new length is an additional 5 bits from the
            // original 11
            // the final mask is 8-len zeros in the low part.
            len = 2;
            for (int mask = u >>> 11; mask != 0; mask >>>= 5) {
                len++;
            }
            for (int i = len - 1; i > 0; i--) {
                b[i] = (byte) (0x80 | (u & 0x3f));
                u >>>= 6; // 64 bits per octet.
            }

            b[0] = (byte) (~((1 << (8 - len)) - 1) | u);
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append('%');
            if ((b[i] & 0xff) < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b[i] & 0xff).toUpperCase());
        }

        return sb.toString();
    }

    private static void error(String string, int k) {
        error(string, k, "Incorrect character");
    }

    private static Exception error(String string, int k, String message) {
        throw new IllegalArgumentException(message + ". Failed to parse URI on position: " + k + ": " + string);
    }

    // 'uriEscaped' except for alphanumeric chars
    private static final String URI_UNESCAPED_NONALPHANUMERIC = "-_.!~*'()";
    // 'uriReserved' + '#'
    private static final String URI_RESERVED = ";/?:@&=+$,#";

    private static boolean isUnescaped(final char ch, boolean component) {
        if (('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z')
                || ('0' <= ch && ch <= '9')) {
            return true;
        }

        if (URI_UNESCAPED_NONALPHANUMERIC.indexOf(ch) >= 0) {
            return true;
        }

        if (!component) {
            return URI_RESERVED.indexOf(ch) >= 0;
        }

        return false;
    }

    public static Map<String, String> rawQueryParameters(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx == -1) {
                    params.put(pair, null);
                } else {
                    params.put(pair.substring(0, idx), pair.substring(idx + 1));
                }
            }
        }
        return params;
    }

    public static String convertUrl(URL u, @NotNull String newQuery) {
        String s;
        return u.getProtocol()
                + ':'
                + ((s = u.getAuthority()) != null && !s.isEmpty()
                ? "//" + s : "")
                + ((s = u.getPath()) != null ? s : "")
                + (newQuery.isEmpty() ? "" : '?' + newQuery)
                + ((s = u.getRef()) != null ? '#' + s : "");
    }
}
