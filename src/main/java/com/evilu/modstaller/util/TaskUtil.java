package com.evilu.modstaller.util;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.concurrent.Task;

/**
 * TaskUtil
 */
public interface TaskUtil {

    public static <T, R> Task<R> chain(final Task<T> firstTask, final Function<T, Task<R>> taskMapper) {
        return new Task<R>() {
          @Override
          protected R call() throws Exception {
              firstTask.progressProperty().addListener((obs, oldVal, newVal) -> {
                updateProgress(newVal.doubleValue(), 2d);
              });

              firstTask.titleProperty().addListener((obs, oldVal, newVal) -> {
                updateTitle(newVal);
              });

              firstTask.messageProperty().addListener((obs, oldVal, newVal) -> {
                updateMessage(newVal);
              });

              final T intRes = firstTask.get();

              final Task<R> secondTask = taskMapper.apply(intRes);
              secondTask.progressProperty().addListener((obs, oldVal, newVal) -> {
                updateProgress(newVal.doubleValue() + 1d, 2d);
              });

              secondTask.titleProperty().addListener((obs, oldVal, newVal) -> {
                updateTitle(newVal);
              });

              secondTask.messageProperty().addListener((obs, oldVal, newVal) -> {
                updateMessage(newVal);
              });

              return secondTask.get();
          }
            
        };
    }

    public static <T> Task<T> simpleTask(final String msg, final Supplier<T> supplier) {
      return new Task<T>() {
        @Override
        protected T call() throws Exception {
          updateMessage(msg);
          updateProgress(0d, 1d);

          final T res = supplier.get();
          updateProgress(1d, 1d);

          return res;
        }
      };
    }

    public static <T, R> Task<R> map(final Task<T> task, final Function<T, R> mappingFunc) {
      return new Task<R>() {
        @Override
        protected R call() throws Exception {
          task.progressProperty().addListener((obs, oldVal, newVal) -> {
            updateProgress(newVal.doubleValue(), 1d);
          });

          task.titleProperty().addListener((obs, oldVal, newVal) -> {
            updateTitle(newVal);
          });

          task.messageProperty().addListener((obs, oldVal, newVal) -> {
            updateMessage(newVal);
          });

          return mappingFunc.apply(task.get());
        }
      };
    }

    public static <R> R get(final Task<R> task) {
        final Thread t = new Thread(task);
        t.start();


        try {
            t.join();
            switch (task.getState()) {
              case SUCCEEDED:
                return task.get();
              case FAILED:
                throw new IllegalStateException("Task failed!", task.getException());
              default:
                return null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
}
