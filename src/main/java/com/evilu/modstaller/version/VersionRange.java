package com.evilu.modstaller.version;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * VersionRange
 */
public interface VersionRange {

    public String getVersionString();

    public boolean includes(final Version version);

    default boolean excludes(final Version version) {
        return !includes(version);
    }

    @JsonCreator
    public static VersionRange of( final String versionRange) {
        return VersionParser.parse(versionRange);
    }

    
}
