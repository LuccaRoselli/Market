package com.devlucca.ymarket.commands.subcommands;

import org.bukkit.command.*;

import com.devlucca.ymarket.app.*;
import com.devlucca.ymarket.commands.*;
import com.devlucca.ymarket.database.*;

import java.sql.*;

public class SubCmdDespunir extends SubCommand
{
    public SubCmdDespunir(final String command) {
        super("despunir", "§cUse: /" + command + " despunir [jogador]", "ymarket.command.despunir", new String[] { "desbanir", "unban" });
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (args.length >= 2) {
            try {
                final String target = args[1];
                final Database database = yMarket.database;
                final String table = yMarket.table.concat("_punicoes");
                long tempo = 0L;
                database.open();
                final ResultSet result = database.query("select * from " + table + " where player='" + target + "';");
                if (result.next()) {
                    tempo = result.getLong("tempo");
                    if (System.currentTimeMillis() < tempo) {
                        database.execute("delete from " + table + " where player='" + target + "';");
                        sender.sendMessage(yMarket.mensagens.get("UNBAN_SUCESS").replace("{player}", target));
                    }
                    else {
                        sender.sendMessage(yMarket.mensagens.get("PLAYER_NULL").replace("{player}", target));
                    }
                }
                else {
                    sender.sendMessage(yMarket.mensagens.get("PLAYER_NULL").replace("{player}", target));
                }
                database.close();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        else {
            sender.sendMessage(this.getUsage());
        }
    }
}
