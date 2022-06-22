package com.evilu.modstaller.ui.list;

import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Mod;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

/**
 * ModListView
 */
public class ModListView extends ListView<Mod> {

    public ModListView(final TranslationService ts, final ObservableList<Mod> mods) {
        super(mods);
        setCellFactory(view -> {
            return new ListCell<>() {

                @Override
                protected void updateItem(final Mod item, final boolean isEmpty) {
                    super.updateItem(item, isEmpty);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                    if (!isEmpty) {
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

                        final Label versionValueLabel = new Label(item.getVersion().toString());
                        pane.add(versionValueLabel, 1, 1);

                        setGraphic(pane);
                        setPadding(new Insets(0d));
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        prefWidthProperty().bind(pane.prefWidthProperty());
                    } else {
                        setGraphic(null);
                    }
                }

            };
        });
    }


    
}
