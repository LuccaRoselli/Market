package com.devlucca.ymarket.app;

import java.io.File;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.devlucca.ymarket.database.Database;
import com.devlucca.ymarket.database.MySQL;
import com.devlucca.ymarket.database.SQLite;
import com.devlucca.ymarket.lib.fanciful.FancyMessage;
import com.devlucca.ymarket.license.License;
import com.devlucca.ymarket.manager.Mensagens;
import com.devlucca.ymarket.plugin.MercadoManager;
import com.devlucca.ymarket.util.Config;
import com.devlucca.ymarket.util.Formater;
import com.devlucca.ymarket.util.Vault;

public class yMarket implements Listener {
	public static final String VERSION = "1.1";
	public static final String PROJECT_ID = "c45c1272f7eb4c419ec1c5347fde6ba9";
	public static String table;
	public static JavaPlugin instance;
	public static Database database;
	public static Mensagens mensagens;
	public static Config config;
	public static MercadoManager manager;
	public static Vault vault;

	public yMarket(final JavaPlugin javaplugin) {
		yMarket.instance = javaplugin;
	}

	public void onEnable() {
		if (!new File(yMarket.instance.getDataFolder(), "config.yml").exists()) {
			yMarket.instance.saveResource("config.yml", false);
		}
		License l = new License();
		if (l.check()) {
			Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + License.plugin_name + "] §fACEITA");
			Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + License.plugin_name + "] §fCarregando plugin...");

