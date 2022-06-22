package com.evilu.modstaller.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * FuncUtil
 */
public interface FuncUtil {

    @SafeVarargs
    public static <T> Predicate<T> and(final Predicate<T>... predicates) {
        final List<Predicate<T>> p = Arrays.asList(predicates);
        return t -> p.stream().allMatch(pred -> pred.test(t));
    }

    @SafeVarargs
    public static <T> Predicate<T> or(final Predicate<T>... predicates) {
        final List<Predicate<T>> p = Arrays.asList(predicates);
        return t -> p.stream().anyMatch(pred -> pred.test(t));
    }

    public static <T, I, R> Function<T, R> compose(final Function<T, I> func1, final Function<I, R> func2) {
        return func1.andThen(func2);
    }

    public static <T, R> Function<T, R> function(final Function<T, R> fn) {
        return fn;
    }

    public static <T> Function<T, Void> function(final Consumer<T> fn) {
        return t -> {
            fn.accept(t);
            return null;
        };
    }

    public static <T> Function<T, T> inplaceFunction(final Consumer<T> fn) {
        return t -> {
            fn.accept(t);
            return t;
        };
    }
    
}
