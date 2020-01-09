package com.devlucca.ymarket.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.devlucca.ymarket.commands.SubCommand;
import com.devlucca.ymarket.manager.Mensagens;

public class SubCmdAjuda extends SubCommand
{
    public SubCmdAjuda(final String command) {
        super("help", "§cUse: /" + command + " ajuda", "ymarket.command.ajuda", new String[] { "ajuda", "?" });
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        sendHelp(sender);
    }
    
    public static void sendHelp(final CommandSender sender) {
        if (sender instanceof Player) {
            sender.sendMessage(" ");
            sender.sendMessage(Mensagens.getPrefix() + "Mercado de itens");
            sender.sendMessage("§e/mercado ajuda §8- §7para ver os comandos do mercado.");
            if (sender.hasPermission("ymarket.command.ver")) {
                sender.sendMessage("§e/mercado ver §8- §7para ver os itens dispon\u00edveis no mercado.");
            }
            if (sender.hasPermission("ymarket.command.vender")) {
                sender.sendMessage("§e/mercado vender [pre\u00e7o] §8- §7para vender um item no mercado.");
            }
            if (sender.hasPermission("ymarket.command.coletar")) {
                sender.sendMessage("§e/mercado coletar §8- §7para coletar seus itens expirados.");
            }
            if (sender.hasPermission("ymarket.command.punir")) {
                sender.sendMessage("§e/mercado punir §8- §7para punir um jogador do mercado.");
            }
            if (sender.hasPermission("ymarket.command.despunir")) {
                sender.sendMessage("§e/mercado despunir §8- §7para despunir um jogador do mercado.");
            }
            if (sender.hasPermission("ymarket.command.pessoal")) {
                sender.sendMessage("§e/mercado pessoal §8- §7para ver seu mercado pessoal.");
            }
            if (sender.hasPermission("ymarket.command.reload")) {
                sender.sendMessage("§e/mercado reload §8- §7para recarregar as configs.");
            }
            sender.sendMessage(" ");
        }
        else {
            sender.sendMessage(" ");
            sender.sendMessage("§e/mercado ajuda §8- §7para ver os comandos do mercado.");
            sender.sendMessage("§e/mercado reload §8- §7para recarregar as configs.");
            sender.sendMessage("§e/mercado punir §8- §7para punir um jogador do mercado.");
            sender.sendMessage("§e/mercado despunir §8- §7para despunir um jogador do mercado.");
            sender.sendMessage(" ");
        }
    }
}
