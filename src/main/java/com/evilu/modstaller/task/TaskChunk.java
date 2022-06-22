package com.evilu.modstaller.task;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Condition;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import io.vavr.CheckedFunction1;
import javafx.beans.Observable;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * TaskChunk
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskChunk<R> {

    public static interface TaskChunkStatusCallback {

        public void setProgress(final double progress);

        public boolean shouldCancel();

        public static TaskChunkStatusCallback compose(final Consumer<Double> progressSetter, final Supplier<Boolean> cancelCallback) {
            return new TaskChunkStatusCallback() {
                public void setProgress(final double progress) {
                    progressSetter.accept(progress);
                }

                public boolean shouldCancel() {
                    return cancelCallback.get();
                }
            };
        }
    }

    private final StringExpression message;
    private final CheckedFunction1<TaskChunkStatusCallback, R> callback;
    private final Condition precondition;

    private final ReadOnlyObjectWrapper<R> resultWrapper = new ReadOnlyObjectWrapper<>();


    public R run(final TaskChunkStatusCallback statusCallback) throws Throwable {
        final R result = callback.apply(statusCallback);
        resultWrapper.setValue(result);
        return result;
    }

    public ReadOnlyObjectProperty<R> resultProperty() {
        return resultWrapper.getReadOnlyProperty();
    }

    public static <T> TaskChunk<T> create(final String message, final CheckedFunction1<TaskChunkStatusCallback, T> callback) {
        return new TaskChunk<>(StringExpression.stringExpression(new SimpleStringProperty(message)), callback, null);
    }

    public static <T> TaskChunk<T> create(final StringExpression message, final CheckedFunction1<TaskChunkStatusCallback, T> callback) {
        return new TaskChunk<>(message, callback, null);
    }

    public boolean shouldRun() {
        return precondition == null || precondition.isFulfilled();
    }

    public TaskChunk<R> withPrecondition(final Condition precondition) {
        return new TaskChunk<>(this.message, this.callback, precondition);
    }

    @SuppressWarnings({
        "rawtypes",
        "unchecked"
    })
    public static <R> Task<R> assemble(final String title, final TaskChunk... chunks) {
        final List<TaskChunk> chunkList = Arrays.asList(chunks);
        final TranslationService ts = ApplicationContext.get().getTranslationService();

        return TaskBuilder.build(statusCallback -> {
            statusCallback.setTitle(title);
            statusCallback.setProgress(0d, chunkList.size());

            int currentChunk = 0;
            Object result = null;
            for (final TaskChunk chunk : chunkList) {
                if (chunk.shouldRun()) {
                    statusCallback.setMessage(chunk.message.getValue());
                    result = chunk.run(TaskChunkStatusCallback.compose(
                        progress -> statusCallback.setProgress(currentChunk + progress, chunkList.size()),
                        statusCallback::shouldCancel)
                    );
                }
            }

            statusCallback.setMessage(ts.translateOnce("task.done"));
            return (R) result;
        });
    }




    
}
