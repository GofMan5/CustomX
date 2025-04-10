package com.CustomX.plugin.commands.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.CustomX.plugin.CustomX;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

/**
 * Обработчик команд отображения справки по зонам
 */
public class ZoneHelpHandler extends BaseCommandHandler {
    
    /**
     * Конструктор обработчика справки
     * 
     * @param plugin Экземпляр плагина
     */
    public ZoneHelpHandler(CustomX plugin) {
        super(plugin);
    }
    
    /**
     * Показывает основное меню справки
     * 
     * @param player игрок
     * @return true если команда выполнена успешно
     */
    public boolean showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Справка" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Общая информация
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Информация о плагине:");
        player.sendMessage(ChatColor.WHITE + "  CustomX - плагин для создания и управления зонами на сервере.");
        player.sendMessage(ChatColor.WHITE + "  Зоны могут иметь различные эффекты, функцию починки и телепортации.");
        
        // Команды
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Основные команды (с сокращениями):");
        
        // Создание зоны
        TextComponent createZoneCmd = new TextComponent("  /cx create [имя] или /cx c [имя]");
        createZoneCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        createZoneCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx c "));
        createZoneCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Создать новую зону из выделенного региона WorldEdit")));
        createZoneCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Создать зону из выделения WorldEdit"));
        player.spigot().sendMessage(createZoneCmd);
        
        // Список зон
        TextComponent listZonesCmd = new TextComponent("  /cx list или /cx l");
        listZonesCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        listZonesCmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx l"));
        listZonesCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы выполнить команду просмотра списка зон")));
        listZonesCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Список всех зон"));
        player.spigot().sendMessage(listZonesCmd);
        
        // Информация о зоне
        TextComponent infoCmd = new TextComponent("  /cx info [имя] или /cx i [имя]");
        infoCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        infoCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx i "));
        infoCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду просмотра информации о зоне")));
        infoCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Информация о зоне"));
        player.spigot().sendMessage(infoCmd);
        
        // Телепортация к зоне
        TextComponent tpCmd = new TextComponent("  /cx tp [имя]");
        tpCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        tpCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx tp "));
        tpCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду телепортации к зоне")));
        tpCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Телепортация к зоне"));
        player.spigot().sendMessage(tpCmd);
        
        // Команды телепортации
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Управление телепортацией:");
        
        // Включить телепортацию
        TextComponent tpOnCmd = new TextComponent("  /cx tpon [имя] или /cx zte [имя]");
        tpOnCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        tpOnCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx tpon "));
        tpOnCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Включить телепортацию для зоны")));
        tpOnCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Включить телепортацию"));
        player.spigot().sendMessage(tpOnCmd);
        
        // Выключить телепортацию
        TextComponent tpOffCmd = new TextComponent("  /cx tpoff [имя] или /cx ztd [имя]");
        tpOffCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        tpOffCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx tpoff "));
        tpOffCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Выключить телепортацию для зоны")));
        tpOffCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Выключить телепортацию"));
        player.spigot().sendMessage(tpOffCmd);
        
        // Установить точку телепортации
        TextComponent tpSetCmd = new TextComponent("  /cx tpset [имя] или /cx ztdest [имя]");
        tpSetCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        tpSetCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx tpset "));
        tpSetCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Установить текущую позицию как точку телепортации")));
        tpSetCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Установить точку телепортации"));
        player.spigot().sendMessage(tpSetCmd);
        
        // Установить кулдаун телепортации
        TextComponent tpCoolCmd = new TextComponent("  /cx tpcool [имя] [секунды] или /cx ztcool [имя] [секунды]");
        tpCoolCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        tpCoolCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx tpcool "));
        tpCoolCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Установить кулдаун телепортации в секундах")));
        tpCoolCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Установить кулдаун телепортации"));
        player.spigot().sendMessage(tpCoolCmd);
        
        // Удаление зоны
        TextComponent deleteCmd = new TextComponent("  /cx delete [имя] или /cx d [имя]");
        deleteCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        deleteCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx d "));
        deleteCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы заполнить команду удаления зоны")));
        deleteCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Удалить зону"));
        player.spigot().sendMessage(deleteCmd);
        
        // Команды управления эффектами
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Управление эффектами:");
        
