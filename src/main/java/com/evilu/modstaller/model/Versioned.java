package com.evilu.modstaller.model;

import com.evilu.modstaller.version.VersionRange;

/**
 * Versioned
 */
public interface Versioned {

    public VersionRange getVersion();
    
}
