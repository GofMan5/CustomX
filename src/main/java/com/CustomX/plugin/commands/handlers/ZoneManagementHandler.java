package com.CustomX.plugin.commands.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.CustomX.plugin.CustomX;
import com.CustomX.plugin.worldedit.WorldEditHook;
import com.CustomX.plugin.zones.Zone;
import com.CustomX.plugin.zones.ZoneManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Обработчик команд управления зонами
 */
public class ZoneManagementHandler extends BaseCommandHandler {
    
    private ZoneManager zoneManager;
    private WorldEditHook worldEditHook;
    
    /**
     * Конструктор обработчика управления зонами
     * 
     * @param plugin Экземпляр плагина
     */
    public ZoneManagementHandler(CustomX plugin) {
        super(plugin);
        this.zoneManager = plugin.getZoneManager();
        this.worldEditHook = plugin.getWorldEditHook();
    }
    
    /**
     * Показывает список всех зон
     * 
     * @param player игрок
     */
    public void listZones(Player player) {
        Map<String, Zone> zonesMap = zoneManager.getZones();
        Collection<Zone> zones = zonesMap.values();
        
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "Список зон" + ChatColor.GOLD + " ══════════╗");
        
        if (zones.isEmpty()) {
            player.sendMessage(ChatColor.RED + "  Зоны не найдены!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "  Найдено " + zones.size() + " зон:");
            player.sendMessage("");
            
            for (Zone zone : zones) {
                TextComponent zoneInfo = new TextComponent("  • " + zone.getName());
                zoneInfo.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                
                // Информация о зоне
                TextComponent infoBtn = new TextComponent(" [Инфо]");
                infoBtn.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                infoBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx zi " + zone.getName()));
                infoBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Показать информацию о зоне " + zone.getName())));
                
                // ТП к зоне
                TextComponent tpBtn = new TextComponent(" [ТП]");
                tpBtn.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                tpBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx tp " + zone.getName()));
                tpBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Телепортироваться к зоне " + zone.getName())));
                
                // Удаление зоны
                TextComponent deleteBtn = new TextComponent(" [Удалить]");
                deleteBtn.setColor(net.md_5.bungee.api.ChatColor.RED);
                deleteBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx d " + zone.getName()));
                deleteBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Удалить зону " + zone.getName())));
                
                // Добавляем все кнопки
                zoneInfo.addExtra(infoBtn);
                zoneInfo.addExtra(tpBtn);
                zoneInfo.addExtra(deleteBtn);
                
                player.spigot().sendMessage(zoneInfo);
            }
        }
        
        player.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════╝");
    }
    
