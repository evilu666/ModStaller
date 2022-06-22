package com.evilu.modstaller.ui.dialog;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.TaskFailedException;
import com.evilu.modstaller.ui.pane.BorderedTitledPane;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.ui.util.NodeUtil;
import com.evilu.modstaller.util.TryUtil;

import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * TaskDialog
 */
public class TaskListDialog<R> extends Dialog<List<R>> {

    private final TranslationService ts;
    private final List<Task<R>> tasks;

    private final ReadOnlyIntegerWrapper tasksDone = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper tasksSuccessful = new ReadOnlyIntegerWrapper(0);


    public TaskListDialog(final StringExpression titleExpression, final StringExpression statusExpression, final List<Task<R>> tasks) {
        this.ts = ApplicationContext.get().getTranslationService();
        this.tasks = tasks;

        final ExecutorService executorService = ApplicationContext.get().getConfig().getSettings().getExecutorServiceExpression().get();

        tasks.forEach(task -> task.stateProperty().addListener((obs, o, n) -> {
            if (n == State.SUCCEEDED) increaseTasksSuccessful();
            if (Set.of(State.SUCCEEDED, State.CANCELLED, State.FAILED).contains(n)) increaseTasksDone();
            if (Set.of(State.FAILED, State.CANCELLED).contains(n)) {
                tasks.forEach(Task::cancel);
            }
        }));

        setOnShown(e -> tasks.forEach(executorService::execute));

        titleProperty().bind(titleExpression);

        final DialogPane dialogPane = getDialogPane();
        NodeUtil.installStyle(dialogPane.getStylesheets());

        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.FINISH);

        final Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.textProperty().bind(ts.translated("button.cancel"));

        final Button finishButton = (Button) dialogPane.lookupButton(ButtonType.FINISH);
        finishButton.textProperty().bind(ts.translated("button.done"));
        finishButton.disableProperty().bind(tasksDone.isNotEqualTo(tasks.size()));

        // General status
        final GridPane statusPane = new GridPane();
        statusPane.setHgap(10d);
        statusPane.setVgap(10d);
        statusPane.prefWidthProperty().bind(dialogPane.widthProperty());

        final Label taskLabel = new Label();
        taskLabel.textProperty().bind(ts.translated("taskListDialog.task"));
        statusPane.add(taskLabel, 0, 0);

        final Label taskTextLabel = new Label();
        taskTextLabel.textProperty().bind(titleExpression);
        statusPane.add(taskTextLabel, 1, 0);

        final Label statusLabel = new Label();
        statusLabel.textProperty().bind(ts.translated("taskListDialog.status"));
        statusPane.add(statusLabel, 0, 1);

        final TextField statusField = new TextField();
        statusField.textProperty().bind(statusExpression);
        statusField.setDisable(true);
        statusPane.add(statusField, 1, 1);
        finishButton.disabledProperty().addListener((obs, o, n) -> {
            if (!n) {
                statusField.textProperty().unbind();

                if (tasksSuccessful.get() == tasksDone.get()) {
                    statusField.textProperty().bind(ts.translated("task.done"));

                    setResultConverter(cb -> tasks.stream()
                            .map(TryUtil.wrapping(Task::get, RuntimeException::new))
                            .collect(Collectors.toList()));
                } else {
                    statusField.textProperty().bind(ts.translated("task.failed"));
                }
            }
        });

        final Label progressLabel = new Label();
        progressLabel.textProperty().bind(ts.translated("taskListDialog.progress"));
        statusPane.add(progressLabel, 0, 2);

        final ProgressBar progressBar = new ProgressBar(0d);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.progressProperty().bind(tasksSuccessful.divide((double) tasks.size()));
        statusPane.add(progressBar, 1, 2);

        final ColumnConstraints growColumn = new ColumnConstraints();
        growColumn.setHgrow(Priority.ALWAYS);
        statusPane.getColumnConstraints().addAll(new ColumnConstraints(), growColumn);

        // Running Task List
        final VBox taskBox = new VBox(10d);

        tasks.stream()
            .map(TaskProgressIndicator::new)
            .forEach(indicator -> {
                indicator.task.stateProperty().addListener((obs, o, n) -> {
                    if (n == State.SUCCEEDED) {
                        taskBox.getChildren().remove(indicator);
                    } else if (n == State.RUNNING) {
                        taskBox.getChildren().add(indicator);
                    }
                });
            });
            

        final ScrollPane scrollPane = new ScrollPane(taskBox);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        taskBox.prefWidthProperty().bind(scrollPane.widthProperty());

        final BorderedTitledPane tasksPane = new BorderedTitledPane(ts.translated("taskListDialog.runningTasks"), scrollPane);


        final VBox rootBox = new VBox(statusPane, tasksPane);
        VBox.setVgrow(tasksPane, Priority.ALWAYS);
        dialogPane.setContent(rootBox);

    }

    private synchronized void increaseTasksSuccessful() {
        tasksSuccessful.set(tasksSuccessful.get() + 1);
    }

    private synchronized void increaseTasksDone() {
        tasksDone.set(tasksDone.get() + 1);
    }

    private static final class TaskProgressIndicator extends VBox {

        private final Task<?> task;
        final ProgressBar bar;

        private TaskProgressIndicator(final Task<?> task) {
            this.task = task;
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

            task.stateProperty().addListener((obs, o, n) -> {
                if (n == State.FAILED) {
                    statusLabel.textProperty().unbind();

                    final Throwable t = task.getException();
                    if (t == null) {
                        statusLabel.textProperty().bind(ts.translated("task.failed"));
                    } else if (t instanceof TaskFailedException) {
                        statusLabel.textProperty().bind(((TaskFailedException) t).messageExpression());
                    } else {
                        statusLabel.setText(t.getLocalizedMessage());
                    }
                }

            });
        }
        
    }


    
}
