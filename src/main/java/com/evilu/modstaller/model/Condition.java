package com.evilu.modstaller.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 * Condition
 */
@FunctionalInterface
public interface Condition {

    public boolean isFulfilled();

    default boolean isNotFulfilled() {
        return !isFulfilled();
    }

    default Condition negated() {
        return this::isNotFulfilled;
    }

    default Condition and(final BooleanSupplier... suppliers) {
        final List<Condition> extraConditions = Arrays.stream(suppliers)
            .map(Condition::of)
            .collect(Collectors.toList());

        return () -> isFulfilled() && extraConditions.stream().allMatch(Condition::isFulfilled);
    }

    default Condition or(final BooleanSupplier... suppliers) {
        final List<Condition> extraConditions = Arrays.stream(suppliers)
            .map(Condition::of)
            .collect(Collectors.toList());

        return () -> isFulfilled() || extraConditions.stream().anyMatch(Condition::isFulfilled);
    }




    public static Condition of(final BooleanSupplier supplier) {
        return supplier::getAsBoolean;
    }

    public static Condition NOT(final BooleanSupplier supplier) {
        return Condition.of(supplier)::isNotFulfilled;
    }

    public static Condition AND(final BooleanSupplier... suppliers) {
        final List<Condition> extraConditions = Arrays.stream(suppliers)
            .map(Condition::of)
            .collect(Collectors.toList());

        return () -> extraConditions.stream().allMatch(Condition::isFulfilled);
    }

    public static Condition OR(final BooleanSupplier... suppliers) {
        final List<Condition> extraConditions = Arrays.stream(suppliers)
            .map(Condition::of)
            .collect(Collectors.toList());

        return () -> extraConditions.stream().anyMatch(Condition::isFulfilled);
    }



    
}
