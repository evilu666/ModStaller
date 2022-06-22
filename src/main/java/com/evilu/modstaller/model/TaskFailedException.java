package com.evilu.modstaller.model;

import javafx.beans.binding.StringExpression;

/**
 * TaskFailedException
 */
public class TaskFailedException extends RuntimeException {

    private final StringExpression message;

    public TaskFailedException(final StringExpression message, final Throwable cause) {
        super(message.getValueSafe(), cause);
        this.message = message;
    }

    public StringExpression messageExpression() {
        return message;
    }

    
}
