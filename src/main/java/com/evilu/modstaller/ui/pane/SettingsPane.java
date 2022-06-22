package com.evilu.modstaller.ui.pane;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import java.io.File;

import com.evilu.modstaller.constant.Language;
import com.evilu.modstaller.constant.Nameable;
import com.evilu.modstaller.constant.StyleType;
import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Settings;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.ui.util.NodeUtil;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.synedra.validatorfx.TooltipWrapper;
import net.synedra.validatorfx.Validator;
import net.synedra.validatorfx.Check.Context;

/**
 * SettingsPane
 */
public class SettingsPane extends VBox implements TaskPane {

    private final TranslationService ts;
    private final Settings settings;

    private final Validator validator = new Validator();

    public SettingsPane() {
        super(10d);

        final ApplicationContext ctx = ApplicationContext.get();
        ts = ctx.getTranslationService();
        settings = ctx.getConfig().getSettings();

        final GridPane pane = new GridPane();
        pane.setHgap(10d);
        pane.setVgap(10d);

        int rowIndex = 0;

        rowIndex = addGroup("settings.group.interface", pane, rowIndex);
        rowIndex = addEnumSetting(pane, rowIndex, "language", Language.class, settings.getLanguageProperty());
        rowIndex = addEnumSetting(pane, rowIndex, "styleType", StyleType.class, settings.getStyleTypeProperty());

        rowIndex = addGroup("settings.group.minecraft", pane, rowIndex);
        rowIndex = addFileSetting(pane, rowIndex, "minecraftPath", settings.getMinecraftPathProperty(), true, false, true);

        rowIndex = addGroup("settings.group.config", pane, rowIndex);
        rowIndex = addFileSetting(pane, rowIndex, "repoPath", settings.getModRepoPathProperty(), false, true, true);
        rowIndex = addIntSetting(pane, rowIndex, "maxThreads", settings.getMaxThreadsProperty(), 0, 32);

        pane.getColumnConstraints().addAll(
            NodeUtil.defaultConstraint(),
            NodeUtil.growColumnConstraints()
        );

        final Button saveButton = new Button();
        saveButton.textProperty().bind(ts.translated("action.save"));
        final TooltipWrapper<Button> saveTooltip = new TooltipWrapper<>(
                saveButton,
                validator.containsErrorsProperty(),
                validator.createStringBinding()
        );

        saveButton.setOnAction((e -> ctx.saveConfig()));

        final HBox buttonBox = new HBox(saveButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

        getChildren().addAll(pane, buttonBox);
        VBox.setVgrow(pane, Priority.ALWAYS);
    }

	@Override
	public String getPaneId() {
      return "settings";
	}

	@Override
	public Node getContent() {
		return this;
	}

	@Override
	public int getOrder() {
      return 99;
	}

  private int addGroup(final String nameKey, final GridPane pane, final int rowIndex) {
      final Label label = new Label();
      label.textProperty().bind(ts.translated(nameKey));
      label.setFont(new Font(20d));

      final HBox titleBox = new HBox(label);
      titleBox.setAlignment(Pos.CENTER);

      int newRowIndex = rowIndex;

      pane.add(titleBox, 0, newRowIndex++, 2, 1);
      pane.add(new Separator(Orientation.HORIZONTAL), 0, newRowIndex++, 2, 1);

      return newRowIndex;
  }

  private <T extends Enum<T> & Nameable> int addEnumSetting(final GridPane pane, final int rowIndex, final String nameKey, final Class<T> enumClass, final ObjectProperty<T> selectedValue) {
      final List<T> values = Arrays.asList(enumClass.getEnumConstants());
      final Label label = new Label();
      label.textProperty().bind(ts.translated(String.format("settings.label.%s", nameKey)));

      final ComboBox<T> box = new ComboBox<>(FXCollections.observableList(values));
      box.setCellFactory(lw -> new ListCell<>() {
          protected void updateItem(final T item, boolean empty) {
              super.updateItem(item, empty);

              if (empty) {
                  textProperty().unbind();
              } else {
                  textProperty().bind(item.getDisplayName());
              }
          };
      });

      box.getSelectionModel().select(selectedValue.get());
      selectedValue.bind(box.getSelectionModel().selectedItemProperty());

      final Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(ts.translated("settings.tooltip." + nameKey));
      Tooltip.install(label, tooltip);
      Tooltip.install(box, tooltip);

      pane.addRow(rowIndex, label, box);

      return rowIndex + 1;
  }

  private int addIntSetting(final GridPane pane, final int rowIndex, final String nameKey, final IntegerProperty currentValue, final Integer min, final Integer max) {
      final Label label = new Label();
      label.textProperty().bind(ts.translated(String.format("settings.label.%s", nameKey)));

      final TextField field = new TextField(currentValue.get()+"");
      validator.createCheck()
          .dependsOn(nameKey, field.textProperty())
          .withMethod(change -> {
              try {
                  final int value = Integer.parseInt(change.get(nameKey));
                  if (min != null && value < min) {
                      change.error(ts.translateOnce("validator.integer.tooSmall"));
                  } else if (max != null && value > max) {
                      change.error(ts.translateOnce("validator.integer.tooBig"));
                  } else {
                      currentValue.set(value);
                  }
              } catch (final NumberFormatException e) {
                  change.error(ts.translateOnce("validator.integer.invalid"));
              }
          })
      .decoratingWith(NodeUtil::createValidationDecoration)
      .decorates(field)
      .immediate();

      final Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(ts.translated("settings.tooltip." + nameKey));
      Tooltip.install(label, tooltip);
      //Tooltip.install(field, tooltip);

      pane.addRow(rowIndex, label, field);

      return rowIndex + 1;
  }

  private int addStringSetting(final GridPane pane, final int rowIndex, final String nameKey, final StringProperty currentValue, final BiFunction<String, Context, Boolean> fieldValidator) {
      final Label label = new Label();
      label.textProperty().bind(ts.translated(String.format("settings.label.%s", nameKey)));

      final TextField field = new TextField(currentValue.get());
      validator.createCheck()
          .dependsOn(nameKey, field.textProperty())
          .withMethod(change -> {
              final String value = change.get(nameKey);
              final boolean valid = fieldValidator.apply(value, change);
              if (valid) currentValue.set(value);
          })
      .decoratingWith(NodeUtil::createValidationDecoration)
      .decorates(field)
      .immediate();

      final Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(ts.translated("settings.tooltip." + nameKey));
      Tooltip.install(label, tooltip);
      //Tooltip.install(field, tooltip);

      pane.addRow(rowIndex, label, field);
      return rowIndex + 1;
  }

  private int addFileSetting(final GridPane pane, final int rowIndex, final String nameKey, final StringProperty currentValue, final boolean errorNotExists, final boolean warnNotExists, final boolean selectFolder) {
      final Label label = new Label();
      label.textProperty().bind(ts.translated(String.format("settings.label.%s", nameKey)));

      final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>(new File(currentValue.get()));

      final TextField field = new TextField(currentValue.get());
      field.textProperty().addListener((obs, o, n) -> {
          if (n != null) selectedFile.set(new File(n));
      });

      final Button button = new Button();
      button.setOnAction(e -> {
          final File result;
          if (selectFolder) {
              final DirectoryChooser chooser = new DirectoryChooser();
              chooser.setInitialDirectory(new File(currentValue.get()));
              chooser.titleProperty().bind(ts.translatedExpression("action.selectNamedFile", label.textProperty()));
              result = chooser.showDialog(getScene().getWindow());
          } else {
              final FileChooser chooser = new FileChooser();
              chooser.setInitialDirectory(new File(currentValue.get()));
              chooser.titleProperty().bind(ts.translatedExpression("action.selectNamedFile", label.textProperty()));
              result = chooser.showOpenDialog(getScene().getWindow());
          }

          selectedFile.set(result);
      });
      button.textProperty().bind(ts.translated("action.selectFile"));

      validator.createCheck()
          .dependsOn(nameKey, field.textProperty())
          .withMethod(change -> {
              final File file = selectedFile.get();

              if (!file.exists()) {
                  if (errorNotExists) {
                      change.error(ts.translateOnce(selectFolder ? "validator.file.errorFolderNotExists" : "validator.file.errorFileNotExists"));
                      return;
                  } else if (warnNotExists) {
                      change.warn(ts.translateOnce("validator.file.warnNotExists"));
                      return;
                  }
              }

              currentValue.set(file.getAbsolutePath());
          })
      .decoratingWith(NodeUtil::createValidationDecoration)
      .decorates(field)
      .immediate();

      final Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(ts.translated("settings.tooltip." + nameKey));
      Tooltip.install(label, tooltip);
      //Tooltip.install(field, tooltip);
      //Tooltip.install(button, tooltip);

      final HBox box = new HBox(field, button);
      box.setSpacing(10d);
      HBox.setHgrow(field, Priority.ALWAYS);

      pane.addRow(rowIndex, label, box);

      return rowIndex + 1;
  }

    
}
