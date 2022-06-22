package com.evilu.modstaller.ui.pane;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.ModPackRepository;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Mod;
import com.evilu.modstaller.model.ModPack;
import com.evilu.modstaller.task.CommonTasks;
import com.evilu.modstaller.ui.dialog.TaskDialogs;
import com.evilu.modstaller.ui.dialog.TaskListDialog;
import com.evilu.modstaller.ui.util.AlertUtil;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.util.FuncUtil;
import com.evilu.modstaller.util.StringUtil;
import com.evilu.modstaller.util.TryUtil;
import com.github.javafaker.Faker;
import com.google.inject.Inject;

import org.apache.commons.io.IOUtils;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * ModpackFileImportPane
 */
public class ModpackImportPane extends VBox implements TaskPane {

    private final TranslationService ts;
    private final ModPackRepository modPackRepo;
    private final Faker faker;

    private final TextField fileField;
    private final TextField urlField;
    private final RadioButton fileRadio;
    private final RadioButton urlRadio;
    private final TextField nameField;

    private final ObjectBinding<ExecutorService> executorProperty;


    public ModpackImportPane() {
        final ApplicationContext ctx = ApplicationContext.get();
        this.ts = ctx.getTranslationService();
        this.faker = ctx.getFaker();
        this.modPackRepo = ctx.getModPackRepository();

        executorProperty = BindingUtil.map(
            ctx.getConfig().getSettings().getMaxThreadsProperty(),
            FuncUtil.<Number, Integer, ExecutorService>compose(Number::intValue, Executors::newFixedThreadPool),
            (Supplier<ExecutorService>) Executors::newSingleThreadExecutor
        );

        final GridPane gridPane = new GridPane();
        gridPane.setHgap(10d);
        gridPane.setVgap(10d);
        int cRow = 0;

        // Name
        final Label nameLabel = new Label();
        nameLabel.textProperty().bind(ts.translated("label.name"));

        gridPane.add(nameLabel, 0, cRow);

        nameField = new TextField(faker.company().name());
        gridPane.add(nameField, 1, cRow++);

        // Import from File
        fileRadio = new RadioButton();
        fileRadio.textProperty().bind(ts.translated("taskPane.modPackImport.importFromFile"));
        gridPane.add(fileRadio, 0, cRow);

        fileField = new TextField();
        final Button fileImportButton = new Button();
        fileImportButton.textProperty().bind(ts.translated("action.selectFile"));
        fileImportButton.setOnAction(e -> selectFile());
        final HBox fileImportBox = new HBox(10d);
        fileImportBox.getChildren().addAll(fileField, fileImportButton);
        HBox.setHgrow(fileField, Priority.ALWAYS);
        gridPane.add(fileImportBox, 1, cRow++);

        // Import from URL
        urlRadio = new RadioButton();
        urlRadio.textProperty().bind(ts.translated("taskPane.modPackImport.importFromURL"));
        gridPane.add(urlRadio, 0, cRow);

        urlField = new TextField();
        gridPane.add(urlField, 1, cRow++);

        // ToggleGroup
        final ToggleGroup importToggleGroup = new ToggleGroup();
        fileRadio.setToggleGroup(importToggleGroup);
        urlRadio.setToggleGroup(importToggleGroup);
        importToggleGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            final boolean isFile = fileRadio.isSelected();
            fileField.setDisable(!isFile);
            fileField.setEditable(isFile);
            fileImportButton.setDisable(!isFile);

            urlField.setEditable(!isFile);
            urlField.setDisable(isFile);
        });

        fileRadio.setSelected(true);

        // Import Button
        final Button importButton = new Button();
        importButton.textProperty().bind(ts.translated("action.import"));
        importButton.setOnAction(e -> startImport());

        final HBox buttonBox = new HBox(10d);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().add(importButton);

        getChildren().addAll(gridPane, buttonBox);
        VBox.setVgrow(gridPane, Priority.ALWAYS);
    }

    private void selectFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.titleProperty().bind(ts.translated("taskPane.modPackImport.fileChooser.title"));
        fileChooser.setSelectedExtensionFilter(new ExtensionFilter(ts.translateOnce("taskPane.modPackImport.fileChooser.extensionName"), ".txt"));

        Optional.ofNullable(fileChooser.showOpenDialog(getScene().getWindow()))
            .map(File::getAbsolutePath)
            .ifPresent(fileField::setText);
    }

    private void startImport() {
        if (fileRadio.isSelected()) {
            final File modFile = new File(fileField.getText());
            if (!modFile.exists()) {
                AlertUtil.showError("error.noFileSelected");
            } else {

                try {
                    final List<Task<Mod>> tasks = IOUtils.readLines(new FileInputStream(modFile), StandardCharsets.UTF_8).stream()
                        .map(TryUtil.wrapping(URL::new, RuntimeException::new)) //TODO: Proper exception handling
                        .map(CommonTasks::importMod)
                        .collect(Collectors.toList());
                    final Dialog<List<Mod>> dialog = new TaskListDialog<>(ts.translated("task.importModPack.title"), ts.translated("task.importModPack.msg", nameField.getText()), tasks);
                    dialog.showAndWait()
                        .ifPresent(mods -> TaskDialogs.create(CommonTasks.createModPack(nameField.getText(), mods)).showAndWait()); 
                } catch (final Throwable e) {
                    AlertUtil.showError("error.unknown", e.getMessage());
                }
            }
        }
    }

    public String getPaneId() {
        return "modPackImport";
    }

    public Node getContent() {
        return this;
    }

    
    @Override
    public int getOrder() {
        return 1;
    }



    
}
