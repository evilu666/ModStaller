package com.evilu.modstaller.ui.pane;

import java.util.Objects;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.ModRepository;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Mod;
import com.evilu.modstaller.task.CommonTasks;
import com.evilu.modstaller.ui.dialog.TaskDialogs;
import com.evilu.modstaller.ui.list.ModListView;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.util.FuncUtil;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * ModListPane
 */
public class ModListPane extends HBox implements TaskPane {

	private final TranslationService ts;
	private final ModRepository repo;
	private final ReadOnlyObjectWrapper<Mod> selectedModProperty = new ReadOnlyObjectWrapper<>();

	public ModListPane() {
		super(10d);

		final ApplicationContext ctx = ApplicationContext.get();
		ts = ctx.getTranslationService();

		repo = ctx.getModRepository();

		// Mod List
		final ModListView listView = new ModListView(ts, repo.getAll());
		selectedModProperty.bind(listView.getSelectionModel().selectedItemProperty());


		final GridPane detailsPane = new GridPane();
		detailsPane.setVgap(10d);
		detailsPane.setHgap(10d);

		final Label nameLabel = new Label();
		nameLabel.textProperty().bind(ts.translated("label.name"));
		detailsPane.add(nameLabel, 0, 0);

		final TextField nameField = new TextField();
		nameLabel.setDisable(true);
		detailsPane.add(nameField, 1, 0);

		final Label versionLabel = new Label();
		versionLabel.textProperty().bind(ts.translated("label.version"));
		detailsPane.add(versionLabel, 0, 1);

		final TextField versionField = new TextField();
		versionField.setDisable(true);
		detailsPane.add(versionField, 1, 1);

		//final ModListView dependencies = new ModListView(ts, BindingUtil.map(listView.getSelectionModel().selectedItemProperty(), Mod::getDependencies))

		// Details Action Buttons
		final HBox buttonBox = new HBox(10d);
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

		final Button deleteButton = new Button();
		deleteButton.textProperty().bind(ts.translated("action.delete"));
		deleteButton.disableProperty().bind(selectedModProperty.isNull());
		deleteButton.setOnAction(this::deleteMod);

		final Button installButton = new Button();
		installButton.textProperty().bind(ts.translated("action.install"));
		installButton.disableProperty().bind(selectedModProperty.isNull());
		installButton.setOnAction(this::installMod);

		buttonBox.getChildren().addAll(deleteButton, installButton);

		final VBox detailsBox = new VBox(10d);
		detailsBox.getChildren().addAll(detailsPane, buttonBox);
		VBox.setVgrow(detailsPane, Priority.ALWAYS);

		getChildren().addAll(listView, detailsBox);
		HBox.setHgrow(listView, Priority.ALWAYS);
		HBox.setHgrow(detailsBox, Priority.ALWAYS);

		selectedModProperty.addListener((obs, o, n) -> {
			if (Objects.nonNull(n)) {
				nameField.setText(Objects.requireNonNullElse(n.getDisplayName(), n.getName()));
				versionField.setText(n.getVersion().toString());
			}
		});
	}

	private void deleteMod(final ActionEvent e) {
		if (selectedModProperty.isNotNull().get()) {
			final Mod mod = selectedModProperty.get();
			repo.delete(mod);
		}
	}

	private void installMod(final ActionEvent e) {
		if (selectedModProperty.isNotNull().get()) {
			final Mod mod = selectedModProperty.get();
			TaskDialogs.create(CommonTasks.installMod(mod))
				.show();
		}
	}


	@Override
	public String getPaneId() {
      return "modList";
	}

	@Override
	public Node getContent() {
		return this;
	}

	@Override
	public int getOrder() {
		return 3;
	}

    
}
