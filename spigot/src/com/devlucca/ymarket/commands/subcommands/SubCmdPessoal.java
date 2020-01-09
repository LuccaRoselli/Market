package com.devlucca.ymarket.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.devlucca.ymarket.app.yMarket;
import com.devlucca.ymarket.commands.SubCommand;
import com.devlucca.ymarket.manager.Mensagens;

public class SubCmdPessoal extends SubCommand
{
    public SubCmdPessoal(final String command) {
        super("pessoal", "§cUse: /" + command + " pessoal", "ymarket.command.pessoal", new String[] { "particular" });
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            yMarket.manager.pessoal(player);
        }
        else {
            sender.sendMessage(Mensagens.getPrefix() + "§cEste recurso esta disponivel somente para jogadores em jogo.");
        }
    }
}
