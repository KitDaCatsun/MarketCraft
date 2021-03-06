package com.kitdacatsun.marketcraft;

import com.kitdacatsun.marketcraft.GUIBuilder.InvPos;
import com.kitdacatsun.marketcraft.MarketCraft.files;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class ListenerShop implements Listener {

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Shop |") || event.getCurrentItem() == null || event.getClickedInventory() == null) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem().getLore() == null) {
            CommandVillager.openShop((Player) event.getWhoClicked(), event.getCurrentItem(), event.getCurrentItem().getItemMeta().getDisplayName());
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        Inventory botInventory = player.getOpenInventory().getBottomInventory();
        List<String> itemLore = Objects.requireNonNull(clickedItem.getItemMeta().getLore());

        switch (itemLore.get(0)) {
            case "Buy":
            case "Sell":
                changeOrder(player, clickedItem, topInventory, botInventory);
                return;

            case "Confirm":
                doOrder(player, topInventory, botInventory, clickedItem.getItemMeta().getDisplayName().split(" ")[0]);
                return;

            case "Back":
            case "Return to Previous Menu":
                openPrevious(player);
                return;

            default:
        }
    }

    private void changeOrder(Player player, ItemStack option, Inventory inventory, Inventory shopInv) {
        ItemStack order = inventory.getItem(InvPos.MID);
        if (order == null) {
            player.sendMessage("Order is null");
            return;
        }

        GUIItem item;

        item = new GUIItem();
        item.name = option.getItemMeta().getDisplayName() + " For: £" + MarketCraft.getPrice(order) * order.getAmount();
        item.lore.add("Confirm");
        item.amount = 1;
        item.material = Material.LIME_DYE;
        inventory.setItem(InvPos.BOT_MID, item.getItemStack());

        item = new GUIItem();
        item.material = Objects.requireNonNull(inventory.getItem(InvPos.MID)).getType();
        item.amount = option.getAmount();
        inventory.setItem(InvPos.MID, item.getItemStack());

        player.openInventory(inventory);
    }

    private void doOrder(Player player, Inventory shopInv, Inventory playerInv, String type) {
        ItemStack order = shopInv.getItem(InvPos.MID);
        if (order == null) {
            player.sendMessage("Order is null");
            return;
        }

        String balanceKey = "players." + player.getUniqueId() + ".balance";
        int balance = files.balance.getInt(balanceKey);

        int cost = MarketCraft.getPrice(order) * order.getAmount();

        switch (type) {
            case "Sell":
                if (!playerInv.containsAtLeast(order, order.getAmount())) {
                    player.sendMessage(ChatColor.RED + "Not enough of that item type to sell");
                    return;
                }

                playerInv.removeItemAnySlot(order);

                files.balance.set(balanceKey, balance + cost);

                player.sendMessage(ChatColor.GOLD + "You have sold " + order.getAmount() + " of " + order.getI18NDisplayName() + " for: £" + cost);

                return;


            case "Buy":
                if (player.getInventory().firstEmpty() != -1) {

                    if (balance >= cost) {

                        playerInv.addItem(order);

                        files.balance.set(balanceKey, balance - cost);

                        player.sendMessage(ChatColor.GOLD + "You have bought " + order.getAmount() + " of " + order.getI18NDisplayName() + " for: £" + cost);

                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough money to buy this item (Cost: £" + cost + ").");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Not enough inventory room for this item.");
                }

                return;

            default:
        }
    }
    private void openPrevious(Player player) {
        if (files.shop.contains(player.getUniqueId().toString())){
            String name = files.shop.getString(player.getUniqueId().toString());
            if (name.equals(player.getOpenInventory().getTitle())){
                return;
            }
            name = name.substring(12);
            if (name.equals("Shop menu")){
                name = "root";
            }
            new CommandShopMenu().doMenu(name, player, name);
            files.shop.set(player.getUniqueId().toString(), player.getOpenInventory().getTitle());
        } else {
            player.sendMessage(ChatColor.RED + "No menu to go to");
        }
    }
}
