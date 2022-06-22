package com.evilu.modstaller.version;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * ForgeVersion
 */
@RequiredArgsConstructor
@Getter
public class ForgeVersion implements Version {

    @JsonValue
    private final String versionString;

    private final SemanticVersion forgeVersion;
    @NonNull
    private final SemanticVersion modVersion;
    private final SemanticVersion minecraftVersion;

    public boolean hasForgeVersion() {
        return forgeVersion != null;
    }

    public boolean hasMinecraftVersion() {
        return minecraftVersion != null;
    }

    @Override
    public boolean includes(final Version version) {
        Objects.requireNonNull(version, "Version must not be null!");

        if (version instanceof SemanticVersion) {
            return modVersion.includes(version);
        } else if (version instanceof ForgeVersion) {
            final ForgeVersion v = (ForgeVersion) version;

            if (v.forgeVersion == null && forgeVersion != null || forgeVersion == null && v.forgeVersion != null) {
                return false;
            } else if (forgeVersion != null && !forgeVersion.includes(v.forgeVersion)) {
                return false;
            }

            if (v.minecraftVersion == null && minecraftVersion != null || minecraftVersion == null && v.minecraftVersion != null) {
                return false;
            } else if (minecraftVersion != null && !minecraftVersion.includes(v.minecraftVersion)) {
                return false;
            }

            return modVersion.includes(v.modVersion);

        }

        return false;
    }

    @Override
    public int compareTo(final Version version) {
        if (version instanceof SemanticVersion) {
            return modVersion.compareTo(version);
        } else if (version instanceof ForgeVersion) {
            final ForgeVersion v = (ForgeVersion) version;
            if (forgeVersion != null && v.forgeVersion != null) {
                final int forgeCmp = forgeVersion.compareTo(v.forgeVersion);
                if (forgeCmp != 0)
                    return forgeCmp;
            }

            if (minecraftVersion != null && v.minecraftVersion != null) {
                final int mcCmp = minecraftVersion.compareTo(v.minecraftVersion);
                if (mcCmp != 0)
                    return mcCmp;
            }

            return modVersion.compareTo(v.modVersion);
        }

        return versionString.compareTo(version.getVersionString());
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Version version)
            return includes(version);
        return false;
    }

    @Override
    public String toString() {
        return versionString;
    }

}
