package com.evilu.modstaller.util;

import java.io.File;

import com.evilu.modstaller.App;
import com.evilu.modstaller.constant.StyleType;

import org.apache.commons.lang3.SystemUtils;

import dev.dirs.ProjectDirectories;

/**
 * PlatformUtil
 */
public interface PlatformUtil {

    public static enum Platform {
        WINDOWS,
        LINUX,
        MAC
    }

    public static final String CONFIG_FILE_NAME = "config.json";

    public static final ProjectDirectories PROJECT_DIRS = ProjectDirectories.from("com", "evilu", App.APP_NAME);

    public static File getDataDir() {
        final File dataDir = new File(PROJECT_DIRS.dataDir);
        createIfNeeded(dataDir);
        return dataDir;
    }

    public static File getConfigDir() {
        final File configDir = new File(PROJECT_DIRS.configDir);
        createIfNeeded(configDir);
        return configDir;
    }

    public static File getConfigFile() {
        final File configDir = getConfigDir();
        return new File(configDir, "config.json");
    }

    public static File getMinecraftDir() {
        switch (getPlatform()) {
            case WINDOWS:
                return new File(System.getenv("APPDATA"), ".minecraft");
            case MAC:
                return new File(System.getenv("HOME"), "Library/Application Support/minecraft");
            case LINUX:
                return new File(System.getenv("HOME"), ".minecraft");
            default:
                throw new IllegalStateException("Unhandled platform: " + getPlatform());
        }
    }

    public static Platform getPlatform() {
        if (SystemUtils.IS_OS_WINDOWS) return Platform.WINDOWS;
        if (SystemUtils.IS_OS_MAC) return Platform.MAC;

        return Platform.LINUX;
    }

    public static StyleType detectDefaultStyle() {
        final String username = System.getProperty("user.name").toLowerCase();

        if (username.contains("milan") || username.contains("schmidt")) return StyleType.M1L4N;
        if (username.contains("friese") || username.contains("kimari") || username.contains("lu")) return StyleType.LU154;
        if (username.contains("haze")) return StyleType.H4Z3;
        
        return StyleType.DEFAULT;
    }

    
    private static void createIfNeeded(final File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static int getDefaultThreadCount() {
        return Integer.min(Runtime.getRuntime().availableProcessors(), 4);
    }
}
