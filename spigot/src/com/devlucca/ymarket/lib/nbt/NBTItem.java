package com.devlucca.ymarket.lib.nbt;

import org.bukkit.inventory.*;

import com.devlucca.ymarket.util.*;

public class NBTItem
{
    private ItemStack item;
    private Reflection reflection;
    
    public NBTItem() {
        this.reflection = new Reflection();
    }
    
    public NBTItem(final ItemStack item) {
        this.reflection = new Reflection();
        this.setItem(item);
    }
    
    public Reflection getReflection() {
        return this.reflection;
    }
    
    public void setItem(final ItemStack item) {
        this.item = item;
    }
    
    public ItemStack getItem() {
        return this.item;
    }
    
    public NBTTagCompound getTag() {
        final NBTTagCompound tag = new NBTTagCompound();
        try {
            final Object nmsItem = this.getReflection().getObcClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(new Object[0], this.getItem());
            final Object nmsItemTag = nmsItem.getClass().getMethod("getTag", (Class<?>[])new Class[0]).invoke(nmsItem, new Object[0]);
            if (nmsItemTag != null) {
                tag.setNbtTag(nmsItemTag);
            }
            this.item = (ItemStack)this.getReflection().getObcClass("inventory.CraftItemStack").getMethod("asBukkitCopy", this.getReflection().getNmsClass("ItemStack")).invoke(new Object[0], nmsItem);
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
        return tag;
    }
    
    public void setTag(final NBTTagCompound tag) {
        try {
            if (tag != null) {
                final Object nmsItem = this.getReflection().getObcClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(new Object[0], this.getItem());
                nmsItem.getClass().getMethod("setTag", this.getReflection().getNmsClass("NBTTagCompound")).invoke(nmsItem, tag.getNbtTag());
                this.item = (ItemStack)this.getReflection().getObcClass("inventory.CraftItemStack").getMethod("asBukkitCopy", this.getReflection().getNmsClass("ItemStack")).invoke(new Object[0], nmsItem);
            }
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
    }
}
