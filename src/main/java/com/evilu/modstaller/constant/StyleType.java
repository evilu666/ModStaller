package com.evilu.modstaller.constant;

import com.evilu.modstaller.core.ApplicationContext;

import javafx.beans.binding.StringExpression;

/**
 * StyleType
 */
public enum StyleType implements Nameable {

    DEFAULT,
    M1L4N,
    LU154,
    H4Z3;

    public String getStylesheet() {
        return String.format("styles/%s.css", name().toLowerCase());
    }

    public StringExpression getDisplayName() {
        return ApplicationContext.get().getTranslationService().translated("styleType." + name());
    }
    
}
