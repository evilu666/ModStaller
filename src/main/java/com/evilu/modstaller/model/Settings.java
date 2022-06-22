package com.evilu.modstaller.model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.File;
import java.nio.file.Paths;

import com.evilu.modstaller.constant.Language;
import com.evilu.modstaller.constant.StyleType;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.evilu.modstaller.util.PlatformUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Settings
 */
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, getterVisibility = Visibility.PUBLIC_ONLY, setterVisibility = Visibility.PUBLIC_ONLY)
@Getter
public class Settings {

    @JsonIgnore
    private final StringProperty minecraftPathProperty = new SimpleStringProperty(PlatformUtil.getMinecraftDir().getAbsolutePath());
    @JsonIgnore
    private final StringProperty modRepoPathProperty = new SimpleStringProperty(Paths.get(PlatformUtil.getDataDir().getAbsolutePath(), "mods").toAbsolutePath().toString());
    @JsonIgnore
    private final ObjectProperty<Language> languageProperty = new SimpleObjectProperty<>(Language.EN);
    @JsonIgnore
    private final ObjectProperty<StyleType> styleTypeProperty = new SimpleObjectProperty<>(PlatformUtil.detectDefaultStyle());
    @JsonIgnore
    private final IntegerProperty maxThreadsProperty = new SimpleIntegerProperty(PlatformUtil.getDefaultThreadCount());

    @JsonIgnore
    private final ObjectExpression<ExecutorService> executorServiceExpression = BindingUtil.<ExecutorService>map(maxThreadsProperty, Executors::newFixedThreadPool, Executors::newSingleThreadExecutor);
    @JsonIgnore
    private final ObjectExpression<File> minecraftFolderExpression = BindingUtil.map(minecraftPathProperty, p -> new File(p, "mods"), (File) null);

    @JsonProperty
    public void setMinecraftPath(final String path) {
        minecraftPathProperty.setValue(path);
    }

    @JsonProperty
    public String getMinecraftPath() {
        return minecraftPathProperty.get();
    }

    @JsonProperty
    public void setModRepoPath(final String path) {
        modRepoPathProperty.set(path);
    }

    @JsonProperty
    public String getModRepoPath() {
        return modRepoPathProperty.get();
    }

    @JsonProperty
    public void setLanguage(final Language lang) {
        languageProperty.set(lang);
    }

    @JsonProperty
    public Language getLanguage() {
        return languageProperty.get();
    }

    @JsonProperty
    public void setStyleType(final StyleType styleType) {
        styleTypeProperty.set(styleType);
    }

    @JsonProperty
    public StyleType getStyleType() {
        return styleTypeProperty.get();
    }

    @JsonProperty
    public void setMaxThreads(final int maxThreads) {
        maxThreadsProperty.set(maxThreads);
    }
}