        // Показать эффекты зоны
        TextComponent effectsCmd = new TextComponent("  /cx effects [имя] или /cx e [имя]");
        effectsCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        effectsCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx e "));
        effectsCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Показать все эффекты зоны")));
        effectsCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Список эффектов зоны"));
        player.spigot().sendMessage(effectsCmd);
        
        // Добавить эффект
        TextComponent addEffectCmd = new TextComponent("  /cx add_effect [имя] [эффект] [уровень] [время] или /cx ae [имя] [эффект] [уровень] [время]");
        addEffectCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        addEffectCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx ae "));
        addEffectCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Добавить эффект в зону")));
        addEffectCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Добавить эффект"));
        player.spigot().sendMessage(addEffectCmd);
        
        // Удалить эффект
        TextComponent removeEffectCmd = new TextComponent("  /cx remove_effect [имя] [эффект] или /cx re [имя] [эффект]");
        removeEffectCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        removeEffectCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx re "));
        removeEffectCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Удалить эффект из зоны")));
        removeEffectCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Удалить эффект"));
        player.spigot().sendMessage(removeEffectCmd);
        
        // Очистить эффекты
        TextComponent clearEffectsCmd = new TextComponent("  /cx clear_effects [имя] или /cx ce [имя]");
        clearEffectsCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        clearEffectsCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx ce "));
        clearEffectsCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Очистить все эффекты зоны")));
        clearEffectsCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Очистить все эффекты"));
        player.spigot().sendMessage(clearEffectsCmd);
        
        // Переопределение границ зоны (только для админов)
        if (player.hasPermission("CustomX.zones.admin")) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Команды администратора:");
            
            TextComponent defineCmd = new TextComponent("  /cx edit define [имя]");
            defineCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            defineCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx edit define "));
            defineCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Обновить границы зоны текущим выделением WorldEdit")));
            defineCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Обновить границы зоны"));
            player.spigot().sendMessage(defineCmd);
            
            // Перезагрузка плагина
            TextComponent reloadCmd = new TextComponent("  /cx reload");
            reloadCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            reloadCmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx reload"));
            reloadCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Перезагрузить плагин и все зоны")));
            reloadCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Перезагрузить плагин"));
            player.spigot().sendMessage(reloadCmd);
            
