package com.evilu.modstaller.task;

import java.util.List;

import java.io.File;
import java.net.URL;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.ModPackRepository;
import com.evilu.modstaller.core.ModRepository;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Condition;
import com.evilu.modstaller.model.Mod;
import com.evilu.modstaller.model.ModPack;
import com.evilu.modstaller.model.Settings;
import com.evilu.modstaller.parser.ModJarParser;
import com.evilu.modstaller.source.ModSource;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.version.Version;

import javafx.concurrent.Task;

/**
 * CommonTasks
 */
public interface CommonTasks {

    public static Task<Mod> importMod(final URL url) {
        try {
            final File tempFile = File.createTempFile("mod", ".jar");
            tempFile.deleteOnExit();

            final Mod mod = Mod.builder()
                .name("unknown")
                .displayName("Unkown Mod")
                .version(Version.of("0.0.1"))
                .build();

            final ApplicationContext ctx = ApplicationContext.get();
            final ModRepository modRepo = ctx.getModRepository();
            final TranslationService ts = ctx.getTranslationService();

            final TaskChunk<File> downloadChunk = ModSource.fromURL(url).writeToFile(BindingUtil.createObservable(mod), BindingUtil.createObservable(tempFile));
            final TaskChunk<Mod> parseChunk = ModJarParser.parseFile(tempFile);
            final TaskChunk<File> importChunk = modRepo.saveModFile(parseChunk.resultProperty(), downloadChunk.resultProperty());
            final TaskChunk<Mod> saveChunk = modRepo.saveMod(parseChunk.resultProperty(), importChunk.resultProperty());

            return TaskChunk.assemble(ts.translateOnce("task.modImport.title"), downloadChunk, parseChunk, importChunk, saveChunk);
        } catch (final Throwable t) {
            throw new RuntimeException("Error creating mod import task", t);
        }
    }

    public static Task<ModPack> createModPack(final String name, final List<Mod> mods) {
        final ModPackRepository repo = ApplicationContext.get().getModPackRepository();
        final TranslationService ts = ApplicationContext.get().getTranslationService();

        final TaskChunk<ModPack> saveChunk = repo.save(name, mods);
        return TaskChunk.assemble(ts.translateOnce("task.importModPack.title"), saveChunk);
    }

    public static Task<File> installMod(final Mod mod) {
        final ApplicationContext ctx = ApplicationContext.get();

        final ModRepository modRepo = ctx.getModRepository();
        final TranslationService ts = ctx.getTranslationService();
        final Settings settings = ctx.getConfig().getSettings();

        final File sourceFile = modRepo.getModFile(mod);
        final File targetFile = new File(settings.getMinecraftFolderExpression().getValue(), modRepo.getModFileName(mod));

        final TaskChunk<File> copyChunk = mod.getModSource().writeToFile(BindingUtil.createObservable(mod), BindingUtil.createObservable(targetFile))
            .withPrecondition(Condition.AND(targetFile::exists, () -> targetFile.getTotalSpace() == sourceFile.getTotalSpace()).negated());

        return TaskChunk.assemble(ts.translateOnce("task.installMod.title"), copyChunk);
    }
    
}
