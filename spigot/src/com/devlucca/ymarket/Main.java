package com.devlucca.ymarket;

import org.bukkit.plugin.java.*;

import com.devlucca.ymarket.app.*;
import com.devlucca.ymarket.commands.*;

import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.command.*;

public class Main extends JavaPlugin
{
    public static Main main;
    public static yMarket plugin;
    
    public void onEnable() {
        (Main.plugin = new yMarket(this)).onEnable();
        
        if (Bukkit.getPluginManager().isPluginEnabled((Plugin)this)) {
            this.getCommand("mercado").setExecutor((CommandExecutor)new yCommand("mercado"));
        }
    }
    
    public void onDisable() {
        Main.plugin.onDisable();
    }
}
