package com.CustomX.plugin.commands.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.CustomX.plugin.CustomX;
import com.CustomX.plugin.zones.Zone;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.Map;
import java.util.Collection;

/**
 * Обработчик команд для управления эффектами зон
 */
public class ZoneEffectHandler extends BaseCommandHandler {
    
    /**
     * Конструктор обработчика эффектов зон
     * 
     * @param plugin Экземпляр плагина
     */
    public ZoneEffectHandler(CustomX plugin) {
        super(plugin);
    }
    
    /**
     * Метод для добавления эффекта к зоне
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Название зоны
     * @param effectName Название эффекта
     * @param level Уровень эффекта (начиная с 1)
     * @param duration Длительность эффекта в секундах (-1 = бесконечно)
     * @return true, если операция выполнена успешно
     */
    public boolean addEffect(Player player, String zoneName, String effectName, int level, int duration) {
        // Проверка существования зоны
        if (plugin.getZoneManager().getZone(zoneName) == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            player.sendMessage(ChatColor.YELLOW + "Сначала создайте зону с помощью команды: " + ChatColor.WHITE + "/cx zone create " + zoneName);
            return false;
        }
        
        // Проверка существования эффекта
        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        
        if (effectType == null) {
            player.sendMessage(ChatColor.RED + "Эффект " + ChatColor.GOLD + effectName + ChatColor.RED + " не найден!");
            showEffectsList(player);
            return false;
        }
        
        // Проверка корректности уровня
        if (level < 1) {
            player.sendMessage(ChatColor.RED + "Уровень эффекта должен быть положительным числом!");
            return false;
        }
        
        // Добавляем эффект (уровень корректируем, т.к. в Minecraft отсчет начинается с 0)
        if (plugin.getZoneManager().addEffect(zoneName, effectType, level - 1, duration)) {
            player.sendMessage(ChatColor.GREEN + "Эффект " + ChatColor.GOLD + getEffectName(effectType) + " " + 
                              translateRomanNumeral(level) + ChatColor.GREEN + " добавлен в зону " + 
                              ChatColor.GOLD + zoneName + ChatColor.GREEN + "!");
            
            // Если длительность бесконечная, выводим дополнительное сообщение
            if (duration == -1) {
                player.sendMessage(ChatColor.YELLOW + "Эффект будет действовать бесконечно, пока игрок находится в зоне.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Эффект будет действовать " + ChatColor.GOLD + duration + 
                                  ChatColor.YELLOW + " секунд при нахождении в зоне.");
            }
            
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось добавить эффект " + ChatColor.GOLD + effectName + 
                              ChatColor.RED + " в зону " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
    
    /**
     * Метод для удаления эффекта из зоны
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Название зоны
     * @param effectName Название эффекта
     * @return true, если операция выполнена успешно
     */
    public boolean removeEffect(Player player, String zoneName, String effectName) {
        // Проверка существования зоны
        if (plugin.getZoneManager().getZone(zoneName) == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            return false;
        }
        
        // Проверка существования эффекта
        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        
        if (effectType == null) {
            player.sendMessage(ChatColor.RED + "Эффект " + ChatColor.GOLD + effectName + ChatColor.RED + " не найден!");
            showEffectsList(player);
            return false;
        }
        
        // Удаляем эффект
        if (plugin.getZoneManager().removeEffect(zoneName, effectType)) {
            player.sendMessage(ChatColor.GREEN + "Эффект " + ChatColor.GOLD + getEffectName(effectType) + 
                              ChatColor.GREEN + " удален из зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось удалить эффект " + ChatColor.GOLD + effectName + 
                              ChatColor.RED + " из зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
    
    /**
     * Метод для очистки всех эффектов из зоны
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Название зоны
     * @return true, если операция выполнена успешно
     */
    public boolean clearEffects(Player player, String zoneName) {
        // Проверка существования зоны
        if (plugin.getZoneManager().getZone(zoneName) == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            return false;
        }
        
        // Очищаем все эффекты
        if (plugin.getZoneManager().clearEffects(zoneName)) {
            player.sendMessage(ChatColor.GREEN + "Все эффекты удалены из зоны " + ChatColor.GOLD + zoneName + ChatColor.GREEN + "!");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось удалить эффекты из зоны " + ChatColor.GOLD + zoneName + ChatColor.RED + "!");
            return false;
        }
    }
    
    /**
     * Показать список доступных эффектов
     * 
     * @param player Игрок, которому показывается список
     */
    public void showEffectsList(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "CustomX - Список эффектов" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Основные эффекты
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Основные эффекты:");
        sendEffectInfo(player, "SPEED", "Скорость - увеличивает скорость передвижения");
        sendEffectInfo(player, "SLOW", "Замедление - снижает скорость передвижения");
        sendEffectInfo(player, "FAST_DIGGING", "Спешка - увеличивает скорость копания");
        sendEffectInfo(player, "SLOW_DIGGING", "Усталость - снижает скорость копания");
        sendEffectInfo(player, "INCREASE_DAMAGE", "Сила - увеличивает наносимый урон");
        sendEffectInfo(player, "WEAKNESS", "Слабость - снижает наносимый урон");
        sendEffectInfo(player, "JUMP", "Прыгучесть - увеличивает высоту прыжка");
        
        // Защитные эффекты
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Защитные эффекты:");
        sendEffectInfo(player, "REGENERATION", "Регенерация - восстанавливает здоровье");
        sendEffectInfo(player, "DAMAGE_RESISTANCE", "Сопротивление урону - снижает получаемый урон");
        sendEffectInfo(player, "FIRE_RESISTANCE", "Огнестойкость - защищает от огня и лавы");
        sendEffectInfo(player, "WATER_BREATHING", "Подводное дыхание - позволяет дышать под водой");
        
        // Негативные эффекты
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Негативные эффекты:");
        sendEffectInfo(player, "POISON", "Отравление - наносит урон здоровью");
        sendEffectInfo(player, "WITHER", "Иссушение - наносит урон здоровью, как яд");
        sendEffectInfo(player, "HUNGER", "Голод - увеличивает истощение");
        sendEffectInfo(player, "CONFUSION", "Тошнота - искажает экран");
        sendEffectInfo(player, "BLINDNESS", "Слепота - ограничивает поле зрения");
        
        // Примеры использования
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Примеры использования:");
        player.sendMessage(ChatColor.YELLOW + "  /cx ae spawn SPEED 1 -1" + 
                          ChatColor.WHITE + " - Добавляет бесконечный эффект Скорость I в зону spawn");
        player.sendMessage(ChatColor.YELLOW + "  /cx ae pvp INCREASE_DAMAGE 2 300" + 
                          ChatColor.WHITE + " - Добавляет эффект Сила II на 5 минут в зону pvp");
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╚═════════════════════════════════════════════════╝");
    }
    
    /**
     * Отправить информацию об эффекте с кликабельной командой
     * 
     * @param player Игрок
     * @param effectName Название эффекта
     * @param description Описание эффекта
     */
    private void sendEffectInfo(Player player, String effectName, String description) {
        TextComponent component = new TextComponent("  " + ChatColor.AQUA + effectName);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cx ae zone_name " + effectName + " 1 60"));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Нажмите, чтобы использовать этот эффект")));
        component.addExtra(new TextComponent(ChatColor.WHITE + " - " + description));
        player.spigot().sendMessage(component);
    }
    
    /**
     * Получить локализованное название эффекта
     * 
     * @param type Тип эффекта
     * @return Локализованное название
     */
    private String getEffectName(PotionEffectType type) {
        switch (type.getName()) {
            case "SPEED": return "Скорость";
            case "SLOW": return "Замедление";
            case "FAST_DIGGING": return "Спешка";
            case "SLOW_DIGGING": return "Усталость";
            case "INCREASE_DAMAGE": return "Сила";
            case "HEAL": return "Исцеление";
            case "HARM": return "Моментальный урон";
            case "JUMP": return "Прыгучесть";
            case "CONFUSION": return "Тошнота";
            case "REGENERATION": return "Регенерация";
            case "DAMAGE_RESISTANCE": return "Сопротивление урону";
            case "FIRE_RESISTANCE": return "Огнестойкость";
            case "WATER_BREATHING": return "Подводное дыхание";
            case "INVISIBILITY": return "Невидимость";
            case "BLINDNESS": return "Слепота";
            case "NIGHT_VISION": return "Ночное зрение";
            case "HUNGER": return "Голод";
            case "WEAKNESS": return "Слабость";
            case "POISON": return "Отравление";
            case "WITHER": return "Иссушение";
            case "HEALTH_BOOST": return "Повышение здоровья";
            case "ABSORPTION": return "Поглощение";
            case "SATURATION": return "Насыщение";
            case "GLOWING": return "Свечение";
            case "LEVITATION": return "Левитация";
            case "LUCK": return "Удача";
            case "UNLUCK": return "Невезение";
            case "SLOW_FALLING": return "Медленное падение";
            case "CONDUIT_POWER": return "Сила источника";
            case "DOLPHINS_GRACE": return "Грация дельфина";
            case "BAD_OMEN": return "Дурное знамение";
            case "HERO_OF_THE_VILLAGE": return "Герой деревни";
            default: return type.getName();
        }
    }
    
    /**
     * Отобразить список эффектов для конкретной зоны
     * 
     * @param player Игрок, выполняющий команду
     * @param zoneName Название зоны
     * @return true, если операция выполнена успешно
     */
    public boolean listEffects(Player player, String zoneName) {
        // Проверка существования зоны
        com.CustomX.plugin.zones.Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            return false;
        }
        
        // Получаем эффекты
        Map<PotionEffectType, PotionEffect> effects = zone.getEffects();
        
        // Выводим заголовок
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "Эффекты зоны " + 
                          ChatColor.GOLD + zoneName + ChatColor.GOLD + " ══════════╗");
        
        if (effects.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "В этой зоне нет активных эффектов.");
            player.sendMessage("");
            
            // Предложение добавить эффект
            TextComponent addBtn = new TextComponent(ChatColor.GREEN + "[ Добавить эффект ]");
            addBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                    "/cx ae " + zoneName + " SPEED 1 -1"));
            addBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    new Text("Добавить эффект в зону " + zoneName)));
            
            player.spigot().sendMessage(addBtn);
        } else {
            player.sendMessage(ChatColor.YELLOW + "Активные эффекты в зоне:");
            player.sendMessage("");
            
            // Выводим список эффектов
            for (Map.Entry<PotionEffectType, PotionEffect> entry : effects.entrySet()) {
                PotionEffectType type = entry.getKey();
                PotionEffect effect = entry.getValue();
                
                int level = effect.getAmplifier() + 1;
                int duration = effect.getDuration();
                String durationText = (duration == Integer.MAX_VALUE) ? "бесконечно" : 
                                    (duration / 20) + " сек.";
                
                TextComponent effectInfo = new TextComponent("  " + ChatColor.AQUA + getEffectName(type) + 
                                                         " " + ChatColor.WHITE + translateRomanNumeral(level) + 
                                                         ChatColor.YELLOW + " (" + durationText + ")");
                
                // Кнопка удаления
                TextComponent removeBtn = new TextComponent(" " + ChatColor.RED + "[X]");
                removeBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                        "/cx re " + zoneName + " " + type.getName()));
                removeBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        new Text("Удалить эффект " + getEffectName(type) + " из зоны")));
                
                effectInfo.addExtra(removeBtn);
                player.spigot().sendMessage(effectInfo);
            }
            
            player.sendMessage("");
            
            // Предложение добавить еще эффект и очистить все
            TextComponent addBtn = new TextComponent(ChatColor.GREEN + "[ Добавить эффект ]");
            addBtn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                    "/cx ae " + zoneName + " SPEED 1 -1"));
            addBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    new Text("Добавить эффект в зону " + zoneName)));
            
            TextComponent clearBtn = new TextComponent(ChatColor.RED + " [ Очистить все эффекты ]");
            clearBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                    "/cx ce " + zoneName));
            clearBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    new Text("Удалить все эффекты из зоны " + zoneName)));
            
            TextComponent buttonsRow = new TextComponent("");
            buttonsRow.addExtra(addBtn);
            buttonsRow.addExtra(clearBtn);
            
            player.spigot().sendMessage(buttonsRow);
        }
        
        player.sendMessage(ChatColor.GOLD + "╚═════════════════════════════════════════════════╝");
        
        return true;
    }
    
    /**
     * Показать список эффектов конкретной зоны с интерактивными кнопками добавления
     * 
     * @param player Игрок, которому показывается список
     * @param zoneName Имя зоны
     */
    public void showZoneEffectsList(Player player, String zoneName) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "Зона с именем " + ChatColor.GOLD + zoneName + ChatColor.RED + " не найдена!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.WHITE + "Эффекты зоны: " + zoneName + ChatColor.GOLD + " ══════════╗");
        player.sendMessage("");
        
        // Текущие эффекты зоны
        Map<PotionEffectType, PotionEffect> effectsMap = zone.getEffects();
        Collection<PotionEffect> effects = effectsMap.values();
        
        if (!effects.isEmpty()) {
            player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Текущие эффекты зоны:");
            
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
            
            player.sendMessage("");
        }
        
        // Добавление новых эффектов
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Добавить новый эффект:");
        player.sendMessage(ChatColor.WHITE + "  Нажмите на эффект, чтобы добавить его в зону:");
        
        // Создаем категории для эффектов
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Положительные эффекты:");
        
        // Создаем кнопки для популярных положительных эффектов
        createEffectButton(player, zoneName, "SPEED", "Скорость передвижения", ChatColor.AQUA);
        createEffectButton(player, zoneName, "JUMP", "Прыгучесть", ChatColor.GREEN);
        createEffectButton(player, zoneName, "FAST_DIGGING", "Скорость копания", ChatColor.YELLOW);
        createEffectButton(player, zoneName, "INCREASE_DAMAGE", "Сила", ChatColor.RED);
        createEffectButton(player, zoneName, "DAMAGE_RESISTANCE", "Сопротивление урону", ChatColor.BLUE);
        createEffectButton(player, zoneName, "REGENERATION", "Регенерация здоровья", ChatColor.LIGHT_PURPLE);
        createEffectButton(player, zoneName, "FIRE_RESISTANCE", "Огнестойкость", ChatColor.GOLD);
        createEffectButton(player, zoneName, "WATER_BREATHING", "Подводное дыхание", ChatColor.BLUE);
        createEffectButton(player, zoneName, "INVISIBILITY", "Невидимость", ChatColor.GRAY);
        createEffectButton(player, zoneName, "NIGHT_VISION", "Ночное зрение", ChatColor.DARK_BLUE);
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Отрицательные эффекты:");
        
        // Создаем кнопки для отрицательных эффектов
        createEffectButton(player, zoneName, "SLOW", "Замедление", ChatColor.RED);
        createEffectButton(player, zoneName, "SLOW_DIGGING", "Усталость", ChatColor.GRAY);
        createEffectButton(player, zoneName, "WEAKNESS", "Слабость", ChatColor.DARK_GRAY);
        createEffectButton(player, zoneName, "CONFUSION", "Тошнота", ChatColor.DARK_GREEN);
        createEffectButton(player, zoneName, "BLINDNESS", "Слепота", ChatColor.DARK_GRAY);
        createEffectButton(player, zoneName, "HUNGER", "Голод", ChatColor.DARK_GREEN);
        createEffectButton(player, zoneName, "POISON", "Отравление", ChatColor.DARK_GREEN);
        createEffectButton(player, zoneName, "WITHER", "Иссушение", ChatColor.DARK_GRAY);
        
        // Настройка уровня и продолжительности
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "● " + ChatColor.YELLOW + "Настройки для эффектов:");
        player.sendMessage(ChatColor.WHITE + "  После выбора эффекта, укажите:");
        player.sendMessage(ChatColor.WHITE + "  • Уровень (1-5) - сила эффекта");
        player.sendMessage(ChatColor.WHITE + "  • Продолжительность (в секундах, -1 = бесконечно)");
        player.sendMessage(ChatColor.WHITE + "  Пример: " + ChatColor.YELLOW + "/cx ae " + zoneName + " SPEED 1 -1");
        
        // Нижний колонтитул
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════╝");
    }
    
    /**
     * Создает кнопку для добавления эффекта
     * 
     * @param player Игрок
     * @param zoneName Имя зоны
     * @param effectName Название эффекта
     * @param description Описание эффекта
     * @param color Цвет текста
     */
    private void createEffectButton(Player player, String zoneName, String effectName, String description, ChatColor color) {
        TextComponent btn = new TextComponent("  [" + description + "]");
        btn.setColor(net.md_5.bungee.api.ChatColor.getByChar(color.getChar()));
        btn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
                "/cx ae " + zoneName + " " + effectName + " 1 -1"));
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text("Добавить эффект " + description + "\nПример: /cx ae " + zoneName + " " + effectName + " 1 -1")));
        player.spigot().sendMessage(btn);
    }
}