package com.devlucca.ymarket.plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import com.devlucca.ymarket.app.yMarket;
import com.devlucca.ymarket.database.Database;
import com.devlucca.ymarket.lib.fanciful.FancyMessage;
import com.devlucca.ymarket.lib.itembuilder.ItemBuilder;
import com.devlucca.ymarket.lib.nbt.NBTItem;
import com.devlucca.ymarket.lib.nbt.NBTTagCompound;
import com.devlucca.ymarket.plugin.objetos.Categoria;
import com.devlucca.ymarket.util.Base64;

public class MercadoManager implements Listener
{
    private Map<String, Categoria> categorias;
    private Map<String, Categoria> categoriasPessoais;
    private double precomax;
    private Map<String, Long> broadcastDelays;
    
    public MercadoManager() {
        final YamlConfiguration config = yMarket.config.getYaml();
        this.categorias = new HashMap<String, Categoria>();
        this.categoriasPessoais = new HashMap<String, Categoria>();
        this.precomax = config.getDouble("preco_max");
        this.broadcastDelays = new HashMap<String, Long>();
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)yMarket.instance);
        this.loadCategorias();
    }
    
    private List<String> toList(String string) {
        final List<String> list = new ArrayList<String>();
        if (string != null && !string.isEmpty()) {
            string = string.replace("&", "§");
            if (string.contains(", ")) {
                final String[] split = string.split(", ");
                String[] arrayOfString1;
                for (int j = (arrayOfString1 = split).length, i = 0; i < j; ++i) {
                    final String s = arrayOfString1[i];
                    list.add(s);
                }
            }
            else {
                list.add(string);
            }
        }
        return list;
    }
    
    public void loadCategorias() {
        final YamlConfiguration config = yMarket.config.getYaml();
        if (!this.categorias.isEmpty()) {
            for (final Categoria c : this.categorias.values()) {
                c.unregister();
            }
        }
        for (final String id : config.getConfigurationSection("categorias").getKeys(false)) {
            final String path = "categorias." + id + ".";
            final String name = config.getString(path.concat("name")).replace("&", "§");
            final int slot = config.getInt(path.concat("slot"));
            final ItemStack icone = new ItemBuilder(config.getString(path.concat("icone")).replace("&", "§")).toItemStack();
            final boolean sellall = config.getBoolean(path.concat("sellall"));
            final List<String> itens = this.toList(config.getString(path.concat("itens")));
            final List<String> filternames = this.toList(config.getString(path.concat("filternames")));
            final List<String> filterlores = this.toList(config.getString(path.concat("filterlores")));
            final List<String> filterenchants = this.toList(config.getString(path.concat("filterenchants")));
            final String description = config.getString(path.concat("description"));
            if (description != null) {
                description.replace("&", "§");
            }
            final Categoria categoria = new Categoria(icone, id, name, slot, sellall);
            if (description != null) {
                categoria.setDescription(description);
            }
            if (!itens.isEmpty()) {
                for (final String items : itens) {
                    categoria.addItem(items);
                }
            }
            categoria.setFilterNames(filternames);
            categoria.setFilterLores(filterlores);
            categoria.setFilterEnchants(filterenchants);
            categoria.register();
            this.categorias.put(id.toLowerCase(), categoria);
        }
    }
    
    public void vender(final Player player, final double valor, final ItemStack item, final boolean vendertudo) {
        if (valor <= 0.0 || valor > this.precomax) {
            player.sendMessage(yMarket.mensagens.get("NUMBER_NULL").replace("{valor_max}", yMarket.numberFormat(this.precomax)));
            return;
        }
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(yMarket.mensagens.get("ITEM_NULL"));
            return;
        }
        final YamlConfiguration config = yMarket.config.getYaml();
        final Database db = yMarket.database;
        final String table = yMarket.table.concat("_mercado");
        final long tempo = System.currentTimeMillis() + config.getLong("tempo") * 1000L;
        final List<ItemStack> itens = new ArrayList<ItemStack>();
        String categoria_id = null;
        for (final Categoria categoria : this.categorias.values()) {
            if (categoria.verify(item)) {
                if (vendertudo) {
                    if (!categoria.isSellall()) {
                        player.sendMessage(yMarket.mensagens.get("ITEM_NOCOMP2"));
                        return;
                    }
                    int slot = 0;
                    final ItemStack hand = player.getItemInHand();
                    if (hand.equals((Object)item)) {
                        for (int i = 0; i < player.getInventory().getContents().length; ++i) {
                            final ItemStack contents = player.getInventory().getItem(i);
                            if (contents != null && this.equals(item, contents)) {
                                player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                                itens.add(contents);
                            }
                            ++slot;
                        }
                    }
                }
                else {
                    final ItemStack hand2 = player.getItemInHand();
                    if (hand2.equals((Object)item)) {
                        player.setItemInHand(new ItemStack(Material.AIR));
                        itens.add(item);
                    }
                }
                categoria_id = categoria.getId();
                break;
            }
        }
        if (categoria_id == null) {
            player.sendMessage(yMarket.mensagens.get("ITEM_NOCOMP"));
            return;
        }
        if (itens.isEmpty()) {
            player.sendMessage(yMarket.mensagens.get("SELL_ERROR"));
            return;
        }
        db.open();
        db.execute("insert into ".concat(table).concat(" values ('").concat(player.getName()).concat("', '").concat(categoria_id).concat("', '").concat(UUID.randomUUID().toString()).concat("', '").concat(String.valueOf(valor)).concat("', '").concat(String.valueOf(tempo)).concat("', '").concat(Base64.toBase64(itens)).concat("');"));
        db.close();
        player.sendMessage(yMarket.mensagens.get("SELL_SUCESS").replace("{valor}", yMarket.numberFormat(valor)));
        if (config.getBoolean("broadcast.enable")) {
            final long delay = config.getLong("broadcast.delay");
            final Long lastdelay = this.broadcastDelays.get(player.getName());
            if (lastdelay != null && System.currentTimeMillis() < lastdelay) {
                return;
            }
            if (valor >= config.getDouble("broadcast.valor") && player.hasPermission("ymarket.broadcast")) {
                this.broadcastDelays.put(player.getName(), System.currentTimeMillis() + 1000L * delay);
                FancyMessage msg = null;
                if (itens.size() > 1) {
                    msg = new FancyMessage("§l[MERCADO] ");
                    msg.color(ChatColor.BLUE).then(String.valueOf(player.getName()) + " colocou um ba\u00fa de itens no mercado por " + yMarket.numberFormat(valor) + " coins.").color(ChatColor.GRAY).itemTooltip(new ItemBuilder("54 1 name:&9Ba\u00fa_De_Itens").toItemStack()).command("/mercado ver " + categoria_id);
                }
                else {
                    msg = new FancyMessage("§l[MERCADO] ");
                    msg.color(ChatColor.BLUE).then(String.valueOf(player.getName()) + " colocou um item no mercado por " + yMarket.numberFormat(valor) + " coins.").color(ChatColor.GRAY).itemTooltip(item).command("/mercado ver " + categoria_id);
                }
                for (final World world : Bukkit.getWorlds()) {
                    for (final Player targetPlayer : world.getPlayers()) {
                        msg.send(targetPlayer);
                    }
                }
            }
        }
    }
    
    public void vender(final Player player, final String target, final double valor, final ItemStack item, final boolean vendertudo) {
        if (valor <= 0.0 || valor > this.precomax) {
            player.sendMessage(yMarket.mensagens.get("NUMBER_NULL").replace("{valor_max}", yMarket.numberFormat(this.precomax)));
            return;
        }
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(yMarket.mensagens.get("ITEM_NULL"));
            return;
        }
        if (player.getName().equalsIgnoreCase(target)) {
            player.sendMessage(yMarket.mensagens.get("SELL_ERRO_PESSOAL"));
            return;
        }
        Categoria pessoal = this.categoriasPessoais.get(target);
        if (Bukkit.getPlayer(target) != null) {
            this.loadPessoal(Bukkit.getPlayer(target));
            pessoal = this.categoriasPessoais.get(target);
        }
        if (pessoal != null) {
            final YamlConfiguration config = yMarket.config.getYaml();
            final Database db = yMarket.database;
            final String table = yMarket.table.concat("_mercado");
            final long tempo = System.currentTimeMillis() + config.getLong("tempo") * 1000L;
            final List<ItemStack> itens = new ArrayList<ItemStack>();
            String categoria_id = null;
            for (final Categoria categoria : this.categorias.values()) {
                if (categoria.verify(item)) {
                    if (vendertudo) {
                        if (!categoria.isSellall()) {
                            player.sendMessage(yMarket.mensagens.get("ITEM_NOCOMP2"));
                            return;
                        }
                        int slot = 0;
                        final ItemStack hand = player.getItemInHand();
                        if (hand.equals((Object)item)) {
                            for (int i = 0; i < player.getInventory().getContents().length; ++i) {
                                final ItemStack contents = player.getInventory().getItem(i);
                                if (contents != null && this.equals(item, contents)) {
                                    player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                                    itens.add(contents);
                                }
                                ++slot;
                            }
                        }
                    }
                    else {
                        final ItemStack hand2 = player.getItemInHand();
                        if (hand2.equals((Object)item)) {
                            player.setItemInHand(new ItemStack(Material.AIR));
                            itens.add(item);
                        }
                    }
                    categoria_id = pessoal.getId();
                    break;
                }
            }
            if (categoria_id == null) {
                player.sendMessage(yMarket.mensagens.get("ITEM_NOCOMP"));
                return;
            }
            if (itens.isEmpty()) {
                return;
            }
            db.open();
            db.execute("insert into ".concat(table).concat(" values ('").concat(player.getName()).concat("', '").concat(categoria_id).concat("', '").concat(UUID.randomUUID().toString()).concat("', '").concat(String.valueOf(valor)).concat("', '").concat(String.valueOf(tempo)).concat("', '").concat(Base64.toBase64(itens)).concat("');"));
            db.close();
            player.sendMessage(yMarket.mensagens.get("SELL_SUCESS_PESSOAL").replace("{valor}", yMarket.numberFormat(valor)).replace("{player}", target));
        }
        else {
            player.sendMessage(yMarket.mensagens.get("PLAYER_NULL").replace("{player}", target));
        }
    }
    
    private boolean equals(ItemStack i, ItemStack o) {
        i = i.clone();
        i.setAmount(1);
        o = o.clone();
        o.setAmount(1);
        return i.equals((Object)o);
    }
    
    public void mercado(final Player player) {
        final YamlConfiguration config = yMarket.config.getYaml();
        final int menurows = config.getInt("menu.rows");
        final int pessoalslot = config.getInt("menu.pessoalslot");
        final int expiradoslot = config.getInt("menu.expiradoslot");
        final boolean contagem = config.getBoolean("contagem");
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 9 * menurows, "§0§0§0§8Mercado");
        if (!this.categorias.isEmpty()) {
            for (final Categoria categoria : this.categorias.values()) {
                try {
                    if (categoria == null) {
                        continue;
                    }
                    inventory.setItem(categoria.getSlot() - 1, categoria.toItemStack(player, contagem));
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        final ItemStack pessoal = this.load(player, "pessoal");
        final ItemStack expirado = this.load(player, "expirado");
        inventory.setItem(expiradoslot - 1, expirado);
        inventory.setItem(pessoalslot - 1, pessoal);
        player.openInventory(inventory);
    }
    
    public int expiradosSize(final Player player) {
        int size = 0;
        try {
            final Database database = yMarket.database;
            database.open();
            ResultSet result = database.query("select * from " + yMarket.table + "_expirados where player='" + player.getName() + "';");
            while (result.next()) {
                ++size;
            }
            result = database.query("select * from " + yMarket.table + "_mercado where player='" + player.getName() + "';");
            while (result.next()) {
                if (System.currentTimeMillis() >= result.getLong("tempo")) {
                    ++size;
                }
            }
            database.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
    
    public void coletar(final Player player) {
        final Database database = yMarket.database;
        boolean i = false;
        database.open();
        try {
            final ResultSet result = database.query("select * from " + yMarket.table + "_expirados where player='" + player.getName() + "';");
            final List<UUID> uuids = new ArrayList<UUID>();
            while (result.next()) {
                if (!i) {
                    i = true;
                }
                final List<ItemStack> itens = Base64.fromBase64(result.getString("cache"));
                if (this.getEspaco((Inventory)player.getInventory()) < itens.size()) {
                    player.sendMessage(yMarket.mensagens.get("INVENTORY_FULL"));
                    break;
                }
                for (final ItemStack item : itens) {
                    player.getInventory().addItem(new ItemStack[] { item });
                }
                uuids.add(UUID.fromString(result.getString("uuid")));
            }
            for (final UUID uuid : uuids) {
                database.execute("delete from " + yMarket.table + "_expirados where uuid='" + uuid.toString() + "';");
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        database.close();
        if (!i) {
            player.sendMessage(yMarket.mensagens.get("EXPIRE_NOTFOUND"));
        }
        else {
            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);
            player.sendMessage(yMarket.mensagens.get("EXPIRE_COLLECT"));
        }
        this.expirados(player);
    }
    
    public void coletar(final Player player, final UUID uuid) {
        final Database database = yMarket.database;
        database.open();
        try {
            final ResultSet result = database.query("select * from " + yMarket.table + "_expirados where uuid='" + uuid + "';");
            if (result.next()) {
                final List<ItemStack> itens = Base64.fromBase64(result.getString("cache"));
                if (this.getEspaco((Inventory)player.getInventory()) >= itens.size()) {
                    database.execute("delete from " + yMarket.table + "_expirados where uuid='" + uuid.toString() + "';");
                    for (final ItemStack item : itens) {
                        player.getInventory().addItem(new ItemStack[] { item });
                    }
                }
                else {
                    player.sendMessage(yMarket.mensagens.get("INVENTORY_FULL"));
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        database.close();
        this.expirados(player);
    }
    
    public void coletar(final Database database, final Player player, final UUID uuid) {
        try {
            final ResultSet result = database.query("select * from " + yMarket.table + "_expirados where uuid='" + uuid + "';");
            if (result.next()) {
                final List<ItemStack> itens = Base64.fromBase64(result.getString("cache"));
                if (this.getEspaco((Inventory)player.getInventory()) >= itens.size()) {
                    database.execute("delete from " + yMarket.table + "_expirados where uuid='" + uuid.toString() + "';");
                    for (final ItemStack item : itens) {
                        player.getInventory().addItem(new ItemStack[] { item });
                    }
                }
                else {
                    player.sendMessage(yMarket.mensagens.get("INVENTORY_FULL"));
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        this.expirados(player);
    }
    
    public void expirados(final Player player) {
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 54, "§0§0§8Itens expirados");
        final Database database = yMarket.database;
        database.open();
        try {
            final ResultSet mercado_result = database.query("select * from " + yMarket.table + "_mercado where player='" + player.getName() + "';");
            while (mercado_result.next()) {
                final long tempo = mercado_result.getLong("tempo");
                if (System.currentTimeMillis() >= tempo) {
                    final UUID uuid = UUID.fromString(mercado_result.getString("uuid"));
                    final ResultSet result = database.query("select * from " + yMarket.table.concat("_mercado") + " where uuid='" + uuid + "';");
                    if (!result.next()) {
                        continue;
                    }
                    final String cache = result.getString("cache");
                    database.execute("insert into " + yMarket.table.concat("_expirados") + " values ('" + player.getName() + "', '" + uuid + "', '" + cache + "');");
                    database.execute("delete from " + yMarket.table.concat("_mercado").concat(" where uuid='").concat(uuid.toString()).concat("';"));
                }
            }
            final ResultSet result2 = database.query("select * from " + yMarket.table + "_expirados where player='" + player.getName() + "';");
            while (result2.next()) {
                if (inventory.firstEmpty() == -1) {
                    break;
                }
                final List<ItemStack> itens = Base64.fromBase64(result2.getString("cache"));
                final String uuid2 = result2.getString("uuid");
                ItemStack icone = itens.get(0).clone();
                if (itens.size() > 1) {
                    icone = new ItemBuilder("54 1 name:&9Ba\u00fa_De_Itens").toItemStack();
                }
                final NBTItem itemnbt = new NBTItem(icone);
                final NBTTagCompound itemtag = itemnbt.getTag();
                final NBTTagCompound newtag = new NBTTagCompound();
                newtag.setString("type", "coletar");
                newtag.setString("uuid", uuid2);
                itemtag.setCompound("huntersmarket", newtag);
                itemnbt.setTag(itemtag);
                icone = itemnbt.getItem();
                final ItemMeta meta = icone.getItemMeta();
                List<String> lore = new ArrayList<String>();
                if (meta.getLore() != null) {
                    lore = (List<String>)meta.getLore();
                }
                lore.add("");
                lore.add("§aClique aqui para coletar esse item.");
                meta.setLore(lore);
                icone.setItemMeta(meta);
                inventory.addItem(new ItemStack[] { icone });
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        database.close();
        inventory.setItem(45, this.backIcon());
        inventory.setItem(53, this.coletarIcon());
        player.openInventory(inventory);
    }
    
    public ItemStack backIcon() {
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
    
    public ItemStack coletarIcon() {
        ItemStack item = new ItemStack(Material.ENDER_PORTAL_FRAME);
        final NBTItem itemnbt = new NBTItem(item);
        final NBTTagCompound itemtag = itemnbt.getTag();
        final NBTTagCompound newtag = new NBTTagCompound();
        newtag.setString("type", "coletar");
        itemtag.setCompound("huntersmarket", newtag);
        itemnbt.setTag(itemtag);
        item = itemnbt.getItem();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Coletar itens");
        meta.setLore(Arrays.asList("§7Clique para coletar seus itens expirados."));
        item.setItemMeta(meta);
        return item;
    }
    
    public void pessoal(final Player player) {
        Categoria categoria = this.categoriasPessoais.get(player.getName());
        if (categoria != null) {
            categoria.vizualizar(player);
        }
        else {
            this.loadPessoal(player);
            categoria = this.categoriasPessoais.get(player.getName());
            categoria.vizualizar(player);
        }
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
    
    public Map<String, Categoria> getCategorias() {
        return this.categorias;
    }
    
    public void setCategorias(final Map<String, Categoria> categorias) {
        this.categorias = categorias;
    }
    
    public Map<String, Categoria> getCategoriasPessoais() {
        return this.categoriasPessoais;
    }
    
    public void setCategoriasPessoais(final Map<String, Categoria> categoriasPessoais) {
        this.categoriasPessoais = categoriasPessoais;
    }
    
    private ItemStack load(final Player player, final String type) {
        final YamlConfiguration config = yMarket.config.getYaml();
        final boolean contagem = config.getBoolean("contagem");
        ItemStack item = null;
        switch (type.toLowerCase()) {
            case "expirado": {
                final int size = this.expiradosSize(player);
                item = new ItemStack(Material.ENDER_CHEST);
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
                if (contagem) {
                    item.setAmount((size >= 64) ? 64 : size);
                }
                return item;
            }
            case "pessoal": {
                final Categoria categoria = this.categoriasPessoais.get(player.getName());
                if (categoria != null) {
                    item = categoria.toItemStack(player, contagem);
                }
                else {
                    this.loadPessoal(player);
                    item = this.load(player, type);
                }
                return item;
            }
            default:
                break;
        }
        return null;
    }
    
    public void loadPessoal(final Player player) {
        Categoria categoria = this.categoriasPessoais.get(player.getName());
        if (categoria == null) {
            final YamlConfiguration config = yMarket.config.getYaml();
            final int pessoalslot = config.getInt("menu.pessoalslot");
            final ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
            final SkullMeta meta = (SkullMeta)skull.getItemMeta();
            meta.setOwner(player.getName());
            meta.setDisplayName("§6Mercado pessoal");
            skull.setItemMeta((ItemMeta)meta);
            categoria = new Categoria(skull, "PESSOAL:" + player.getName(), player.getName(), pessoalslot, true);
            categoria.register();
            this.categoriasPessoais.put(player.getName(), categoria);
        }
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler
    public void onClick(final InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player)event.getWhoClicked();
            if (event.getInventory().getTitle().startsWith("§0§0§0§8Mercado")) {
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                final NBTItem itemnbt = new NBTItem(event.getCurrentItem());
                final NBTTagCompound itemtag = itemnbt.getTag();
                final NBTTagCompound hunterstag = itemtag.getCompound("huntersmarket");
                if (hunterstag.getNbtTag() != null) {
                    final String type = hunterstag.getString("type");
                    if (type.equalsIgnoreCase("expirado")) {
                        this.expirados(player);
                    }
                    else if (type.startsWith("PESSOAL:")) {
                        this.pessoal(player);
                    }
                    else {
                        final Categoria categoria = this.categorias.get(type);
                        if (categoria != null) {
                            categoria.vizualizar(player);
                        }
                    }
                }
            }
            else if (event.getInventory().getTitle().startsWith("§0§0§8Itens expirados")) {
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                final NBTItem itemnbt = new NBTItem(event.getCurrentItem());
                final NBTTagCompound itemtag = itemnbt.getTag();
                final NBTTagCompound hunterstag = itemtag.getCompound("huntersmarket");
                if (hunterstag.getNbtTag() != null) {
                    final String type = hunterstag.getString("type");
                    if (type.equalsIgnoreCase("back")) {
                        this.mercado(player);
                    }
                    else if (type.equalsIgnoreCase("coletar")) {
                        if (hunterstag.has("uuid")) {
                            this.coletar(player, UUID.fromString(hunterstag.getString("uuid")));
                            player.sendMessage(yMarket.mensagens.get("COLLECT_SUCESS"));
                            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);
                        }
                        else {
                            this.coletar(player);
                        }
                    }
                }
            }
            else if (event.getInventory().getTitle().startsWith("§e§e§8Confirmar Compra")) {
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                final NBTItem itemnbt = new NBTItem(event.getCurrentItem());
                final NBTTagCompound itemtag = itemnbt.getTag();
                final NBTTagCompound hunterstag = itemtag.getCompound("huntersmarket");
                if (hunterstag.getNbtTag() != null) {
                    final String type = hunterstag.getString("type");
                    final String uuid = hunterstag.getString("uuid");
                    String categoria2 = null;
                    String dono = null;
                    double valor = 0.0;
                    List<ItemStack> itens = null;
                    boolean disponivel = false;
                    final Database database = yMarket.database;
                    database.open();
                    final ResultSet result = database.query("select * from " + yMarket.table + "_mercado where uuid='" + uuid + "';");
                    try {
                        if (result.next()) {
                            disponivel = true;
                            dono = result.getString("player");
                            categoria2 = result.getString("categoria");
                            valor = result.getDouble("preco");
                            itens = Base64.fromBase64(result.getString("cache"));
                        }
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                    database.close();
                    if (type.equalsIgnoreCase("confirmar")) {
                        if (disponivel) {
                            final double money = yMarket.vault.getEconomy().getBalance((OfflinePlayer)player);
                            if (money >= valor) {
                                if (this.getEspaco((Inventory)player.getInventory()) >= itens.size()) {
                                    database.open();
                                    database.execute("delete from " + yMarket.table + "_mercado where uuid='" + uuid + "';");
                                    database.close();
                                    for (final ItemStack item : itens) {
                                        player.getInventory().addItem(new ItemStack[] { item });
                                    }
                                    yMarket.vault.getEconomy().withdrawPlayer((OfflinePlayer)player, valor);
                                    yMarket.vault.getEconomy().depositPlayer(dono, valor);
                                    player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);
                                    player.sendMessage(yMarket.mensagens.get("BUY_SUCESS").replace("{valor}", yMarket.numberFormat(valor)).replace("{player}", dono));
                                    final Player target = Bukkit.getPlayer(dono);
                                    if (target != null) {
                                        target.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);
                                        target.sendMessage(yMarket.mensagens.get("SELL_BUY_TARGET").replace("{valor}", yMarket.numberFormat(valor)).replace("{player}", player.getName()));
                                    }
                                }
                                else {
                                    player.sendMessage(yMarket.mensagens.get("INVENTORY_FULL"));
                                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                                }
                            }
                            else {
                                player.sendMessage(yMarket.mensagens.get("NO_MONEY"));
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                            }
                        }
                        else {
                            player.sendMessage(yMarket.mensagens.get("ITEM_NOTFOUND"));
                            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                        }
                        final Categoria cat = this.categorias.get(categoria2);
                        if (cat != null) {
                            cat.vizualizar(player);
                        }
                        else {
                            this.mercado(player);
                        }
                    }
                    if (type.equalsIgnoreCase("cancelar")) {
                        final Categoria cat = this.categorias.get(categoria2);
                        if (cat != null) {
                            cat.vizualizar(player);
                        }
                        else {
                            this.mercado(player);
                        }
                        player.sendMessage(yMarket.mensagens.get("CONFIRM_NO"));
                    }
                }
            }
        }
    }
}
