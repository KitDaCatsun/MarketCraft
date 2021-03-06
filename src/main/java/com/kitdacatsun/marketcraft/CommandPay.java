package com.kitdacatsun.marketcraft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public class CommandPay implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            pay(sender, args);
            return true;
        }
        Player player = (Player) sender;

        if (player.isOp() && player.getGameMode().toString().equals("CREATIVE")) {
            pay(sender, args);

        } else {

            if (args.length != 2) {
                return false;
            }

            Player receiver = MarketCraft.server.getPlayer(args[0]);
            if (receiver == null) {
                sender.sendMessage(ChatColor.RED + "Player not found");
                return true;
            }

            int amount = Integer.parseInt(args[1]);

            UUID Uuid = player.getUniqueId();
            String playerBalanceKey = "Players." + Uuid.toString() + ".balance";

            if ((int) MarketCraft.balance.get(playerBalanceKey) >= amount){

                if (MarketCraft.balance.contains(playerBalanceKey)) {
                    int balance = (int) MarketCraft.balance.get(playerBalanceKey) - amount;
                    MarketCraft.balance.set(playerBalanceKey, balance);
                } else {
                    MarketCraft.balance.set(playerBalanceKey, amount);
                    MarketCraft.balance.options().copyDefaults(true);
                }

                receiver.sendMessage(ChatColor.GOLD + "You have been payed £" + amount + " by " + sender.getName());
                player.sendMessage(ChatColor.GOLD + "You have payed " + receiver.getDisplayName() + " £" + amount);

                Uuid = receiver.getUniqueId();
                playerBalanceKey = "Players." + Uuid.toString() + ".balance";

                if (MarketCraft.balance.contains(playerBalanceKey)) {
                    int balance = (int) MarketCraft.balance.get(playerBalanceKey) + amount;
                    MarketCraft.balance.set(playerBalanceKey, balance);
                } else {
                    MarketCraft.balance.set(playerBalanceKey, amount);
                    MarketCraft.balance.options().copyDefaults(true);
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have enough money to pay the specified player");
            }

        }

        return true;
    }

    public void pay(@NotNull CommandSender sender, String[] args){
        if (args.length != 2) {
            return;
        }

        Player receiver = MarketCraft.server.getPlayer(args[0]);
        if (receiver == null) {
            sender.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        UUID Uuid = receiver.getUniqueId();

        int amount = Integer.parseInt(args[1]);
        receiver.sendMessage(ChatColor.GOLD + "You have been payed £" + amount + " by " + sender.getName());

        String playerBalanceKey = "Players." + Uuid.toString() + ".balance";

        if (MarketCraft.balance.contains(playerBalanceKey)) {
            int balance = (int) MarketCraft.balance.get(playerBalanceKey) + amount;
            MarketCraft.balance.set(playerBalanceKey, balance);
        } else {
            MarketCraft.balance.set(playerBalanceKey, amount);
            MarketCraft.balance.options().copyDefaults(true);
        }

    }
}
