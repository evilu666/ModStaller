package com.evilu.modstaller.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.TaskFailedException;

import io.vavr.CheckedFunction1;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.concurrent.Task;
import lombok.extern.log4j.Log4j2;

/**
 * TaskBuilder
 */
@Log4j2
public class TaskBuilder {


    public static interface TaskCallback {

        public void setTitle(final String title);

        public void setMessage(final String message);

        public void setProgress(final double workDone, final double totalWork);

        public ReadOnlyDoubleProperty getProgressProperty();

        public boolean shouldCancel();
    }

    public static interface TaskStepCallback {

        public void setMessage(final String message);

        public void setProgress(final double workDone, final double totalWork);

        public boolean shouldCancel();
    }

    @SuppressWarnings("unchecked")
    public static class TaskStep<T, R, I> {
        private final List<TaskStep<Object, Object, I>> steps;
        private final BiFunction<T, TaskStepCallback, R> callback;

        private TaskStep(final BiFunction<T, TaskStepCallback, R> callback) {
            this.callback = callback;
            steps = Arrays.asList((TaskStep<Object, Object, I>)this);
        }

        private TaskStep(final BiFunction<T, TaskStepCallback, R> callback, final List<TaskStep<Object, Object, I>> previousSteps) {
            this.callback = callback;
            steps = previousSteps;
            steps.add((TaskStep<Object, Object, I>)this);
        }

        public <RN> TaskStep<R, RN, I> then(final BiFunction<R, TaskStepCallback, RN> callback) {
            return new TaskStep<>(callback, this.steps);
        }

        @SuppressWarnings("rawtypes")
        public <RN> TaskStep<R, RN, I> then(final TaskStep<?, RN, R> step) {
            final List<TaskStep> newSteps = new ArrayList<>(steps);

            newSteps.addAll(step.steps);
            return new TaskStep(newSteps.get(newSteps.size()-1).callback, newSteps);
        }

        public Task<R> buildTask(final String title, final I arg) {
            return build(tc -> {
                tc.setTitle(title);
                tc.setProgress(0d, steps.size());

                Object currentValue = arg;
                final AtomicInteger currentStep = new AtomicInteger(1);
                for (final TaskStep<Object, Object, I> step : steps) {
                    currentValue = step.callback.apply(currentValue, new TaskStepCallback() {
                        @Override
                        public void setMessage(String message) {
                            tc.setMessage(message);
                        }

                        @Override
                        public void setProgress(double workDone, double totalWork) {
                            tc.setProgress((workDone / totalWork) * currentStep.get(), steps.size());
                        }

                        @Override
                        public boolean shouldCancel() {
                            return tc.shouldCancel();
                        }
                    });

                    currentStep.incrementAndGet();
                }

                return (R) currentValue;
            });
        }
    }

    public static <T, R> TaskStep<T, R, T> build(final BiFunction<T, TaskStepCallback, R> callback) {
        return new TaskStep<>(callback);
    }

    public static <T> Task<T> build(final CheckedFunction1<TaskCallback, T> callback) {
        final TranslationService ts = ApplicationContext.get().getTranslationService();

        return new Task<T>() {
          @Override
          protected T call() throws Exception {
              try {
                  return callback.apply(new TaskCallback() {

                    @Override
                    public void setTitle(final String title) {
                        updateTitle(title);
                    }

                    @Override
                    public void setMessage(final String message) {
                        updateMessage(message);
                    }

                    @Override
                    public void setProgress(double workDone, double totalWork) {
                        updateProgress(workDone, totalWork);
                    }

                    @Override
                    public ReadOnlyDoubleProperty getProgressProperty() {
                        return progressProperty();
                    }

                    @Override
                    public boolean shouldCancel() {
                        return isCancelled();
                    }
                  });
              } catch (final Throwable t) {

                  if (t instanceof TaskFailedException) throw (TaskFailedException) t;

                  log.error("Error while running task", t);
                  throw new TaskFailedException(ts.translated("task.failed", t.getLocalizedMessage()), t);
              }
          }
        };
    }
    
}
