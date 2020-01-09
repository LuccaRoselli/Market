package com.devlucca.ymarket.lib.itembuilder.parts;

import org.bukkit.inventory.*;
import org.bukkit.*;
import java.util.*;
import org.bukkit.inventory.meta.*;

public class ItemLore implements ItemPart
{
    private String lore;
    
    public ItemLore(final String constructor) {
        try {
            if (constructor.startsWith("lore:")) {
                this.lore = constructor.replace("lore:", "").replace("&", "§").replace("_", " ");
            }
        }
        catch (Exception ex) {}
    }
    
    @Override
    public void send(final ItemStack item) {
        if (item != null && !item.getType().equals((Object)Material.AIR) && this.getLore() != null) {
            List<String> lore = new ArrayList<String>();
            final ItemMeta meta = item.getItemMeta();
            if (meta.getLore() != null) {
                lore = (List<String>)meta.getLore();
            }
            lore.add(this.getLore());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }
    
    public String getLore() {
        return this.lore;
    }
}
