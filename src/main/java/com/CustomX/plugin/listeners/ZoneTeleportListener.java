package com.CustomX.plugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.CustomX.plugin.CustomX;
import com.CustomX.plugin.zones.Zone;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Класс для обработки телепортации при входе в зоны
 */
public class ZoneTeleportListener implements Listener {

    private final CustomX plugin;
    private final Set<UUID> recentlyTeleported;
    
    /**
     * Конструктор класса ZoneTeleportListener
     * 
     * @param plugin экземпляр плагина
     */
    public ZoneTeleportListener(CustomX plugin) {
        this.plugin = plugin;
        this.recentlyTeleported = new HashSet<>();
    }
    
    /**
     * Обработка события движения игрока для телепортации
     * 
     * @param event событие движения игрока
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Проверяем, изменился ли блок (оптимизация)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Если игрок недавно телепортировался, пропускаем
        if (recentlyTeleported.contains(playerUUID)) {
            return;
        }
        
        // Проверяем, вошел ли игрок в телепортационную зону
        Collection<Zone> zones = plugin.getZoneManager().getZonesForPlayer(player);
        
        for (Zone zone : zones) {
            if (zone.isTeleportZone() && zone.getTeleportDestination() != null) {
                // Добавляем игрока в список недавно телепортированных
                recentlyTeleported.add(playerUUID);
                
                // Выполняем телепортацию
                if (zone.teleportPlayer(player)) {
                    String message = ChatColor.AQUA + "Вы были телепортированы зоной " + ChatColor.GOLD + zone.getName();
                    player.sendMessage(message);
                }
                
                // Удаляем игрока из списка через секунду
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    recentlyTeleported.remove(playerUUID);
                }, 20L); // 20 тиков = 1 секунда
                
                break;
            }
        }
    }
} 