			if (!new File(yMarket.instance.getDataFolder(), "mensagens.yml").exists()) {
				yMarket.instance.saveResource("mensagens.yml", false);
			}
			yMarket.vault = new Vault();
			if (yMarket.vault.setupEconomy()) {
				this.database();
				yMarket.mensagens = new Mensagens();
				yMarket.config = new Config((Plugin) yMarket.instance, "config.yml");
				yMarket.manager = new MercadoManager();
				yMarket.vault.setupChat();
				Bukkit.getPluginManager().registerEvents((Listener) this, (Plugin) yMarket.instance);
			} else {
				echo("sistema de economia nao encontrado, plugin desativado!");
				Bukkit.getPluginManager().disablePlugin((Plugin) yMarket.instance);
			}
		}
	}

	public void onDisable() {
		if (yMarket.database != null && yMarket.database.connection()) {
			yMarket.database.close();
		}
	}

	public void database() {
		try {
			echo("iniciando banco de dados...");
			final FileConfiguration config = yMarket.instance.getConfig();
			final boolean usemysql = config.getBoolean("mysql.enable");
			if (usemysql) {
				echo("tipo do banco de dados \"MySQL\" selecionado.");
				final String hostname = config.getString("mysql.hostname");
				final String database_name = config.getString("mysql.database");
				final String username = config.getString("mysql.username");
				final String password = config.getString("mysql.password");
				final String table_name = config.getString("mysql.table");
				final int port = config.getInt("mysql.port");
				final MySQL mysql = new MySQL((Plugin) yMarket.instance);
				mysql.setHostname(hostname);
				mysql.setDatabase(database_name);
				mysql.setUsername(username);
				mysql.setPassword(password);
				mysql.setPort(port);
				yMarket.table = table_name;
				yMarket.database = mysql;
			} else {
				echo("tipo do banco de dados \"SQLite\" selecionado.");
				yMarket.table = "HuntersMarket".toLowerCase();
				yMarket.database = new SQLite((Plugin) yMarket.instance);
			}
			echo("testando conexao com banco de dados...");
			if (yMarket.database.open()) {
				echo("conexao testada com sucesso, tudo OK!");
				yMarket.database.close();
			} else {
				echo("houve um erro ao conectar-se com o banco de dados!");
				echo("tipo do banco de dados \"SQLite\" selecionado.");
				yMarket.table = "HuntersMarket".toLowerCase();
				yMarket.database = new SQLite((Plugin) yMarket.instance);
			}
			yMarket.database.open();
			yMarket.database.execute("create table if not exists " + yMarket.table + "_mercado"
					+ " (player varchar(40), categoria varchar(40), uuid varchar(40), preco double, tempo long, cache blob);");
			yMarket.database.execute("create table if not exists " + yMarket.table + "_punicoes"
					+ " (player varchar(40), staff varchar(40), tempo long, motivo varchar(100));");
			yMarket.database.execute("create table if not exists " + yMarket.table + "_expirados"
					+ " (player varchar(40), uuid varchar(40), cache blob);");
			yMarket.database.close();
		} catch (Exception e) {
			e.printStackTrace();
			echo("houve um erro ao tentar iniciar o banco de dados.");
		}
	}

	public static int getTotalItens(final Player player) {
		int totalitens = 0;
		try {
			yMarket.database.open();
			ResultSet result = yMarket.database
					.query("select * from " + yMarket.table + "_mercado where player='" + player.getName() + "';");
			while (result.next()) {
				++totalitens;
			}
			result = yMarket.database
					.query("select * from " + yMarket.table + "_expirados where player='" + player.getName() + "';");
			while (result.next()) {
				++totalitens;
			}
			yMarket.database.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return totalitens;
	}

	public static int getMaxItens(final Player player) {
		final YamlConfiguration yaml = yMarket.config.getYaml();
		int maxitens = yaml.getInt("limites.default");
		for (final String section : yaml.getConfigurationSection("limites.permissions").getKeys(false)) {
			if (player.hasPermission("solarymarket.limite." + section.toLowerCase())) {
				final int i = yaml.getInt("limites.permissions." + section);
				if (i <= maxitens) {
					continue;
				}
				maxitens = i;
			}
		}
		return maxitens;
	}

	public static boolean sell(final Player player) {
		return getTotalItens(player) < getMaxItens(player);
	}

	public static String numberFormat(final double valor) {
		final YamlConfiguration config = yMarket.config.getYaml();
		switch (config.getInt("preco_format")) {
		case 1:
			return Formater.format(valor);
		case 2:
			return NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).format(valor);
		case 3:
			return String.valueOf(valor);
		default:
			return Formater.format(valor);
		}
	}

	public static void echo(final String message) {
		yMarket.instance.getLogger().info(message);
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		try {
			yMarket.database.open();
			final ResultSet mercado_result = yMarket.database
					.query("select * from " + yMarket.table + "_mercado where player='" + player.getName() + "';");
			while (mercado_result.next()) {
				final long tempo = mercado_result.getLong("tempo");
				if (System.currentTimeMillis() >= tempo) {
					final UUID uuid = UUID.fromString(mercado_result.getString("uuid"));
					final ResultSet result = yMarket.database
							.query("select * from " + yMarket.table.concat("_mercado") + " where uuid='" + uuid + "';");
					if (!result.next()) {
						continue;
					}
					final String cache = result.getString("cache");
					yMarket.database.execute("insert into " + yMarket.table.concat("_expirados") + " values ('"
							+ player.getName() + "', '" + uuid + "', '" + cache + "');");
					yMarket.database.execute("delete from " + yMarket.table.concat("_mercado").concat(" where uuid='")
							.concat(uuid.toString()).concat("';"));
				}
			}
			final ResultSet result2 = yMarket.database
					.query("select * from " + yMarket.table + "_expirados where player='" + player.getName() + "';");
			if (result2.next()) {
				player.sendMessage("");
				player.sendMessage(
						"§eParece que voc\u00ea havia colocado alguns itens no mercado, infelizmente nenhum jogador os comprou. Para colet\u00e1los ");
				final FancyMessage msg = new FancyMessage("§lCLIQUE §lAQUI");
				msg.color(ChatColor.GREEN).command("/mercado coletar")
						.then(" ou utilize o comando \"/mercado coletar\"").color(ChatColor.YELLOW).send(player);
				player.sendMessage("");
			}
			yMarket.database.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
