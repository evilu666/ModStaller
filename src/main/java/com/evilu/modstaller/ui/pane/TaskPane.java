package com.evilu.modstaller.ui.pane;

import com.evilu.modstaller.core.ApplicationContext;

import javafx.beans.binding.StringExpression;
import javafx.scene.Node;
import javafx.scene.image.Image;

import static com.evilu.modstaller.util.StringUtil.$f;

/**
 * TaskPane
 */
public interface TaskPane {

    public String getPaneId();

    public Node getContent();

    public int getOrder();

    default Image getIcon() {
        return new Image(getClass().getResourceAsStream($f("/icons/%s.png", getPaneId())));
    }

    default StringExpression titleProperty() {
        return ApplicationContext.get().getTranslationService().translated($f("taskPane.%s.title", getPaneId()));
    }

    default StringExpression tooltipProperty() {
        return ApplicationContext.get().getTranslationService().translated($f("taskPane.%s.tooltip", getPaneId()));
    }
}
