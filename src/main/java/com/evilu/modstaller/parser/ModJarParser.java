package com.evilu.modstaller.parser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import java.io.File;
import java.io.IOException;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Mod;
import com.evilu.modstaller.model.ModDependency;
import com.evilu.modstaller.task.TaskChunk;
import com.evilu.modstaller.version.Version;
import com.evilu.modstaller.version.VersionRange;
import com.moandjiezana.toml.Toml;

import org.apache.commons.text.WordUtils;

import io.vavr.Tuple2;
import javafx.concurrent.Task;

/**
 * ModJarParser
 */
public interface ModJarParser {

    public static Task<Tuple2<File, Mod>> createTask(final File file, final TranslationService translationService) {
        return new Task<>() {
              @Override
              protected Tuple2<File, Mod> call() throws Exception {
                  updateMessage(translationService.translate("modJarParser.parseTask.statusMsg", file.getName()));
                  updateProgress(0d, 1d);
                  final Mod mod = parse(file);
                  updateProgress(1d, 1d);

                  return new Tuple2<>(file, mod);
              }
        };
    }

    public static TaskChunk<Mod> parseFile(final File file) {
        final TranslationService ts = ApplicationContext.get().getTranslationService();
        return TaskChunk.create(ts.translateOnce("task.parseMod"),  statusCallback -> {
            statusCallback.setProgress(0d);
            final Mod mod = parse(file);
            statusCallback.setProgress(1d);
            return mod;
        });
    }


    public static Mod parse(final File file) throws IOException {
        try (final JarFile jar = new JarFile(file, true)) {
            final Manifest manifest = jar.getManifest();
            final JarEntry modsEntry = jar.getJarEntry("META-INF/mods.toml");

            if (Objects.nonNull(modsEntry)) {
                final Toml modsToml = new Toml().read(jar.getInputStream(modsEntry));
                if (modsToml.containsTableArray("mods")) {
                    final Toml modTable = modsToml.getTables("mods").get(0);
                    final String modVersionString = "${file.jarVersion}".equals(modTable.getString("version").trim()) ? manifest.getMainAttributes().getValue("Implementation-Version") : modTable.getString("version");
                    final Mod mod = Mod.builder()
                        .name(modTable.getString("modId"))
                        .version(Version.of(modVersionString))
                        .displayName(modTable.getString("displayName"))
                        .build();

                    Optional.ofNullable(modsToml.getTables("dependencies." + mod.getName()))
                        .orElseGet(List::of)
                        .stream()
                        .map(dep -> ModDependency.builder()
                                .modName(dep.getString("modId"))
                                .versionRange(VersionRange.of(dep.getString("versionRange")))
                                .optional(!dep.getBoolean("mandatory"))
                                .build())
                        .forEach(mod.getDependencies()::add);

                    return mod;
                }
            }

            return Mod.builder()
                .name(manifest.getMainAttributes().getValue("Specification-Title"))
                .version(Version.of(manifest.getMainAttributes().getValue("Specification-Version")))
                .displayName(WordUtils.capitalizeFully(manifest.getMainAttributes().getValue("Specification-Title")))
                .build();
        }
    }
    
}