            // Просмотр потребления ресурсов
            TextComponent gcCmd = new TextComponent("  /cx gc");
            gcCmd.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            gcCmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx gc"));
            gcCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Показать статистику потребления ресурсов")));
            gcCmd.addExtra(new TextComponent(ChatColor.WHITE + " - Статистика потребления ресурсов"));
            player.spigot().sendMessage(gcCmd);
        }
        
        // Сообщение о более подробной информации
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Дополнительная информация:");
        player.sendMessage(ChatColor.WHITE + "  Используйте " + ChatColor.YELLOW + "/cx help effects" + ChatColor.WHITE + " для просмотра всех эффектов");
        player.sendMessage(ChatColor.WHITE + "  Нажмите на команду выше для её заполнения или выполнения");
        
        // Нижний колонтитул
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╚══════════ " + ChatColor.WHITE + "CustomX - Справка" + ChatColor.GOLD + " ══════════╝");
        
        return true;
    }
    
    /**
     * Показать список доступных эффектов
     * 
     * @param player игрок
     * @return true если команда выполнена успешно
     */
    public boolean showEffectsList(Player player) {
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
        
        return true;
    }
    
    /**
     * Создает кликабельную команду для добавления эффекта
     * 
     * @param player игрок
     * @param effectName название эффекта
     * @param description описание эффекта
     */
    private void createEffectCommand(Player player, String effectName, String description) {
        TextComponent effectComponent = new TextComponent("  " + ChatColor.AQUA + effectName);
        effectComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                "/cx ae zone_name " + effectName + " 1 60"));
        effectComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Нажмите, чтобы использовать эффект " + effectName)));
        effectComponent.addExtra(new TextComponent(ChatColor.WHITE + " - " + description));
        player.spigot().sendMessage(effectComponent);
    }
    
    /**
     * Показать справку по созданию зон
     * 
     * @param player игрок
     */
    public void showCreateHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Создание зон" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Общая информация
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Создание зоны:");
        player.sendMessage(ChatColor.WHITE + "  Для создания зоны вам потребуется WorldEdit и права на его использование.");
        player.sendMessage(ChatColor.WHITE + "  Следуйте инструкциям ниже:");
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Шаги по созданию зоны:");
        player.sendMessage(ChatColor.WHITE + "  1. Выделите область с помощью WorldEdit (команды //wand, //pos1, //pos2)");
        player.sendMessage(ChatColor.WHITE + "  2. Создайте зону: " + ChatColor.YELLOW + "/cx c [имя_зоны]");
        player.sendMessage(ChatColor.WHITE + "  3. Добавьте нужные функции (эффекты, починку, телепортацию)");
        
        // Пример создания зоны
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Пример создания зоны с эффектами:");
        player.sendMessage(ChatColor.WHITE + "  1. " + ChatColor.YELLOW + "/cx c pvp_arena");
        player.sendMessage(ChatColor.WHITE + "  2. " + ChatColor.YELLOW + "/cx ae pvp_arena INCREASE_DAMAGE 1 -1");
        player.sendMessage(ChatColor.WHITE + "  3. " + ChatColor.YELLOW + "/cx ae pvp_arena SPEED 1 -1");
        
        // Настройки зоны
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Дополнительные настройки:");
        
        // Кнопка для вывода справки по эффектам
        TextComponent effectsHelpBtn = new TextComponent(ChatColor.YELLOW + "[ Список эффектов ]");
        effectsHelpBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cx help effects"));
        effectsHelpBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Показать список доступных эффектов")));
        player.spigot().sendMessage(effectsHelpBtn);
        
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
     * Отображает информацию о потреблении ресурсов плагином
     * 
     * @param player игрок, который запросил информацию
     * @return true, если команда выполнена успешно
     */
    public boolean showGarbageCollectionInfo(Player player) {
        if (!player.hasPermission("CustomX.zones.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для просмотра этой информации!");
            return false;
        }
        
        Runtime runtime = Runtime.getRuntime();
        
        // Получаем данные о памяти
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024; 
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        
        // Вычисляем процент использования
        int memoryUsagePercent = (int) ((usedMemory * 100) / maxMemory);
        
        // Загруженные зоны и их объекты
        int zoneCount = plugin.getZoneManager().getZones().size();
        
        // Количество активных игроков в зонах
        int playersInZonesCount = getPlayersInZonesCount();
        
        // Оценочный размер объектов в плагине
        long estimatedPluginMemory = estimatePluginMemoryUsage(zoneCount);
        int pluginMemoryPercent = (int) ((estimatedPluginMemory * 100) / usedMemory);
        
        // Отправляем результаты
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Статистика ресурсов" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Информация о памяти
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Общее состояние памяти:");
        player.sendMessage(ChatColor.WHITE + "  Используется: " + ChatColor.YELLOW + usedMemory + " МБ / " + maxMemory + " МБ " + 
                          getMemoryBar(memoryUsagePercent) + ChatColor.YELLOW + " " + memoryUsagePercent + "%");
        player.sendMessage(ChatColor.WHITE + "  Выделено JVM: " + ChatColor.YELLOW + totalMemory + " МБ");
        player.sendMessage(ChatColor.WHITE + "  Свободно: " + ChatColor.YELLOW + freeMemory + " МБ");
        
        // Информация о потреблении ресурсов плагином
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Потребление ресурсов плагином CustomX:");
        player.sendMessage(ChatColor.WHITE + "  Примерное использование памяти: " + ChatColor.YELLOW + estimatedPluginMemory + " МБ " + 
                          getMemoryBar(pluginMemoryPercent) + ChatColor.YELLOW + " " + pluginMemoryPercent + "% от общей");
        player.sendMessage(ChatColor.WHITE + "  Загружено зон: " + ChatColor.YELLOW + zoneCount);
        player.sendMessage(ChatColor.WHITE + "  Активных игроков в зонах: " + ChatColor.YELLOW + playersInZonesCount);
        player.sendMessage(ChatColor.WHITE + "  Тикрейт влияние: " + ChatColor.YELLOW + getTickRateImpact(zoneCount));
        
        // Информация о системе
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Системная информация:");
        player.sendMessage(ChatColor.WHITE + "  Доступные процессоры: " + ChatColor.YELLOW + runtime.availableProcessors());
        player.sendMessage(ChatColor.WHITE + "  Версия Java: " + ChatColor.YELLOW + System.getProperty("java.version"));
        player.sendMessage(ChatColor.WHITE + "  Версия сервера: " + ChatColor.YELLOW + plugin.getServer().getVersion());
        player.sendMessage(ChatColor.WHITE + "  Время работы сервера: " + ChatColor.YELLOW + formatUptime());
        
        // Информация о плагине
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Информация о плагине:");
        player.sendMessage(ChatColor.WHITE + "  Версия плагина: " + ChatColor.YELLOW + plugin.getDescription().getVersion());
        player.sendMessage(ChatColor.WHITE + "  Интервал обновления зон: " + ChatColor.YELLOW + plugin.getConfigManager().getZoneUpdateInterval() + " тиков");
        
        // Оптимизация
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Советы по оптимизации:");
        if (zoneCount > 50) {
            player.sendMessage(ChatColor.RED + "  • Большое количество зон может замедлить сервер");
            player.sendMessage(ChatColor.WHITE + "  • Рекомендуется объединить мелкие зоны в более крупные");
        } else {
            player.sendMessage(ChatColor.GREEN + "  • Количество зон в пределах нормы");
        }
        
        if (playersInZonesCount > 30) {
            player.sendMessage(ChatColor.RED + "  • Большое количество игроков в зонах увеличивает нагрузку");
            player.sendMessage(ChatColor.WHITE + "  • Увеличьте интервал обновления зон в конфигурации");
        } else {
            player.sendMessage(ChatColor.GREEN + "  • Количество игроков в зонах в пределах нормы");
        }
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╚═════════════════════════════════════════════════╝");
        
        return true;
    }
    
    /**
     * Получает количество игроков, находящихся в зонах
     * 
     * @return количество игроков в зонах
     */
    private int getPlayersInZonesCount() {
        int count = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.getZoneManager().getZonesForPlayer(player).isEmpty()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Оценивает использование памяти плагином
     * 
     * @param zoneCount количество зон
     * @return приблизительное использование памяти в МБ
     */
    private long estimatePluginMemoryUsage(int zoneCount) {
        // Примерная оценка на основе количества зон и объектов
        // Базовый размер плагина (код, конфигурации)
        long baseSize = 5;
        
        // Размер на одну зону (включая эффекты, конфигурации и т.д.)
        long perZoneSize = 1;
        
        // Примерный размер в МБ
        return baseSize + (perZoneSize * zoneCount);
    }
    
    /**
     * Возвращает оценку влияния плагина на тикрейт сервера
     * 
     * @param zoneCount количество зон
     * @return строка с описанием влияния
     */
    private String getTickRateImpact(int zoneCount) {
        if (zoneCount < 20) {
            return ChatColor.GREEN + "Низкое";
        } else if (zoneCount < 50) {
            return ChatColor.YELLOW + "Среднее";
        } else {
            return ChatColor.RED + "Высокое";
        }
    }
    
    /**
     * Форматирует время в секундах в читаемый формат
     * 
     * @param seconds время в секундах
     * @return отформатированное время
     */
    private String formatTime(long seconds) {
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" д. ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append(" ч. ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append(" м. ");
        }
        sb.append(seconds).append(" с.");
        
        return sb.toString();
    }
    
    /**
     * Получает отформатированное время работы сервера
     * 
     * @return отформатированное время работы сервера
     */
    private String formatUptime() {
        // Получаем текущее время в мс
        long currentTime = System.currentTimeMillis();
        
        // Обычно точное время старта сервера недоступно через API,
        // поэтому используем системное время работы JVM как приближенное значение
        long uptimeMillis = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        
        return formatTime(uptimeMillis / 1000);
    }
    
    /**
     * Генерирует визуальный индикатор использования памяти
     * 
     * @param percent процент использования
     * @return строка с цветным индикатором
     */
    private String getMemoryBar(int percent) {
        int bars = percent / 5; // 20 делений = 100%
        StringBuilder sb = new StringBuilder();
        
        sb.append(getBarColor(percent)).append("[");
        
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                sb.append("|");
            } else {
                sb.append(" ");
            }
        }
        
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * Возвращает цвет для индикатора в зависимости от процента использования
     * 
     * @param percent процент использования
     * @return объект цвета
     */
    private ChatColor getBarColor(int percent) {
        if (percent < 60) {
            return ChatColor.GREEN;
        } else if (percent < 85) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.RED;
        }
    }
} 