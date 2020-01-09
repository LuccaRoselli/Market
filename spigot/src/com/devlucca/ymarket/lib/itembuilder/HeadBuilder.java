package com.devlucca.ymarket.lib.itembuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.devlucca.ymarket.lib.itembuilder.parts.reflection.Reflection;
import com.devlucca.ymarket.lib.itembuilder.parts.reflection.ReflectionUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public class HeadBuilder
{
    private ItemStack is;
    private String name;
    private static final Base64 base64;
    
    static {
        base64 = new Base64();
    }
    
    
    public HeadBuilder(final Material m) {
        this(m, 1, (short)0);
    }
    
    public HeadBuilder() {
        this.is = new ItemStack(Material.STONE, 1, (short)0);
    }
    
    public HeadBuilder(final ItemStack is) {
        this.is = is.clone();
    }
    
    public HeadBuilder(final Material m, final short data) {
        this.is = new ItemStack(m, 1, data);
    }
    
    public HeadBuilder(final Material m, final int amount, final short data) {
        this.is = new ItemStack(m, amount, data);
    }
    
    public HeadBuilder clone() {
        return new HeadBuilder(this.is);
    }
    
    public HeadBuilder durability(final int dur) {
        this.is.setDurability((short)dur);
        return this;
    }
    
    public HeadBuilder name(final String name) {
        final ItemMeta im = this.is.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.is.setItemMeta(im);
        this.name = name;
        return this;
    }
    
    public HeadBuilder unsafeEnchantment(final Enchantment ench, final int level) {
        this.is.addUnsafeEnchantment(ench, level);
        return this;
    }
    
    public HeadBuilder enchant(final Enchantment ench, final int level) {
        final ItemMeta im = this.is.getItemMeta();
        im.addEnchant(ench, level, true);
        this.is.setItemMeta(im);
        return this;
    }

    public HeadBuilder removeEnchantment(final Enchantment ench) {
        this.is.removeEnchantment(ench);
        return this;
    }
    
    public HeadBuilder owner(final String owner) {
        try {
            this.is = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
            final SkullMeta im = (SkullMeta)this.is.getItemMeta();
            im.setOwner(owner);
            this.is.setItemMeta((ItemMeta)im);
        }
        catch (ClassCastException ex) {}
        return this;
    }
    
    public HeadBuilder infinityDurabilty() {
        this.is.setDurability((short)32767);
        return this;
    }
    
    public HeadBuilder lore(final String... lore) {
        final ItemMeta im = this.is.getItemMeta();
        final List<String> out = (im.getLore() == null) ? new ArrayList<String>() : im.getLore();
        for (final String string : lore) {
            out.add(ChatColor.translateAlternateColorCodes('&', string));
        }
        im.setLore(out);
        this.is.setItemMeta(im);
        return this;
    }
    
    public HeadBuilder lore(final List<String> lore) {
        final ItemMeta im = this.is.getItemMeta();
        final List<String> out = (im.getLore() == null) ? new ArrayList<String>() : im.getLore();
        for (final String string : lore) {
            out.add(ChatColor.translateAlternateColorCodes('&', string));
        }
        im.setLore(out);
        this.is.setItemMeta(im);
        return this;
    }
    
    public HeadBuilder amount(int amount) {
        if (amount > 64) {
            amount = 64;
        }
        this.is.setAmount(amount);
        return this;
    }
    
    public HeadBuilder removeAttributes() {
        final ItemMeta meta = this.is.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        this.is.setItemMeta(meta);
        return this;
    }
    
    public ItemStack build() {
        if (this.name != null) {
            final ItemMeta im = this.is.getItemMeta();
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.name));
            this.is.setItemMeta(im);
        }
        return this.is;
    }
 
	public HeadBuilder glow(boolean b) {
		if (b)
			return makeNBTStack(this.is, true, is.getItemMeta().getDisplayName(), is.getItemMeta().getLore());
		else
			return this;
	}

	private HeadBuilder makeNBTStack(ItemStack item, boolean enchanted, String name, List<String> lore) {
		try {
			Class<?> ItemStack = ReflectionUtils.getCraftClass("ItemStack");
			Class<?> NBTTagCompound = ReflectionUtils.getCraftClass("NBTTagCompound");
			Class<?> NBTTagList = ReflectionUtils.getCraftClass("NBTTagList");
			Class<?> CraftItemStack = ReflectionUtils.getBukkitClass("inventory.CraftItemStack");
			Class<?> NBTTagString = ReflectionUtils.getCraftClass("NBTTagString");
			Class<?> NBTBase = ReflectionUtils.getCraftClass("NBTBase");

			Method asNMSCopy = CraftItemStack.getDeclaredMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
			Method asCraftMirror = CraftItemStack.getDeclaredMethod("asCraftMirror", ItemStack);
			Method hasTag = ItemStack.getDeclaredMethod("hasTag");
			Method setTag = ItemStack.getDeclaredMethod("setTag", NBTTagCompound);
			Method getTag = ItemStack.getDeclaredMethod("getTag");
			Method set = NBTTagCompound.getDeclaredMethod("set", String.class, NBTBase);
			Method add = NBTTagList.getDeclaredMethod("add", NBTBase);

			asNMSCopy.setAccessible(true);
			asCraftMirror.setAccessible(true);
			hasTag.setAccessible(true);
			setTag.setAccessible(true);
			getTag.setAccessible(true);
			set.setAccessible(true);

			Constructor<?> NBTTagCompoundConstructor = NBTTagCompound.getConstructor();
			Constructor<?> NBTTagListConstructor = NBTTagList.getConstructor();
			Constructor<?> NBTTagStringConstructor = NBTTagString.getConstructor(String.class);

			NBTTagCompoundConstructor.setAccessible(true);
			NBTTagListConstructor.setAccessible(true);

			Object nmsStack = asNMSCopy.invoke(null, item);
			Object tag = null;

			if ((Boolean) hasTag.invoke(nmsStack))
				tag = getTag.invoke(nmsStack);
			else
				tag = NBTTagCompoundConstructor.newInstance();

			if (enchanted) {
				Object ench = NBTTagListConstructor.newInstance();
				set.invoke(tag, "ench", ench);
			}

			Object display = NBTTagCompoundConstructor.newInstance();
			set.invoke(display, "Name", NBTTagStringConstructor.newInstance(name));

			if (!(lore == null)) {
				Object loreObj = NBTTagListConstructor.newInstance();
				for (String str : lore) {
					add.invoke(loreObj, NBTTagStringConstructor.newInstance(str));
				}
				set.invoke(display, "Lore", loreObj);
			}

			set.invoke(tag, "display", display);

			setTag.invoke(nmsStack, tag);
			this.is = (org.bukkit.inventory.ItemStack) asCraftMirror.invoke(null, nmsStack);
			return this;
		} catch (Exception e) {
			e.printStackTrace();
			return this;
		}
	}
	
    
    public HeadBuilder head(final String texture) {
        final GameProfile profile = new GameProfile(UUID.randomUUID(), (String)null);
        final PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) {
            throw new IllegalStateException("Profile doesn't contain a property map");
        }
        final byte[] encodedData = HeadBuilder.base64.encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", texture).getBytes());
        propertyMap.put("textures", new Property("textures", new String(encodedData)));
        this.is = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        final ItemMeta headMeta = this.is.getItemMeta();
        final Class<?> headMetaClass = headMeta.getClass();
        Reflection.getField(headMetaClass, "profile", GameProfile.class).set(headMeta, profile);
        this.is.setItemMeta(headMeta);
        return this;
    }
    
    public HeadBuilder head(final HeadsENUM headenum) {
        this.head(headenum.toString());
        return this;
    }
    
    public static boolean RefSet(final Class<?> sourceClass, final Object instance, final String fieldName, final Object value) {
        try {
            final Field field = sourceClass.getDeclaredField(fieldName);
            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            final int modifiers = modifiersField.getModifiers();
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if ((modifiers & 0x10) == 0x10) {
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modifiers & 0xFFFFFFEF);
            }
            try {
                field.set(instance, value);
            }
            finally {
                if ((modifiers & 0x10) == 0x10) {
                    modifiersField.setInt(field, modifiers | 0x10);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(false);
                }
            }
            if ((modifiers & 0x10) == 0x10) {
                modifiersField.setInt(field, modifiers | 0x10);
            }
            if (!field.isAccessible()) {
                field.setAccessible(false);
            }
            return true;
        }
        catch (Exception var11) {
            Bukkit.getLogger().log(Level.WARNING, "Unable to inject Gameprofile", var11);
            return false;
        }
    }
    
    public enum HeadsENUM
    {
        ARROW_LEFT("ARROW_LEFT", 0, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), 
        ARROW_RIGHT("ARROW_RIGHT", 1, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b"), 
        ARROW_UP("ARROW_UP", 2, "http://textures.minecraft.net/texture/d48b768c623432dfb259fb3c3978e98dec111f79dbd6cd88f21155374b70b3c"), 
        ARROW_DOWN("ARROW_DOWN", 3, "http://textures.minecraft.net/texture/2dadd755d08537352bf7a93e3bb7dd4d733121d39f2fb67073cd471f561194dd"), 
        QUESTION("QUESTION", 4, "http://textures.minecraft.net/texture/d48b768c623432dfb259fb3c3978e98dec111f79dbd6cd88f21155374b70b3c"), 
        EXCLAMATION("EXCLAMATION", 5, "http://textures.minecraft.net/texture/6a53bdd1545531c9ebb9c6f895bc576012f61820e6f489885988a7e8709a3f48"), 
        NOTEBOOK("NOTEBOOK", 6, "http://textures.minecraft.net/texture/2e5793f0cc40a9368252714bc5263a5c3df2233bddf8a57e3d8d3f54af6726c"), 
        ZOMBIE_PIGMAN("ZOMBIE_PIGMAN", 7, "http://textures.minecraft.net/texture/74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"), 
        PIG("PIG", 8, "http://textures.minecraft.net/texture/621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4"), 
        SHEEP("SHEEP", 9, "http://textures.minecraft.net/texture/f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70"), 
        BLAZE("BLAZE", 10, "http://textures.minecraft.net/texture/b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0"),
        CHICKEN("CHICKEN", 11, "MHF_Chicken"), 
        COW("COW", 12, "MHF_Cow"), 
        SLIME("SLIME", 13, "MHF_Slime"), 
        SPIDER("SPIDER", 14, "MHF_Spider"), 
        SQUID("SQUID", 15, "MHF_Squid"), 
        VILLAGER("VILLAGER", 16, "MHF_Villager"), 
        OCELOT("OCELOT", 17, "MHF_Ocelot"), 
        HEROBRINE("HEROBRINE", 18, "MHF_Herobrine"), 
        LAVA_SLIME("LAVA_SLIME", 19, "MHF_LavaSlime"), 
        MOOSHROOM("MOOSHROOM", 20, "MHF_MushroomCow"), 
        GOLEM("GOLEM", 21, "MHF_Golem"), 
        GHAST("GHAST", 22, "MHF_Ghast"), 
        ENDERMAN("ENDERMAN", 23, "MHF_Enderman"), 
        CAVE_SPIDER("CAVE_SPIDER", 24, "MHF_CaveSpider"), 
        CACTUS("CACTUS", 25, "MHF_Cactus"), 
        CAKE("CAKE", 26, "MHF_Cake"), 
        CHEST("CHEST", 27, "MHF_Chest"), 
        MELON("MELON", 28, "MHF_Melon"), 
        LOG("LOG", 29, "MHF_OakLog"), 
        PUMPKIN("PUMPKIN", 30, "MHF_Pumpkin"), 
        TNT("TNT", 31, "MHF_TNT"), 
        DYNAMITE("DYNAMITE", 32, "MHF_TNT2"),
        QUESTION_NEW("QUESTION_NEW", 33, "http://textures.minecraft.net/texture/53d7575a6d8feffb22783e1e2b6dfbe2996fadbc9779f88e742a012c62aa");
        
        private final String text;
        
        private HeadsENUM(final String s, final int n, final String text) {
            this.text = text;
        }
        
        @Override
        public String toString() {
            return this.text;
        }
    }
    
}

