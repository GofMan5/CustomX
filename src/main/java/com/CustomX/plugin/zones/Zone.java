package com.CustomX.plugin.zones;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

/**
 * Класс, представляющий зону с эффектами и функцией починки
 */
public class Zone {
    private String name;
    private String worldName;
    private BlockVector3 min;
    private BlockVector3 max;
    private boolean isRepairZone;
    private int repairInterval; // в секундах, 0 = только при входе/выходе
    private Map<PotionEffectType, PotionEffect> effects;
    private Map<UUID, Long> lastRepairTime;
    
    // Новые поля для телепортации
    private boolean isTeleportZone;
    private Location teleportDestination;
    private Map<UUID, Long> lastTeleportTime;
    private int teleportCooldown; // Кулдаун телепортации в секундах

    /**
     * Конструктор зоны
     * 
     * @param name Название зоны
     * @param region WorldEdit регион
     */
    public Zone(String name, Region region) {
        this.name = name;
        this.worldName = region.getWorld().getName();
        this.min = region.getMinimumPoint();
        this.max = region.getMaximumPoint();
        this.isRepairZone = false;
        this.repairInterval = 0;
        this.effects = new HashMap<>();
        this.lastRepairTime = new HashMap<>();
        
        // Инициализация новых полей
        this.isTeleportZone = false;
        this.teleportDestination = null;
        this.lastTeleportTime = new HashMap<>();
        this.teleportCooldown = 0;
    }

    /**
     * Конструктор для загрузки зоны из конфигурации
     * 
     * @param name Название зоны
     * @param worldName Имя мира
     * @param min Минимальная точка зоны
     * @param max Максимальная точка зоны
     */
    public Zone(String name, String worldName, BlockVector3 min, BlockVector3 max) {
        this.name = name;
        this.worldName = worldName;
        this.min = min;
        this.max = max;
        this.isRepairZone = false;
        this.repairInterval = 0;
        this.effects = new HashMap<>();
        this.lastRepairTime = new HashMap<>();
        
        // Инициализация новых полей
        this.isTeleportZone = false;
        this.teleportDestination = null;
        this.lastTeleportTime = new HashMap<>();
        this.teleportCooldown = 0;
    }

    /**
     * Проверить, находится ли игрок в зоне
     * 
     * @param player Игрок
     * @return true, если игрок в зоне
     */
    public boolean isInZone(Player player) {
        Location loc = player.getLocation();
        
        if (!loc.getWorld().getName().equals(worldName)) {
            return false;
        }
        
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        
        return x >= min.getX() && x <= max.getX() &&
               y >= min.getY() && y <= max.getY() &&
               z >= min.getZ() && z <= max.getZ();
    }
    
    /**
     * Починить все предметы игрока
     * 
     * @param player Игрок
     * @return true, если что-то было починено
     */
    public boolean repairItems(Player player) {
        if (!isRepairZone) {
            return false;
        }
        
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Проверяем, прошел ли интервал
        if (repairInterval > 0) {
            Long lastRepair = lastRepairTime.get(playerUUID);
            if (lastRepair != null && currentTime - lastRepair < repairInterval * 1000) {
                return false;
            }
        }
        
        boolean repaired = false;
        
        // Починка брони
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (repairItem(item)) {
                repaired = true;
            }
        }
        
        // Починка содержимого инвентаря
        for (ItemStack item : player.getInventory().getContents()) {
            if (repairItem(item)) {
                repaired = true;
            }
        }
        
        // Обновляем время последней починки
        if (repaired) {
            lastRepairTime.put(playerUUID, currentTime);
        }
        
