package com.evilu.modstaller.version;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * BoundVersionRange
 */
@Getter
public class BoundVersionRange implements VersionRange {


    @JsonValue
    private final String versionString;

    private final Version startVersion, endVersion;
    private final boolean isStartInclusive, isEndInclusive;

    public BoundVersionRange(final String versionString, final Version startVersion, final Version endVersion, final boolean isStartInclusive, final boolean isEndInclusive) {
        this.versionString = versionString;
        this.startVersion = Objects.requireNonNullElse(startVersion, Version.MIN);
        this.endVersion = Objects.requireNonNullElse(endVersion, Version.MAX);
        this.isStartInclusive = isStartInclusive;
        this.isEndInclusive = isEndInclusive;
    }

    @Override
    public boolean includes(final Version version) {
        final int startCmp = version.compareTo(startVersion);
        final int endCmp = version.compareTo(endVersion);

        if (isStartInclusive) {
            if (startCmp < 0) return false;
        } else {
            if (startCmp <= 0) return false;
        }

        if (isEndInclusive) {
            if (endCmp > 0) return false;
        } else {
            if (endCmp >= 0) return false;
        }

        return true;
    }

}
