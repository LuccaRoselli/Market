package com.devlucca.ymarket.lib.itembuilder.parts;

import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

public class ItemGlow implements ItemPart
{
    private boolean glow;
    
    public ItemGlow(final String constructor) {
        this.glow = constructor.equalsIgnoreCase("glow:true");
    }
    
    @Override
    public void send(final ItemStack item) {
        if (item != null && item.getType() != Material.AIR && this.glow) {
            final ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
            meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
            meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_POTION_EFFECTS });
            item.setItemMeta(meta);
        }
    }
}
