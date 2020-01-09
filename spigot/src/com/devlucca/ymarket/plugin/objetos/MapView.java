package com.devlucca.ymarket.plugin.objetos;

import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.*;

import java.util.*;
import org.bukkit.inventory.meta.*;

import com.devlucca.ymarket.app.*;
import com.devlucca.ymarket.database.*;
import com.devlucca.ymarket.lib.itembuilder.*;
import com.devlucca.ymarket.lib.itembuilder.HeadBuilder.HeadsENUM;
import com.devlucca.ymarket.lib.nbt.*;

import java.sql.*;

public class MapView
{
    private Map<Integer, Inventory> inventorys;
    private int atual;
    private String title;
    private Player player;
    private ItemStack icone;
    
    public MapView(final ItemStack icone, final Player player, final String title) {
        this.player = player;
        this.title = title;
        this.inventorys = new HashMap<Integer, Inventory>();
        this.icone = icone;
        this.inventorys.put(1, this.newPage(1));
        this.atual = 1;
    }
    
    public void visualizar() {
        final Inventory page = this.inventorys.get(1);
        if (page != null) {
            this.player.openInventory(page);
        }
    }
    
    public void nextPage() {
        final Inventory nextpage = this.inventorys.get(this.atual + 1);
        if (nextpage != null) {
            this.player.openInventory(nextpage);
            ++this.atual;
        }
    }
    
    public void backPage() {
        final Inventory nextpage = this.inventorys.get(this.atual - 1);
        if (nextpage != null) {
            this.player.openInventory(nextpage);
            --this.atual;
        }
    }
    
    public void add(final ItemStack item) {
        Inventory inv = this.inventorys.get(this.inventorys.size());
        if (inv.firstEmpty() >= 46) {
            this.inventorys.put(this.inventorys.size() + 1, this.newPage(this.inventorys.size() + 1));
            inv = this.inventorys.get(this.inventorys.size());
        }
        inv.addItem(new ItemStack[] { item });
    }
    
    public Map<Integer, Inventory> getInventorys() {
        return this.inventorys;
    }
    
    public void setInventorys(final Map<Integer, Inventory> inventorys) {
        this.inventorys = inventorys;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
    private Inventory newPage(final int id) {
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 54, this.title.concat(" - " + id));
        inventory.setItem(45, this.backIcon());
        inventory.setItem(48, this.backPageIcon());
        inventory.setItem(49, this.refreshIcon());
        inventory.setItem(50, this.nextPageIcon());
        inventory.setItem(53, this.expiradosIcon());
        return inventory;
    }
    
    private ItemStack backIcon() {
        ItemStack item = new ItemStack(Material.ARROW);
        final NBTItem itemnbt = new NBTItem(item);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        newtag.setString("type", "back");
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        item = itemnbt.getItem();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cVoltar");
        meta.setLore(Arrays.asList("§7Clique para voltar a p\u00e1gina anterior."));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack backPageIcon() {
    	ItemStack item = new HeadBuilder().head(HeadsENUM.ARROW_LEFT).build();
        final NBTItem itemnbt = new NBTItem(item);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        newtag.setString("type", "back-page");
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        item = itemnbt.getItem();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e«");
        meta.setLore(Arrays.asList("§7Voltar para a p\u00e1gina anterior."));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack refreshIcon() {
        ItemStack item = this.icone.clone();
        final NBTItem itemnbt = new NBTItem(item);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        newtag.setString("type", "refresh");
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        item = itemnbt.getItem();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Atualizar");
        meta.setLore(Arrays.asList("§7Clique para atualizar o mercado."));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack nextPageIcon() {
    	ItemStack item = new HeadBuilder().head(HeadsENUM.ARROW_RIGHT).build();
        final NBTItem itemnbt = new NBTItem(item);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        newtag.setString("type", "next-page");
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        item = itemnbt.getItem();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e»");
        meta.setLore(Arrays.asList("§7Avan\u00e7ar para a pr\u00f3xima p\u00e1gina."));
        item.setItemMeta(meta);
        return item;
    }
    
    public int expiradosSize(final Player player) {
        int size = 0;
        try {
            final Database database = yMarket.database;
            database.open();
            final ResultSet result = database.query("select * from " + yMarket.table + "_expirados where player='" + player.getName() + "';");
            while (result.next()) {
                ++size;
            }
            database.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
    
    private ItemStack expiradosIcon() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        final int size = this.expiradosSize(this.player);
        final NBTItem itemnbt = new NBTItem(item);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        newtag.setString("type", "expirado");
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        item = itemnbt.getItem();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Itens expirados");
        meta.setLore(Arrays.asList("", "§7Colete seus itens expirados.", "§7Itens: " + size));
        item.setItemMeta(meta);
        return item;
    }
}
