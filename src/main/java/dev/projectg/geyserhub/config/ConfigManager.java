package dev.projectg.geyserhub.config;

import dev.projectg.geyserhub.GeyserHubMain;
import dev.projectg.geyserhub.SelectorLogger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {

    private static ConfigManager CONFIG_MANAGER;

    private final Map<ConfigId, FileConfiguration> configurations = new HashMap<>();

    private final SelectorLogger logger;

    public ConfigManager() {
        if (CONFIG_MANAGER == null) {
            CONFIG_MANAGER = this;
        } else {
            throw new UnsupportedOperationException("Only one instance of ConfigManager is allowed!");
        }

        this.logger = SelectorLogger.getLogger();
    }

    /**
     * Load every config in {@link ConfigId}
     * @return false if there was a failure loading any of configurations
     */
    public boolean loadAllConfigs() {
        boolean totalSuccess = true;
        for (ConfigId configId : ConfigId.VALUES) {
            if (!loadConfig(configId)) {
                totalSuccess = false;
                logger.severe("Configuration error in " + ConfigId.MAIN.fileName + " - Fix the issue or regenerate a new file.");
            }
        }
        return totalSuccess;
    }

    /**
     * Load a configuration from file.
     * @param config The configuration to load
     * @return The success state
     */
    public boolean loadConfig(@Nonnull ConfigId config) {
        GeyserHubMain plugin = GeyserHubMain.getInstance();

        File file = new File(plugin.getDataFolder(), config.fileName);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                try {
                    if (!file.getParentFile().mkdirs()) {
                        logger.severe("Failed to create plugin folder!");
                        return false;
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            plugin.saveResource(config.fileName, false);
        }
        FileConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        if (!configuration.contains("Config-Version", true)) {
            logger.severe("Config-Version does not exist in" + config.fileName + " !");
            return false;
        } else if (!configuration.isInt("Config-Version")) {
            logger.severe("Config-Version is not an integer in" + config.fileName + " !");
            return false;
        } else if (configuration.getInt("Config-Version") != config.version) {
            logger.severe("Mismatched config version in " + config.fileName + " ! Generate a new config and migrate your settings!");
            return false;
        } else {
            plugin.reloadConfig();
            this.configurations.put(config, configuration);
            logger.debug("Loaded configuration " + config.fileName + " successfully");
            return true;
        }
    }

    /**
     * Get the given FileConfiguration in the stored map.
     * @param config the config ID
     * @return the FileConfiguration
     */
    public FileConfiguration getFileConfiguration(@Nonnull ConfigId config) {
        Objects.requireNonNull(config);
        return configurations.get(config);
    }

    /**
     * @return A the map of configuration names to FileConfigurations
     */
    public Map<ConfigId, FileConfiguration> getAllFileConfigurations() {
        return configurations;
    }

}
