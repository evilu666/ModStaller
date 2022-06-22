package com.evilu.modstaller.ui.pane;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import java.io.File;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.ModPackRepository;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.ModPack;
import com.evilu.modstaller.task.CommonTasks;
import com.evilu.modstaller.ui.dialog.TaskListDialog;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * ModpackListPane
 */
public class ModpackListPane extends HBox implements TaskPane {

    private final TranslationService ts;
    private final ModPackRepository packRepo;

    private final TextField nameField;
    private final TextField versionField;

    private final ReadOnlyObjectWrapper<ModPack> selectedModPack;
    private final ObjectExpression<ExecutorService> executorProperty;

    public ModpackListPane() {
        super(10d);
        
        final ApplicationContext ctx = ApplicationContext.get();
        ts = ctx.getTranslationService();
        packRepo = ctx.getModPackRepository();
        selectedModPack = new ReadOnlyObjectWrapper<>(null);
        executorProperty = ctx.getConfig().getSettings().getExecutorServiceExpression();

        // ModPack List

        final ListView<ModPack> listView = new ListView<>(packRepo.getAll());
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        selectedModPack.bind(listView.getSelectionModel().selectedItemProperty());
        listView.setCellFactory(lw -> new ModPackListCell());


        // ModPack Details

        final GridPane detailsPane = new GridPane();
        detailsPane.setVgap(10d);
        detailsPane.setHgap(10d);

        final Label nameLabel = new Label();
        nameLabel.textProperty().bind(ts.translated("label.name"));
        detailsPane.add(nameLabel, 0, 0);

        nameField = new TextField();
        nameField.disableProperty().bind(selectedModPack.isNull());
        detailsPane.add(nameField, 1, 0);

        final Label versionLabel = new Label();
        versionLabel.textProperty().bind(ts.translated("label.version"));
        detailsPane.add(versionLabel, 0, 1);

        versionField = new TextField();
        versionField.disableProperty().bind(selectedModPack.isNull());
        detailsPane.add(versionField, 1, 1);

        // Details Action Buttons

        final HBox buttonBox = new HBox(10d);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

        final Button deleteButton = new Button();
        deleteButton.textProperty().bind(ts.translated("action.delete"));
        deleteButton.disableProperty().bind(selectedModPack.isNull());
        deleteButton.setOnAction(this::deleteModPack);

        final Button installButton = new Button();
        installButton.textProperty().bind(ts.translated("action.install"));
        installButton.disableProperty().bind(selectedModPack.isNull());
        installButton.setOnAction(this::installModPack);

        final Button saveButton = new Button();
        saveButton.textProperty().bind(ts.translated("action.save"));
        saveButton.disableProperty().bind(selectedModPack.isNull());
        saveButton.setOnAction(this::saveModPack);

        buttonBox.getChildren().addAll(deleteButton, installButton, saveButton);

        final VBox detailsBox = new VBox(10d);
        detailsBox.getChildren().addAll(detailsPane, buttonBox);
        VBox.setVgrow(detailsPane, Priority.ALWAYS);

        getChildren().addAll(listView, detailsBox);
        HBox.setHgrow(listView, Priority.ALWAYS);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        // Logic
        selectedModPack.addListener((obs, o, n) -> {
            if (n != null) {
                nameField.setText(n.getName());
                versionField.setText(n.getVersion().getVersionString());
            } else {
                nameField.setText("");
                versionField.setText("");
            }
        });

    }

	@Override
	public String getPaneId() {
      return "modPackList";
	}

	@Override
	public Node getContent() {
      return this;
	}

  @Override
  public int getOrder() {
      return 2;
  }

  public ReadOnlyObjectProperty<ModPack> selectedModPackProperty() {
      return selectedModPack.getReadOnlyProperty();
  }

  private void deleteModPack(final ActionEvent e) {
      if (selectedModPack.isNotNull().get()) {

      }
  }

  private void installModPack(final ActionEvent e) {
      if (selectedModPack.isNotNull().get()) {
          final ModPack pack = selectedModPack.getValue();
          final List<Task<File>> tasks = pack.getMods()
              .stream()
              .map(CommonTasks::installMod)
              .collect(Collectors.toList());
          final Dialog<List<File>> dialog = new TaskListDialog<>(ts.translated("task.installModPack.title", pack.getName()), ts.translated("task.installModPack.status"), tasks);
          dialog.getDialogPane().setPrefWidth(800d);
          dialog.getDialogPane().setPrefHeight(500d);
          dialog.show();
      }
  }

  private void saveModPack(final ActionEvent e) {
      if (selectedModPack.isNotNull().get()) {
          packRepo.save(selectedModPack.get());
      }
  }

  private class ModPackListCell extends ListCell<ModPack> {

      @Override
      protected void updateItem(final ModPack item, boolean empty) {
          super.updateItem(item, empty);

          if (!empty) {
              final GridPane pane = new GridPane();
              pane.setStyle("-fx-border-width: 2px; -fx-border-color: -dark;");
              pane.setPadding(new Insets(5d));
              pane.setHgap(10d);
              pane.setVgap(10d);

              final Label nameLabel = new Label();
              nameLabel.textProperty().bind(ts.translated("label.name"));
              pane.add(nameLabel, 0, 0);

              final Label nameValueLabel = new Label(item.getName());
              pane.add(nameValueLabel, 1, 0);

              final Label versionLabel = new Label();
              versionLabel.textProperty().bind(ts.translated("label.version"));
              pane.add(versionLabel, 0, 1);

              final Label versionValueLabel = new Label(item.getVersion().getVersionString());
              pane.add(versionValueLabel, 1, 1);

              setGraphic(pane);
              setPadding(new Insets(0d));
              setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
              prefWidthProperty().bind(pane.prefWidthProperty());
          } else {
              setGraphic(null);
              setContentDisplay(ContentDisplay.TEXT_ONLY);
          }

      }

  }

    
}
