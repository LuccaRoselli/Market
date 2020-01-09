package com.devlucca.ymarket.util;

import net.milkbowl.vault.economy.*;
import net.milkbowl.vault.chat.*;
import org.bukkit.*;
import org.bukkit.plugin.*;

import com.devlucca.ymarket.app.*;

public class Vault
{
    private Economy economy;
    private Chat chat;
    
    public boolean setupEconomy() {
        final RegisteredServiceProvider<Economy> service = (RegisteredServiceProvider<Economy>)Bukkit.getServicesManager().getRegistration(Economy.class);
        if (service != null) {
            this.economy = (Economy)service.getProvider();
        }
        return this.economy != null;
    }
    
    public boolean setupChat() {
        final RegisteredServiceProvider<Chat> service = (RegisteredServiceProvider<Chat>)Bukkit.getServicesManager().getRegistration(Chat.class);
        if (service != null) {
            this.chat = (Chat)service.getProvider();
        }
        return this.chat != null;
    }
    
    public Economy getEconomy() {
        return this.economy;
    }
    
    @SuppressWarnings("deprecation")
	public String getPrefix(final String jogador) {
        final String world = yMarket.config.getString("world");
        String prefix = "";
        if (this.chat != null) {
            prefix = this.chat.getPlayerPrefix(world, jogador).replace("&", "§");
        }
        return prefix;
    }
}
