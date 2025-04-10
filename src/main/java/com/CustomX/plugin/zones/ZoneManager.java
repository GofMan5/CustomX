package com.CustomX.plugin.zones;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.CustomX.plugin.CustomX;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

/**
 * Класс для управления зонами
 */
public class ZoneManager {

    private final CustomX plugin;
    private final Map<String, Zone> zones;
    private final Map<Player, Collection<Zone>> playerZones;
    private File zonesFile;
    private FileConfiguration zonesConfig;
    private BukkitTask updateTask;

    /**
     * Конструктор менеджера зон
     * 
     * @param plugin Экземпляр основного класса плагина
     */
    public ZoneManager(CustomX plugin) {
        this.plugin = plugin;
        this.zones = new HashMap<>();
        this.playerZones = new HashMap<>();
        this.loadZonesConfig();
        
        // Запускаем задачу регулярного обновления зон
        startUpdateTask();
    }
    
    /**
     * Запускает задачу периодического обновления зон для всех игроков
     */
    private void startUpdateTask() {
        int interval = plugin.getConfigManager().getZoneUpdateInterval();
        
        // Отменяем существующую задачу, если она есть
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Запускаем новую задачу
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerZones(player);
            }
        }, interval, interval);
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Запущена задача обновления зон с интервалом " + interval + " тиков");
        }
    }

    /**
     * Загрузить конфигурацию зон
     */
    private void loadZonesConfig() {
        zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        
        if (!zonesFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                zonesFile.createNewFile();
                plugin.getLogger().info("Создан новый файл zones.yml");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось создать файл zones.yml", e);
            }
        }
        
        zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
        loadZones();
    }

    /**
     * Загрузить зоны из конфигурации
     */
    private void loadZones() {
        zones.clear();
        
        ConfigurationSection zonesSection = zonesConfig.getConfigurationSection("zones");
        if (zonesSection == null) {
            return;
        }
        
        for (String name : zonesSection.getKeys(false)) {
            ConfigurationSection zoneSection = zonesSection.getConfigurationSection(name);
            if (zoneSection == null) {
                continue;
            }
            
            String worldName = zoneSection.getString("world");
            int minX = zoneSection.getInt("min.x");
            int minY = zoneSection.getInt("min.y");
            int minZ = zoneSection.getInt("min.z");
            int maxX = zoneSection.getInt("max.x");
            int maxY = zoneSection.getInt("max.y");
            int maxZ = zoneSection.getInt("max.z");
            
            BlockVector3 min = BlockVector3.at(minX, minY, minZ);
            BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);
            
            Zone zone = new Zone(name, worldName, min, max);
            
            // Загрузка свойств зоны починки
            boolean isRepairZone = zoneSection.getBoolean("repair.enabled", false);
            int repairInterval = zoneSection.getInt("repair.interval", plugin.getConfigManager().getDefaultRepairInterval());
            zone.setRepairZone(isRepairZone);
            zone.setRepairInterval(repairInterval);
            
            // Загрузка телепортационных свойств
            boolean isTeleportZone = zoneSection.getBoolean("teleport.enabled", false);
            int teleportCooldown = zoneSection.getInt("teleport.cooldown", 0);
            zone.setTeleportZone(isTeleportZone);
            zone.setTeleportCooldown(teleportCooldown);
            
            // Загрузка локации телепортации, если она установлена
            if (zoneSection.contains("teleport.destination")) {
                String destWorldName = zoneSection.getString("teleport.destination.world");
                double destX = zoneSection.getDouble("teleport.destination.x");
                double destY = zoneSection.getDouble("teleport.destination.y");
                double destZ = zoneSection.getDouble("teleport.destination.z");
                float destYaw = (float) zoneSection.getDouble("teleport.destination.yaw", 0.0);
                float destPitch = (float) zoneSection.getDouble("teleport.destination.pitch", 0.0);
                
                if (destWorldName != null) {
                    org.bukkit.World world = Bukkit.getWorld(destWorldName);
                    if (world != null) {
                        Location destination = new Location(world, destX, destY, destZ, destYaw, destPitch);
                        zone.setTeleportDestination(destination);
                    }
                }
            }
            
            // Загрузка эффектов
            ConfigurationSection effectsSection = zoneSection.getConfigurationSection("effects");
            if (effectsSection != null) {
                for (String effectName : effectsSection.getKeys(false)) {
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);
                    if (effectType != null) {
                        int amplifier = effectsSection.getInt(effectName + ".amplifier", 0);
                        int duration = effectsSection.getInt(effectName + ".duration", -1);
                        boolean showParticles = effectsSection.getBoolean(effectName + ".showParticles", plugin.getConfigManager().showEffectParticles());
                        boolean ambient = effectsSection.getBoolean(effectName + ".ambient", false);
                        zone.addEffect(effectType, amplifier, duration, showParticles, ambient);
                    }
                }
            }
            
            zones.put(name, zone);
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("Загружена зона: " + name);
            }
        }
    }

    /**
     * Сохранить зоны в конфигурацию
     */
    public void saveZones() {
        zonesConfig.set("zones", null);
        
        for (Zone zone : zones.values()) {
            String path = "zones." + zone.getName();
            zonesConfig.set(path + ".world", zone.getWorldName());
            zonesConfig.set(path + ".min.x", zone.getMin().getX());
            zonesConfig.set(path + ".min.y", zone.getMin().getY());
            zonesConfig.set(path + ".min.z", zone.getMin().getZ());
            zonesConfig.set(path + ".max.x", zone.getMax().getX());
            zonesConfig.set(path + ".max.y", zone.getMax().getY());
            zonesConfig.set(path + ".max.z", zone.getMax().getZ());
            
            // Сохранение свойств зоны починки
            zonesConfig.set(path + ".repair.enabled", zone.isRepairZone());
            zonesConfig.set(path + ".repair.interval", zone.getRepairInterval());
            
            // Сохранение свойств телепортации
            zonesConfig.set(path + ".teleport.enabled", zone.isTeleportZone());
            zonesConfig.set(path + ".teleport.cooldown", zone.getTeleportCooldown());
            
            // Сохранение локации телепортации, если она установлена
            Location teleportDestination = zone.getTeleportDestination();
            if (teleportDestination != null) {
                zonesConfig.set(path + ".teleport.destination.world", teleportDestination.getWorld().getName());
                zonesConfig.set(path + ".teleport.destination.x", teleportDestination.getX());
                zonesConfig.set(path + ".teleport.destination.y", teleportDestination.getY());
                zonesConfig.set(path + ".teleport.destination.z", teleportDestination.getZ());
                zonesConfig.set(path + ".teleport.destination.yaw", (double) teleportDestination.getYaw());
                zonesConfig.set(path + ".teleport.destination.pitch", (double) teleportDestination.getPitch());
            }
            
            // Сохранение эффектов
            Map<PotionEffectType, PotionEffect> effects = zone.getEffects();
            for (Map.Entry<PotionEffectType, PotionEffect> entry : effects.entrySet()) {
                String effectName = entry.getKey().getName();
                PotionEffect effect = entry.getValue();
                int amplifier = effect.getAmplifier();
                int duration = effect.getDuration() == Integer.MAX_VALUE ? -1 : effect.getDuration() / 20;
                
                zonesConfig.set(path + ".effects." + effectName + ".amplifier", amplifier);
                zonesConfig.set(path + ".effects." + effectName + ".duration", duration);
                zonesConfig.set(path + ".effects." + effectName + ".showParticles", effect.hasParticles());
                zonesConfig.set(path + ".effects." + effectName + ".ambient", effect.isAmbient());
            }
        }
        
        try {
            zonesConfig.save(zonesFile);
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("Зоны сохранены в файл zones.yml");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить файл zones.yml", e);
        }
    }

    /**
     * Создать новую зону
     * 
     * @param name Название зоны
     * @param region WorldEdit регион
     * @return true если зона успешно создана
     */
    public boolean createZone(String name, Region region) {
        if (zones.containsKey(name)) {
            return false;
        }
        
        Zone zone = new Zone(name, region);
        zones.put(name, zone);
        
        if (plugin.getConfigManager().isAutoSaveEnabled()) {
            saveZones();
        }
        
        return true;
    }

    /**
     * Удалить зону
     * 
     * @param name Название зоны
     * @return true если зона успешно удалена
     */
    public boolean deleteZone(String name) {
        Zone zone = zones.remove(name);
        if (zone != null) {
            if (plugin.getConfigManager().isAutoSaveEnabled()) {
                saveZones();
            }
            return true;
        }
        return false;
    }

    /**
     * Получить зону по имени
     * 
     * @param name Название зоны
     * @return Зона или null, если не найдена
     */
    public Zone getZone(String name) {
        return zones.get(name);
    }

    /**
     * Получить все зоны
     * 
     * @return Карта зон
     */
    public Map<String, Zone> getZones() {
        return new HashMap<>(zones);
    }

    /**
     * Установить зону как зону починки
     * 
     * @param name Название зоны
     * @param enabled true - включить, false - выключить
     * @return true если зона найдена и настроена
     */
    public boolean setRepairZone(String name, boolean enabled) {
        Zone zone = zones.get(name);
        if (zone != null) {
            zone.setRepairZone(enabled);
            
            if (plugin.getConfigManager().isAutoSaveEnabled()) {
                saveZones();
            }
            
            return true;
        }
        return false;
    }

    /**
     * Установить интервал починки для зоны
     * 
     * @param name Название зоны
     * @param seconds Интервал в секундах
     * @return true если зона найдена и настроена
     */
    public boolean setRepairInterval(String name, int seconds) {
        Zone zone = zones.get(name);
        if (zone != null) {
            zone.setRepairInterval(seconds);
            
            if (plugin.getConfigManager().isAutoSaveEnabled()) {
                saveZones();
            }
            
            return true;
        }
        return false;
    }

    /**
     * Добавить эффект к зоне
     * 
     * @param name Название зоны
     * @param effectType Тип эффекта
     * @param amplifier Уровень эффекта
     * @param duration Длительность эффекта в секундах
     * @return true если зона найдена и эффект добавлен
     */
    public boolean addEffect(String name, PotionEffectType effectType, int amplifier, int duration) {
        Zone zone = zones.get(name);
        if (zone != null && effectType != null) {
            boolean showParticles = plugin.getConfigManager().showEffectParticles();
            boolean ambient = plugin.getConfigManager().isEffectAmbient();
            zone.addEffect(effectType, amplifier, duration, showParticles, ambient);
            
            if (plugin.getConfigManager().isAutoSaveEnabled()) {
                saveZones();
            }
            
            return true;
        }
        return false;
    }

    /**
     * Удалить эффект из зоны
     * 
     * @param name Имя зоны
     * @param effectType Тип эффекта
     * @return true, если эффект удален успешно
     */
    public boolean removeEffect(String name, PotionEffectType effectType) {
        Zone zone = zones.get(name);
        if (zone == null) {
            return false;
        }
        
        zone.removeEffect(effectType);
        saveZones();
        
        return true;
    }

    /**
     * Очистить все эффекты из зоны
     * 
     * @param name Имя зоны
     * @return true, если зона найдена и эффекты очищены
     */
    public boolean clearEffects(String name) {
        Zone zone = zones.get(name);
        if (zone == null) {
            return false;
        }
        
        zone.clearEffects();
        saveZones();
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Очищены все эффекты из зоны " + name);
        }
        
        return true;
    }

    /**
     * Обновление зон для игрока
     * 
     * @param player Игрок
     */
    public void updatePlayerZones(Player player) {
        updatePlayerZones(player, false);
    }

    /**
     * Обновление зон для игрока с возможностью управления телепортацией
     * 
     * @param player Игрок
     * @param skipTeleport Пропустить телепортацию (true, если вызывается из обработчика телепортации)
     */
    public void updatePlayerZones(Player player, boolean skipTeleport) {
        if (!player.isOnline()) {
            return;
        }
        
        Collection<Zone> oldZones = playerZones.getOrDefault(player, new ArrayList<>());
        Collection<Zone> currentZones = getZonesForPlayer(player);
        
        // Находим зоны, из которых игрок вышел
        List<Zone> exitedZones = new ArrayList<>();
        for (Zone zone : oldZones) {
            if (!currentZones.contains(zone)) {
                exitedZones.add(zone);
            }
        }
        
        // Находим зоны, в которые игрок вошел (новые зоны)
        List<Zone> enteredZones = new ArrayList<>();
        for (Zone zone : currentZones) {
            if (!oldZones.contains(zone)) {
                enteredZones.add(zone);
            }
        }
        
        // Обработка для зон, из которых игрок вышел
        for (Zone zone : exitedZones) {
            // Удаляем только постоянные эффекты (duration = -1 или Integer.MAX_VALUE)
            Map<PotionEffectType, PotionEffect> zoneEffects = zone.getEffects();
            for (Map.Entry<PotionEffectType, PotionEffect> entry : zoneEffects.entrySet()) {
                PotionEffect effect = entry.getValue();
                if (effect.getDuration() == Integer.MAX_VALUE) {
                    // Удаляем только бесконечные эффекты, временные пусть "текут по своей волне"
                    player.removePotionEffect(entry.getKey());
                }
            }
        }
        
        // Обработка для зон, в которые игрок вошел
        for (Zone zone : enteredZones) {
            // Применяем телепортацию при входе в зону, только если не пропускаем телепортацию
            if (!skipTeleport && zone.isTeleportZone() && zone.getTeleportDestination() != null) {
                // Проверяем, есть ли у игрока метаданные телепортации, чтобы избежать циклов
                if (!player.hasMetadata("zonetp")) {
                    zone.teleportPlayer(player);
                }
            }
            
            // Применяем новые эффекты
            zone.applyEffects(player);
        }
        
        // Обновляем список зон игрока
        playerZones.put(player, currentZones);
        
        // Делаем починку только в текущих зонах
        repairPlayerItems(player, currentZones);
    }

    /**
     * Получить все зоны, в которых находится игрок
     * 
     * @param player Игрок
     * @return Коллекция зон
     */
    public Collection<Zone> getZonesForPlayer(Player player) {
        List<Zone> result = new ArrayList<>();
        
        for (Zone zone : zones.values()) {
            if (zone.isInZone(player)) {
                result.add(zone);
            }
        }
        
        return result;
    }

    /**
     * Применить эффекты от зон к игроку для существующих зон
     * 
     * @param player Игрок
     * @param zones Коллекция зон
     */
    private void applyZoneEffects(Player player, Collection<Zone> zones) {
        Collection<Zone> playerCurrentZones = playerZones.getOrDefault(player, new ArrayList<>());
        for (Zone zone : zones) {
            // Применяем эффекты только для зон, которые игрок уже посещает
            // и которые не были обработаны в updatePlayerZones (т.е. не новые зоны)
            if (playerCurrentZones.contains(zone)) {
                zone.applyEffects(player);
            }
        }
    }

    /**
     * Починить предметы игрока в нескольких зонах
     * 
     * @param player Игрок
     * @param zones Коллекция зон
     */
    private void repairPlayerItems(Player player, Collection<Zone> zones) {
        for (Zone zone : zones) {
            if (zone.repairItems(player)) {
                // Уведомление о починке только если оно включено в конфигурации
                if (plugin.getConfigManager().notifyOnRepair()) {
                    player.sendMessage(ChatColor.GREEN + "Ваши предметы были починены!");
                }
                
                // Звуковой эффект только если он включен в конфигурации
                Sound repairSound = plugin.getConfigManager().getRepairSound();
                if (repairSound != null) {
                    player.playSound(player.getLocation(), repairSound, 1.0f, 1.0f);
                }
            }
        }
    }

    /**
     * Починить предметы игрока в конкретной зоне
     * 
     * @param player Игрок
     * @param zone Зона
     */
    private void repairPlayerItems(Player player, Zone zone) {
        if (zone.repairItems(player)) {
            // Уведомление о починке только если оно включено в конфигурации
            if (plugin.getConfigManager().notifyOnRepair()) {
                player.sendMessage(ChatColor.GREEN + "Ваши предметы были починены!");
            }
            
            // Звуковой эффект только если он включен в конфигурации
            Sound repairSound = plugin.getConfigManager().getRepairSound();
            if (repairSound != null) {
                player.playSound(player.getLocation(), repairSound, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Удалить игрока из отслеживания
     * 
     * @param player Игрок
     */
    public void removePlayer(Player player) {
        Collection<Zone> zones = playerZones.remove(player);
        if (zones != null) {
            for (Zone zone : zones) {
                zone.removeEffects(player);
            }
        }
    }

    /**
     * Перезагрузить конфигурацию зон
     */
    public void reload() {
        loadZonesConfig();
        
        // Перезапускаем задачу обновления с новым интервалом
        startUpdateTask();
        
        // Обновление зон для всех игроков
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerZones(player);
        }
    }

    /**
     * Обновляет границы существующей зоны
     * 
     * @param zoneName имя зоны
     * @param newRegion новое выделение WorldEdit
     * @return true если зона успешно обновлена
     */
    public boolean updateZoneBoundaries(String zoneName, Region newRegion) {
        Zone zone = zones.get(zoneName);
        if (zone == null) {
            return false;
        }
        
        // Обновляем границы
        BlockVector3 newMin = newRegion.getMinimumPoint();
        BlockVector3 newMax = newRegion.getMaximumPoint();
        
        // Создаем новый объект зоны с теми же свойствами, но новыми границами
        Zone newZone = new Zone(zoneName, newRegion.getWorld().getName(), newMin, newMax);
        
        // Копируем свойства
        newZone.setRepairZone(zone.isRepairZone());
        newZone.setRepairInterval(zone.getRepairInterval());
        
        // Копируем эффекты
        for (Map.Entry<PotionEffectType, PotionEffect> entry : zone.getEffects().entrySet()) {
            PotionEffect effect = entry.getValue();
            newZone.addEffect(entry.getKey(), effect.getAmplifier(), 
                    effect.getDuration() == Integer.MAX_VALUE ? -1 : effect.getDuration() / 20,
                    effect.hasParticles(), effect.isAmbient());
        }
        
        // Заменяем старую зону
        zones.put(zoneName, newZone);
        
        // Сохраняем изменения
        if (plugin.getConfigManager().isAutoSaveEnabled()) {
            saveZones();
        }
        
        return true;
    }

    /**
     * Установить зону как телепортационную
     * 
     * @param name Имя зоны
     * @param enabled Включено/выключено
     * @return true, если зона найдена и обновлена
     */
    public boolean setTeleportZone(String name, boolean enabled) {
        Zone zone = zones.get(name);
        if (zone == null) {
            return false;
        }
        
        zone.setTeleportZone(enabled);
        saveZones();
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Телепортация " + (enabled ? "включена" : "выключена") + " для зоны " + name);
        }
        
        return true;
    }
    
    /**
     * Установить точку назначения телепортации для зоны
     * 
     * @param name Имя зоны
     * @param destination Точка назначения
     * @return true, если зона найдена и обновлена
     */
    public boolean setTeleportDestination(String name, Location destination) {
        Zone zone = zones.get(name);
        if (zone == null) {
            return false;
        }
        
        zone.setTeleportDestination(destination);
        saveZones();
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Установлена точка телепортации для зоны " + name + ": " + 
                                  destination.getWorld().getName() + " " + 
                                  destination.getX() + "," + 
                                  destination.getY() + "," + 
                                  destination.getZ());
        }
        
        return true;
    }
    
    /**
     * Установить кулдаун телепортации для зоны
     * 
     * @param name Имя зоны
     * @param seconds Кулдаун в секундах
     * @return true, если зона найдена и обновлена
     */
    public boolean setTeleportCooldown(String name, int seconds) {
        Zone zone = zones.get(name);
        if (zone == null) {
            return false;
        }
        
        if (seconds < 0) {
            seconds = 0;
        }
        
        zone.setTeleportCooldown(seconds);
        saveZones();
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Установлен кулдаун телепортации " + seconds + " сек. для зоны " + name);
        }
        
        return true;
    }
} 