package com.evilu.modstaller.ui.util;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

/**
 * AlertUtil
 */
public interface AlertUtil {

    public static void showError(final String msgProperty, final Object... values) {
        final TranslationService translationService = ApplicationContext.get().getTranslationService();
        final Alert alert = new Alert(AlertType.ERROR, translationService.translate(msgProperty, values), ButtonType.OK);
        alert.setTitle(translationService.translate("error.title"));
        alert.showAndWait();
    }
    
}
