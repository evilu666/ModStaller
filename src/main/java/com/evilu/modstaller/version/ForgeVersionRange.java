package com.evilu.modstaller.version;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * ForgeVersionRange
 */
@RequiredArgsConstructor
@Getter
public class ForgeVersionRange implements VersionRange {

    @JsonValue
    private final String versionString;

    private final PrefixVersionRange forgeRange, minecraftRange;

    @NonNull
    private final PrefixVersionRange modRange;

    @Override
    public boolean includes(final Version version) {
        if (version instanceof SemanticVersion) {
            return modRange.includes(version);
        } else if (version instanceof ForgeVersion) {
            final ForgeVersion v = (ForgeVersion) version;

            if (v.getMinecraftVersion() != null && minecraftRange != null) {
                if (minecraftRange.excludes(v.getMinecraftVersion())) return false;
            }

            if (v.getForgeVersion() != null && forgeRange != null) {
                if (forgeRange.excludes(v.getForgeVersion()))  return false;
            }

            return modRange.includes(v.getModVersion());
        }
        return false;
    }
}
