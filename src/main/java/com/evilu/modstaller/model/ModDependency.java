package com.evilu.modstaller.model;

import java.util.Objects;
import java.util.Optional;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.ModRepository;
import com.evilu.modstaller.version.VersionRange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ModDependency
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ModDependency {

    private String modName;
    private VersionRange versionRange;
    private boolean optional;

    public boolean matches(final Mod mod) {
        return Objects.equals(mod.getName(), modName) && versionRange.includes(mod.getVersion());
    }

    public Optional<Mod> resolveHighest() {
        final ModRepository repo = ApplicationContext.get().getModRepository();
        return repo.getHighestVerion(modName, versionRange);
    }
}
