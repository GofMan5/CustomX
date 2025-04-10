package com.CustomX.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import com.CustomX.plugin.commands.MainCommand;
import com.CustomX.plugin.listeners.PlayerListener;
import com.CustomX.plugin.listeners.ZoneTeleportListener;
import com.CustomX.plugin.config.ConfigManager;
import com.CustomX.plugin.zones.ZoneManager;
import com.CustomX.plugin.worldedit.WorldEditHook;

/**
 * Основной класс плагина CustomX
 */
public final class CustomX extends JavaPlugin {

    private static CustomX instance;
    private ConfigManager configManager;
    private ZoneManager zoneManager;
    private WorldEditHook worldEditHook;
    
    // ID плагина для bStats
    private static final int BSTATS_ID = 25426; // Зарегистрируйте свой плагин на https://bstats.org/ чтобы получить настоящий ID

    @Override
    public void onEnable() {
        // Сохраняем экземпляр плагина для доступа из других классов
        instance = this;
        
        // Инициализация менеджера конфигураций
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Инициализация bStats метрик
        if (configManager.isMetricsEnabled()) {
            try {
                Metrics metrics = new Metrics(this, BSTATS_ID);
                if (configManager.isDebugEnabled()) {
                    getLogger().info("Метрики bStats включены");
                }
            } catch (Exception e) {
                getLogger().warning("Не удалось инициализировать bStats: " + e.getMessage());
            }
        }
        
        // Инициализация менеджера зон
        zoneManager = new ZoneManager(this);
        
        // Инициализация хука WorldEdit
        worldEditHook = new WorldEditHook(this);
        
        // Регистрация команд
        MainCommand mainCommand = new MainCommand(this);
        getCommand("customx").setExecutor(mainCommand);
        getCommand("cx").setExecutor(mainCommand);
        
        // Регистрация слушателей событий
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ZoneTeleportListener(this), this);
        
        // Логирование информации о успешном запуске
        getLogger().info(configManager.getPrefix() + "Плагин CustomX успешно загружен!");
    }

    @Override
    public void onDisable() {
        // Сохранение данных и завершение работы
        if (configManager != null) {
            configManager.saveConfigs();
        }
        
        if (zoneManager != null) {
            zoneManager.saveZones();
        }
        
        getLogger().info(configManager != null ? configManager.getPrefix() + "Плагин CustomX выключен!" : "Плагин CustomX выключен!");
    }
    
    /**
     * Получить экземпляр плагина
     * 
     * @return экземпляр плагина
     */
    public static CustomX getInstance() {
        return instance;
    }
    
    /**
     * Получить менеджер конфигураций
     * 
     * @return менеджер конфигураций
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Получить менеджер зон
     * 
     * @return менеджер зон
     */
    public ZoneManager getZoneManager() {
        return zoneManager;
    }
    
    /**
     * Получить хук WorldEdit
     * 
     * @return хук WorldEdit
     */
    public WorldEditHook getWorldEditHook() {
        return worldEditHook;
    }
} 