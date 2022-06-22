package com.evilu.modstaller.ui.dialog;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.TaskFailedException;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.ui.util.NodeUtil;
import com.evilu.modstaller.util.FuncUtil;
import com.evilu.modstaller.util.TryUtil;

import javafx.application.Platform;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * SteppedTaskProgressDialog
 */
public class TaskDialogs {

    public static <R> Dialog<R> create(final Task<R> task) {
        final Dialog<R> dialog = new Dialog<>();
        NodeUtil.installStyle(dialog.getDialogPane().getStylesheets());

        dialog.titleProperty().bind(task.titleProperty());
        dialog.headerTextProperty().bind(task.messageProperty());

        final ProgressBar progBar = new ProgressBar(0d);
        progBar.progressProperty().bind(task.progressProperty());

        dialog.getDialogPane().setContent(progBar);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);


        dialog.setOnShown(e -> new Thread(task).start());

        task.setOnFailed(e -> {
            final TranslationService ts = ApplicationContext.get().getTranslationService();
            final Throwable t = task.getException();

            dialog.headerTextProperty().unbind();

            if (t == null) {
                dialog.headerTextProperty().bind(ts.translated("task.failed"));
            } else if (t instanceof TaskFailedException) {
                dialog.headerTextProperty().bind(((TaskFailedException) t).messageExpression());
            } else {
                dialog.setHeaderText(t.getLocalizedMessage());
            }
        });

        return dialog;
    }

    private static final class TaskProgressIndicator extends VBox {

        final ProgressBar bar;

        private TaskProgressIndicator(final Task<?> task) {
            final TranslationService ts = ApplicationContext.get().getTranslationService();

            final Label titleLabel = new Label();
            titleLabel.setFont(new Font(20d));
            titleLabel.textProperty().bind(task.titleProperty());

            final HBox titleBox = new HBox(titleLabel);
            titleBox.setAlignment(Pos.CENTER);

            final Label statusLabel = new Label();
            statusLabel.textProperty().bind(ts.translatedExpression("dialog.taskList.status", task.messageProperty()));

            final HBox statusBox = new HBox(statusLabel);
            statusBox.setAlignment(Pos.CENTER_LEFT);

            bar = new ProgressBar();
            bar.progressProperty().bind(task.progressProperty());
            bar.prefWidthProperty().bind(widthProperty());

            getChildren().addAll(titleBox, statusBox, bar);
            setPadding(new Insets(10d));

            task.setOnFailed(e -> {
                statusLabel.textProperty().unbind();

                final Throwable t = task.getException();
                if (t == null) {
                    statusLabel.textProperty().bind(ts.translated("task.failed"));
                } else if (t instanceof TaskFailedException) {
                    statusLabel.textProperty().bind(((TaskFailedException) t).messageExpression());
                } else {
                    statusLabel.setText(t.getLocalizedMessage());
                }
            });
        }

        private ReadOnlyDoubleProperty progressProperty() {
            return bar.progressProperty();
        }
        
    }

    public static <R> Dialog<List<R>> create(final StringExpression titleExpression, final List<Task<R>> tasks, final ExecutorService executorService) {
        final TranslationService ts = ApplicationContext.get().getTranslationService();

        final Dialog<List<R>> dialog = new Dialog<>();
        NodeUtil.installStyle(dialog.getDialogPane().getStylesheets());

        dialog.titleProperty().bind(titleExpression);

        final HBox totalProgBox = new HBox(10d);

        final Label totalProgLabel = new Label();
        totalProgLabel.textProperty().bind(ts.translated("dialog.taskList.totalProgress"));

        final ProgressBar totalBar = new ProgressBar();

        totalProgBox.getChildren().addAll(totalProgLabel, totalBar);
        HBox.setHgrow(totalBar, Priority.ALWAYS);

        final List<TaskProgressIndicator> indicators = tasks.stream()
            .map(TaskProgressIndicator::new)
            .collect(Collectors.toList());

        final VBox taskBox = new VBox(10d);
        taskBox.getChildren().addAll(indicators);

        totalBar.progressProperty().bind(indicators.stream()
            .map(TaskProgressIndicator::progressProperty)
            .map(DoubleExpression.class::cast)
            .reduce(DoubleExpression::add)
            .orElse(BindingUtil.staticExpression(1d)));


        final ScrollPane scrollPane = new ScrollPane(taskBox);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        taskBox.prefWidthProperty().bind(scrollPane.widthProperty());


        final VBox rootBox = new VBox(totalProgBox, scrollPane);
        dialog.getDialogPane().setContent(rootBox);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.CANCEL || bt == ButtonType.CLOSE) {
                tasks.stream()
                    .filter(t -> !(t.isDone() || t.isCancelled()))
                    .forEach(Task::cancel);
            } else {
                if (tasks.stream().noneMatch(FuncUtil.or(Task::isRunning, Task::isCancelled))) {
                    return tasks.stream()
                        .map(TryUtil.wrapping(Task::get, RuntimeException::new)) //TODO: propper exception handling!!!
                        .collect(Collectors.toList());
                }
            }

            return null;
        });



        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.FINISH, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.FINISH).setDisable(true);

        final CountDownLatch doneLatch = new CountDownLatch(tasks.size());

        tasks.forEach(task -> {
            task.setOnSucceeded(e -> doneLatch.countDown());
            task.setOnFailed(e -> doneLatch.countDown());
            task.setOnCancelled(e -> doneLatch.countDown());
        });

        new Thread(() -> {
            try {
                doneLatch.await();
                Platform.runLater(() -> {
                    dialog.getDialogPane().lookupButton(ButtonType.FINISH).setDisable(false);
                    if (tasks.stream().map(Task::getState).allMatch(State.SUCCEEDED::equals)) {
                        dialog.setResult(tasks.stream().map(TryUtil.wrapping(Task::get, RuntimeException::new)).collect(Collectors.toList()));
                    }
                });
            } catch (final Throwable t) {
                throw new RuntimeException("Error waiting for tasks to finish: " + t.getMessage(), t);
            }
        }).start();


        dialog.setOnShown(e -> tasks.forEach(executorService::execute));

        return dialog;
    }



    
}
