package com.devlucca.ymarket.commands;

import java.util.*;
import org.bukkit.command.*;

public abstract class SubCommand
{
    private String name;
    private String usage;
    private String permission;
    private List<String> alias;
    
    public SubCommand(final String name, final String usage, final String permission, final String... alias) {
        this.name = name;
        this.usage = usage;
        this.permission = permission;
        this.alias = Arrays.asList(alias);
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getUsage() {
        return this.usage;
    }
    
    public void setUsage(final String usage) {
        this.usage = usage;
    }
    
    public String getPermission() {
        return this.permission;
    }
    
    public void setPermission(final String permission) {
        this.permission = permission;
    }
    
    public List<String> getAlias() {
        return this.alias;
    }
    
    public void setAlias(final List<String> alias) {
        this.alias = alias;
    }
    
    public abstract void execute(final CommandSender p0, final String[] p1);
}
