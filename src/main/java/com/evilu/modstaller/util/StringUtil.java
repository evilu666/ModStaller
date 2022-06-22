package com.evilu.modstaller.util;

import java.util.Optional;

/**
 * StringUtil
 */
public interface StringUtil {

    public static String $f(final String formatString, final Object... variables) {
        return String.format(formatString, variables);
    }

    public static Optional<String> optional(final String str) {
        if (str == null || str.trim().length() == 0) {
            return Optional.empty();
        }

        return Optional.of(str);
    }

    
}
