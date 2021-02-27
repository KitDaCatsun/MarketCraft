package com.kitdacatsun.marketcraft;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.UUID;

public class GuiListener implements Listener {


    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        if (event.getView().getBottomInventory().getType() != InventoryType.PLAYER) {
            return;
        }

        try {
            Objects.requireNonNull(event.getCurrentItem());
        } catch (NullPointerException e) {
            return;
        }

        if (event.getView().getTitle().equals("Shop")) {
            try {
                if (Objects.requireNonNull(event.getCurrentItem()).getLore() != null) {
                    shopEvent(event);
                } else {
                    switchItem(event);
                }
            } catch (NullPointerException ignored) { }
        } else if (event.getView().getTitle().equals("Shop Menu")) {
            try {
                shopMenu(event);
            } catch (NullPointerException e) {
                switchItem(event);
            }
        }
    }

    private void shopEvent(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        assert clickedItem != null;

        Player player = (Player) event.getWhoClicked();

        switch (Objects.requireNonNull(clickedItem.getItemMeta().getLore()).get(0)) {
            case "Buy":
            case "Sell":
                Inventory inventory = event.getClickedInventory();
                assert inventory != null;

                GUIItem item;

                item = new GUIItem();
                item.material = Objects.requireNonNull(inventory.getItem(GUIBuilder.MID)).getType();
                item.amount = clickedItem.getAmount();
                item.lore = "Selected Item";
                inventory.setItem(GUIBuilder.MID, item.getItemStack());

                if (inventory.getItem(GUIBuilder.MID) != null) {
                    item = new GUIItem();
                    item.name = clickedItem.getItemMeta().getDisplayName();
                    item.lore = "Confirm";
                    item.amount = 1;
                    item.material = Material.LIME_DYE;
                    inventory.setItem(GUIBuilder.BOT_MID, item.getItemStack());
                }

                player.openInventory(inventory);

                return;
            case "Return to Previous Menu":
                // Go back
                return;
            case "Confirm":
                // Do order

                ItemMeta clickedItemMeta = clickedItem.getItemMeta();

                Inventory shop = player.getOpenInventory().getTopInventory();
                Inventory playersInv = player.getOpenInventory().getBottomInventory();

                int cost = 10;

                if (clickedItemMeta.getLore().get(0).equals("Confirm") && !clickedItemMeta.getDisplayName().equals("Select an option") && shop.getItem(GUIBuilder.MID) != null){

                    int saleAmount = Objects.requireNonNull(shop.getItem(GUIBuilder.MID)).getAmount();

                    Material selectedItemMaterial = Objects.requireNonNull(shop.getItem(GUIBuilder.MID)).getType();
                    ItemStack selectedItem = new ItemStack(selectedItemMaterial, saleAmount);

                    // Selling an item
                    if (clickedItemMeta.getDisplayName().contains("Sell")){

                        // Check if player has enough of the item type
                        if (playersInv.contains(selectedItem.getType(), saleAmount)){
                            playersInv.removeItemAnySlot(selectedItem);

                            UUID Uuid = player.getUniqueId();
                            String playerBalanceKey = "Players." + Uuid.toString() + ".balance";

                            int balance = (int) MarketCraft.playerBalances.get(playerBalanceKey) + cost;
                            MarketCraft.playerBalances.set(playerBalanceKey, balance);

                            player.sendMessage(ChatColor.GOLD + "You have sold " + saleAmount + " of " + selectedItem.getI18NDisplayName() + " for: £" + saleAmount * cost);
                        } else {
                            player.sendMessage(ChatColor.RED + "Not enough of that item type to sell");
                        }
                    } else if (clickedItemMeta.getDisplayName().contains("Buy")){

                        if (!(player.getInventory().firstEmpty() == -1)){

                            String playerBalanceKey = "Players." + player.getUniqueId() + ".balance";
                            int balance = (int) MarketCraft.playerBalances.get(playerBalanceKey);

                            if (balance >= cost * saleAmount) {

                                playersInv.addItem(selectedItem);

                                balance = balance - cost * saleAmount;
                                MarketCraft.playerBalances.set(playerBalanceKey, balance);

                                player.sendMessage(ChatColor.GOLD + "You have Bought " + saleAmount + " of " + selectedItem.getI18NDisplayName() + " for: £" + saleAmount * cost);

                            } else {
                                player.sendMessage(ChatColor.RED + "Not enough money to buy this item.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Not enough inventory room for this item.");
                        }
                    }
                }

                return;
            default:
        }

    }

    private void switchItem(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        GUIItem item = new GUIItem();
        item.material = Objects.requireNonNull(event.getCurrentItem()).getType();
        item.amount = event.getCurrentItem().getAmount();
        item.lore = "Selected Item";

        CommandShop.openShop(player, item.getItemStack());
    }

    private void shopMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        CommandShopMenu commandShopMenu = new CommandShopMenu();
        commandShopMenu.doMenu(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().getDisplayName(), player);
    }
}
