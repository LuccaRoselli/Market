package com.devlucca.ymarket.plugin.objetos;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.devlucca.ymarket.app.yMarket;
import com.devlucca.ymarket.database.Database;
import com.devlucca.ymarket.lib.itembuilder.ItemBuilder;
import com.devlucca.ymarket.lib.nbt.NBTItem;
import com.devlucca.ymarket.lib.nbt.NBTTagCompound;
import com.devlucca.ymarket.util.Base64;
import com.devlucca.ymarket.util.StringUtils;

public class Categoria implements Listener
{
    private ItemStack icone;
    private String id;
    private String name;
    private int slot;
    private boolean sellall;
    private String description;
    private List<String> itens;
    private List<String> filterNames;
    private List<String> filterLores;
    private List<String> filterEnchants;
    private Map<String, MapView> mapViews;
    
    public Categoria(final ItemStack icone, final String id, final String name, final int slot, final boolean sellall) {
        this.icone = icone;
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.sellall = sellall;
        this.description = "";
        this.itens = new ArrayList<String>();
        this.filterNames = new ArrayList<String>();
        this.filterLores = new ArrayList<String>();
        this.filterEnchants = new ArrayList<String>();
        this.mapViews = new HashMap<String, MapView>();
    }
    
    public void register() {
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)yMarket.instance);
    }
    
    public void unregister() {
        HandlerList.unregisterAll((Listener)this);
    }
    
    public void expirar(final String jogador, final String uuid, final boolean remove) {
        try {
            final Database database = yMarket.database;
            final String mercado = yMarket.table.concat("_mercado");
            final String expirado = yMarket.table.concat("_expirados");
            database.open();
            final ResultSet result = database.query("select * from " + mercado + " where uuid='" + uuid + "';");
            if (result.next()) {
                final String cache = result.getString("cache");
                final String player = result.getString("player");
                database.execute("insert into " + expirado + " values ('" + player + "', '" + uuid + "', '" + cache + "');");
                database.execute("delete from " + mercado.concat(" where uuid='").concat(uuid).concat("';"));
                final Player target = Bukkit.getPlayer(player);
                if (remove && target != null && !target.getName().equalsIgnoreCase(jogador)) {
                    target.sendMessage(yMarket.mensagens.get("ITEM_REMOVE_TARGET"));
                }
            }
            database.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void vizualizar(final Player player) {
        String invname = StringUtils.removeColors(this.name);
        invname = ((invname.length() <= 10) ? invname : invname.substring(0, 10));
        final MapView mapview = new MapView(this.icone.clone(), player, "§0§0§8Mercado - ".concat(invname));
        try {
            final Database database = yMarket.database;
            database.open();
            final ResultSet result = database.query("select * from " + yMarket.table + "_mercado where categoria='" + this.getId() + "';");
            while (result.next()) {
                try {
                    final String dono = result.getString("player");
                    final UUID uuid = UUID.fromString(result.getString("uuid"));
                    final double preco = result.getDouble("preco");
                    final long tempo = result.getLong("tempo");
                    final List<ItemStack> itens = Base64.fromBase64(result.getString("cache"));
                    if (System.currentTimeMillis() >= tempo) {
                        continue;
                    }
                    ItemStack icone = null;
                    if (itens.size() > 1) {
                        icone = new ItemBuilder("54 1 name:&6Ba\u00fa_De_Itens lore:&6 lore:&7Clique_com_o_bot\u00e3o_direito_para_ver_os_itens.").toItemStack();
                    }
                    else {
                        icone = itens.get(0).clone();
                    }
                    icone = this.toProduto(player, icone, uuid, dono, preco);
                    mapview.add(icone);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (this.getId().startsWith("PESSOAL:")) {
                final ResultSet query = database.query("select * from " + yMarket.table + "_mercado where player='" + player.getName() + "';");
                while (query.next()) {
                    try {
                        final String categoria = query.getString("categoria");
                        if (!categoria.startsWith("PESSOAL:")) {
                            continue;
                        }
                        final UUID uuid2 = UUID.fromString(query.getString("uuid"));
                        final double preco2 = query.getDouble("preco");
                        final long tempo2 = query.getLong("tempo");
                        final List<ItemStack> itens2 = Base64.fromBase64(query.getString("cache"));
                        if (System.currentTimeMillis() >= tempo2) {
                            continue;
                        }
                        ItemStack icone2 = null;
                        if (itens2.size() > 1) {
                            icone2 = new ItemBuilder("54 1 name:&6Ba\u00fa_De_Itens lore:&6 lore:&7Clique_com_o_bot\u00e3o_direito_para_ver_os_itens.").toItemStack();
                        }
                        else {
                            icone2 = itens2.get(0).clone();
                        }
                        icone2 = this.toProduto(player, icone2, uuid2, player.getName(), preco2);
                        mapview.add(icone2);
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            database.close();
        }
        catch (Exception e3) {
            e3.printStackTrace();
        }
        this.mapViews.put(player.getName(), mapview);
        mapview.visualizar();
    }
    
    public void addItem(final String item) {
        if (!this.itens.contains(item)) {
            this.itens.add(item);
        }
    }
    
    public ItemStack toItemStack(final Player player, final boolean contagem) {
        if (this.icone != null) {
            try {
                ItemStack item = this.icone.clone();
                final NBTItem itemnbt = new NBTItem(item);
                final NBTTagCompound itemtag = itemnbt.getTag();
                final NBTTagCompound newtag = new NBTTagCompound();
                newtag.setString("type", this.getId());
                itemtag.setCompound("huntersmarket", newtag);
                itemnbt.setTag(itemtag);
                item = itemnbt.getItem();
                final ItemMeta meta = item.getItemMeta();
                final List<String> newlore = new ArrayList<String>();
                newlore.add("");
                if (!this.description.isEmpty()) {
                    newlore.add(this.description.replace("&", "§").replace("{player}", player.getName()));
                }
                final Database db = yMarket.database;
                final String table = yMarket.table.concat("_mercado");
                int quantidade = 0;
                db.open();
                final ResultSet result = db.query("select * from ".concat(table).concat(" where categoria='").concat(this.id).concat("';"));
                while (result.next()) {
                    if (System.currentTimeMillis() < result.getLong("tempo")) {
                        ++quantidade;
                    }
                }
                db.close();
                newlore.add("§7Itens dispon\u00edveis: §e" + quantidade);
                if (meta.getDisplayName() == null) {
                    meta.setDisplayName(this.name.replace("&", "§"));
                }
                meta.setLore(newlore);
                item.setItemMeta(meta);
                if (contagem) {
                    item.setAmount((quantidade >= 64) ? 64 : quantidade);
                }
                return item;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    @SuppressWarnings("deprecation")
	public boolean verify(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        if (this.itens.contains(String.valueOf(String.valueOf(item.getTypeId()) + ":" + item.getDurability())) || this.itens.contains(String.valueOf(item.getTypeId()))) {
            final ItemMeta meta = item.getItemMeta();
            if (!this.filterNames.isEmpty() && meta.getDisplayName() != null) {
                for (final String filtername : this.filterNames) {
                    if (meta.getDisplayName().toLowerCase().contains(filtername.toLowerCase())) {
                        return false;
                    }
                }
            }
            if (!this.filterLores.isEmpty() && meta.getLore() != null) {
                for (final String string : meta.getLore()) {
                    for (final String filterlores : this.filterLores) {
                        if (string.toLowerCase().contains(filterlores.toLowerCase())) {
                            return false;
                        }
                    }
                }
            }
            if (!this.filterEnchants.isEmpty() && !item.getEnchantments().isEmpty()) {
                for (final Enchantment enchant : item.getEnchantments().keySet()) {
                    for (final String filterenchant : this.filterEnchants) {
                        if (enchant.getName().toLowerCase().contains(filterenchant.toLowerCase())) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private int getEspaco(final Inventory inventory) {
        int amount = 0;
        final int size = inventory.getSize();
        ItemStack[] arrayOfItemStack;
        for (int j = (arrayOfItemStack = inventory.getContents()).length, i = 0; i < j; ++i) {
            final ItemStack item = arrayOfItemStack[i];
            if (item != null && item.getType() != Material.AIR) {
                ++amount;
            }
        }
        final int espaco = size - amount;
        return espaco;
    }
    
    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        String invname = StringUtils.removeColors(this.name);
        invname = ((invname.length() <= 10) ? invname : invname.substring(0, 10));
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player)event.getWhoClicked();
            if (event.getInventory().getTitle().startsWith("§0§0§8Mercado - ".concat(invname))) {
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                final NBTItem itemnbt = new NBTItem(event.getCurrentItem());
                final NBTTagCompound itemtag = itemnbt.getTag();
                final NBTTagCompound hunterstag = itemtag.getCompound("huntersmarket");
                if (hunterstag.getNbtTag() != null) {
                    final MapView mapview = this.mapViews.get(player.getName());
                    if (mapview != null) {
                        final String type = hunterstag.getString("type");
                        if (type.equalsIgnoreCase("produto")) {
                            try {
                                final Database database = yMarket.database;
                                database.open();
                                final ResultSet result = database.query("select * from " + yMarket.table + "_mercado where uuid='" + hunterstag.getString("uuid") + "';");
                                if (result.next()) {
                                    final List<ItemStack> itens = Base64.fromBase64(result.getString("cache"));
                                    final String dono = result.getString("player");
                                    final double preco = result.getDouble("preco");
                                    if (event.getClick() == ClickType.RIGHT) {
                                        this.vizualizarItens(player, itens, dono, preco, UUID.fromString(hunterstag.getString("uuid")));
                                    }
                                    else if (player.getName().equals(dono)) {
                                        if (this.getEspaco((Inventory)player.getInventory()) >= itens.size()) {
                                            database.execute("delete from " + yMarket.table + "_mercado where uuid='" + hunterstag.getString("uuid") + "';");
                                            for (final ItemStack item : itens) {
                                                player.getInventory().addItem(new ItemStack[] { item });
                                            }
                                            player.sendMessage(yMarket.mensagens.get("COLLECT_SUCESS"));
                                            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);
                                            this.vizualizar(player);
                                        }
                                        else {
                                            player.sendMessage(yMarket.mensagens.get("INVENTORY_FULL"));
                                            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                                        }
                                    }
                                    else if (yMarket.vault.getEconomy().getBalance((OfflinePlayer)player) >= preco) {
                                        this.confirmar(player, itens, dono, preco, UUID.fromString(hunterstag.getString("uuid")));
                                    }
                                    else {
                                        player.sendMessage(yMarket.mensagens.get("NO_MONEY"));
                                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                                    }
                                }
                                else {
                                    player.sendMessage(yMarket.mensagens.get("ITEM_NOTFOUND"));
                                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                                    this.vizualizar(player);
                                }
                                database.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (type.equalsIgnoreCase("back")) {
                            yMarket.manager.mercado(player);
                        }
                        if (type.equalsIgnoreCase("back-page")) {
                            mapview.backPage();
                        }
                        if (type.equalsIgnoreCase("refresh")) {
                            this.vizualizar(player);
                        }
                        if (type.equalsIgnoreCase("next-page")) {
                            mapview.nextPage();
                        }
                        if (type.equalsIgnoreCase("expirado")) {
                            yMarket.manager.expirados(player);
                        }
                    }
                }
            }
            else if (event.getInventory().getTitle().startsWith("§e§e§8Detalhes - ".concat(invname))) {
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                final NBTItem itemnbt = new NBTItem(event.getCurrentItem());
                final NBTTagCompound itemtag = itemnbt.getTag();
                final NBTTagCompound hunterstag = itemtag.getCompound("huntersmarket");
                if (hunterstag.getNbtTag() != null) {
                    final String type2 = hunterstag.getString("type");
                    if (type2.equalsIgnoreCase("back")) {
                        this.vizualizar(player);
                        return;
                    }
                    if (type2.equalsIgnoreCase("remove")) {
                        if (player.hasPermission("ymarket.admin")) {
                            this.expirar(player.getName(), hunterstag.getString("uuid"), true);
                            this.vizualizar(player);
                            player.sendMessage(yMarket.mensagens.get("ITEM_REMOVE_SUCESS"));
                            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);
                        }
                        else {
                            this.vizualizar(player);
                            player.sendMessage(yMarket.mensagens.get("NO_PERMISSION"));
                            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                        }
                    }
                }
            }
        }
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
    
    private ItemStack removeIcon(final UUID uuid) {
        ItemStack item = new ItemStack(Material.WOOL, 1, (short)1);
        final NBTItem itemnbt = new NBTItem(item);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        newtag.setString("type", "remove");
        newtag.setString("uuid", uuid.toString());
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        item = itemnbt.getItem();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Remover");
        meta.setLore(Arrays.asList("§7Clique aqui para remover este item do mercado.", "§7Este item vai para os itens expirados do dono."));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack toProduto(ItemStack icone, final UUID uuid, final String dono, final double valor) {
        final String prefix = yMarket.vault.getPrefix(dono);
        final NBTItem itemnbt = new NBTItem(icone);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        List<String> lore = new ArrayList<String>();
        newtag.setString("type", "produto");
        newtag.setString("uuid", uuid.toString());
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        icone = itemnbt.getItem();
        final ItemMeta meta = icone.getItemMeta();
        if (meta.getLore() != null) {
            lore = (List<String>)meta.getLore();
        }
        lore.add("");
        lore.add("§7Vendedor: §a" + prefix + dono);
        lore.add("§7Pre\u00e7o: §a" + yMarket.numberFormat(valor));
        meta.setLore(lore);
        icone.setItemMeta(meta);
        return icone;
    }
    
    private ItemStack toProduto(final Player player, ItemStack icone, final UUID uuid, final String dono, final double valor) {
        final String prefix = yMarket.vault.getPrefix(dono);
        final NBTItem itemnbt = new NBTItem(icone);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        List<String> lore = new ArrayList<String>();
        newtag.setString("type", "produto");
        newtag.setString("uuid", uuid.toString());
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        icone = itemnbt.getItem();
        final ItemMeta meta = icone.getItemMeta();
        if (meta.getLore() != null) {
            lore = (List<String>)meta.getLore();
        }
        lore.add("");
        if (player.getName().equalsIgnoreCase(dono)) {
            lore.add("§7Vendedor: §a" + prefix + dono);
            lore.add("§7Pre\u00e7o: §a" + yMarket.numberFormat(valor));
            lore.add("");
            lore.add("§aClique aqui para coletar esse item.");
        }
        else {
            final double saldo = yMarket.vault.getEconomy().getBalance((OfflinePlayer)player);
            if (saldo >= valor) {
                lore.add("§7Vendedor: §a" + prefix + dono);
                lore.add("§7Pre\u00e7o: §a" + yMarket.numberFormat(valor));
                lore.add("");
                lore.add("§aClique aqui para adquirir esse item.");
            }
            else {
                lore.add("§7Vendedor: §c" + prefix + dono);
                lore.add("§7Pre\u00e7o: §c" + yMarket.numberFormat(valor));
                lore.add("");
                lore.add("§cSaldo insuficiente.");
            }
        }
        meta.setLore(lore);
        icone.setItemMeta(meta);
        return icone;
    }
    
    private void vizualizarItens(final Player player, final List<ItemStack> itens, final String dono, final double valor, final UUID uuid) {
        Inventory inventory = null;
        final String invname = StringUtils.removeColors(this.name);
        if (itens.size() > 1) {
            inventory = Bukkit.createInventory((InventoryHolder)null, 54, "§e§e§8Detalhes - ".concat(invname));
            for (final ItemStack item : itens) {
                inventory.addItem(new ItemStack[] { item });
            }
            inventory.setItem(45, this.backIcon());
            if (player.hasPermission("ymarket.admin")) {
                inventory.setItem(53, this.removeIcon(uuid));
            }
        }
        else {
            inventory = Bukkit.createInventory((InventoryHolder)null, 36, "§e§e§8Detalhes - ".concat(invname));
            inventory.setItem(13, (ItemStack)itens.get(0));
            inventory.setItem(27, this.backIcon());
            if (player.hasPermission("ymarket.admin")) {
                inventory.setItem(35, this.removeIcon(uuid));
            }
        }
        player.openInventory(inventory);
    }
    
    private void confirmar(final Player player, final List<ItemStack> itens, final String dono, final double valor, final UUID uuid) {
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 36, "§e§e§8Confirmar Compra");
        if (itens.size() > 1) {
            inventory.setItem(13, this.toProduto(new ItemBuilder("54 1 name:&6Ba\u00fa_De_Itens").toItemStack(), uuid, dono, valor));
        }
        else {
            inventory.setItem(13, this.toProduto(itens.get(0), uuid, dono, valor));
        }
        ItemStack yes = new ItemBuilder("35:5 1 name:&aComprar_Item lore:&7Clique_para_comprar_este_item.").toItemStack();
        ItemStack no = new ItemBuilder("35:14 1 name:&cCancelar lore:&7Clique_para_cancelar_esta_opera\u00e7\u00e3o.").toItemStack();
        final NBTItem yesnbt = new NBTItem(yes);
        final NBTTagCompound yestag = yesnbt.getTag();
        final NBTTagCompound newyestag = new NBTTagCompound();
        newyestag.setString("type", "confirmar");
        newyestag.setString("uuid", uuid.toString());
        yestag.setCompound("huntersmarket", newyestag);
        yesnbt.setTag(yestag);
        yes = yesnbt.getItem();
        final NBTItem nonbt = new NBTItem(no);
        final NBTTagCompound notag = nonbt.getTag();
        final NBTTagCompound newnotag = new NBTTagCompound();
        newnotag.setString("type", "cancelar");
        newnotag.setString("uuid", uuid.toString());
        notag.setCompound("huntersmarket", newnotag);
        nonbt.setTag(notag);
        no = nonbt.getItem();
        inventory.setItem(24, no);
        inventory.setItem(20, yes);
        player.openInventory(inventory);
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        if (description == null) {
            this.description = "";
        }
        this.description = description;
    }
    
    public ItemStack getIcone() {
        return this.icone;
    }
    
    public void setIcone(final ItemStack icone) {
        this.icone = icone;
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public int getSlot() {
        return this.slot;
    }
    
    public void setSlot(final int slot) {
        this.slot = slot;
    }
    
    public boolean isSellall() {
        return this.sellall;
    }
    
    public void setSellall(final boolean sellall) {
        this.sellall = sellall;
    }
    
    public List<String> getItens() {
        return this.itens;
    }
    
    public void setItens(final List<String> itens) {
        this.itens = itens;
    }
    
    public List<String> getFilterNames() {
        return this.filterNames;
    }
    
    public void setFilterNames(final List<String> filterNames) {
        this.filterNames = filterNames;
    }
    
    public List<String> getFilterLores() {
        return this.filterLores;
    }
    
    public void setFilterLores(final List<String> filterLores) {
        this.filterLores = filterLores;
    }
    
    public List<String> getFilterEnchants() {
        return this.filterEnchants;
    }
    
    public void setFilterEnchants(final List<String> filterEnchants) {
        this.filterEnchants = filterEnchants;
    }
}
