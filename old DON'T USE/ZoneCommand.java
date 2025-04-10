
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import com.CustomX.plugin.CustomX;
import com.CustomX.plugin.zones.Zone;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

/**
 * Класс для обработки команд управления зонами
 */
public class ZoneCommand implements CommandExecutor {

    private final CustomX plugin;
    
    /**
     * Конструктор класса ZoneCommand
     * 
     * @param plugin экземпляр плагина
     */
    public ZoneCommand(CustomX plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        // Обработка команды /cx help с подсекциями
        if (args[0].equalsIgnoreCase("help")) {
            if (args.length == 1) {
                showHelp(player);
                return true;
            } else if (args.length >= 2) {
                String helpSection = args[1].toLowerCase();
                switch (helpSection) {
                    case "effects":
                        showEffectsList(player);
                        break;
                    case "create":
                        showCreateHelp(player);
                        break;
                    case "edit":
                        showEditHelp(player);
                        break;
                    case "repair":
                        showRepairHelp(player);
                        break;
                    case "info":
                        showInfoHelp(player);
                        break;
                    case "admin":
                        if (player.hasPermission("CustomX.zones.admin")) {
                            showAdminHelp(player);
                        } else {
                            player.sendMessage(ChatColor.RED + "У вас нет прав на просмотр этого раздела справки!");
                        }
                        break;
                    default:
                        showHelp(player);
                        break;
                }
                return true;
            }
        }
        
        // Проверка на права администратора
        if (!player.hasPermission("CustomX.zones.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды!");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                reloadPlugin(player);
                break;
            case "zones":
                listZones(player);
                break;
            case "zone":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Использование: /cx zone [set|edit|info|delete|tp|define|tp_set] [имя зоны] [параметры]");
                    return true;
                }
                
                String zoneAction = args[1].toLowerCase();
                String zoneName = args[2];
                
                switch (zoneAction) {
                    case "tp_set":
                        setTeleportDestination(player, zoneName);
                        break;
                    case "set":
                        if (args.length < 4) {
                            player.sendMessage(ChatColor.RED + "Использование: /cx zone set [имя зоны] [repair|effect|teleport]");
                            return true;
                        }
                        
                        String zoneType = args[3].toLowerCase();
                        
                        if (zoneType.equals("repair")) {
                            setRepairZone(player, zoneName);
                        } else if (zoneType.equals("teleport")) {
                            setTeleportZone(player, zoneName);
                        } else {
                            player.sendMessage(ChatColor.RED + "Неизвестный тип зоны. Доступные типы: repair, teleport");
                        }
                        break;
                    case "edit":
                        if (args.length < 5) {
                            player.sendMessage(ChatColor.RED + "Использование: /cx zone edit [add|remove] [имя зоны] [effect|repair|teleport] [параметры]");
                            return true;
                        }
                        
                        String editAction = args[2].toLowerCase();
                        zoneName = args[3];
                        String editType = args[4].toLowerCase();
                        
                        if (editAction.equals("add")) {
                            if (editType.equals("effect")) {
                                if (args.length < 7) {
                                    player.sendMessage(ChatColor.RED + "Использование: /cx zone edit add [имя зоны] effect [эффект] [уровень] [длительность]");
                                    return true;
                                }
                                
                                String effectName = args[5].toUpperCase();
                                int level;
                                int duration;
                                
                                try {
                                    level = Integer.parseInt(args[6]) - 1; // -1 т.к. в API уровни начинаются с 0
                                    duration = args.length > 7 ? (args[7].equals("*") ? -1 : Integer.parseInt(args[7])) : -1;
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ChatColor.RED + "Уровень и длительность должны быть числами!");
                                    return true;
                                }
                                
                                addEffect(player, zoneName, effectName, level, duration);
                            } else if (editType.equals("repair")) {
                                if (args.length < 6) {
                                    player.sendMessage(ChatColor.RED + "Использование: /cx zone edit add [имя зоны] repair [yes|no]");
                                    return true;
                                }
                                
                                String repairState = args[5].toLowerCase();
                                boolean enabled = repairState.equals("yes");
                                
                                setRepairState(player, zoneName, enabled);
                            } else if (editType.equals("teleport")) {
                                if (args.length < 6) {
                                    player.sendMessage(ChatColor.RED + "Использование: /cx zone edit add [имя зоны] teleport [yes|no]");
                                    return true;
                                }
                                
                                String teleportState = args[5].toLowerCase();
                                boolean enabled = teleportState.equals("yes");
                                
                                setTeleportState(player, zoneName, enabled);
                            } else {
                                player.sendMessage(ChatColor.RED + "Неизвестный тип редактирования. Доступные типы: effect, repair, teleport");
                            }
                        } else if (editAction.equals("remove")) {
                            if (editType.equals("effect")) {
                                if (args.length < 6) {
                                    player.sendMessage(ChatColor.RED + "Использование: /cx zone edit remove [имя зоны] effect [эффект]");
                                    return true;
                                }
                                
                                String effectName = args[5].toUpperCase();
                                removeEffect(player, zoneName, effectName);
                            } else {
                                player.sendMessage(ChatColor.RED + "Неизвестный тип редактирования. Доступные типы: effect");
                            }
                        } else if (editAction.equals("cooldown")) {
                            if (editType.equals("teleport")) {
                                if (args.length < 6) {
                                    player.sendMessage(ChatColor.RED + "Использование: /cx zone edit cooldown [имя зоны] teleport [секунды]");
                                    return true;
                                }
                                
                                int cooldown;
                                try {
                                    cooldown = Integer.parseInt(args[5]);
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ChatColor.RED + "Кулдаун должен быть числом!");
                                    return true;
                                }
                                
                                setTeleportCooldown(player, zoneName, cooldown);
                            } else {
                                player.sendMessage(ChatColor.RED + "Неизвестный тип для установки кулдауна. Доступные типы: teleport");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Неизвестное действие. Доступные действия: add, remove");
                        }
                        break;
                    case "info":
                        showZoneInfo(player, zoneName);
                        break;
                    case "delete":
                        deleteZone(player, zoneName);
                        break;
                    case "tp":
                        teleportToZone(player, zoneName);
                        break;
                    case "define":
                        if (!player.hasPermission("CustomX.zones.admin")) {
                            player.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды!");
                            return true;
                        }
                        redefineZone(player, zoneName);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Неизвестное действие. Доступные действия: set, edit, info, delete, tp, define, tp_set");
                        break;
                }
                break;
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /cx create [имя зоны]");
                    return true;
                }
                
                String createZoneName = args[1];
                createZone(player, createZoneName);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Неизвестная команда. Используйте /cx help для получения списка команд.");
                break;
        }
        
        return true;
    }
    
    /**
     * Показывает основное меню справки
     * 
     * @param player игрок
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Справка" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Общая информация
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Информация о плагине:");
        player.sendMessage(ChatColor.WHITE + "  CustomX - плагин для создания и управления зонами на сервере.");
        player.sendMessage(ChatColor.WHITE + "  Зоны могут иметь различные эффекты, функцию починки и телепортации.");
        
        // Команды
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Основные команды:");
        
        // Создание зоны
        TextComponent createZoneCmd = new TextComponent("  /cx create [имя_зоны]");
        createZoneCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        createZoneCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx create "));
        createZoneCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Создать новую зону из выделенного региона WorldEdit")));
        createZoneCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Создать зону из выделения WorldEdit"));
        player.spigot().sendMessage(createZoneCmd);
        
        // Основные команды
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Основные команды:");
        
        // Создание зоны
        TextComponent createCmd = new TextComponent("  /cx zone set [имя] repair");
        createCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        createCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone set zone_name repair"));
        createCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду создания зоны")));
        createCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Создать зону"));
        player.spigot().sendMessage(createCmd);
        
        // Список зон
        TextComponent listZonesCmd = new TextComponent("  /cx zones");
        listZonesCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        listZonesCmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx zones"));
        listZonesCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы выполнить команду просмотра списка зон")));
        listZonesCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Список всех зон"));
        player.spigot().sendMessage(listZonesCmd);
        
        // Информация о зоне
        TextComponent infoCmd = new TextComponent("  /cx zone info [имя]");
        infoCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        infoCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone info zone_name"));
        infoCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду просмотра информации о зоне")));
        infoCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Информация о зоне"));
        player.spigot().sendMessage(infoCmd);
        
        // Телепортация к зоне
        TextComponent tpCmd = new TextComponent("  /cx zone tp [имя]");
        tpCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        tpCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone tp zone_name"));
        tpCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду телепортации к зоне")));
        tpCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Телепортироваться к зоне"));
        player.spigot().sendMessage(tpCmd);
        
        // Удаление зоны
        TextComponent deleteCmd = new TextComponent("  /cx zone delete [имя]");
        deleteCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        deleteCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone delete zone_name"));
        deleteCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду удаления зоны")));
        deleteCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Удалить зону"));
        player.spigot().sendMessage(deleteCmd);
        
        // Переопределение границ зоны (только для админов)
        if (player.hasPermission("CustomX.zones.admin")) {
            TextComponent defineCmd = new TextComponent("  /cx zone define [имя]");
            defineCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            defineCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone define zone_name"));
            defineCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду переопределения границ зоны")));
            defineCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Обновить границы зоны"));
            player.spigot().sendMessage(defineCmd);
        }
        
        // Сообщение о более подробной информации
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Дополнительная информация:");
        player.sendMessage(ChatColor.WHITE + "  Нажмите на команду выше для заполнения или выполнения");
        player.sendMessage(ChatColor.WHITE + "  Выберите раздел справки для получения более подробной информации");
        
        // Административные команды
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Администрирование:");
        
        // Перезагрузка плагина
        TextComponent reloadCmd = new TextComponent("  /cx reload");
        reloadCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        reloadCmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx reload"));
        reloadCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы перезагрузить плагин")));
        reloadCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Перезагрузить плагин"));
        player.spigot().sendMessage(reloadCmd);
        
        // Нижний колонтитул
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "CustomX - Справка" + ChatColor.GOLD + " ══════════╝");
    }
    
    /**
     * Показать список доступных эффектов
     * 
     * @param player игрок
     */
    private void showEffectsList(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Список эффектов" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Положительные эффекты
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.GREEN + "Положительные эффекты:");
        
        // Создадим кликабельные команды для добавления положительных эффектов
        createEffectCommand(player, "SPEED", "Увеличивает скорость передвижения");
        createEffectCommand(player, "JUMP", "Увеличивает высоту прыжка");
        createEffectCommand(player, "REGENERATION", "Восстанавливает здоровье со временем");
        createEffectCommand(player, "DAMAGE_RESISTANCE", "Уменьшает получаемый урон");
        createEffectCommand(player, "FIRE_RESISTANCE", "Защищает от огня");
        createEffectCommand(player, "NIGHT_VISION", "Позволяет видеть в темноте");
        createEffectCommand(player, "WATER_BREATHING", "Позволяет дышать под водой");
        createEffectCommand(player, "INVISIBILITY", "Делает игрока невидимым");
        
        // Отрицательные эффекты
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.RED + "Отрицательные эффекты:");
        
        // Создадим кликабельные команды для добавления отрицательных эффектов
        createEffectCommand(player, "SLOW", "Замедляет передвижение");
        createEffectCommand(player, "WEAKNESS", "Уменьшает наносимый урон");
        createEffectCommand(player, "POISON", "Наносит урон здоровью со временем");
        createEffectCommand(player, "WITHER", "Наносит урон здоровью со временем, игнорируя броню");
        createEffectCommand(player, "BLINDNESS", "Ограничивает видимость");
        createEffectCommand(player, "HUNGER", "Увеличивает голод");
        createEffectCommand(player, "CONFUSION", "Искажает экран игрока");
        createEffectCommand(player, "SLOW_DIGGING", "Замедляет скорость копания");
        
        // Примеры использования
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Примеры использования:");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone edit add my_zone effect SPEED 1 60" + ChatColor.WHITE + " - Добавляет Скорость I на 60 секунд");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone edit add my_zone effect REGENERATION 2 30" + ChatColor.WHITE + " - Добавляет Регенерацию II на 30 секунд");
        
        // Нижний колонтитул и кнопка возврата
        player.sendMessage("");
        TextComponent backButton = new TextComponent("[ Вернуться к основной справке ]");
        backButton.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        backButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help"));
        backButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы вернуться к главной справке")));
        player.spigot().sendMessage(backButton);
        
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "CustomX - Список эффектов" + ChatColor.GOLD + " ══════════╝");
    }
    
    /**
     * Создает кликабельную команду для добавления эффекта
     * 
     * @param player игрок
     * @param effectName название эффекта
     * @param description описание эффекта
     */
    private void createEffectCommand(Player player, String effectName, String description) {
        TextComponent effectCmd = new TextComponent("  " + effectName);
        effectCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        effectCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                "/cx zone edit add zone_name effect " + effectName + " 1 60"));
        effectCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Нажмите, чтобы заполнить команду добавления эффекта " + effectName)));
        
        TextComponent effectDesc = new TextComponent(" - " + description);
        effectDesc.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        effectCmd.addExtra(effectDesc);
        
        player.spigot().sendMessage(effectCmd);
    }
    
    /**
     * Показать справку по созданию зон
     * 
     * @param player игрок
     */
    private void showCreateHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Создание зон" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Инструкция по созданию зон
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Шаги по созданию зоны:");
        player.sendMessage(ChatColor.WHITE + "  1. Выделите регион с помощью WorldEdit (//wand)");
        player.sendMessage(ChatColor.WHITE + "  2. Используйте команду ниже для создания зоны");
        
        // Кликабельная команда создания зоны
        TextComponent createCmd = new TextComponent("  /cx zone set [имя_зоны] repair");
        createCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        createCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone set zone_name repair"));
        createCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду создания зоны")));
        player.spigot().sendMessage(createCmd);
        
        // Примеры
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Примеры:");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone set spawn_zone repair" + ChatColor.WHITE + " - Создать зону починки с именем 'spawn_zone'");
        
        // Дополнительная информация
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Примечания:");
        player.sendMessage(ChatColor.WHITE + "  - Имя зоны должно быть уникальным");
        player.sendMessage(ChatColor.WHITE + "  - После создания зоны вы можете добавить эффекты с помощью команды edit");
        player.sendMessage(ChatColor.WHITE + "  - Используйте /cx zones для просмотра всех созданных зон");
        
        // Нижний колонтитул и кнопка возврата
        player.sendMessage("");
        TextComponent backButton = new TextComponent("[ Вернуться к основной справке ]");
        backButton.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        backButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help"));
        backButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы вернуться к главной справке")));
        player.spigot().sendMessage(backButton);
        
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "CustomX - Создание зон" + ChatColor.GOLD + " ══════════╝");
    }
    
    /**
     * Показать справку по редактированию зон
     * 
     * @param player игрок
     */
    private void showEditHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Редактирование зон" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Команды редактирования
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Основные команды редактирования:");
        
        // Починка
        TextComponent repairCmd = new TextComponent("  /cx zone edit add [имя] repair [yes|no]");
        repairCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        repairCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone edit add zone_name repair yes"));
        repairCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду")));
        repairCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Вкл/выкл починку"));
        player.spigot().sendMessage(repairCmd);
        
        // Добавление эффекта
        TextComponent addEffectCmd = new TextComponent("  /cx zone edit add [имя] effect [эффект] [уровень] [длительность]");
        addEffectCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        addEffectCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone edit add zone_name effect SPEED 1 60"));
        addEffectCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду")));
        addEffectCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Добавить эффект"));
        player.spigot().sendMessage(addEffectCmd);
        
        // Удаление эффекта
        TextComponent removeEffectCmd = new TextComponent("  /cx zone edit remove [имя] effect [эффект]");
        removeEffectCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        removeEffectCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone edit remove zone_name effect SPEED"));
        removeEffectCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду")));
        removeEffectCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Удалить эффект"));
        player.spigot().sendMessage(removeEffectCmd);
        
        // Примеры
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Примеры:");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone edit add spawn repair yes" + ChatColor.WHITE + " - Включить починку в зоне 'spawn'");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone edit add pvp effect STRENGTH 2 30" + ChatColor.WHITE + " - Добавить Силу II на 30 секунд в зону 'pvp'");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone edit remove pvp effect STRENGTH" + ChatColor.WHITE + " - Удалить эффект Силы из зоны 'pvp'");
        
        // Просмотр информации о зоне
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Просмотр информации:");
        
        TextComponent infoCmd = new TextComponent("  /cx zone info [имя]");
        infoCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        infoCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone info zone_name"));
        infoCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду")));
        infoCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Просмотр информации о зоне"));
        player.spigot().sendMessage(infoCmd);
        
        // Кнопки навигации
        player.sendMessage("");
        TextComponent effectsButton = new TextComponent("[ Список эффектов ] ");
        effectsButton.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        effectsButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help effects"));
        effectsButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы посмотреть список доступных эффектов")));
        
        TextComponent backButton = new TextComponent("[ Вернуться к основной справке ]");
        backButton.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        backButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help"));
        backButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы вернуться к главной справке")));
        
        player.spigot().sendMessage(effectsButton);
        player.spigot().sendMessage(backButton);
        
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "CustomX - Редактирование зон" + ChatColor.GOLD + " ══════════╝");
    }
    
    /**
     * Перезагрузить плагин
     * 
     * @param player игрок
     */
    private void reloadPlugin(Player player) {
        // Перезагружаем конфигурацию
        plugin.getConfigManager().reloadConfig();
        
        // Перезагружаем зоны
        plugin.getZoneManager().reload();
        
        player.sendMessage(ChatColor.GREEN + "Плагин успешно перезагружен!");
    }
    
    /**
     * Показать список зон
     * 
     * @param player игрок
     */
    private void listZones(Player player) {
        Map<String, Zone> zones = plugin.getZoneManager().getZones();
        
        if (zones.isEmpty()) {
            player.sendMessage(ChatColor.RED + "На сервере нет зон!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "Список зон" + ChatColor.GOLD + " ══════════╗");
        
        for (Zone zone : zones.values()) {
            // Основная информация о зоне
            TextComponent zoneInfo = new TextComponent(ChatColor.YELLOW + zone.getName() + ChatColor.WHITE + " - ");
            
            List<String> zoneTypes = new ArrayList<>();
            
            if (zone.isRepairZone()) {
                zoneTypes.add(ChatColor.GREEN + "Починка");
            }
            
            if (!zone.getEffects().isEmpty()) {
                zoneTypes.add(ChatColor.BLUE + "Эффекты(" + zone.getEffects().size() + ")");
            }
            
            if (zoneTypes.isEmpty()) {
                zoneInfo.addExtra(new TextComponent(ChatColor.RED + "Без функций"));
            } else {
                zoneInfo.addExtra(new TextComponent(String.join(ChatColor.WHITE + ", ", zoneTypes)));
            }
            
            // Кнопки действий с зоной
            TextComponent infoBtn = new TextComponent(" [Инфо]");
            infoBtn.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            infoBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx zone info " + zone.getName()));
            infoBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Показать подробную информацию о зоне")));
            
            TextComponent tpBtn = new TextComponent(" [ТП]");
            tpBtn.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            tpBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx zone tp " + zone.getName()));
            tpBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Телепортироваться к зоне")));
            
            TextComponent defineBtn = new TextComponent(" [Переопределить]");
            defineBtn.setColor(net.md_5.bungee.api.ChatColor.GOLD);
            defineBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx zone define " + zone.getName()));
            defineBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Переопределить границы зоны текущим выделением WorldEdit")));
            
            // Создаем полную строку
            zoneInfo.addExtra(infoBtn);
            zoneInfo.addExtra(tpBtn);
            
            // Кнопка переопределения только для админов
            if (player.hasPermission("CustomX.zones.admin")) {
                zoneInfo.addExtra(defineBtn);
            }
            
            player.spigot().sendMessage(zoneInfo);
        }
        
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "" + ChatColor.GOLD + " ══════════╝");
    }
    
    /**
     * Создать новую зону без дополнительных функций
     * 
     * @param player игрок
     * @param zoneName имя зоны
     */
    private void createZone(Player player, String zoneName) {
        // Проверяем, существует ли уже зона с таким именем
        Zone existingZone = plugin.getZoneManager().getZone(zoneName);
        if (existingZone != null) {
            player.sendMessage(ChatColor.RED + "Зона с именем " + ChatColor.GOLD + zoneName + ChatColor.RED + " уже существует!");
            return;
        }
        
        // Получаем выделение WorldEdit
            Region selection = plugin.getWorldEditHook().getPlayerSelection(player);
            if (selection == null) {
                player.sendMessage(ChatColor.RED + "Сначала выделите регион с помощью WorldEdit!");
                return;
            }
            
        // Создаем новую зону
        if (plugin.getZoneManager().createZone(zoneName, selection)) {
            player.sendMessage(ChatColor.GREEN + "Зона " + ChatColor.GOLD + zoneName + ChatColor.GREEN + " успешно создана!");
            player.sendMessage(ChatColor.YELLOW + "Теперь вы можете добавить к ней различные функции:");
            
            // Показываем доступные команды для новой зоны
            TextComponent repairCmd = new TextComponent("  /cx zone set " + zoneName + " repair");
            repairCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            repairCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone set " + zoneName + " repair"));
            repairCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Добавить функцию починки предметов")));
            player.spigot().sendMessage(repairCmd);
            
            TextComponent teleportCmd = new TextComponent("  /cx zone set " + zoneName + " teleport");
            teleportCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            teleportCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone set " + zoneName + " teleport"));
            teleportCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Добавить функцию телепортации")));
            player.spigot().sendMessage(teleportCmd);
            
            TextComponent effectCmd = new TextComponent("  /cx zone edit add " + zoneName + " effect [эффект] [уровень] [длительность]");
            effectCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            effectCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone edit add " + zoneName + " effect SPEED 1 -1"));
            effectCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Добавить эффект зелья к зоне")));
            player.spigot().sendMessage(effectCmd);
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось создать зону " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
        }
    }
    
    /**
     * Создать зону починки
     * 
     * @param player игрок
     * @param zoneName имя зоны
     */
    private void setRepairZone(Player player, String zoneName) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx create " + zoneName);
            return;
        }
        
        // Устанавливаем функцию починки
        if (plugin.getZoneManager().setRepairZone(zoneName, true)) {
            player.sendMessage(ChatColor.GREEN + "Функция починки добавлена в зону " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось добавить функцию починки в зону " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
        }
    }
    
    /**
     * Установить состояние зоны починки
     * 
     * @param player игрок
     * @param zoneName имя зоны
     * @param enabled включить/выключить
     */
    private void setRepairState(Player player, String zoneName, boolean enabled) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + zoneName + " не найдена!");
            return;
        }
        
        // Устанавливаем функцию починки
        if (plugin.getZoneManager().setRepairZone(zoneName, enabled)) {
            player.sendMessage(ChatColor.GREEN + "Функция починки " + (enabled ? "включена" : "выключена") + " в зоне " + zoneName + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось изменить функцию починки в зоне " + zoneName + "!");
        }
    }
    
    /**
     * Добавить эффект в зону
     * 
     * @param player игрок
     * @param zoneName имя зоны
     * @param effectName название эффекта
     * @param level уровень эффекта
     * @param duration длительность эффекта в секундах (-1 = бесконечно)
     */
    private void addEffect(Player player, String zoneName, String effectName, int level, int duration) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx create " + zoneName);
            return;
        }
        
        // Проверка существования эффекта
        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        
        if (effectType == null) {
            player.sendMessage(ChatColor.RED + "Эффект " + effectName + " не найден!");
            return;
        }
        
        // Добавляем эффект
        if (plugin.getZoneManager().addEffect(zoneName, effectType, level, duration)) {
            player.sendMessage(ChatColor.GREEN + "Эффект " + ChatColor.GOLD + effectName + ChatColor.GREEN + " добавлен в зону " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось добавить эффект " + ChatColor.GOLD + effectName + ChatColor.RED + " в зону " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
        }
    }
    
    /**
     * Удалить эффект из зоны
     * 
     * @param player игрок
     * @param zoneName имя зоны
     * @param effectName название эффекта
     */
    private void removeEffect(Player player, String zoneName, String effectName) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx create " + zoneName);
            return;
        }
        
        // Проверка существования эффекта
        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        
        if (effectType == null) {
            player.sendMessage(ChatColor.RED + "Эффект " + ChatColor.GOLD + effectName + ChatColor.RED + " не найден!");
            return;
        }
        
        // Удаляем эффект
        if (plugin.getZoneManager().removeEffect(zoneName, effectType)) {
            player.sendMessage(ChatColor.GREEN + "Эффект " + ChatColor.GOLD + effectName + ChatColor.GREEN + " удален из зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось удалить эффект " + ChatColor.GOLD + effectName + ChatColor.RED + " из зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
        }
    }
    
    /**
     * Показать справку по зонам починки
     * 
     * @param player игрок
     */
    private void showRepairHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Зоны починки" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Общая информация
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Информация о зонах починки:");
        player.sendMessage(ChatColor.WHITE + "  Зоны починки автоматически восстанавливают прочность всех предметов");
        player.sendMessage(ChatColor.WHITE + "  игрока при входе в зону и периодически, если игрок остается в зоне.");
        
        // Команды для управления зонами починки
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Команды для зон починки:");
        
        // Создание зоны починки
        TextComponent createCmd = new TextComponent("  /cx zone set [имя_зоны] repair");
        createCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        createCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone set zone_name repair"));
        createCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду создания зоны починки")));
        createCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Создать зону починки"));
        player.spigot().sendMessage(createCmd);
        
        // Включение/выключение починки
        TextComponent toggleCmd = new TextComponent("  /cx zone edit add [имя_зоны] repair [yes|no]");
        toggleCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        toggleCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone edit add zone_name repair yes"));
        toggleCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду включения/выключения починки")));
        toggleCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Включить/выключить починку"));
        player.spigot().sendMessage(toggleCmd);
        
        // Примеры
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Примеры:");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone set spawn repair" + ChatColor.WHITE + " - Создать зону починки с именем 'spawn'");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone edit add spawn repair yes" + ChatColor.WHITE + " - Включить починку в зоне 'spawn'");
        player.sendMessage(ChatColor.YELLOW + "  /cx zone edit add spawn repair no" + ChatColor.WHITE + " - Выключить починку в зоне 'spawn'");
        
        // Нижний колонтитул и кнопка возврата
        player.sendMessage("");
        TextComponent backButton = new TextComponent("[ Вернуться к основной справке ]");
        backButton.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        backButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help"));
        backButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы вернуться к главной справке")));
        player.spigot().sendMessage(backButton);
        
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "CustomX - Зоны починки" + ChatColor.GOLD + " ══════════╝");
    }
    
    /**
     * Показать справку по информации о зонах
     * 
     * @param player игрок
     */
    private void showInfoHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Информация о зонах" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Команды для получения информации
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Команды для просмотра информации:");
        
        // Список зон
        TextComponent listCmd = new TextComponent("  /cx zones");
        listCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        listCmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx zones"));
        listCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы просмотреть список всех зон")));
        listCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Показать список всех зон"));
        player.spigot().sendMessage(listCmd);
        
        // Информация о зоне
        TextComponent infoCmd = new TextComponent("  /cx zone info [имя]");
        infoCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        infoCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone info zone_name"));
        infoCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду просмотра информации о зоне")));
        infoCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Показать подробную информацию о зоне"));
        player.spigot().sendMessage(infoCmd);
        
        // Телепортация к зоне
        TextComponent tpCmd = new TextComponent("  /cx zone tp [имя]");
        tpCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        tpCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone tp zone_name"));
        tpCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду телепортации к зоне")));
        tpCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Телепортироваться к зоне"));
        player.spigot().sendMessage(tpCmd);
        
        // Примеры
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Отображаемая информация:");
        player.sendMessage(ChatColor.WHITE + "  • Название зоны");
        player.sendMessage(ChatColor.WHITE + "  • Координаты зоны (мин/макс)");
        player.sendMessage(ChatColor.WHITE + "  • Статус зоны починки");
        player.sendMessage(ChatColor.WHITE + "  • Список активных эффектов с уровнями и длительностью");
        
        // Нижний колонтитул и кнопка возврата
        player.sendMessage("");
        TextComponent backButton = new TextComponent("[ Вернуться к основной справке ]");
        backButton.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        backButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help"));
        backButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы вернуться к главной справке")));
        player.spigot().sendMessage(backButton);
        
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "CustomX - Информация о зонах" + ChatColor.GOLD + " ══════════╝");
    }
    
    /**
     * Показать справку по административным командам
     * 
     * @param player игрок
     */
    private void showAdminHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Администрирование" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Административные команды
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Административные команды:");
        
        // Перезагрузка плагина
        TextComponent reloadCmd = new TextComponent("  /cx reload");
        reloadCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        reloadCmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx reload"));
        reloadCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы перезагрузить плагин")));
        reloadCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Перезагрузить плагин"));
        player.spigot().sendMessage(reloadCmd);
        
        // Удаление зоны
        TextComponent deleteCmd = new TextComponent("  /cx zone delete [имя_зоны]");
        deleteCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        deleteCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx zone delete zone_name"));
        deleteCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду удаления зоны")));
        deleteCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Удалить существующую зону"));
        player.spigot().sendMessage(deleteCmd);
        
        // Примечания по правам доступа
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Права доступа:");
        player.sendMessage(ChatColor.WHITE + "  • CustomX.zones.admin - доступ ко всем командам плагина");
        player.sendMessage(ChatColor.WHITE + "  • CustomX.zones.use - доступ к командам просмотра зон");
        
        // Примечания по безопасности
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Примечания по безопасности:");
        player.sendMessage(ChatColor.WHITE + "  • Удаление зоны не может быть отменено");
        player.sendMessage(ChatColor.WHITE + "  • Перезагрузка плагина перезагружает все зоны из конфигурации");
        player.sendMessage(ChatColor.WHITE + "  • Все изменения автоматически сохраняются в конфигурации");
        
        // Нижний колонтитул и кнопки навигации
        player.sendMessage("");
        TextComponent backButton = new TextComponent("[ Вернуться к основной справке ]");
        backButton.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        backButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help"));
        backButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы вернуться к главной справке")));
        player.spigot().sendMessage(backButton);
        
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "CustomX - Администрирование" + ChatColor.GOLD + " ══════════╝");
    }
    
    /**
     * Показать информацию о зоне
     * 
     * @param player игрок
     * @param zoneName имя зоны
     */
    private void showZoneInfo(Player player, String zoneName) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + zoneName + " не найдена!");
            return;
        }
        
        // Выводим информацию о зоне
        player.sendMessage(ChatColor.GOLD + "========== Информация о зоне " + zoneName + " ==========");
        
        // Основная информация
        player.sendMessage(ChatColor.YELLOW + "Мир: " + ChatColor.WHITE + zone.getWorldName());
        player.sendMessage(ChatColor.YELLOW + "Координаты: " + ChatColor.WHITE + 
                "от [" + zone.getMin().getX() + ", " + zone.getMin().getY() + ", " + zone.getMin().getZ() + "] " +
                "до [" + zone.getMax().getX() + ", " + zone.getMax().getY() + ", " + zone.getMax().getZ() + "]");
        
        // Информация о починке
        if (zone.isRepairZone()) {
            String interval = zone.getRepairInterval() > 0 ? 
                    "каждые " + zone.getRepairInterval() + " секунд" : 
                    "только при входе";
            player.sendMessage(ChatColor.YELLOW + "Починка: " + ChatColor.GREEN + "Включена (" + interval + ")");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Починка: " + ChatColor.RED + "Выключена");
        }
        
        // Информация об эффектах
        Map<PotionEffectType, PotionEffect> effects = zone.getEffects();
        
        if (effects.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Эффекты: " + ChatColor.RED + "Отсутствуют");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Эффекты:");
            
            for (Map.Entry<PotionEffectType, PotionEffect> entry : effects.entrySet()) {
                PotionEffect effect = entry.getValue();
                String effectName = entry.getKey().getName();
                int amplifier = effect.getAmplifier() + 1; // +1 т.к. в API уровни начинаются с 0
                String duration = effect.getDuration() == Integer.MAX_VALUE ? 
                        "бесконечно" : 
                        (effect.getDuration() / 20) + " секунд";
                
                String effectColor = isPositiveEffect(entry.getKey()) ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
                
                player.sendMessage("  " + effectColor + effectName + " " + 
                        translateRomanNumeral(amplifier) + ChatColor.WHITE + " - " + duration);
            }
        }
        
        // Кнопки действий
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Действия с зоной:");
        
        // Добавить эффект
        TextComponent addEffectBtn = new TextComponent("  [ Добавить эффект ]");
        addEffectBtn.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        addEffectBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                "/cx zone edit add " + zoneName + " effect SPEED 1 60"));
        addEffectBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Добавить эффект к зоне " + zoneName)));
        player.spigot().sendMessage(addEffectBtn);
        
        // Управление починкой
        String repairCommand = zone.isRepairZone() ? 
                "/cx zone edit add " + zoneName + " repair no" : 
                "/cx zone edit add " + zoneName + " repair yes";
        String repairText = zone.isRepairZone() ? "[ Выключить починку ]" : "[ Включить починку ]";
        TextComponent repairBtn = new TextComponent("  " + repairText);
        repairBtn.setColor(zone.isRepairZone() ? net.md_5.bungee.api.ChatColor.RED : net.md_5.bungee.api.ChatColor.GREEN);
        repairBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, repairCommand));
        repairBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text((zone.isRepairZone() ? "Выключить" : "Включить") + " починку в зоне " + zoneName)));
        player.spigot().sendMessage(repairBtn);
        
        // Удалить зону
        TextComponent deleteBtn = new TextComponent("  [ Удалить зону ]");
        deleteBtn.setColor(net.md_5.bungee.api.ChatColor.RED);
        deleteBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                "/cx zone delete " + zoneName));
        deleteBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Удалить зону " + zoneName)));
        player.spigot().sendMessage(deleteBtn);
    }
    
    /**
     * Удалить зону
     * 
     * @param player игрок
     * @param zoneName имя зоны
     */
    private void deleteZone(Player player, String zoneName) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + zoneName + " не найдена!");
            return;
        }
        
        // Удаляем зону
        if (plugin.getZoneManager().deleteZone(zoneName)) {
            player.sendMessage(ChatColor.GREEN + "Зона " + zoneName + " успешно удалена!");
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось удалить зону " + zoneName + "!");
        }
    }
    
    /**
     * Проверить, является ли эффект положительным
     * 
     * @param effectType тип эффекта
     * @return true, если эффект положительный
     */
    private boolean isPositiveEffect(PotionEffectType effectType) {
        return effectType == PotionEffectType.SPEED || 
                effectType == PotionEffectType.FAST_DIGGING || 
                effectType == PotionEffectType.INCREASE_DAMAGE || 
                effectType == PotionEffectType.JUMP || 
                effectType == PotionEffectType.REGENERATION || 
                effectType == PotionEffectType.DAMAGE_RESISTANCE || 
                effectType == PotionEffectType.FIRE_RESISTANCE || 
                effectType == PotionEffectType.WATER_BREATHING || 
                effectType == PotionEffectType.INVISIBILITY || 
                effectType == PotionEffectType.NIGHT_VISION || 
                effectType == PotionEffectType.HEALTH_BOOST || 
                effectType == PotionEffectType.ABSORPTION || 
                effectType == PotionEffectType.SATURATION || 
                effectType == PotionEffectType.GLOWING || 
                effectType == PotionEffectType.LUCK || 
                effectType == PotionEffectType.LEVITATION;
    }
    
    /**
     * Преобразовать число в римскую запись
     * 
     * @param num число (1-10)
     * @return римская запись
     */
    private String translateRomanNumeral(int num) {
        switch (num) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(num);
        }
    }

    /**
     * Телепортирует игрока к зоне
     * 
     * @param player игрок
     * @param zoneName имя зоны
     */
    private void teleportToZone(Player player, String zoneName) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + zoneName + " не найдена!");
            return;
        }
        
        // Проверка, существует ли мир
        World world = Bukkit.getWorld(zone.getWorldName());
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Мир зоны не найден: " + zone.getWorldName());
            return;
        }
        
        // Вычисляем центр зоны
        BlockVector3 min = zone.getMin();
        BlockVector3 max = zone.getMax();
        
        int x = min.getX() + (max.getX() - min.getX()) / 2;
        int y = min.getY() + (max.getY() - min.getY()) / 2;
        int z = min.getZ() + (max.getZ() - min.getZ()) / 2;
        
        // Находим безопасную позицию для телепортации
        Location target = new Location(world, x, y, z);
        
        // Простая проверка безопасности - находим верхний непустой блок
        while (!world.getBlockAt(x, y, z).getType().isAir() && y < max.getY()) {
            y++;
        }
        
        // Если достигли верха зоны и все ещё в блоке, найдем другую точку
        if (y >= max.getY()) {
            y = min.getY();
            while (!world.getBlockAt(x, y, z).getType().isAir() && y > 0) {
                y--;
            }
            
            // Если не нашли безопасное место, используем оригинальный центр
            if (y <= 0) {
                y = (min.getY() + max.getY()) / 2;
            }
        }
        
        // Используем безопасную точку для телепортации
        target.setY(y);
        
        // Телепортируем игрока
        player.teleport(target);
        player.sendMessage(ChatColor.GREEN + "Вы телепортированы к зоне " + zoneName);
    }

    /**
     * Переопределяет границы существующей зоны
     * 
     * @param player игрок
     * @param zoneName имя зоны
     */
    private void redefineZone(Player player, String zoneName) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + zoneName + " не найдена!");
            return;
        }
        
        // Получаем текущее выделение WorldEdit
        Region selection = plugin.getWorldEditHook().getPlayerSelection(player);
        
        if (selection == null) {
            player.sendMessage(ChatColor.RED + "Сначала выделите новую область с помощью WorldEdit!");
            return;
        }
        
        // Проверяем, что выделение находится в том же мире
        if (!selection.getWorld().getName().equals(zone.getWorldName())) {
            player.sendMessage(ChatColor.RED + "Выделение должно быть в том же мире, что и зона: " + zone.getWorldName());
            return;
        }
        
        // Обновляем границы зоны
        if (plugin.getZoneManager().updateZoneBoundaries(zoneName, selection)) {
            player.sendMessage(ChatColor.GREEN + "Границы зоны " + zoneName + " обновлены!");
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось обновить границы зоны " + zoneName + "!");
        }
    }

    /**
     * Установить зону как телепортационную
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Имя зоны
     */
    private void setTeleportZone(Player player, String zoneName) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx create " + zoneName);
            return;
        }
        
        if (plugin.getZoneManager().setTeleportZone(zoneName, true)) {
            player.sendMessage(
                ChatColor.GREEN + "Зона " + ChatColor.GOLD + zoneName + ChatColor.GREEN + " теперь является телепортационной зоной!" +
                ChatColor.YELLOW + " Не забудьте установить точку назначения: " + ChatColor.WHITE + "/cx zone " + zoneName + " tp_set"
            );
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось настроить зону " + ChatColor.GOLD + zoneName + ChatColor.RED + " как телепортационную!");
        }
    }
    
    /**
     * Установить телепортационное состояние зоны
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Имя зоны
     * @param enabled Включено/выключено
     */
    private void setTeleportState(Player player, String zoneName, boolean enabled) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx create " + zoneName);
            return;
        }
        
        if (plugin.getZoneManager().setTeleportZone(zoneName, enabled)) {
            player.sendMessage(
                ChatColor.GREEN + "Телепортация " + ChatColor.GOLD + (enabled ? "включена" : "выключена") + 
                ChatColor.GREEN + " для зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!"
            );
            
            if (enabled) {
                if (zone.getTeleportDestination() == null) {
                    player.sendMessage(
                        ChatColor.YELLOW + "Не забудьте установить точку назначения: " + 
                        ChatColor.WHITE + "/cx zone " + zoneName + " tp_set"
                    );
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось изменить настройки телепортации для зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
        }
    }
    
    /**
     * Установить кулдаун телепортации для зоны
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Имя зоны
     * @param seconds Кулдаун в секундах
     */
    private void setTeleportCooldown(Player player, String zoneName, int seconds) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx create " + zoneName);
            return;
        }
        
        if (plugin.getZoneManager().setTeleportCooldown(zoneName, seconds)) {
            player.sendMessage(
                ChatColor.GREEN + "Кулдаун телепортации установлен на " + ChatColor.GOLD + seconds + 
                ChatColor.GREEN + " сек. для зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!"
            );
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось установить кулдаун телепортации для зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
        }
    }
    
    /**
     * Установить точку назначения телепортации
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Имя зоны
     */
    private void setTeleportDestination(Player player, String zoneName) {
        // Проверка существования зоны
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx create " + zoneName);
            return;
        }
        
        // Используем текущую позицию игрока как точку назначения
        Location destination = player.getLocation();
        
        if (plugin.getZoneManager().setTeleportDestination(zoneName, destination)) {
            player.sendMessage(
                ChatColor.GREEN + "Точка телепортации для зоны " + ChatColor.GOLD + zoneName + 
                ChatColor.GREEN + " установлена на вашу текущую позицию!"
            );
            
            // Если зона еще не является телепортационной, предложим включить телепортацию
            if (!zone.isTeleportZone()) {
                player.sendMessage(
                    ChatColor.YELLOW + "Зона пока не является телепортационной. Включить телепортацию? " +
                    ChatColor.WHITE + "/cx zone edit add " + zoneName + " teleport yes"
                );
            }
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось установить точку телепортации для зоны " + 
                             ChatColor.GOLD + zoneName + ChatColor.RED + "!");
        }
    }
} 