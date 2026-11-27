package com.smartshop.common.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {

    private SlugUtils() {
    }

    public static String toSlugBase(String input, int maxLength, String fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");

        if (normalized.isEmpty()) {
            return fallback;
        }
        if (normalized.length() > maxLength) {
            String truncated = normalized.substring(0, maxLength).replaceAll("-+$", "");
            return truncated.isEmpty() ? fallback : truncated;
        }
        return normalized;
    }
}
