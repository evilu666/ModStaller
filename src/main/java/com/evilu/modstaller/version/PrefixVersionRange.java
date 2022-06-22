package com.evilu.modstaller.version;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PrefixVersionRange
 */
@RequiredArgsConstructor
@Getter
public class PrefixVersionRange implements VersionRange {

    private final String versionString;
    private final Integer major, minor, patch, hotfix;

    @Override
    public boolean includes(final Version version) {
        if (version instanceof SemanticVersion || version instanceof ForgeVersion) {
            final SemanticVersion v = version instanceof SemanticVersion ? (SemanticVersion) version : ((ForgeVersion) version).getModVersion();

            if (major != null) {
                if (major != v.getMajor()) return false;

                if (minor != null) {
                    if (minor != v.getMinor()) return false;

                    if (patch != null) {
                        if (patch != v.getPatch()) return false;

                        if (hotfix != null) {
                            if (hotfix != v.getHotfix()) return false;
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

}
