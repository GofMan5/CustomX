package com.CustomX.plugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Arrays;

import com.CustomX.plugin.CustomX;
import com.CustomX.plugin.commands.handlers.ZoneEffectHandler;
import com.CustomX.plugin.commands.handlers.ZoneManagementHandler;
import com.CustomX.plugin.commands.handlers.ZoneHelpHandler;

/**
 * Главный обработчик команд плагина
 */
public class MainCommand implements CommandExecutor {

    private final CustomX plugin;
    
    // Обработчики команд
    private final ZoneManagementHandler zoneManagementHandler;
    private final ZoneEffectHandler zoneEffectHandler;
    private final ZoneHelpHandler zoneHelpHandler;
    
    /**
     * Конструктор класса MainCommand
     * 
     * @param plugin экземпляр плагина
     */
    public MainCommand(CustomX plugin) {
        this.plugin = plugin;
        
        // Инициализируем обработчики
        this.zoneManagementHandler = new ZoneManagementHandler(plugin);
        this.zoneEffectHandler = new ZoneEffectHandler(plugin);
        this.zoneHelpHandler = new ZoneHelpHandler(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверка, что отправитель - игрок
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Если нет аргументов, показываем основную справку
        if (args.length == 0) {
            zoneHelpHandler.showHelp(player);
            return true;
        }
        
        // Обрабатываем команды через новые обработчики
        return processCommands(player, args);
    }
    
    /**
     * Обрабатывает команды через соответствующие обработчики
     * 
     * @param player игрок, выполнивший команду
     * @param args аргументы команды
     * @return true, если команда была успешно обработана
     */
    private boolean processCommands(Player player, String[] args) {
        // Проверка прав оператора - плагин доступен только для операторов
        if (!player.isOp()) {
            player.sendMessage("§cЭтот плагин доступен только для операторов сервера!");
            return false;
        }
        
        if (args.length > 0) {
            // Сокращенные версии команд (алиасы)
            // Команды для управления зонами
            if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("create")) {
                if (args.length > 1) {
                    return zoneManagementHandler.createZone(player, args[1]);
                } else {
                    player.sendMessage("§cИспользование: /cx c [имя зоны]");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("delete")) {
                if (args.length > 1) {
                    return zoneManagementHandler.deleteZone(player, args[1]);
                } else {
                    player.sendMessage("§cИспользование: /cx d [имя зоны]");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("list")) {
                zoneManagementHandler.listZones(player);
                return true;
            }
            else if (args[0].equalsIgnoreCase("zi") || args[0].equalsIgnoreCase("info")) {
                return zoneManagementHandler.zoneInfo(player, Arrays.copyOfRange(args, 1, args.length));
            }
            else if (args[0].equalsIgnoreCase("ze") || args[0].equalsIgnoreCase("edit")) {
                return zoneManagementHandler.editZone(player, Arrays.copyOfRange(args, 1, args.length));
            }
            
            // Команды для эффектов
            else if (args[0].equalsIgnoreCase("ae") || args[0].equalsIgnoreCase("addeffect")) {
                if (args.length < 4) {
                    player.sendMessage("§cИспользование: /cx ae [зона] [эффект] [уровень] [продолжительность]");
                    return false;
                }
                String zoneName = args[1];
                String effectName = args[2];
                int level;
                int duration;
                
                try {
                    level = Integer.parseInt(args[3]);
                    duration = args.length > 4 ? Integer.parseInt(args[4]) : 0;
                    return zoneEffectHandler.addEffect(player, zoneName, effectName, level, duration);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cУровень и продолжительность должны быть числами!");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("re") || args[0].equalsIgnoreCase("removeeffect")) {
                if (args.length < 3) {
                    player.sendMessage("§cИспользование: /cx re [зона] [эффект]");
                    return false;
                }
                String zoneName = args[1];
                String effectName = args[2];
                return zoneEffectHandler.removeEffect(player, zoneName, effectName);
            }
            else if (args[0].equalsIgnoreCase("ce") || args[0].equalsIgnoreCase("cleareffects")) {
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /cx ce [зона]");
                    return false;
                }
                String zoneName = args[1];
                return zoneEffectHandler.clearEffects(player, zoneName);
            }
            else if (args[0].equalsIgnoreCase("le") || args[0].equalsIgnoreCase("listeffects")) {
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /cx le [зона]");
                    return false;
                }
                String zoneName = args[1];
                return zoneEffectHandler.listEffects(player, zoneName);
            }
            
            // Команды для телепортации
            else if (args[0].equalsIgnoreCase("zte") || args[0].equalsIgnoreCase("tpon")) {
                return zoneManagementHandler.setTeleportEnabled(player, Arrays.copyOfRange(args, 1, args.length));
            }
            else if (args[0].equalsIgnoreCase("ztd") || args[0].equalsIgnoreCase("tpoff")) {
                return zoneManagementHandler.setTeleportDisabled(player, Arrays.copyOfRange(args, 1, args.length));
            }
            else if (args[0].equalsIgnoreCase("ztdest") || args[0].equalsIgnoreCase("tpset")) {
                return zoneManagementHandler.setTeleportDestination(player, Arrays.copyOfRange(args, 1, args.length));
            }
            else if (args[0].equalsIgnoreCase("ztcool") || args[0].equalsIgnoreCase("tpcool")) {
                return zoneManagementHandler.setTeleportCooldown(player, Arrays.copyOfRange(args, 1, args.length));
            }
            else if (args[0].equalsIgnoreCase("tp")) {
                return zoneManagementHandler.teleportToZone(player, Arrays.copyOfRange(args, 1, args.length));
            }
            // Команда управления функцией починки
            else if (args[0].equalsIgnoreCase("repair")) {
                return zoneManagementHandler.setRepairMode(player, Arrays.copyOfRange(args, 1, args.length));
            }
            // Команда установки интервала починки
            else if (args[0].equalsIgnoreCase("repair_interval")) {
                return zoneManagementHandler.setRepairInterval(player, Arrays.copyOfRange(args, 1, args.length));
            }
            
            // Команды помощи
            else if (args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("help")) {
                if (args.length > 1 && args[1].equalsIgnoreCase("effects")) {
                    return zoneHelpHandler.showEffectsList(player);
                } else {
                    return zoneHelpHandler.showHelp(player);
                }
            }
            // Команда перезагрузки
            else if (args[0].equalsIgnoreCase("reload")) {
                player.sendMessage("§aПерезагрузка плагина CustomX...");
                plugin.getConfigManager().reloadConfig();
                plugin.getZoneManager().reload();
                player.sendMessage("§aПлагин CustomX успешно перезагружен!");
                return true;
            }
            // Команда просмотра потребления ресурсов
            else if (args[0].equalsIgnoreCase("gc")) {
                return zoneHelpHandler.showGarbageCollectionInfo(player);
            }
        }
        
        // Если команда не была обработана, покажем помощь
        return zoneHelpHandler.showHelp(player);
    }
}
