package com.evilu.modstaller.core;

import java.io.File;
import java.io.IOException;

import com.evilu.modstaller.model.Config;
import com.evilu.modstaller.util.PlatformUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import org.yaml.snakeyaml.Yaml;

import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * ApplicationContext
 */
@Log4j2
public class ApplicationContext extends AbstractModule {

    private static final ApplicationContext context = new ApplicationContext();

    public static ApplicationContext get() {
        return context;
    }

    public static ApplicationContext init() {
        context.doInit();
        return context;
    }

    @Getter
    private final Config config;

    @Getter
    private final ObjectMapper objectMapper;

    @Getter
    private final TranslationService translationService;

    @Getter
    private final Faker faker;

    @Getter
    private final Yaml yaml;

    @Getter
    private final ModRepository modRepository;

    @Getter
    private final ModPackRepository modPackRepository;

    private final Injector injector;

    private ApplicationContext() {
        objectMapper = new ObjectMapper();

        final File configFile = PlatformUtil.getConfigFile();
        if (configFile.exists()) {
            config = Try.of(() -> objectMapper.readValue(configFile, Config.class))
                .recover(t -> {
                    log.error("Error reading config from path: " + configFile.getAbsolutePath(), t);
                    return new Config();
                })
                .get();
        } else {
            config = new Config();
        }

        yaml = new Yaml();

        try {
            translationService = new TranslationService(config, yaml);
        } catch (final IOException e) {
            throw new IllegalStateException("Error creating translationService", e);
        }

        faker = new Faker();


        modRepository = new ModRepository(config, translationService);
        modPackRepository = new ModPackRepository(objectMapper, translationService);

        injector = Guice.createInjector(this);
    }

    private void doInit() {
        modPackRepository.init();
    }

    public void saveConfig() {
        final File configFile = PlatformUtil.getConfigFile();
        try {
            objectMapper.writeValue(configFile, config);
        } catch (final IOException e) {
            throw new RuntimeException("Error saving config: " + e.getMessage(), e);
        }
    }
}
