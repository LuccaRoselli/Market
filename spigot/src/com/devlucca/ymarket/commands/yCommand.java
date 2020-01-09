package com.devlucca.ymarket.commands;

import org.bukkit.command.*;

import com.devlucca.ymarket.app.*;
import com.devlucca.ymarket.commands.subcommands.*;
import com.devlucca.ymarket.database.*;
import com.devlucca.ymarket.util.*;

import java.sql.*;
import java.util.*;

public class yCommand implements CommandExecutor
{
    private List<SubCommand> subcommands;
    
    public yCommand(final String comando) {
        (this.subcommands = new ArrayList<SubCommand>()).add(new SubCmdAjuda(comando));
        this.subcommands.add(new SubCmdVer(comando));
        this.subcommands.add(new SubCmdColetar(comando));
        this.subcommands.add(new SubCmdPessoal(comando));
        this.subcommands.add(new SubCmdVender(comando));
        this.subcommands.add(new SubCmdReload(comando));
        this.subcommands.add(new SubCmdPunir(comando));
        this.subcommands.add(new SubCmdDespunir(comando));
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        try {
            final Database database = yMarket.database;
            final String table = yMarket.table.concat("_punicoes");
            boolean banned = false;
            String staff = "";
            String motivo = "";
            long tempo = 0L;
            database.open();
            final ResultSet result = database.query("select * from " + table + " where player='" + sender.getName() + "';");
            if (result.next()) {
                staff = result.getString("staff");
                motivo = result.getString("motivo");
                tempo = result.getLong("tempo");
                banned = (System.currentTimeMillis() < tempo);
                if (!banned) {
                    database.execute("delete from " + table + " where player='" + sender.getName() + "';");
                }
            }
            database.close();
            if (banned) {
                sender.sendMessage("");
                sender.sendMessage(yMarket.mensagens.get("BAN_MESSAGE"));
                sender.sendMessage("");
                sender.sendMessage("§cMotivo: " + motivo);
                sender.sendMessage("§cStaffer: " + staff);
                sender.sendMessage("§cTempo Restante: " + StringUtils.formatDelay(tempo - System.currentTimeMillis()));
                sender.sendMessage("");
                return false;
            }
            if (args.length >= 1) {
                final String arg = args[0].toLowerCase();
                if (!this.subcommands.isEmpty()) {
                    for (final SubCommand subCommand : this.subcommands) {
                        if (arg.equalsIgnoreCase(subCommand.getName().toLowerCase()) || subCommand.getAlias().contains(arg)) {
                            if (sender.hasPermission(subCommand.getPermission()) || subCommand.getPermission().isEmpty()) {
                                subCommand.execute(sender, args);
                            }
                            else {
                                sender.sendMessage(yMarket.mensagens.get("NO_PERMISSION"));
                            }
                            return false;
                        }
                    }
                }
            }
            else {
                SubCmdAjuda.sendHelp(sender);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }
}
