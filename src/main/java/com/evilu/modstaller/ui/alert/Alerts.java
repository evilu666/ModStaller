package com.evilu.modstaller.ui.alert;

import javafx.beans.binding.StringExpression;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

/**
 * Alerts
 */
public interface Alerts {

    public static Alert info(final StringExpression titleExpression, final StringExpression textExpression) {
        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.getButtonTypes().add(ButtonType.OK);
        alert.titleProperty().bind(titleExpression);
        alert.contentTextProperty().bind(textExpression);
        return alert;
    }
    
}
