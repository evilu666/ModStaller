package com.evilu.modstaller.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.evilu.modstaller.model.Config;
import com.evilu.modstaller.model.Mod;
import com.evilu.modstaller.parser.ModJarParser;
import com.evilu.modstaller.source.ModSource;
import com.evilu.modstaller.task.CommonChunks;
import com.evilu.modstaller.task.TaskChunk;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.util.FuncUtil;
import com.evilu.modstaller.util.PlatformUtil;
import com.evilu.modstaller.util.StreamUtil;
import com.evilu.modstaller.version.VersionRange;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;

import static com.evilu.modstaller.util.TryUtil.tryMapping;
import static com.evilu.modstaller.util.FuncUtil.function;
import static com.evilu.modstaller.util.FuncUtil.compose;
import static com.evilu.modstaller.util.FuncUtil.inplaceFunction;

/**
 * ModRepository
 */
@Log4j2
public class ModRepository {

    private final File repoDir;
    private final TranslationService translationService;
    private Map<String, Set<Mod>> mods = new HashMap<>();
    private final ObservableList<Mod> allMods = FXCollections.observableArrayList();

    @Inject
    public ModRepository(final Config config, final TranslationService translationService) {
        repoDir = new File(config.getSettings().getModRepoPath());
        if (!repoDir.exists()) {
            repoDir.mkdirs();
        }

        this.translationService = translationService;
        scanRepo();
    }

    public void scanRepo() {
        mods = Arrays.stream(repoDir.listFiles((file, name) -> name.endsWith(".jar")))
            .parallel()
            .map(tryMapping(ModJarParser::parse, (throwable, modFile) -> {
                log.error("Error loading mod from jar: " + modFile.getAbsolutePath(), throwable);
                return null;
            }))
            .filter(Objects::nonNull)
            .peek(mod -> mod.setModSource(getModSource(mod)))
            .peek(mod -> Platform.runLater(() -> allMods.add(mod)))
            .collect(Collectors.groupingByConcurrent(Mod::getName, Collectors.toSet()));
    }

    public boolean hasMod(final String name) {
        return mods.containsKey(name) && !mods.get(name).isEmpty();
    }

    public boolean hasMod(final String name, final String version) {
        return hasMod(name, VersionRange.of(version));
    }

    public boolean hasMod(final String name, final VersionRange versionRange) {
        if (!mods.containsKey(name)) return false;

        return mods.get(name).stream()
            .map(Mod::getVersion)
            .anyMatch(versionRange::includes);
    }

    public Optional<Mod> getHighestVerion(final String name) {
        if (!mods.containsKey(name)) return Optional.empty();

        return mods.get(name).stream()
            .sorted(Comparator.comparing(Mod::getVersion).reversed())
            .findFirst();
    }

    public Optional<Mod> getHighestVerion(final String name, final String version) {
        return getHighestVerion(name, VersionRange.of(version));
    }

    public Optional<Mod> getHighestVerion(final String name, final VersionRange versionRange) {
        if (!mods.containsKey(name)) return Optional.empty();

        return mods.get(name).stream()
            .filter(StreamUtil.mapFilter(Mod::getVersion, versionRange::includes))
            .sorted(Comparator.comparing(Mod::getVersion).reversed())
            .findFirst();
    }


    public TaskChunk<File> saveModFile(final ObservableValue<Mod> modValue, final ObservableValue<File> fileValue) {
        return TaskChunk.create(translationService.translatedExpression("task.saveModFile", BindingUtil.mapString(modValue, mod -> getModFile(mod).getName(), "Unknown Mod")), statusCallback -> {
              statusCallback.setProgress(0d);

              final File modFile = getModFile(modValue.getValue());
              final File sourceFile = fileValue.getValue();

              if (!sourceFile.renameTo(modFile)) {
                  CommonChunks.copy(sourceFile.getTotalSpace(), new FileInputStream(sourceFile), new FileOutputStream(modFile), 8192, statusCallback);
              }

              statusCallback.setProgress(1d);
              return modFile;
        });
    }

    public TaskChunk<Mod> saveMod(final ObservableValue<Mod> modValue, final ObservableValue<File> fileValue) {
        return TaskChunk.create(translationService.translatedExpression("task.saveMod", BindingUtil.mapString(modValue, Mod::getDisplayName, "Unknown Mod")), statusCallback -> {
            final Mod mod = modValue.getValue();
            final File file = fileValue.getValue();

            statusCallback.setProgress(0d);
            if (!mods.containsKey(mod.getName())) {
                mods.put(mod.getName(), new HashSet<>());
            }

            mods.get(mod.getName()).add(mod);
            mod.setModSource(ModSource.fromFile(file));
            allMods.add(mod);

            return mod;
        });
    }

    public void delete(final Mod mod) {
        final File modFile = getModFile(mod);
        if (modFile.exists()) {
            modFile.delete();
        }

        allMods.remove(mod);
        mods.getOrDefault(mod.getName(), Set.of()).remove(mod);
    }

    private ModSource getModSource(final Mod mod) {
        return ModSource.fromFile(getModFile(mod));
    }

    public ObservableList<Mod> getAll() {
        return FXCollections.unmodifiableObservableList(allMods);
    }

    public String getModFileName(final Mod mod) {
        return String.format("%s_%s.jar", mod.getName(), mod.getVersion().toString());
    }

    public File getModFile(final Mod mod) {
        return new File(repoDir, mod.getName() + "_" + mod.getVersion().toString() + ".jar");
    }


    
}
