package com.evilu.modstaller.constant;

import com.evilu.modstaller.core.ApplicationContext;

import javafx.beans.binding.StringExpression;

/**
 * Language
 */
public enum Language implements Nameable {

    EN, DE;

    public StringExpression getDisplayName() {
        return ApplicationContext.get().getTranslationService().translated("language." + name().toLowerCase());
    }
    
}
