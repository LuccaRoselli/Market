package com.devlucca.ymarket.lib.itembuilder.parts;

import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.inventory.meta.*;

public class ItemName implements ItemPart
{
    private String name;
    
    public ItemName(final String constructor) {
        try {
            if (constructor.startsWith("name:")) {
                this.name = constructor.replace("name:", "").replace("&", "§").replace("_", " ");
            }
        }
        catch (Exception ex) {}
    }
    
    @Override
    public void send(final ItemStack item) {
        if (item != null && !item.getType().equals((Object)Material.AIR) && this.getName() != null) {
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(this.getName());
            item.setItemMeta(meta);
        }
    }
    
    public String getName() {
        return this.name;
    }
}
