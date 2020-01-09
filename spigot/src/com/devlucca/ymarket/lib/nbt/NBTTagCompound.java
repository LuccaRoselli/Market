package com.devlucca.ymarket.lib.nbt;

import com.devlucca.ymarket.util.*;

public class NBTTagCompound
{
    private Object nbtTag;
    private Reflection reflection;
    
    public NBTTagCompound() {
        this.reflection = new Reflection();
        try {
            this.setNbtTag(this.getReflection().getNmsClass("NBTTagCompound").getConstructor((Class<?>[])new Class[0]).newInstance(new Object[0]));
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
    }
    
    public Reflection getReflection() {
        return this.reflection;
    }
    
    public Object getNbtTag() {
        return this.nbtTag;
    }
    
    public void setNbtTag(final Object nbtTag) {
        this.nbtTag = nbtTag;
    }
    
    public void setCompound(final String name, final NBTTagCompound nbtTagCompound) {
        try {
            this.getReflection().getNmsClass("NBTTagCompound").getMethod("set", String.class, this.getReflection().getNmsClass("NBTBase")).invoke(this.nbtTag, name, nbtTagCompound.getNbtTag());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public NBTTagCompound getCompound(final String name) {
        final NBTTagCompound tag = new NBTTagCompound();
        try {
            final Object nbt = this.getReflection().getNmsClass("NBTTagCompound").getMethod("getCompound", String.class).invoke(this.nbtTag, name);
            if (nbt != null) {
                tag.setNbtTag(nbt);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return tag;
    }
    
    public void setString(final String key, final String value) {
        try {
            if (value == null) {
                this.remove(key);
            }
            else {
                this.getNbtTag().getClass().getMethod("setString", String.class, String.class).invoke(this.getNbtTag(), key, value);
            }
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
    }
    
    public String getString(final String key) {
        String value = null;
        try {
            value = (String)this.getNbtTag().getClass().getMethod("getString", String.class).invoke(this.getNbtTag(), key);
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
        return value;
    }
    
    public double getDouble(final String key) {
        double value = 0.0;
        try {
            value = (double)this.getNbtTag().getClass().getMethod("getDouble", String.class).invoke(this.getNbtTag(), key);
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
        return value;
    }
    
    public void setDouble(final String key, final double value) {
        try {
            this.getNbtTag().getClass().getMethod("setDouble", String.class, Double.TYPE).invoke(this.getNbtTag(), key, value);
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
    }
    
    public boolean has(final String key) {
        return this.getString(key) != null && !this.getString(key).isEmpty();
    }
    
    public void remove(final String key) {
        try {
            this.getNbtTag().getClass().getMethod("remove", String.class).invoke(this.getNbtTag(), key);
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
    }
}
