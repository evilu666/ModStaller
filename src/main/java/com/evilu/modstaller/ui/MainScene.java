package com.evilu.modstaller.ui;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Settings;
import com.evilu.modstaller.ui.pane.ModListPane;
import com.evilu.modstaller.ui.pane.ModpackImportPane;
import com.evilu.modstaller.ui.pane.ModpackListPane;
import com.evilu.modstaller.ui.pane.SettingsPane;
import com.evilu.modstaller.ui.pane.TaskPane;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.ui.util.NodeUtil;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * MainWindow
 */
public class MainScene extends Scene {

    private final TranslationService ts;

    private final List<TaskPane> panes;
    private final Map<TaskPane, Node> menuTiles;

    private final StackPane contentPane = new StackPane();

    private final ReadOnlyObjectWrapper<TaskPane> selectedPaneWrapper;

    public MainScene() {
        super(new HBox(0d));

        NodeUtil.installStyle(getStylesheets());

        final ApplicationContext ctx = ApplicationContext.get();
        ts = ctx.getTranslationService();
        final Settings settings = ctx.getConfig().getSettings();



        final ModpackListPane modpackListPane = new ModpackListPane();
        final ModpackImportPane modpackImportPane = new ModpackImportPane();
        final ModListPane modListPane = new ModListPane();
        final SettingsPane settingsPane = new SettingsPane();

    panes = List.of(modpackImportPane, modpackListPane, modListPane, settingsPane);

    // Side Menu
    final VBox menuBox = new VBox(25d);
    menuBox.setPadding(new Insets(5d));
    menuBox.setMaxWidth(256);

    menuTiles = panes.stream()
        .sorted(Comparator.comparingInt(TaskPane::getOrder))
        .collect(Collectors.toMap(Function.identity(), this::createMenuTile, (a, b) -> a, LinkedHashMap::new));
    menuBox.getChildren().addAll(menuTiles.values());
    selectedPaneWrapper = new ReadOnlyObjectWrapper<>();
    showPane(modpackImportPane);

    final ScrollPane menuScrollPane = new ScrollPane(menuBox);
    menuScrollPane.setStyle("-fx-background-color: transparent;");
    menuScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
    menuScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);

    // Content
    final HBox titleBox = new HBox();
    titleBox.setAlignment(Pos.CENTER);

    final Label titleLabel = new Label();
    titleLabel.textProperty().bind(BindingUtil.mapObservable(selectedPaneWrapper.getReadOnlyProperty(), TaskPane::titleProperty).asString());
    titleLabel.setFont(new Font(32d));
    titleBox.getChildren().add(titleLabel);

    final VBox contentBox = new VBox(10d);
    contentBox.setPadding(new Insets(10d));
    contentBox.getChildren().addAll(titleBox, contentPane);
    VBox.setVgrow(contentPane, Priority.ALWAYS);

    final HBox rootBox = (HBox) getRoot();
    rootBox.getChildren().addAll(menuScrollPane, contentBox);
    HBox.setHgrow(contentBox, Priority.ALWAYS);
    setRoot(rootBox);
}

private Node createMenuTile(final TaskPane pane) {

  final ImageView iv = new ImageView(pane.getIcon());
  iv.fitHeightProperty().bind(iv.fitWidthProperty());
  iv.setFitWidth(128);

  final Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(pane.tooltipProperty());

      final Button button = new Button();
      button.textProperty().bind(pane.titleProperty());
      button.setTooltip(tooltip);
      button.setGraphic(iv);
      button.getStyleClass().add("menu-tile");
      button.setOnAction(ev -> showPane(pane));
      button.setContentDisplay(ContentDisplay.TOP);

      return button;
  }

  private void showPane(final TaskPane pane) {
      final Node node = menuTiles.get(pane);
      menuTiles.values().forEach(tile -> tile.getStyleClass().remove("selected"));
      node.getStyleClass().add("selected");
      contentPane.getChildren().clear();
      contentPane.getChildren().add(pane.getContent());
      selectedPaneWrapper.setValue(pane);
  }

}
