package com.evilu.modstaller.ui.util;

import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;

/**
 * BindingUtil
 */
public interface BindingUtil {

    @Deprecated(forRemoval = true)
    public static <T> StringBinding mapString(final ObservableValue<T> value, Function<T, String> mappingFunc) {
        return mapString(value, mappingFunc, () -> "null");
    }

    public static <T> StringBinding mapString(final ObservableValue<T> value, Function<T, String> mappingFunc, final String nullValue) {
        return mapString(value, mappingFunc, () -> nullValue);
    }

    public static <T> StringBinding mapString(final ObservableValue<T> value, Function<T, String> mappingFunc, final Supplier<String> nullValueSupplier) {
        return Bindings.createStringBinding(() -> {
            final T v = value.getValue();
            return v == null ? nullValueSupplier.get() : mappingFunc.apply(v);
        });
    }

    public static DoubleBinding asDouble(final NumberBinding binding) {
        return Bindings.createDoubleBinding(() -> binding.getValue().doubleValue(), binding);
    }

    @Deprecated(forRemoval = true)
    public static <T, R> ObjectBinding<R> map(final ObservableValue<T> value, Function<T, R> mappingFunc) {
        return Bindings.createObjectBinding(() -> mappingFunc.apply(value.getValue()), value);
    }

    public static <R> ObjectBinding<R> map(final ObservableIntegerValue value, Function<Integer, R> mappingFunc, final R nullValue) {
        return map(value, mappingFunc, (Supplier<R>) () -> nullValue);
    }

    public static <R> ObjectBinding<R> map(final ObservableIntegerValue value, Function<Integer, R> mappingFunc, final Supplier<R> nullValueSupplier) {
        return Bindings.createObjectBinding(() -> {
            final Integer v = value.getValue().intValue();
            return value.getValue() == null ? nullValueSupplier.get() : mappingFunc.apply(v);
        });
    }

    public static <T, R> ObjectBinding<R> map(final ObservableValue<T> value, Function<T, R> mappingFunc, final R nullValue) {
        return map(value, mappingFunc, (Supplier<R>) () -> nullValue);
    }

    public static <T, R> ObjectBinding<R> map(final ObservableValue<T> value, Function<T, R> mappingFunc, final Supplier<R> nullValueSupplier) {
        return Bindings.createObjectBinding(() -> {
            final T v = value.getValue();
            return v == null ? nullValueSupplier.get() : mappingFunc.apply(v);
        });
    }

    public static <T, R> ObjectBinding<R> mapObservable(final ObservableValue<T> value, final Function<T, ObservableValue<R>> mappingFunc) {
        return map(map(value, mappingFunc), ObservableValue::getValue);
    }

    public static <T> ObservableObjectValue<T> createObservable(final T value) {
        return new SimpleObjectProperty<>(value);
    }

    public static ObservableDoubleValue createObservable(final double value) {
        return new SimpleDoubleProperty(value);
    }

    public static <T> ObjectExpression<T> staticExpression(final T value) {
        return ObjectExpression.objectExpression(createObservable(value));
    }

    public static DoubleExpression staticExpression(final double value) {
        return DoubleExpression.doubleExpression(createObservable(value));
    }

    public static BooleanExpression staticExpression(final boolean value) {
        return BooleanExpression.booleanExpression(createObservable(value));
    }
    
}
