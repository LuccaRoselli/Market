package com.devlucca.ymarket.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devlucca.ymarket.app.yMarket;
import com.devlucca.ymarket.commands.SubCommand;
import com.devlucca.ymarket.manager.Mensagens;
import com.devlucca.ymarket.util.Formater;

public class SubCmdVender extends SubCommand
{
    public SubCmdVender(final String command) {
        super("vender", "§cUse: /" + command + " vender [pre\u00e7o] [jogador]", "ymarket.command.vender", new String[] { "sell" });
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            if (args.length >= 2) {
                final ItemStack item = player.getItemInHand();
                double valor = -1.0;
                String input = "";
                try {
                    if (args[1].matches(".*[a-zA-Z]+.*")){
                    	if (!args[1].chars().allMatch(Character::isLetter)){
                    			input = Formater.deformat(args[1]);
                    	} else {
                    		sender.sendMessage(Mensagens.getPrefix() + "§cO valor do money deve ser um n\u00famero v\u00e1lido.");
                    		return;
                    	}
                    }
                    else {
                    	input = args[1];
                    }
				} catch (NumberFormatException e) {
					sender.sendMessage(Mensagens.getPrefix() + "§cO valor do money deve ser um n\u00famero v\u00e1lido.");
				}
                boolean vendertudo = false;
                String target = null;
                try {
                    valor = Double.valueOf(input);
                }
                catch (Exception ex) {}
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("all") || args[2].equalsIgnoreCase("tudo")) {
                        vendertudo = true;
                    }
                    else {
                        target = args[2];
                    }
                }
                if (args.length >= 4) {
                    try {
                        if (args[3].equalsIgnoreCase("all") || args[2].equalsIgnoreCase("tudo")) {
                            vendertudo = true;
                        }
                    }
                    catch (Exception ex2) {}
                }
                if (yMarket.sell(player)) {
                    if (target != null) {
                        yMarket.manager.vender(player, target, valor, item, vendertudo);
                    }
                    else {
                        yMarket.manager.vender(player, valor, item, vendertudo);
                    }
                }
                else {
                    player.sendMessage(yMarket.mensagens.get("MAX_ITENS"));
                }
            }
            else {
                sender.sendMessage(this.getUsage());
            }
        }
        else {
            sender.sendMessage(Mensagens.getPrefix() + "§cEste recurso esta disponivel somente para jogadores em jogo.");
        }
    }
}
