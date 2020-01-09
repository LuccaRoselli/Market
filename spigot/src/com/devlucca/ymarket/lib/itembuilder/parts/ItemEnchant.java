package com.devlucca.ymarket.lib.itembuilder.parts;

import org.bukkit.enchantments.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.inventory.meta.*;

public class ItemEnchant implements ItemPart
{
    private Enchantment enchant;
    private Integer level;
    
    public ItemEnchant(String constructor) {
        try {
            if (constructor.contains(":")) {
                constructor = constructor.toUpperCase();
                constructor = constructor.replace("SHARPNESS", "DAMAGE_ALL");
                constructor = constructor.replace("UNBREAKING", "DURABILITY");
                constructor = constructor.replace("FORTUNE", "LOOT_BONUS_BLOCKS");
                constructor = constructor.replace("LOOTING", "LOOT_BONUS_MOBS");
                constructor = constructor.replace("EFFICIENCY", "DIG_SPEED");
                constructor = constructor.replace("POWER", "ARROW_DAMAGE");
                final String[] split = constructor.split(":");
                this.enchant = Enchantment.getByName(split[0]);
                this.level = Integer.valueOf(split[1]);
            }
        }
        catch (Exception ex) {}
    }
    
    @Override
    public void send(final ItemStack item) {
        if (item != null && this.level != null && this.enchant != null && this.level > 0 && !item.getType().equals((Object)Material.AIR)) {
            final ItemMeta meta = item.getItemMeta();
            if (item.getType().equals((Object)Material.ENCHANTED_BOOK)) {
                ((EnchantmentStorageMeta)meta).addStoredEnchant(this.enchant, (int)this.level, true);
            }
            else {
                meta.addEnchant(this.enchant, (int)this.level, true);
            }
            item.setItemMeta(meta);
        }
    }
}
