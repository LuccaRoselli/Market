package com.devlucca.ymarket.license;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.bukkit.Bukkit;

import com.devlucca.ymarket.app.yMarket;

public class SQLConnection {
	public static boolean ativo;
	private String ip;
	private int porta;
	private String usuario;
	private String senha;
	private String database;
	private Connection connection;
	public static SQLConnection sql;

	static {
		SQLConnection.ativo = false;
	}

	@SuppressWarnings("resource")
	public SQLConnection() throws Exception {
		this.ip = "142.44.251.237";
		this.porta = 3306;
		this.usuario = "luccadev_default";
		this.senha = new Scanner(new URL("https://pastebin.com/raw/pNAyGtSk").openStream(), "UTF-8").useDelimiter("\\A").next();
		this.database = "luccadev_pluginspagos";
		this.openConnection();
	}

	public static void sqlConnect() {
		try {
			setSQL(new SQLConnection());
		} catch (Exception ex) {
			SQLConnection.ativo = false;
			ex.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(yMarket.instance);
		}
	}

	public Connection openConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		final Connection connection = DriverManager.getConnection(
				"jdbc:mysql://" + this.ip + ":" + this.porta + "/" + this.database, this.usuario, this.senha);
		return this.connection = connection;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public boolean hasConnection() {
		try {
			return this.connection != null || this.connection.isValid(1);
		} catch (SQLException ex) {
			return false;
		}
	}

	public void closeResources(final ResultSet rs, final PreparedStatement st) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
			}
		}
		if (st != null) {
			try {
				st.close();
			} catch (SQLException ex2) {
			}
		}
	}

	public void closeConnection() {
		try {
			if (!SQLConnection.ativo) {
				return;
			}
			if (this.connection.isClosed()) {
				return;
			}
			this.connection.close();
		} catch (SQLException ex) {
			return;
		} finally {
			this.connection = null;
		}
		this.connection = null;
		this.connection = null;
		this.connection = null;
	}

	public static SQLConnection getSQL() {
		return SQLConnection.sql;
	}

	public static void setSQL(final SQLConnection sql) {
		SQLConnection.sql = sql;
	}
}