package com.evilu.modstaller.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.vavr.CheckedFunction1;
import io.vavr.control.Either;

/**
 * TryUtil
 */
public interface TryUtil {

    public static <T, R> Function<T, R> tryMapping(final CheckedFunction1<T, R> mappingFunc, final BiFunction<Throwable, T, R> recover) {
        return t -> {
            try {
                return mappingFunc.apply(t);
            } catch(final Throwable tr) {
                return recover.apply(tr, t);
            }
        };
    }

    public static <T, R> Function<T, Optional<R>> mapOptional(final CheckedFunction1<T, R> mappingFunc) {
        return t -> {
            try {
                return Optional.of(mappingFunc.apply(t));
            } catch (final Throwable tr) {
                return Optional.empty();
            }
        };
    }

    public static <T, R> Function<T, Either<R, Throwable>> mapEither(final CheckedFunction1<T, R> mappingFunc) {
        return t -> {
            try {
                return Either.left(mappingFunc.apply(t));
            } catch (final Throwable tr) {
                return Either.right(tr);
            }
        };
    }

    public static <T, R, E extends RuntimeException> Function<T, R> wrapping(final CheckedFunction1<T, R> mappingFunc, final Function<Throwable, E> wrappingFunc) {
        return t -> {
            try {
                return mappingFunc.apply(t);
            } catch (final Throwable tr) {
                throw wrappingFunc.apply(tr);
            }
        };
    }

}
