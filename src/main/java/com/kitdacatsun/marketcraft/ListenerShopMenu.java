package com.kitdacatsun.marketcraft;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import com.kitdacatsun.marketcraft.MarketCraft.files;

import java.util.Objects;

public class ListenerShopMenu implements Listener {
    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Shop menu") || event.getCurrentItem() == null) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem().getLore() == null && Objects.equals(event.getClickedInventory(), player.getOpenInventory().getTopInventory())) {
            setPrevious(event, player);

        } else if (event.getCurrentItem().getLore() != null){
            switch (Objects.requireNonNull(event.getCurrentItem().getItemMeta().getLore()).get(0)) {

                case "Return to Previous Menu":
                    openPrevious(player);
                    return;

                case "Return Home":
                    new CommandShopMenu().doMenu("root", player, "Shop menu");
                    return;

                default:
            }

        } else {
            ItemStack item = event.getCurrentItem();
            new CommandShopMenu().openShop(item, player, item.getI18NDisplayName());
        }
    }

    private void setPrevious(InventoryClickEvent event, Player player) {
        String title = player.getOpenInventory().getTitle().substring(12);
        String name = event.getCurrentItem().getItemMeta().getDisplayName();
        if (name.equals("root")){
            name = "Shop menu";
        }
        files.shop.set(player.getUniqueId().toString(), title);
        new CommandShopMenu().doMenu(name, player, name);
    }

    private void openPrevious(Player player) {
        if (files.shop.contains(player.getUniqueId().toString())){
            String name = files.shop.getString(player.getUniqueId().toString());
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
