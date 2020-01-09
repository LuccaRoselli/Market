package com.devlucca.ymarket.lib.itembuilder.parts;

import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.inventory.meta.*;

public class ItemSkull implements ItemPart
{
    private String name;
    
    public ItemSkull(final String constructor) {
        try {
            if (constructor.startsWith("skull:")) {
                this.name = constructor.replace("skull:", "").replace("&", "§");
            }
        }
        catch (Exception ex) {}
    }
    
    @Override
    public void send(final ItemStack item) {
        if (item != null && !item.getType().equals((Object)Material.AIR) && item.getType().equals((Object)Material.SKULL_ITEM) && item.getDurability() == 3 && this.getName() != null) {
            final SkullMeta meta = (SkullMeta)item.getItemMeta();
            meta.setOwner(this.getName());
            item.setItemMeta((ItemMeta)meta);
        }
    }
    
    public String getName() {
        return this.name;
    }
}
