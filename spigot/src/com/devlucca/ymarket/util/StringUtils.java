package com.devlucca.ymarket.util;

import org.bukkit.*;
import java.util.concurrent.*;

public class StringUtils
{
    public static String capitalise(String string) {
        string = string.toLowerCase();
        return String.valueOf(new StringBuilder(String.valueOf(string.charAt(0))).toString().toUpperCase()) + string.substring(1, string.length());
    }
    
    public static String removeColors(final String string) {
        return ChatColor.stripColor(string);
    }
    
    public static String formatDelay(final long time) {
        if (time == 0L) {
            return "never";
        }
        final long day = TimeUnit.MILLISECONDS.toDays(time);
        final long hours = TimeUnit.MILLISECONDS.toHours(time) - day * 24L;
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.MILLISECONDS.toHours(time) * 60L;
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MILLISECONDS.toMinutes(time) * 60L;
        final StringBuilder sb = new StringBuilder();
        if (day > 0L) {
            sb.append(day).append(" ").append((day == 1L) ? "dia" : "dias").append(" ");
        }
        if (hours > 0L) {
            sb.append(hours).append(" ").append((hours == 1L) ? "hora" : "horas").append(" ");
        }
        if (minutes > 0L) {
            sb.append(minutes).append(" ").append((minutes == 1L) ? "minuto" : "minutos").append(" ");
        }
        if (seconds > 0L) {
            sb.append(seconds).append(" ").append((seconds == 1L) ? "segundo" : "segundos");
        }
        final String diff = sb.toString();
        return diff.isEmpty() ? "agora" : diff;
    }
}
