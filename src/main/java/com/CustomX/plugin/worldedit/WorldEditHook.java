package com.CustomX.plugin.worldedit;

import org.bukkit.entity.Player;

import com.CustomX.plugin.CustomX;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;

/**
 * Класс для работы с WorldEdit API
 */
public class WorldEditHook {

    private final CustomX plugin;
    private final WorldEdit worldEdit;

    /**
     * Конструктор хука WorldEdit
     * 
     * @param plugin экземпляр плагина
     */
    public WorldEditHook(CustomX plugin) {
        this.plugin = plugin;
        this.worldEdit = WorldEdit.getInstance();
    }

    /**
     * Получить выделение игрока
     * 
     * @param player игрок
     * @return регион WorldEdit или null, если нет выделения
     */
    public Region getPlayerSelection(Player player) {
        try {
            SessionManager sessionManager = worldEdit.getSessionManager();
            LocalSession localSession = sessionManager.get(BukkitAdapter.adapt(player));
            
            if (localSession.getSelectionWorld() == null) {
                return null;
            }
            
            return localSession.getSelection(localSession.getSelectionWorld());
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при получении выделения WorldEdit: " + e.getMessage());
            return null;
        }
    }

    /**
     * Проверить, есть ли у игрока выделение
     * 
     * @param player игрок
     * @return true, если у игрока есть выделение
     */
    public boolean hasSelection(Player player) {
        return getPlayerSelection(player) != null;
    }
} 