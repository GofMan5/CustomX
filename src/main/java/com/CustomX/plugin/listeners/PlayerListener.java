package com.CustomX.plugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.CustomX.plugin.CustomX;

/**
 * Класс для обработки событий, связанных с игроками
 */
public class PlayerListener implements Listener {

    private final CustomX plugin;
    
    /**
     * Конструктор класса PlayerListener
     * 
     * @param plugin экземпляр плагина
     */
    public PlayerListener(CustomX plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обработка события входа игрока на сервер
     * 
     * @param event событие входа игрока
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Обновление зон для игрока
        plugin.getZoneManager().updatePlayerZones(event.getPlayer());
    }
    
    /**
     * Обработка события выхода игрока с сервера
     * 
     * @param event событие выхода игрока
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Удаление игрока из отслеживания зон
        plugin.getZoneManager().removePlayer(event.getPlayer());
    }
    
    /**
     * Обработка события движения игрока
     * 
     * @param event событие движения игрока
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Проверяем, изменился ли блок (оптимизация)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        // Обновление зон для игрока
        plugin.getZoneManager().updatePlayerZones(event.getPlayer());
    }
    
    /**
     * Обработка события телепортации игрока
     * 
     * @param event событие телепортации игрока
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Получаем причину телепортации
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        
        // Пропускаем телепортации, вызванные плагином (чтобы избежать рекурсии)
        if (cause == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            // Проверяем, была ли это телепортация из зоны (по метаданным)
            if (event.getPlayer().hasMetadata("zonetp")) {
                // Пропускаем обработку, чтобы избежать бесконечной рекурсии
                return;
            }
        }
        
        // Обновление зон для игрока без вызова телепортации (пропуск телепортации)
        plugin.getZoneManager().updatePlayerZones(event.getPlayer(), true);
    }
    
    /**
     * Обработка события смерти игрока
     * 
     * @param event событие смерти игрока
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Удаление игрока из отслеживания зон
        plugin.getZoneManager().removePlayer(player);
    }
}

