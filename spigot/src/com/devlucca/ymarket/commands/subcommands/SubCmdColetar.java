package com.devlucca.ymarket.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.devlucca.ymarket.app.yMarket;
import com.devlucca.ymarket.commands.SubCommand;
import com.devlucca.ymarket.manager.Mensagens;

public class SubCmdColetar extends SubCommand
{
    public SubCmdColetar(final String command) {
        super("coletar", "§cUse: /" + command + " coletar", "ymarket.command.coletar", new String[] { "expirados" });
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            yMarket.manager.expirados(player);
        }
        else {
            sender.sendMessage(Mensagens.getPrefix() + "§cEste recurso esta disponivel somente para jogadores em jogo.");
        }
    }
}
