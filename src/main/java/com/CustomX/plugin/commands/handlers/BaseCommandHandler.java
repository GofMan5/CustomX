package com.CustomX.plugin.commands.handlers;

import org.bukkit.entity.Player;

import com.CustomX.plugin.CustomX;

/**
 * Базовый класс для обработчиков команд зон
 */
public abstract class BaseCommandHandler {
    
    protected final CustomX plugin;
    
    /**
     * Конструктор базового обработчика
     * 
     * @param plugin Экземпляр плагина
     */
    public BaseCommandHandler(CustomX plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Преобразовать число в римскую запись (используется в нескольких обработчиках)
     * 
     * @param num число (1-10)
     * @return римская запись
     */
    protected String translateRomanNumeral(int num) {
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
} 