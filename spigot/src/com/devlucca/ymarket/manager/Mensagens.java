package com.devlucca.ymarket.manager;

import org.bukkit.plugin.*;

import com.devlucca.ymarket.app.*;
import com.devlucca.ymarket.util.*;

public class Mensagens
{
    private Config config;
    
    public Mensagens() {
        this.reload();
    }
    
    public void reload() {
        this.config = new Config((Plugin)yMarket.instance, "mensagens.yml");
    }
    
    public String get(String string) {
        string = string.toUpperCase();
        return this.config.getString(string).replace("&", "§");
    }
    
	public static String getPrefix(){
		Mensagens m = new Mensagens();
		return m.get("PREFIXO");
	}
}
