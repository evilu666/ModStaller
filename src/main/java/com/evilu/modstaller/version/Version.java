package com.evilu.modstaller.version;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Version
 */
public interface Version extends VersionRange, Comparable<Version> {

    public static final Version MIN = new Version() {

        @Override
        public String getVersionString() {
            return "<MIN>";
        }

        @Override
        public boolean includes(final Version version) {
            return version == this;
        }

        @Override
        public int compareTo(final Version other) {
            if (other == this) return 0;
            return -1;
        }

    };

    public static final Version MAX = new Version() {

        @Override
        public String getVersionString() {
            return "<MAX>";
        }

        @Override
        public boolean includes(Version version) {
            return version == this;
        }

        @Override
        public int compareTo(final Version other) {
            if (other == this) return 0;
            return 1;
        }

    };

    @JsonCreator
    public static Version of(final String versionString) {

        try {
            return VersionParser.parseVersion(versionString);
        } catch (final InvalidVersionException e) {
            return new SimpleVersion(versionString);
        }
    }

    public boolean equals(final Object other);

    public String toString();

}