    /**
     * Показывает информацию о зоне
     * 
     * @param player игрок
     * @param zoneName имя зоны
     */
    public void showZoneInfo(Player player, String zoneName) {
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона с именем " + zoneName + " не найдена!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "Информация о зоне: " + zone.getName() + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Основная информация
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Основная информация:");
        player.sendMessage(ChatColor.WHITE + "  Мир: " + ChatColor.YELLOW + zone.getWorldName());
        player.sendMessage(ChatColor.WHITE + "  Координаты: " + ChatColor.YELLOW + 
                            "от (" + zone.getMin().getBlockX() + ", " + zone.getMin().getBlockY() + ", " + zone.getMin().getBlockZ() + ") " +
                            "до (" + zone.getMax().getBlockX() + ", " + zone.getMax().getBlockY() + ", " + zone.getMax().getBlockZ() + ")");
        
        // Статус зоны
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Статус зоны:");
        
        // Статус зоны починки с кнопкой переключения
        String repairText = zone.isRepairZone() ? 
                ChatColor.GREEN + "Включена" + ChatColor.WHITE + " (интервал: " + zone.getRepairInterval() + " сек)" : 
                ChatColor.RED + "Выключена";
                
        TextComponent repairStatus = new TextComponent(ChatColor.WHITE + "  Зона починки: " + repairText);
        TextComponent toggleRepair = new TextComponent(ChatColor.GRAY + " [" + (zone.isRepairZone() ? ChatColor.RED + "Выключить" : ChatColor.GREEN + "Включить") + ChatColor.GRAY + "]");
        toggleRepair.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                "/cx repair " + zoneName + " " + (zone.isRepairZone() ? "off" : "on")));
        toggleRepair.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text((zone.isRepairZone() ? "Выключить" : "Включить") + " зону починки")));
        repairStatus.addExtra(toggleRepair);
        player.spigot().sendMessage(repairStatus);
        
        // Статус зоны телепортации с кнопками управления
        String teleportText = zone.isTeleportZone() ? 
                ChatColor.GREEN + "Включена" + ChatColor.WHITE + " (кд: " + zone.getTeleportCooldown() + " сек)" : 
                ChatColor.RED + "Выключена";
                
        TextComponent teleportStatus = new TextComponent(ChatColor.WHITE + "  Зона телепортации: " + teleportText);
        
        // Кнопка включения/выключения телепортации
        TextComponent toggleTeleport = new TextComponent(ChatColor.GRAY + " [" + (zone.isTeleportZone() ? ChatColor.RED + "Выключить" : ChatColor.GREEN + "Включить") + ChatColor.GRAY + "]");
        toggleTeleport.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                "/cx " + (zone.isTeleportZone() ? "tpoff" : "tpon") + " " + zoneName));
        toggleTeleport.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text((zone.isTeleportZone() ? "Выключить" : "Включить") + " телепортацию")));
                
        // Кнопка установки точки телепортации
        TextComponent setTpPoint = new TextComponent(ChatColor.GRAY + " [" + ChatColor.YELLOW + "Установить точку" + ChatColor.GRAY + "]");
        setTpPoint.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx tpset " + zoneName));
        setTpPoint.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Установить текущую позицию как точку телепортации")));
        
        // Настройка кулдауна телепортации
        TextComponent cooldownBtn = new TextComponent(ChatColor.GRAY + " [" + ChatColor.YELLOW + "Кулдаун" + ChatColor.GRAY + "]");
        cooldownBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                "/cx tpcool " + zoneName + " "));
        cooldownBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Установить кулдаун телепортации в секундах")));
        
        teleportStatus.addExtra(toggleTeleport);
        teleportStatus.addExtra(setTpPoint);
        teleportStatus.addExtra(cooldownBtn);
        player.spigot().sendMessage(teleportStatus);
        
        // Список эффектов с интерактивными кнопками
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Эффекты зоны:");
        
        Map<PotionEffectType, PotionEffect> effectsMap = zone.getEffects();
        Collection<PotionEffect> effects = effectsMap.values();
        
        // Создаем кнопки для выбора эффектов напрямую
        TextComponent addEffectBtn = new TextComponent(ChatColor.GREEN + "  [+ Добавить эффект]");
        addEffectBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx le " + zoneName));
        addEffectBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Открыть меню добавления эффектов")));
        player.spigot().sendMessage(addEffectBtn);
        
        // Отображаем кнопки для популярных эффектов
        TextComponent speedBtn = new TextComponent(ChatColor.AQUA + "  [Скорость]");
        speedBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx ae " + zoneName + " SPEED "));
        speedBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Добавить эффект скорости (введите уровень и продолжительность)")));
        
        TextComponent strengthBtn = new TextComponent(ChatColor.RED + "  [Сила]");
        strengthBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx ae " + zoneName + " INCREASE_DAMAGE "));
        strengthBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Добавить эффект силы (введите уровень и продолжительность)")));
        
        TextComponent jumpBtn = new TextComponent(ChatColor.GREEN + "  [Прыжок]");
        jumpBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx ae " + zoneName + " JUMP "));
        jumpBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Добавить эффект прыжка (введите уровень и продолжительность)")));
        
        TextComponent resistanceBtn = new TextComponent(ChatColor.BLUE + "  [Защита]");
        resistanceBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx ae " + zoneName + " DAMAGE_RESISTANCE "));
        resistanceBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Добавить эффект защиты (введите уровень и продолжительность)")));
        
        TextComponent regenBtn = new TextComponent(ChatColor.LIGHT_PURPLE + "  [Регенерация]");
        regenBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx ae " + zoneName + " REGENERATION "));
        regenBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Добавить эффект регенерации (введите уровень и продолжительность)")));
        
        // Отображаем выбор популярных эффектов, если нет эффектов
        if (effects.isEmpty()) {
            TextComponent noEffects = new TextComponent(ChatColor.RED + "  Эффекты отсутствуют");
            player.spigot().sendMessage(noEffects);
            
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "  Быстрое добавление эффектов:");
            player.spigot().sendMessage(speedBtn);
            player.spigot().sendMessage(strengthBtn);
            player.spigot().sendMessage(jumpBtn);
            player.spigot().sendMessage(resistanceBtn);
            player.spigot().sendMessage(regenBtn);
            
            // Ссылка на полный список эффектов
            TextComponent allEffectsBtn = new TextComponent(ChatColor.YELLOW + "  [Посмотреть все эффекты]");
            allEffectsBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help effects"));
            allEffectsBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    new Text("Показать полный список доступных эффектов")));
            player.spigot().sendMessage(allEffectsBtn);
        } else {
            // Кнопка для очистки всех эффектов
            TextComponent clearEffectsBtn = new TextComponent(ChatColor.RED + "  [× Очистить все эффекты]");
            clearEffectsBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                    "/cx ce " + zoneName));
            clearEffectsBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    new Text("Удалить все эффекты из зоны")));
            player.spigot().sendMessage(clearEffectsBtn);
            
            player.sendMessage("");
            
            // Список эффектов с кнопками удаления для каждого
            for (PotionEffect effect : effects) {
                String effectName = getEffectName(effect.getType());
                int level = effect.getAmplifier() + 1;
                int duration = effect.getDuration() / 20; // Конвертируем тики в секунды
                String durationText = (duration == Integer.MAX_VALUE / 20) ? "∞" : duration + " сек";
                
                TextComponent effectInfo = new TextComponent(ChatColor.WHITE + "  • " + effectName + " " + translateRomanNumeral(level) + 
                                  ChatColor.WHITE + " (" + durationText + ")");
                
                // Кнопка удаления эффекта
                TextComponent removeBtn = new TextComponent(ChatColor.GRAY + " [" + ChatColor.RED + "×" + ChatColor.GRAY + "]");
                removeBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                        "/cx re " + zoneName + " " + effect.getType().getName()));
                removeBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        new Text("Удалить эффект " + effect.getType().getName())));
                
                effectInfo.addExtra(removeBtn);
                player.spigot().sendMessage(effectInfo);
            }
        }
        
        // Кнопки управления
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Основные действия:");
        
        // Создаем кнопки для взаимодействия с зоной
        TextComponent tpBtn = new TextComponent("  [Телепортироваться]");
        tpBtn.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        tpBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx tp " + zone.getName()));
        tpBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Телепортироваться к зоне " + zone.getName())));
        player.spigot().sendMessage(tpBtn);
        
        TextComponent redefineBtn = new TextComponent("  [Изменить границы]");
        redefineBtn.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        redefineBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx ze " + zone.getName()));
        redefineBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Изменить границы зоны текущим выделением WorldEdit")));
        player.spigot().sendMessage(redefineBtn);
        
        TextComponent deleteBtn = new TextComponent("  [Удалить зону]");
        deleteBtn.setColor(net.md_5.bungee.api.ChatColor.RED);
        deleteBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx d " + zone.getName()));
        deleteBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Удалить зону " + zone.getName())));
        player.spigot().sendMessage(deleteBtn);
        
        // Нижний колонтитул
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════╝");
    }
    
    /**
     * Создает зону из WorldEdit региона
     * 
     * @param player игрок
     * @param zoneName имя зоны
     * @return true если зона создана успешно
     */
    public boolean createZone(Player player, String zoneName) {
        // Проверяем, существует ли уже зона с таким именем
        if (zoneManager.getZone(zoneName) != null) {
            player.sendMessage(ChatColor.RED + "Зона с именем " + zoneName + " уже существует!");
            return false;
        }
        
        // Получаем регион WorldEdit
        Region region = worldEditHook.getPlayerSelection(player);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Сначала выделите регион с помощью WorldEdit!");
            return false;
        }
        
        // Создаем зону
        if (zoneManager.createZone(zoneName, region)) {
            player.sendMessage(ChatColor.GREEN + "Зона " + zoneName + " успешно создана!");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось создать зону " + zoneName + "!");
            return false;
        }
    }
    
    /**
     * Обновляет границы существующей зоны
     * 
     * @param player игрок
     * @param zoneName имя зоны
     * @return true если границы обновлены успешно
     */
    public boolean updateZoneBoundaries(Player player, String zoneName) {
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона с именем " + zoneName + " не найдена!");
            return false;
        }
        
        // Получаем регион WorldEdit
        Region region = worldEditHook.getPlayerSelection(player);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Сначала выделите новый регион с помощью WorldEdit!");
            return false;
        }
        
        // Обновляем границы зоны через ZoneManager
        if (zoneManager.updateZoneBoundaries(zoneName, region)) {
            player.sendMessage(ChatColor.GREEN + "Границы зоны " + zoneName + " успешно обновлены!");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось обновить границы зоны " + zoneName + "!");
            return false;
        }
    }
    
    /**
     * Удаляет зону
     * 
     * @param player игрок
     * @param zoneName имя зоны
     * @return true если зона удалена успешно
     */
    public boolean deleteZone(Player player, String zoneName) {
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона с именем " + zoneName + " не найдена!");
            return false;
        }
        
        // Удаляем зону
        if (zoneManager.deleteZone(zoneName)) {
            player.sendMessage(ChatColor.GREEN + "Зона " + zoneName + " успешно удалена!");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось удалить зону " + zoneName + "!");
            return false;
        }
    }
    
    /**
     * Возвращает название эффекта на русском языке
     * 
     * @param type тип эффекта
     * @return название эффекта
     */
    private String getEffectName(PotionEffectType type) {
        if (type.equals(PotionEffectType.SPEED)) return ChatColor.AQUA + "Скорость";
        if (type.equals(PotionEffectType.SLOW)) return ChatColor.RED + "Замедление";
        if (type.equals(PotionEffectType.FAST_DIGGING)) return ChatColor.YELLOW + "Скорость копания";
        if (type.equals(PotionEffectType.SLOW_DIGGING)) return ChatColor.GRAY + "Усталость";
        if (type.equals(PotionEffectType.INCREASE_DAMAGE)) return ChatColor.RED + "Сила";
        if (type.equals(PotionEffectType.HEAL)) return ChatColor.LIGHT_PURPLE + "Мгновенное лечение";
        if (type.equals(PotionEffectType.HARM)) return ChatColor.DARK_PURPLE + "Мгновенный урон";
        if (type.equals(PotionEffectType.JUMP)) return ChatColor.GREEN + "Прыгучесть";
        if (type.equals(PotionEffectType.CONFUSION)) return ChatColor.DARK_GREEN + "Тошнота";
        if (type.equals(PotionEffectType.REGENERATION)) return ChatColor.LIGHT_PURPLE + "Регенерация";
        if (type.equals(PotionEffectType.DAMAGE_RESISTANCE)) return ChatColor.BLUE + "Сопротивление";
        if (type.equals(PotionEffectType.FIRE_RESISTANCE)) return ChatColor.GOLD + "Огнестойкость";
        if (type.equals(PotionEffectType.WATER_BREATHING)) return ChatColor.BLUE + "Подводное дыхание";
        if (type.equals(PotionEffectType.INVISIBILITY)) return ChatColor.GRAY + "Невидимость";
        if (type.equals(PotionEffectType.BLINDNESS)) return ChatColor.DARK_GRAY + "Слепота";
        if (type.equals(PotionEffectType.NIGHT_VISION)) return ChatColor.BLUE + "Ночное зрение";
        if (type.equals(PotionEffectType.HUNGER)) return ChatColor.DARK_GREEN + "Голод";
        if (type.equals(PotionEffectType.WEAKNESS)) return ChatColor.DARK_GRAY + "Слабость";
        if (type.equals(PotionEffectType.POISON)) return ChatColor.DARK_GREEN + "Отравление";
        if (type.equals(PotionEffectType.WITHER)) return ChatColor.DARK_GRAY + "Иссушение";
        if (type.equals(PotionEffectType.HEALTH_BOOST)) return ChatColor.GOLD + "Повышение здоровья";
        if (type.equals(PotionEffectType.ABSORPTION)) return ChatColor.GOLD + "Поглощение";
        if (type.equals(PotionEffectType.SATURATION)) return ChatColor.YELLOW + "Насыщение";
        
        return ChatColor.WHITE + type.getName();
    }
    
    /**
     * Установить зону как телепортационную
     * 
     * @param player Игрок
     * @param args Аргументы команды
     * @return true если операция выполнена успешно
     */
    public boolean setTeleportEnabled(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Необходимо указать название зоны!");
            return false;
        }
        
        String zoneName = args[0];
        return setTeleportState(player, zoneName, true);
    }
    
    /**
     * Отключить зону телепортации
     * 
     * @param player Игрок, выполняющий команду
     * @param args Аргументы команды
     * @return true если команда выполнена успешно
     */
    public boolean setTeleportDisabled(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Необходимо указать название зоны!");
            return false;
        }
        
        String zoneName = args[0];
        return setTeleportState(player, zoneName, false);
    }
    
    /**
     * Включить/выключить функцию починки в зоне
     * 
     * @param player Игрок, выполняющий команду
     * @param args Аргументы команды
     * @return true, если операция выполнена успешно
     */
    public boolean setRepairMode(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /cx repair [зона] [on/off]");
            return false;
        }
        
        String zoneName = args[0];
        String mode = args[1].toLowerCase();
        
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            return false;
        }
        
        boolean enable = mode.equals("on");
        
        if (zoneManager.setRepairZone(zoneName, enable)) {
            player.sendMessage(ChatColor.GREEN + "Функция починки " + (enable ? "включена" : "отключена") + 
                              " для зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!");
            
            // Если включили, предложим настроить интервал
            if (enable) {
                TextComponent msgComponent = new TextComponent(ChatColor.WHITE + "Нажмите для настройки интервала: ");
                TextComponent setIntervalBtn = new TextComponent(ChatColor.YELLOW + "[Настроить интервал]");
                setIntervalBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                        "/cx repair_interval " + zoneName + " "));
                setIntervalBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        new Text("Установить интервал починки в секундах (0 = только при входе/выходе)")));
                
                msgComponent.addExtra(setIntervalBtn);
                player.spigot().sendMessage(msgComponent);
            }
            
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось " + (enable ? "включить" : "отключить") + 
                              " функцию починки для зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
    
    /**
     * Устанавливает состояние телепортации для зоны
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Имя зоны
     * @param enabled Включено/выключено
     * @return true если команда выполнена успешно
     */
    private boolean setTeleportState(Player player, String zoneName, boolean enabled) {
        // Проверка существования зоны
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx c " + zoneName);
            return false;
        }
        
        if (zoneManager.setTeleportZone(zoneName, enabled)) {
            player.sendMessage(
                ChatColor.GREEN + "Телепортация " + ChatColor.GOLD + (enabled ? "включена" : "выключена") + 
                ChatColor.GREEN + " для зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!"
            );
            
            if (enabled) {
                if (zone.getTeleportDestination() == null) {
                    player.sendMessage(
                        ChatColor.YELLOW + "Не забудьте установить точку назначения: " + 
                        ChatColor.WHITE + "/cx ztdest " + zoneName
                    );
                }
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось изменить настройки телепортации для зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
    
    /**
     * Устанавливает кулдаун телепортации для зоны
     * 
     * @param player Игрок, выполняющий команду
     * @param args Аргументы команды
     * @return true если команда выполнена успешно
     */
    public boolean setTeleportCooldown(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /cx ztcool [имя зоны] [секунды]");
            return false;
        }
        
        String zoneName = args[0];
        int seconds;
        
        try {
            seconds = Integer.parseInt(args[1]);
            if (seconds < 0) {
                player.sendMessage(ChatColor.RED + "Кулдаун не может быть отрицательным!");
                return false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Кулдаун должен быть числом!");
            return false;
        }
        
        // Проверка существования зоны
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx c " + zoneName);
            return false;
        }
        
        if (zoneManager.setTeleportCooldown(zoneName, seconds)) {
            player.sendMessage(
                ChatColor.GREEN + "Кулдаун телепортации установлен на " + ChatColor.GOLD + seconds + 
                ChatColor.GREEN + " сек. для зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!"
            );
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось установить кулдаун телепортации для зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
    
    /**
     * Устанавливает точку назначения телепортации
     * 
     * @param player Игрок, выполняющий команду
     * @param args Аргументы команды
     * @return true если команда выполнена успешно
     */
    public boolean setTeleportDestination(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /cx ztdest [имя зоны]");
            return false;
        }
        
        String zoneName = args[0];
        
        // Проверка существования зоны
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx c " + zoneName);
            return false;
        }
        
        // Используем текущую позицию игрока как точку назначения
        Location destination = player.getLocation();
        
        if (zoneManager.setTeleportDestination(zoneName, destination)) {
            player.sendMessage(
                ChatColor.GREEN + "Точка телепортации для зоны " + ChatColor.GOLD + zoneName + 
                ChatColor.GREEN + " установлена на вашу текущую позицию!"
            );
            
            // Если зона еще не является телепортационной, предложим включить телепортацию
            if (!zone.isTeleportZone()) {
                player.sendMessage(
                    ChatColor.YELLOW + "Зона пока не является телепортационной. Включить телепортацию? " +
                    ChatColor.WHITE + "/cx zte " + zoneName
                );
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось установить точку телепортации для зоны " + 
                             ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
    
    /**
     * Телепортирует игрока к зоне
     * 
     * @param player Игрок
     * @param args Аргументы команды
     * @return true если команда выполнена успешно
     */
    public boolean teleportToZone(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /cx tp [имя зоны]");
            return false;
        }
        
        String zoneName = args[0];
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            return false;
        }
        
        // Получаем точку телепортации или используем центр зоны, если точка не установлена
        Location target = zone.getTeleportDestination();
        if (target == null) {
            // Если точка телепортации не установлена, используем центр зоны
            target = getCenterLocation(zone);
            player.sendMessage(ChatColor.YELLOW + "Точка телепортации не установлена, телепортация в центр зоны.");
        }
        
        // Проверка безопасности точки телепортации
        Location safeTarget = getSafeDestination(target);
        
        // Сохраняем направление взгляда игрока
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        
        // Устанавливаем точное направление взгляда в целевой локации
        safeTarget.setYaw(yaw);
        safeTarget.setPitch(pitch);
        
        // Телепортация игрока с дополнительными параметрами для мгновенной телепортации
        // PlayerTeleportEvent.TeleportCause.PLUGIN указывает, что телепортация происходит от плагина
        if (player.teleport(safeTarget, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            // Даем игроку короткий эффект защиты от урона при падении после телепортации
            player.setFallDistance(0);
            
            // Отправляем сообщение о успешной телепортации
            player.sendMessage(ChatColor.GREEN + "Телепортация к зоне " + ChatColor.GOLD + zoneName + ChatColor.GREEN + " успешно завершена!");
            
            // Запускаем задачу, которая снимет любые блокировки движения игрока
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    // Обновляем положение игрока для клиента
                    player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                }
            }, 1L); // Выполняем через 1 тик (0.05 секунды)
            
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось телепортироваться к зоне " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
    
    /**
     * Вычисляет координаты центра зоны
     * 
     * @param zone Зона
     * @return Локация центра зоны
     */
    private Location getCenterLocation(Zone zone) {
        BlockVector3 min = zone.getMin();
        BlockVector3 max = zone.getMax();
        
        // Вычисляем центр зоны
        double x = min.getX() + (max.getX() - min.getX()) / 2.0;
        double y = min.getY() + (max.getY() - min.getY()) / 2.0;
        double z = min.getZ() + (max.getZ() - min.getZ()) / 2.0;
        
        return new Location(plugin.getServer().getWorld(zone.getWorldName()), x, y, z);
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
        
        // Быстрая проверка - если два блока свободны (для игрока), а снизу есть опора - локация безопасна
        if (safeLoc.getBlock().getType().isAir() && 
            safeLoc.clone().add(0, 1, 0).getBlock().getType().isAir() && 
            !safeLoc.clone().subtract(0, 1, 0).getBlock().getType().isAir()) {
            return safeLoc;
        }
        
        // Проверяем, что блок под ногами не воздух
        if (safeLoc.clone().subtract(0, 1, 0).getBlock().getType().isAir()) {
            // Ищем ближайший твердый блок вниз (не больше 5 блоков)
            for (int i = 1; i <= 5; i++) {
                Location checkLoc = safeLoc.clone().subtract(0, i, 0);
                if (!checkLoc.getBlock().getType().isAir()) {
                    return checkLoc.clone().add(0, 1, 0);
                }
            }
        }
        
        // Если игрок может застрять в блоке, ищем ближайшее безопасное место
        if (!safeLoc.getBlock().getType().isAir() || 
            !safeLoc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            
            // Ищем свободное место рядом по оптимизированному алгоритму
            // Сначала проверяем 4 основных направления (север, юг, запад, восток)
            int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
            for (int[] dir : directions) {
                Location checkLoc = safeLoc.clone().add(dir[0], 0, dir[1]);
                if (checkLoc.getBlock().getType().isAir() && 
                    checkLoc.clone().add(0, 1, 0).getBlock().getType().isAir() &&
                    !checkLoc.clone().subtract(0, 1, 0).getBlock().getType().isAir()) {
                    return checkLoc;
                }
            }
            
            // Если не нашли, проверяем диагональные направления
            int[][] diagonals = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
            for (int[] dir : diagonals) {
                Location checkLoc = safeLoc.clone().add(dir[0], 0, dir[1]);
                if (checkLoc.getBlock().getType().isAir() && 
                    checkLoc.clone().add(0, 1, 0).getBlock().getType().isAir() &&
                    !checkLoc.clone().subtract(0, 1, 0).getBlock().getType().isAir()) {
                    return checkLoc;
                }
            }
        }
        
        return safeLoc;
    }
    
    /**
     * Редактирует зону (обработчик команды)
     * 
     * @param player Игрок
     * @param args Аргументы команды
     * @return true если команда выполнена успешно
     */
    public boolean editZone(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /cx ze [имя зоны]");
            return false;
        }
        
        String zoneName = args[0];
        return updateZoneBoundaries(player, zoneName);
    }
    
    /**
     * Показывает информацию о зоне (обработчик команды)
     * 
     * @param player Игрок
     * @param args Аргументы команды
     * @return true если команда выполнена успешно
     */
    public boolean zoneInfo(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /cx zi [имя зоны]");
            return false;
        }
        
        String zoneName = args[0];
        showZoneInfo(player, zoneName);
        return true;
    }
    
    /**
     * Устанавливает интервал починки для зоны
     * 
     * @param player Игрок, выполняющий команду
     * @param args Аргументы команды
     * @return true если команда выполнена успешно
     */
    public boolean setRepairInterval(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /cx repair_interval [зона] [секунды]");
            return false;
        }
        
        String zoneName = args[0];
        int seconds;
        
        try {
            seconds = Integer.parseInt(args[1]);
            if (seconds < 0) {
                player.sendMessage(ChatColor.RED + "Интервал не может быть отрицательным!");
                return false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Интервал должен быть числом!");
            return false;
        }
        
        // Проверка существования зоны
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            return false;
        }
        
        if (!zone.isRepairZone()) {
            player.sendMessage(ChatColor.RED + "Сначала включите функцию починки для зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
        
        if (zoneManager.setRepairInterval(zoneName, seconds)) {
            if (seconds == 0) {
                player.sendMessage(
                    ChatColor.GREEN + "Починка будет происходить только при входе/выходе из зоны " + 
                    ChatColor.GOLD + zoneName + ChatColor.GREEN + "!"
                );
            } else {
                player.sendMessage(
                    ChatColor.GREEN + "Интервал починки установлен на " + ChatColor.GOLD + seconds + 
                    ChatColor.GREEN + " сек. для зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!"
                );
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось установить интервал починки для зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
} 