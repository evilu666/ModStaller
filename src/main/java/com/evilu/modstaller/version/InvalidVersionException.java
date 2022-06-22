package com.evilu.modstaller.version;

/**
 * InvalidVersionException
 */
public class InvalidVersionException extends IllegalStateException {

    public InvalidVersionException(final String versionString) {
        super("Invalid version string: " + versionString);
    }

    public InvalidVersionException(final String versionString, final String parserError) {
        super(String.format("Invalid version string '%s': %s", versionString, parserError));
    }

    
}
