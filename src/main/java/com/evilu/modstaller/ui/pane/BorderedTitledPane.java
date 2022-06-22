package com.evilu.modstaller.ui.pane;

import com.evilu.modstaller.ui.util.BindingUtil;

import javafx.beans.binding.StringExpression;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class BorderedTitledPane extends StackPane {

    public BorderedTitledPane(final StringExpression titleExpression, final Node content) {
        final Label title = new Label();
        title.textProperty().bind(BindingUtil.mapString(titleExpression, s -> String.format(" %s ", s), ""));
        title.setFont(new Font(28d));
        title.getStyleClass().add("bordered-titled-title");
        StackPane.setAlignment(title, Pos.TOP_CENTER);

        final StackPane contentPane = new StackPane();
        content.getStyleClass().add("bordered-titled-content");
        contentPane.getChildren().add(content);

        getStyleClass().add("bordered-titled-border");
        getChildren().addAll(contentPane, title);
    }
}
