package com.evilu.modstaller.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import java.io.File;
import java.io.IOException;

import com.evilu.modstaller.model.Mod;
import com.evilu.modstaller.model.ModDependency;
import com.evilu.modstaller.model.ModPack;
import com.evilu.modstaller.task.TaskBuilder;
import com.evilu.modstaller.task.TaskChunk;
import com.evilu.modstaller.util.PlatformUtil;
import com.evilu.modstaller.version.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;

/**
 * ModPackRepository
 */
@Log4j2
public class ModPackRepository {

    private static final File repoFile = new File(PlatformUtil.getDataDir(), "modpacks.json");

    private final ObjectMapper objectMapper;
    private Map<String, ModPack> modPacks;
    private ObservableList<ModPack> packList;
    private ReadOnlyListProperty<ModPack> readOnlyPackList;

    private final TranslationService ts;

    private boolean initialized = false;

    public ModPackRepository(final ObjectMapper objectMapper, final TranslationService translationService) {
        this.objectMapper = objectMapper;
        this.ts = translationService;
    }

    public void init() {
        if (repoFile.exists()) {
            try {
                modPacks = objectMapper.readValue(repoFile, new TypeReference<List<ModPack>>() {})
                        .stream()
                        .peek(pack -> pack.setMods(pack.getMods()
                                    .stream()
                                    .map(Mod::toDependency)
                                    .map(dep -> {
                                        final Optional<Mod> maybeMod = dep.resolveHighest();
                                        if (maybeMod.isEmpty()) log.error("Missing mod {} (version: {}) in modpack '{}', removing...", dep.getModName(), dep.getVersionRange(), pack.getName());
                                        return maybeMod;
                                    })
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toSet()))
                        )
                        .collect(Collectors.toMap(ModPack::getName, Function.identity()));
            } catch (final IOException e) {
                throw new RuntimeException("Error creating modpack repository", e);
            }
        } else {
            modPacks = new HashMap<>();
        }

        packList = FXCollections.observableArrayList();
        packList.addAll(modPacks.values());

        readOnlyPackList = new ReadOnlyListWrapper<>(packList);

        initialized = true;
    }

    public boolean hasModPack(final String name) {
        checkInit();

        return modPacks.containsKey(name);
    }

    public Optional<ModPack> get(final String name) {
        checkInit();

        if (!modPacks.containsKey(name))
            return Optional.empty();

        return Optional.of(modPacks.get(name));
    }

    public void delete(final ModPack modPack) {
        checkInit();

        if (modPacks.containsKey(modPack.getName())) {
            modPacks.remove(modPack.getName());
            Platform.runLater(() -> packList.remove(modPack));
        }
    }

    public ModPack save(final ModPack modPack) {
        checkInit();

        modPacks.put(modPack.getName(), modPack);

        Platform.runLater(() -> {
            packList.remove(modPack);
            packList.add(modPack);
        });

        save();
        return modPack;
    }

    public TaskChunk<ModPack> save(final String name, final List<Mod> mods) {
        checkInit();

        return TaskChunk.create(ts.translated("task.saveModPack.msg", name), statusCallback -> {
            statusCallback.setProgress(0d);

            final ModPack pack = save(ModPack.builder()
                .name(name)
                .version(Version.of("0.0.1"))
                .mods(mods.stream().collect(Collectors.toSet()))
                .build());

            statusCallback.setProgress(1d);
            return pack;
        });
    }

    public ReadOnlyListProperty<ModPack> getAll() {
        return readOnlyPackList;
    }

    private void save() {
        try {
            objectMapper.writeValue(repoFile, modPacks.values());
        } catch (final IOException e) {
            throw new RuntimeException("Error saving modpack repository", e);
        }
    }

    private void checkInit() {
        if (!initialized) throw new IllegalStateException("ModPackRepository hasn't been initialized yet!");
    }
    

}
