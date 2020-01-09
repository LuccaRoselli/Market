package com.devlucca.ymarket.license;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.bukkit.Bukkit;

import com.devlucca.ymarket.app.yMarket;

public class License {

	public boolean ligar;
	public static String plugin_name = yMarket.instance.getName();

	public License() {
		SQLConnection.sqlConnect();
		load();
	}

	@SuppressWarnings("resource")
	public boolean check() {
		String serverIP = null;
		try {
			serverIP = new Scanner(new URL("http://checkip.amazonaws.com").openStream(), "UTF-8").useDelimiter("\\A").next();
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §cFalha ao conseguir seu IP.");
			return false;
		}
		for (LicenseObject lo : LicenseObject.getLicenses()) {
			if (lo.getLicense().trim().equalsIgnoreCase(yMarket.instance.getConfig().getString("Licenca"))) {
				if (!lo.isPrivate()) {
					if (serverIP.contains(lo.getIPs())) {
						Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §fNORMAL");
						Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §fIP Checado");
						return true;
					} else {
						Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §fNORMAL");
						Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §cFalha ao verificar IP.");
						Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §cVerifique se o IP a seguir se encontra na licença:");
						Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §a" + serverIP);
						return false;
					}
				} else {
					Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §fPRIVADA");
					return true;
				}
			}
		}
		Bukkit.getConsoleSender().sendMessage("§6[LICENCA - " + plugin_name + "] §cLicenca não encontrada.");
		return false;
	}
	
	@SuppressWarnings("resource")
	public boolean checkNoMessage() {
		String serverIP = null;
		try {
			serverIP = new Scanner(new URL("http://checkip.amazonaws.com").openStream(), "UTF-8").useDelimiter("\\A").next();
		} catch (IOException e) {
			return false;
		}
		for (LicenseObject lo : LicenseObject.getLicenses()) {
			if (lo.getLicense().trim().equalsIgnoreCase(yMarket.instance.getConfig().getString("Licenca"))) {
				if (!lo.isPrivate()) {
					if (serverIP.contains(lo.getIPs())) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}

	void load() {
		try {
			PreparedStatement ps = SQLConnection.getSQL().getConnection()
					.prepareStatement("SELECT * FROM " + plugin_name.toLowerCase());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				new LicenseObject(rs.getString("licenca"), Boolean.parseBoolean(rs.getString("keyprivada")),
						rs.getString("ips"));
			}
			SQLConnection.getSQL().closeConnection();
			ps.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
