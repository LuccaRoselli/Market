package com.devlucca.ymarket.commands.subcommands;

import org.bukkit.command.*;
import org.bukkit.*;
import org.bukkit.entity.*;

import com.devlucca.ymarket.app.*;
import com.devlucca.ymarket.commands.*;
import com.devlucca.ymarket.database.*;
import com.devlucca.ymarket.util.*;

import java.sql.*;

public class SubCmdPunir extends SubCommand
{
    public SubCmdPunir(final String command) {
        super("punir", "§cUse: /" + command + " punir [jogador] [tempo] [motivo]", "ymarket.command.punir", new String[] { "banir", "ban" });
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (args.length >= 4) {
            try {
                final Database database = yMarket.database;
                final String table = yMarket.table.concat("_punicoes");
                final Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(yMarket.mensagens.get("PLAYER_NULL").replace("{player}", args[1]));
                    return;
                }
                final long tempo = this.getTempo(args[2]);
                if (tempo <= 0L) {
                    sender.sendMessage(yMarket.mensagens.get("BAN_ERRO_NUMBER"));
                    return;
                }
                String motivo = "";
                for (int i = 3; i < args.length; ++i) {
                    motivo = String.valueOf(motivo) + args[i];
                    motivo = String.valueOf(motivo) + " ";
                }
                if (motivo.length() >= 100) {
                    motivo = motivo.substring(0, 100);
                }
                String staff = sender.getName();
                if (staff.equalsIgnoreCase("CONSOLE")) {
                    staff = "Console";
                }
                database.open();
                final ResultSet result = database.query("select * from " + table + " where player='" + target.getName() + "';");
                if (result.next()) {
                    final String result_staff = result.getString("staff");
                    final long result_tempo = result.getLong("tempo");
                    final String result_motivo = result.getString("motivo");
                    if (System.currentTimeMillis() >= result_tempo) {
                        database.execute("update " + table + " set tempo='" + (System.currentTimeMillis() + tempo) + "' where player='" + target.getName() + "';");
                        sender.sendMessage("");
                        sender.sendMessage(yMarket.mensagens.get("BAN_SUCESS").replace("{player}", target.getName()));
                        sender.sendMessage("");
                        sender.sendMessage("§cMotivo: " + motivo);
                        sender.sendMessage("§cTempo: " + StringUtils.formatDelay(tempo));
                        sender.sendMessage("");
                        if (sender != target) {
                            target.sendMessage("");
                            target.sendMessage(yMarket.mensagens.get("BAN_MESSAGE"));
                            target.sendMessage("");
                            target.sendMessage("§cMotivo: " + motivo);
                            target.sendMessage("§cStaffer: " + staff);
                            target.sendMessage("§cTempo: " + StringUtils.formatDelay(tempo));
                            target.sendMessage("");
                        }
                    }
                    else {
                        sender.sendMessage("");
                        sender.sendMessage(yMarket.mensagens.get("BAN_ERRO_ISBAN"));
                        sender.sendMessage("");
                        sender.sendMessage("§cMotivo: " + result_motivo);
                        sender.sendMessage("§cStaffer: " + result_staff);
                        sender.sendMessage("§cTempo Restante: " + StringUtils.formatDelay(result_tempo - System.currentTimeMillis()));
                        sender.sendMessage("");
                    }
                }
                else {
                    database.execute("insert into " + table + " values ('" + target.getName() + "', '" + staff + "', '" + (System.currentTimeMillis() + tempo) + "', '" + motivo + "');");
                    sender.sendMessage("");
                    sender.sendMessage(yMarket.mensagens.get("BAN_SUCCESS").replace("{player}", target.getName()));
                    sender.sendMessage("");
                    sender.sendMessage("§cMotivo: " + motivo);
                    sender.sendMessage("§cTempo: " + StringUtils.formatDelay(tempo));
                    sender.sendMessage("");
                    if (sender != target) {
                        target.sendMessage("");
                        target.sendMessage(yMarket.mensagens.get("BAN_MESSAGE"));
                        target.sendMessage("");
                        target.sendMessage("§cMotivo: " + motivo);
                        target.sendMessage("§cStaffer: " + staff);
                        target.sendMessage("§cTempo: " + StringUtils.formatDelay(tempo));
                        target.sendMessage("");
                    }
                }
                database.close();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        sender.sendMessage(this.getUsage());
    }
    
    public long getTempo(String tempo) {
        tempo = tempo.toLowerCase();
        if (tempo.contains("nan")) {
            tempo = "1";
        }
        long i = -1L;
        try {
            i = 60000L + Long.parseLong(tempo);
        }
        catch (Exception exceptionminutos) {
            Label_0105: {
                if (!tempo.endsWith("d") && !tempo.endsWith("dias")) {
                    if (!tempo.endsWith("dia")) {
                        break Label_0105;
                    }
                }
                try {
                    i = Long.parseLong(tempo.replace("d", "").replace("dia", "").replace("dias", "")) * 86400000L;
                }
                catch (Exception exception) {
                    i = -1L;
                }
            }
            Label_0177: {
                if (!tempo.endsWith("h") && !tempo.endsWith("horas")) {
                    if (!tempo.endsWith("hora")) {
                        break Label_0177;
                    }
                }
                try {
                    i = Long.parseLong(tempo.replace("h", "").replace("horas", "").replace("hora", "")) * 3600000L;
                }
                catch (Exception exception) {
                    i = -1L;
                }
            }
            Label_0249: {
                if (!tempo.endsWith("m") && !tempo.endsWith("minutos")) {
                    if (!tempo.endsWith("minuto")) {
                        break Label_0249;
                    }
                }
                try {
                    i = Long.parseLong(tempo.replace("m", "").replace("minutos", "").replace("minuto", "")) * 60000L;
                }
                catch (Exception exception) {
                    i = -1L;
                }
            }
            if (!tempo.endsWith("s") && !tempo.endsWith("segundos")) {
                if (!tempo.endsWith("segundo")) {
                    return i;
                }
            }
            try {
                i = Long.parseLong(tempo.replace("s", "").replace("segundos", "").replace("segundo", "")) * 1000L;
            }
            catch (Exception exception) {
                i = -1L;
            }
        }
        return i;
    }
}
