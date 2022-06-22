package com.evilu.modstaller.version;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SimpleVersion
 */
@RequiredArgsConstructor
@Getter
public class SimpleVersion implements Version {

    @JsonValue
    private final String versionString;

    @Override
    public boolean includes(Version version) {
        return versionString.equals(version.getVersionString());
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Version version) return includes(version);
        return false;
    }

    @Override
    public int compareTo(final Version version) {
        if (version instanceof SimpleVersion) {
            return includes(version) ? 0 : versionString.compareTo(version.getVersionString());
        }

        return versionString.compareTo(version.getVersionString());
    }

    @Override
    public String toString() {
        return versionString;
    }

}
