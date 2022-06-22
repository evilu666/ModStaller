package com.evilu.modstaller.ui.util;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.model.Settings;

import afester.javafx.svg.SvgLoader;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import net.synedra.validatorfx.Decoration;
import net.synedra.validatorfx.GraphicDecoration;
import net.synedra.validatorfx.Severity;
import net.synedra.validatorfx.ValidationMessage;

/**
 * NodeUtil
 */
public abstract class NodeUtil {

    public static ColumnConstraints defaultConstraint() {
        return new ColumnConstraints();
    }

    public static ColumnConstraints growColumnConstraints() {
        final ColumnConstraints cons = new ColumnConstraints();
        cons.setHgrow(Priority.ALWAYS);
        return cons;
    }

    public static void installStyle(final ObservableList<String> styles) {
        styles.add("style.css");

        final Settings settings = ApplicationContext.get().getConfig().getSettings();
        styles.add(settings.getStyleType().getStylesheet());

        settings.getStyleTypeProperty().addListener((obs, o, n) -> {
            styles.remove(o.getStylesheet());
            styles.add(n.getStylesheet());
        });
    }

    public static Decoration createValidationDecoration(final ValidationMessage msg) {
        final Label label = new Label();
        label.setGraphic(msg.getSeverity() == Severity.ERROR ? createErrorNode() : createWarningNode());
        label.getStyleClass().add("validation-icon");
        label.getStyleClass().add("validation-icon-" + msg.getSeverity().name().toLowerCase());
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(2.5d, 35d, 2.5d, 2.5));

        final Tooltip tt = new Tooltip(msg.getText());
        tt.setAutoFix(true);
        tt.getStyleClass().add("validation-tooltip");
        tt.getStyleClass().add("validation-tooltip-" + msg.getSeverity().name().toLowerCase());
        label.setTooltip(tt);

        return new GraphicDecoration(label, Pos.CENTER_RIGHT);
    }

    private static Node createErrorNode() {
        final Group group = svgLoader.loadSvg(NodeUtil.class.getClassLoader().getResourceAsStream("icons/error.svg"));
        group.maxHeight(validationIconSize);
        group.maxWidth(validationIconSize);

        final StackPane pane = new StackPane(group);
        pane.getStyleClass().add("error-icon");
        return pane;
    }

    private static Node createWarningNode() {
        final Group group = svgLoader.loadSvg(NodeUtil.class.getClassLoader().getResourceAsStream("icons/error.svg"));
        group.maxHeight(validationIconSize);
        group.maxWidth(validationIconSize);

        final StackPane pane = new StackPane(group);
        pane.getStyleClass().add("warning-icon");
        return pane;
    }


    private static final SvgLoader svgLoader = new SvgLoader();


    private static final double validationIconSize = 20d;
    private static final Image errorImage = new Image(NodeUtil.class.getClassLoader().getResourceAsStream("icons/error.png"), validationIconSize, validationIconSize, true, true);
    private static final Image warningImage = new Image(NodeUtil.class.getClassLoader().getResourceAsStream("icons/warning.png"), validationIconSize, validationIconSize, true, true);

}
