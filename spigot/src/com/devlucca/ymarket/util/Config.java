package com.devlucca.ymarket.util;

import org.bukkit.plugin.*;
import org.bukkit.configuration.file.*;
import java.io.*;

public class Config
{
    private Plugin plugin;
    private String name;
    private File file;
    private YamlConfiguration yaml;
    
    public Config(final Plugin plugin, final String name) {
        this.plugin = plugin;
        this.name = name;
        this.reload();
    }
    
    public void reload() {
        try {
            if (this.name.contains("/")) {
                final String[] split = this.name.split("/");
                if (split.length >= 2) {
                    final File folder = new File(split[0]);
                    folder.mkdirs();
                    this.file = new File(folder, split[1]);
                }
                else {
                    this.name = this.name.replace("/", "");
                    this.file = new File(this.plugin.getDataFolder(), this.name);
                    if (!this.file.exists()) {
                        this.plugin.saveResource(this.name, false);
                    }
                }
            }
            else {
                this.file = new File(this.plugin.getDataFolder(), this.name);
                if (!this.file.exists()) {
                    this.plugin.saveResource(this.name, false);
                }
            }
            this.yaml = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(new FileInputStream(this.file), "UTF-8"));
        }
        catch (Exception e) {
            System.out.println(this.file.getPath());
            e.printStackTrace();
        }
    }
    
    public void save() {
        try {
            if (this.yaml != null && this.file != null) {
                this.yaml.save(this.file);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getString(final String path) {
        return this.yaml.getString(path).replace("&", "§");
    }
    
    public Plugin getPlugin() {
        return this.plugin;
    }
    
    public String getName() {
        return this.name;
    }
    
    public File getFile() {
        return this.file;
    }
    
    public YamlConfiguration getYaml() {
        return this.yaml;
    }
}
