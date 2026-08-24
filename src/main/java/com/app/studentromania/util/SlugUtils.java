package com.app.studentromania.util;

import java.text.Normalizer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("^-+|-+$");

    public static String toSlug(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String noDiacritics = DIACRITICS.matcher(normalized).replaceAll("");
        String lower = noDiacritics.toLowerCase();
        String dashed = NON_ALPHANUMERIC.matcher(lower).replaceAll("-");
        return EDGE_DASHES.matcher(dashed).replaceAll("");
    }

    /**
     * Tries baseSlug, then baseSlug-2, baseSlug-3, ... until isTaken returns false.
     */
    public static String findAvailableSlug(String baseSlug, Function<String, Boolean> isTaken) {
        String candidate = baseSlug;
        int suffix = 2;
        while (isTaken.apply(candidate)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

}
