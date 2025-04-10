package com.CustomX.plugin.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.CustomX.plugin.CustomX;

/**
 * Менеджер конфигураций плагина
 */
public class ConfigManager {

    private final CustomX plugin;
    private File configFile;
    private FileConfiguration config;

    /**
     * Конструктор менеджера конфигураций
     * 
     * @param plugin экземпляр плагина
     */
    public ConfigManager(CustomX plugin) {
        this.plugin = plugin;
    }

    /**
     * Загружает все конфигурации
     */
    public void loadConfigs() {
        loadMainConfig();
    }

    /**
     * Загружает основную конфигурацию
     */
    private void loadMainConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            plugin.getLogger().info("Создан новый файл config.yml");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Проверяем наличие всех настроек по умолчанию
        checkDefaults();
    }
    
    /**
     * Проверяет наличие всех настроек по умолчанию и добавляет их, если необходимо
     */
    private void checkDefaults() {
        boolean changed = false;
        
        // Основные настройки
        if (!config.isSet("settings.debug")) {
            config.set("settings.debug", false);
            changed = true;
        }
        
        if (!config.isSet("settings.prefix")) {
            config.set("settings.prefix", "&6[CustomX] &r");
            changed = true;
        }
        
        // Настройки зон
        if (!config.isSet("zones.auto-save")) {
            config.set("zones.auto-save", true);
            changed = true;
        }
        
        if (!config.isSet("zones.update-interval")) {
            config.set("zones.update-interval", 10);
            changed = true;
        }
        
        // Настройки эффектов
        if (!config.isSet("effects.default.show-particles")) {
            config.set("effects.default.show-particles", true);
            changed = true;
        }
        
        if (!config.isSet("effects.default.show-icons")) {
            config.set("effects.default.show-icons", true);
            changed = true;
        }
        
        if (!config.isSet("effects.default.ambient")) {
            config.set("effects.default.ambient", false);
            changed = true;
        }
        
        // Настройки починки
        if (!config.isSet("repair.default.interval")) {
            config.set("repair.default.interval", 5);
            changed = true;
        }
        
        if (!config.isSet("repair.default.notify")) {
            config.set("repair.default.notify", true);
            changed = true;
        }
        
        if (!config.isSet("repair.default.sound")) {
            config.set("repair.default.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
            changed = true;
        }
        
        // Настройки bStats
        if (!config.isSet("metrics.enabled")) {
            config.set("metrics.enabled", true);
            changed = true;
        }
        
        // Сохраняем изменения, если были добавлены новые настройки
        if (changed) {
            saveMainConfig();
        }
    }

    /**
     * Сохраняет все конфигурации
     */
    public void saveConfigs() {
        saveMainConfig();
    }

    /**
     * Сохраняет основную конфигурацию
     */
    private void saveMainConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить config.yml", e);
        }
    }

    /**
     * Получает основную конфигурацию
     * 
     * @return основная конфигурация
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Проверяет, включен ли режим отладки
     * 
     * @return true, если режим отладки включен
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("settings.debug", false);
    }
    
    /**
     * Получает префикс для сообщений плагина
     * 
     * @return префикс сообщений
     */
    public String getPrefix() {
        return config.getString("settings.prefix", "&6[CustomX] &r").replace('&', '§');
    }
    
    /**
     * Проверяет, нужно ли автоматически сохранять зоны при изменении
     * 
     * @return true, если нужно автоматически сохранять зоны
     */
    public boolean isAutoSaveEnabled() {
        return config.getBoolean("zones.auto-save", true);
    }
    
    /**
     * Получает интервал обновления зон для игроков в тиках
     * 
     * @return интервал обновления
     */
    public int getZoneUpdateInterval() {
        return config.getInt("zones.update-interval", 10);
    }
    
    /**
     * Проверяет, нужно ли показывать частицы эффектов
     * 
     * @return true, если нужно показывать частицы
     */
    public boolean showEffectParticles() {
        return config.getBoolean("effects.default.show-particles", true);
    }
    
    /**
     * Проверяет, нужно ли показывать иконки эффектов
     * 
     * @return true, если нужно показывать иконки
     */
    public boolean showEffectIcons() {
        return config.getBoolean("effects.default.show-icons", true);
    }
    
    /**
     * Проверяет, должны ли эффекты быть ambient (для правильного отображения иконок)
     * 
     * @return true, если эффекты должны быть ambient
     */
    public boolean isEffectAmbient() {
        return config.getBoolean("effects.default.ambient", false);
    }
    
    /**
     * Получает интервал починки предметов по умолчанию
     * 
     * @return интервал в секундах
     */
    public int getDefaultRepairInterval() {
        return config.getInt("repair.default.interval", 5);
    }
    
    /**
     * Проверяет, нужно ли уведомлять игрока о починке предметов
     * 
     * @return true, если нужно уведомлять
     */
    public boolean notifyOnRepair() {
        // Проверяем наличие настройки
        if (!config.isSet("repair.default.notify")) {
            plugin.getLogger().warning("Настройка repair.default.notify отсутствует в конфигурации. По умолчанию уведомления включены.");
            return true;
        }
        
        // Явное получение значения из конфигурации
        boolean notifyEnabled = config.getBoolean("repair.default.notify", true);
        plugin.getLogger().info("Уведомления о починке: " + (notifyEnabled ? "включены" : "отключены") + 
                " (значение в конфиге: " + config.get("repair.default.notify") + ")");
        
        return notifyEnabled;
    }
    
    /**
     * Получает звук, воспроизводимый при починке предметов
     * 
     * @return звук или null, если звук отключен
     */
    public Sound getRepairSound() {
        // Проверяем наличие настройки
        if (!config.isSet("repair.default.sound")) {
            plugin.getLogger().warning("Настройка repair.default.sound отсутствует в конфигурации. Звуки отключены.");
            return null;
        }
        
        String soundName = config.getString("repair.default.sound", "");
        
        // Если звук отключен (пустая строка, "NONE", "OFF", "DISABLED", "FALSE" и т.п.)
        if (soundName == null || soundName.isEmpty() || 
            soundName.equalsIgnoreCase("NONE") || 
            soundName.equalsIgnoreCase("OFF") ||
            soundName.equalsIgnoreCase("DISABLED") ||
            soundName.equalsIgnoreCase("FALSE")) {
            
            plugin.getLogger().info("Звуки починки отключены в конфигурации (значение: '" + soundName + "')");
            return null;
        }
        
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный звук в конфигурации: " + soundName + ". Проверьте правильность названия звука.");
            return null;
        }
    }
    
    /**
     * Проверяет, включен ли сбор статистики bStats
     * 
     * @return true, если сбор статистики включен
     */
    public boolean isMetricsEnabled() {
        return config.getBoolean("metrics.enabled", true);
    }

    /**
     * Перезагружает конфигурацию из файла
     */
    public void reloadConfig() {
        // Перезагружаем конфигурацию из файла
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Проверяем наличие всех настроек по умолчанию
        checkDefaults();
        
        // Выводим информацию о ключевых настройках в лог
        plugin.getLogger().info("Настройки после перезагрузки:");
        plugin.getLogger().info("- Уведомления о починке: " + (notifyOnRepair() ? "включены" : "отключены"));
        String soundStatus = getRepairSound() != null ? getRepairSound().name() : "отключены";
        plugin.getLogger().info("- Звуки починки: " + soundStatus);
        
        plugin.getLogger().info("Конфигурация успешно перезагружена");
    }
}
