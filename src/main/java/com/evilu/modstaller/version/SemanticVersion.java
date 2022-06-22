package com.evilu.modstaller.version;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Version
 */
@RequiredArgsConstructor
@Getter
@Setter
public class SemanticVersion implements Version {

    @JsonValue
    private final String versionString;

    private final int major, minor, patch, hotfix;
    private final String buildInfo;

    @Override
    public boolean includes(Version version) {
        return compareTo(version) == 0;

    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Version version)
            return includes(version);
        return false;
    }

    @Override
    public int compareTo(final Version version) {
        if (version instanceof SemanticVersion v) {
            if (major > v.major) {
                return 1;
            } else if (major < v.major) {
                return -1;
            } else {
                if (minor > v.minor) {
                    return 1;
                } else if (minor < v.minor) {
                    return -1;
                } else {
                    if (patch > v.patch) {
                        return 1;
                    } else if (patch < v.patch) {
                        return -1;
                    } else {
                        if (hotfix > v.hotfix) {
                            return 1;
                        } else if (hotfix < v.hotfix) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }
            }
        }

        return versionString.compareTo(version.getVersionString());
    }

    @Override
    public String toString() {
        return versionString;
    }

}
