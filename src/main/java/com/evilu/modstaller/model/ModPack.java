package com.evilu.modstaller.model;

import java.util.HashSet;
import java.util.Set;

import com.evilu.modstaller.version.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ModPack
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ModPack implements Named, Versioned {

    private String name;
    private Version version;
    
    @Builder.Default
    private Set<Mod> mods = new HashSet<>();

}
