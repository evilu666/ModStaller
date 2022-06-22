package com.evilu.modstaller.util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.vavr.CheckedFunction1;

/**
 * StreamUtil
 */
public interface StreamUtil {

    public static <T, R> Predicate<T> mapFilter(final Function<T, R> mapFunc, final Predicate<R> predicate) {
        return t -> predicate.test(mapFunc.apply(t));
    }

    public static <T, R> Function<T, R> mapTrying(final CheckedFunction1<T, R> mapFunc, final BiFunction<T, Throwable, ? extends RuntimeException> exceptionWrapper) {
        return t -> {
            try {
                return mapFunc.apply(t);
            } catch (final Throwable tr) {
                throw exceptionWrapper.apply(t, tr);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T, T1 extends T, T2 extends T, R> Function<T, R> mapIfElseType(final Class<T1> ifCls, final Function<T1, R> ifMap, final Class<T2> elseCls, final Function<T2, R> elseMap) {
        return t -> {
            if (ifCls.isInstance(t)) {
                return ifMap.apply((T1) t);
            } else if (elseCls.isInstance(t)) {
                return elseMap.apply((T2) t);
            } else {
                throw new IllegalStateException("Unexpected type: " + t.getClass());
            }
        };
    }

    public static <T> Stream<T> repeating(final T item, final int times) {
        return Stream.generate(() -> item).limit(times);
    }

    public static <T> Stream<T> repeating(final Supplier<T> itemSupplier, final int times) {
        return Stream.generate(itemSupplier).limit(times);
    }
}
