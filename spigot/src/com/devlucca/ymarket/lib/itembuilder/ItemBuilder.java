package com.devlucca.ymarket.lib.itembuilder;

import org.bukkit.inventory.*;

import com.devlucca.ymarket.lib.itembuilder.parts.*;

public class ItemBuilder
{
    private ItemStack itemStack;
    
    @SuppressWarnings("deprecation")
	public ItemBuilder(final String constructor) {
        try {
            short data = 0;
            final String[] split = constructor.split(" ");
            final String id_data = split[0];
            final int quantidade = Integer.parseInt(split[1]);
            int id;
            if (id_data.contains(":")) {
                final String[] id_data_split = id_data.split(":");
                id = Integer.parseInt(id_data_split[0]);
                data = Short.parseShort(id_data_split[1]);
            }
            else {
                id = Integer.parseInt(id_data);
            }
            this.itemStack = new ItemStack(id, quantidade, data);
            if (split.length > 2) {
                for (int i = 1; i < split.length; ++i) {
                    final String compound = split[i];
                    new ItemEnchant(compound).send(this.itemStack);
                    new ItemName(compound).send(this.itemStack);
                    new ItemLore(compound).send(this.itemStack);
                    new ItemSkull(compound).send(this.itemStack);
                    new ItemGlow(compound).send(this.itemStack);
                }
            }
        }
        catch (Exception ex) {}
    }
    
    public ItemStack toItemStack() {
        return this.itemStack;
    }
}
