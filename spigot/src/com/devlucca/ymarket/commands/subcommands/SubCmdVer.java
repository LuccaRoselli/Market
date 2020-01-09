package com.devlucca.ymarket.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.devlucca.ymarket.app.yMarket;
import com.devlucca.ymarket.commands.SubCommand;
import com.devlucca.ymarket.manager.Mensagens;
import com.devlucca.ymarket.plugin.objetos.Categoria;

public class SubCmdVer extends SubCommand
{
    public SubCmdVer(final String command) {
        super("ver", "§cUse: /" + command + " ver", "ymarket.command.ver", new String[] { "view", "visualizar" });
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            if (args.length >= 2) {
                final String name = args[1].toLowerCase();
                final Categoria categoria = yMarket.manager.getCategorias().get(name.toLowerCase());
                if (categoria != null) {
                    categoria.vizualizar(player);
                }
                else {
                    yMarket.manager.mercado(player);
                }
            }
            else {
                yMarket.manager.mercado(player);
            }
        }
        else {
            sender.sendMessage(Mensagens.getPrefix() + "§cEste recurso esta disponivel somente para jogadores em jogo.");
        }
    }
}