        return repaired;
    }
    
    /**
     * Починить отдельный предмет
     * 
     * @param item Предмет
     * @return true, если предмет был починен
     */
    private boolean repairItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.getType().isItem() || item.getType().getMaxDurability() <= 0) {
            return false;
        }
        
        if (item.getDurability() > 0) {
            item.setDurability((short) 0);
            return true;
        }
        
        return false;
    }
    
    /**
     * Применить эффекты к игроку
     * 
     * @param player Игрок
     */
    public void applyEffects(Player player) {
        if (effects.isEmpty()) {
            return;
        }
        
        for (PotionEffect effect : effects.values()) {
            player.addPotionEffect(effect);
        }
    }
    
    /**
     * Удалить эффекты у игрока
     * 
     * @param player Игрок
     */
    public void removeEffects(Player player) {
        if (effects.isEmpty()) {
            return;
        }
        
        for (PotionEffectType effectType : effects.keySet()) {
            player.removePotionEffect(effectType);
        }
    }
    
    /**
     * Добавить эффект в зону
     * 
     * @param effectType Тип эффекта
     * @param amplifier Уровень эффекта (начиная с 0)
     * @param duration Длительность эффекта в секундах (-1 = бесконечно)
     */
    public void addEffect(PotionEffectType effectType, int amplifier, int duration) {
        addEffect(effectType, amplifier, duration, true, true);
    }
    
    /**
     * Добавить эффект в зону с указанием настроек отображения
     * 
     * @param effectType Тип эффекта
     * @param amplifier Уровень эффекта (начиная с 0)
     * @param duration Длительность эффекта в секундах (-1 = бесконечно)
     * @param showParticles Показывать частицы эффекта
     * @param showIcon Показывать иконку эффекта
     */
    public void addEffect(PotionEffectType effectType, int amplifier, int duration, boolean showParticles, boolean showIcon) {
        int ticks = (duration == -1) ? Integer.MAX_VALUE : duration * 20;
        PotionEffect effect = new PotionEffect(effectType, ticks, amplifier, false, showParticles, showIcon);
        effects.put(effectType, effect);
    }
    
    /**
     * Удалить эффект из зоны
     * 
     * @param effectType Тип эффекта
     */
    public void removeEffect(PotionEffectType effectType) {
        effects.remove(effectType);
    }
    
    /**
     * Очистить все эффекты из зоны
     */
    public void clearEffects() {
        effects.clear();
    }
    
    /**
     * Установить зону как зону починки
     * 
     * @param isRepair Включить/выключить
     */
    public void setRepairZone(boolean isRepair) {
        this.isRepairZone = isRepair;
    }
    
    /**
     * Установить интервал починки
     * 
     * @param seconds Интервал в секундах (0 = только при входе/выходе)
     */
    public void setRepairInterval(int seconds) {
        this.repairInterval = seconds;
    }
    
    /**
     * Проверить, является ли зона зоной починки
     * 
     * @return true, если это зона починки
     */
    public boolean isRepairZone() {
        return isRepairZone;
    }
    
    /**
     * Получить интервал починки
     * 
     * @return Интервал в секундах
     */
    public int getRepairInterval() {
        return repairInterval;
    }
    
    /**
     * Получить название зоны
     * 
     * @return Название зоны
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получить имя мира
     * 
     * @return Имя мира
     */
    public String getWorldName() {
        return worldName;
    }
    
    /**
     * Получить минимальную точку зоны
     * 
     * @return Минимальная точка
     */
    public BlockVector3 getMin() {
        return min;
    }
    
    /**
     * Получить максимальную точку зоны
     * 
     * @return Максимальная точка
     */
    public BlockVector3 getMax() {
        return max;
    }
    
    /**
     * Получить карту эффектов
     * 
     * @return Карта эффектов
     */
    public Map<PotionEffectType, PotionEffect> getEffects() {
        return new HashMap<>(effects);
    }

    /**
     * Установить зону как зону телепортации
     * 
     * @param isTeleport Включить/выключить
     */
    public void setTeleportZone(boolean isTeleport) {
        this.isTeleportZone = isTeleport;
    }
    
    /**
     * Проверить, является ли зона зоной телепортации
     * 
     * @return true, если это зона телепортации
     */
    public boolean isTeleportZone() {
        return isTeleportZone;
    }
    
    /**
     * Установить точку назначения телепортации
     * 
     * @param destination Точка назначения
     */
    public void setTeleportDestination(Location destination) {
        this.teleportDestination = destination;
    }
    
    /**
     * Получить точку назначения телепортации
     * 
     * @return Точка назначения или null, если не установлена
     */
    public Location getTeleportDestination() {
        return teleportDestination;
    }
    
    /**
     * Установить кулдаун телепортации
     * 
     * @param seconds Кулдаун в секундах
     */
    public void setTeleportCooldown(int seconds) {
        this.teleportCooldown = seconds;
    }
    
    /**
     * Получить кулдаун телепортации
     * 
     * @return Кулдаун в секундах
     */
    public int getTeleportCooldown() {
        return teleportCooldown;
    }
    
    /**
     * Телепортирует игрока к точке назначения зоны
     * 
     * @param player Игрок
     * @return true если телепортация выполнена успешно
     */
    public boolean teleportPlayer(Player player) {
        if (teleportDestination == null || !isTeleportZone) {
            return false;
        }
        
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Проверка кулдауна
        if (teleportCooldown > 0) {
            Long lastTeleport = lastTeleportTime.get(playerUUID);
            if (lastTeleport != null && currentTime - lastTeleport < teleportCooldown * 1000) {
                // Не сообщаем игроку о кулдауне, чтобы не спамить
                return false;
            }
        }
        
        // Проверяем, находится ли игрок уже в пределах области назначения 
        // (для оптимизации, чтобы избежать ненужных телепортаций)
        if (isNearLocation(player.getLocation(), teleportDestination, 3.0)) {
            return false;
        }
        
        // Обеспечиваем безопасность телепорта
        Location safeDestination = getSafeDestination(teleportDestination);
        
        try {
            // Устанавливаем метаданные для игрока, чтобы избежать рекурсии
            player.setMetadata("zonetp", new org.bukkit.metadata.FixedMetadataValue(
                org.bukkit.Bukkit.getPluginManager().getPlugin("CustomX"), true));
            
            // Сохраняем направление взгляда игрока
            float yaw = player.getLocation().getYaw();
            float pitch = player.getLocation().getPitch();
            
            // Устанавливаем точное направление взгляда в целевой локации
            safeDestination.setYaw(yaw);
            safeDestination.setPitch(pitch);
            
            // Выполняем телепортацию и сохраняем время, указывая PLUGIN как причину
            boolean success = player.teleport(safeDestination, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
            if (success) {
                lastTeleportTime.put(playerUUID, currentTime);
                
                // Обнуляем скорость падения после телепортации
                player.setFallDistance(0);
                
                // Запускаем задачу для стабилизации игрока
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    org.bukkit.Bukkit.getPluginManager().getPlugin("CustomX"),
                    () -> {
                        if (player.isOnline()) {
                            player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                        }
                    },
                    1L
                );
            }
            
            return success;
        } finally {
            // Всегда удаляем метаданные после телепортации
            player.removeMetadata("zonetp", 
                org.bukkit.Bukkit.getPluginManager().getPlugin("CustomX"));
        }
    }
    
    /**
     * Проверяет, находится ли игрок рядом с точкой назначения
     * 
     * @param loc1 Первая локация
     * @param loc2 Вторая локация
     * @param maxDistance Максимальное расстояние
     * @return true, если локации находятся в пределах указанного расстояния
     */
    private boolean isNearLocation(Location loc1, Location loc2, double maxDistance) {
        if (loc1 == null || loc2 == null || !loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        
        return loc1.distance(loc2) <= maxDistance;
    }
    
    /**
     * Получает безопасную локацию для телепортации
     * 
     * @param destination Исходная локация телепортации
     * @return Безопасная локация для телепортации
     */
    private Location getSafeDestination(Location destination) {
        // Проверяем, безопасна ли локация (не в блоке и не в воздухе)
        Location safeLoc = destination.clone();
        
        // Проверяем, что блок под ногами не воздух
        Location groundLoc = safeLoc.clone().subtract(0, 1, 0);
        if (groundLoc.getBlock().getType().isAir()) {
            // Ищем ближайший твердый блок вниз
            for (int i = 1; i < 5; i++) {
                Location checkLoc = safeLoc.clone().subtract(0, i, 0);
                if (!checkLoc.getBlock().getType().isAir()) {
                    safeLoc = checkLoc.clone().add(0, 1, 0);
                    break;
                }
            }
        }
        
        // Проверяем, что игрок не застрянет в блоке
        if (!safeLoc.getBlock().getType().isAir() || 
            !safeLoc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            // Ищем свободное место рядом
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location checkLoc = safeLoc.clone().add(x, 0, z);
                    if (checkLoc.getBlock().getType().isAir() && 
                        checkLoc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                        return checkLoc;
                    }
                }
            }
        }
        
        return safeLoc;
    }
} 