package com.devlucca.ymarket.commands.subcommands;

import org.bukkit.command.CommandSender;

import com.devlucca.ymarket.app.yMarket;
import com.devlucca.ymarket.commands.SubCommand;
import com.devlucca.ymarket.manager.Mensagens;

public class SubCmdReload extends SubCommand
{
    public SubCmdReload(final String command) {
        super("reload", "§cUse: /" + command + " reload", "ymarket.command.reload", new String[] { "rl" });
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        yMarket.config.reload();
        yMarket.manager.loadCategorias();
        yMarket.mensagens.reload();
        sender.sendMessage(Mensagens.getPrefix() + "§9Categorias e mensagens recarregadas com sucesso.");
    }
}
