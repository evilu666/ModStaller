package com.evilu.modstaller.model;

import java.util.HashSet;
import java.util.Set;

import com.evilu.modstaller.source.ModSource;
import com.evilu.modstaller.version.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Mod
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Mod implements Named, Versioned {

    private String name;

    @NonNull
    private Version version;

    @Builder.Default
    private String displayName = "Unknown Mod";

    @JsonIgnore
    private ModSource modSource;

    @Builder.Default
    private final Set<ModDependency> dependencies = new HashSet<>();

    public ModDependency toDependency() {
        return ModDependency.builder()
            .modName(name)
            .versionRange(version)
            .build();
    }
}
