package com.fuzzoland.welcomebook.listener;

import com.destroystokyo.paper.MaterialTags;
import com.fuzzoland.welcomebook.WelcomeBook;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class ShopListener implements Listener {

    private final WelcomeBook plugin;
    private final Component shopPrefix = Component.text("[BookShop]", NamedTextColor.BLUE);

    public ShopListener(WelcomeBook plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMakeShop(SignChangeEvent event) {
        if (event.line(0) == null) {
            return; // Sanity.
        }
        if (PlainComponentSerializer.plain().serialize(event.line(0)).contains("[BookShop]")) {
            for (int i = 1; i < 4; i++) {
                if (event.line(i) == null) {
                    event.line(0, Component.text("[BookShop]", NamedTextColor.RED));
                    return;
                }
            }
            Player player = event.getPlayer();
            Component name = event.line(3);
            if (name.equals(player.getName())) {
                if (player.hasPermission("wbr.makeshop.normal")) {
                    String price = PlainComponentSerializer.plain().serialize(event.line(2));
                    if (price.startsWith("$")) {
                        try {
                            Double.parseDouble(price.replace("$", ""));
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "The price is invalid.");
                            event.line(0, Component.text("[BookShop]", NamedTextColor.RED));
                            return;
                        }
                        if (plugin.settings.getBoolean("BookShop.Fee.Enabled")) {
                            if (!player.hasPermission("wbr.makeshop.normal.nofee")) {
                                double fee = plugin.settings.getDouble("BookShop.Fee.Amount");
                                EconomyResponse cost = plugin.eco.withdrawPlayer(player, fee);
                                if (cost.transactionSuccess()) {
                                    player.sendMessage(ChatColor.GREEN + "Successfully created a normal BookShop for $" + fee + "!");
                                } else {
                                    player.sendMessage(ChatColor.RED + "It costs $" + fee + " to create a BookShop.");
                                    event.line(0, Component.text("[BookShop]", NamedTextColor.RED));
                                    return;
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.GREEN + "Successfully created a BookShop!");
                        }
                        event.setLine(0, ChatColor.BLUE + "[BookShop]");
                    } else {
                        player.sendMessage(ChatColor.RED + "The price is invalid.");
                        event.setLine(0, ChatColor.RED + "[BookShop]");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                    event.setLine(0, ChatColor.RED + "[BookShop]");
                }
            } else if (name.equals(plugin.settings.getString("BookShop.AdminShopName"))) {
                if (player.hasPermission("wbr.makeshop.infinite")) {
                    String price = event.getLine(2);
                    if (price.startsWith("$")) {
                        try {
                            Double.parseDouble(price.replace("$", ""));
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "The price is invalid.");
                            event.setLine(0, ChatColor.RED + "[BookShop]");
                            return;
                        }
                        player.sendMessage(ChatColor.GREEN + "Successfully created an infinite BookShop!");
                        event.setLine(0, ChatColor.BLUE + "[BookShop]");
                    } else {
                        player.sendMessage(ChatColor.RED + "The price is invalid.");
                        event.setLine(0, ChatColor.RED + "[BookShop]");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                    event.setLine(0, ChatColor.RED + "[BookShop]");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You must put your name on the fourth line.");
                event.setLine(0, ChatColor.RED + "[BookShop]");
            }
        }
    }

    @EventHandler
    public void onUseShop(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (sign.line(0).equals(shopPrefix)) {
                    event.setCancelled(true);
                    Player player = event.getPlayer();
                    Location chestloc = block.getLocation().add(0, -1, 0);
                    if (chestloc.getBlock().getType() == Material.CHEST) {
                        Chest chest = (Chest) chestloc.getBlock().getState();
                        Component bookn = sign.line(1);
                        double price = Double.parseDouble(PlainComponentSerializer.plain().serialize(sign.line(2)).replace("$", ""));
                        if (sign.line(3).equals(price)) {
                            if (player.hasPermission("wbr.useshop.infinite")) {
                                for (ItemStack item : chest.getInventory().getContents()) {
                                    if (item.getType() == Material.WRITTEN_BOOK) {
                                        BookMeta meta = (BookMeta) item.getItemMeta();
                                        if (bookn.equals(meta.title())) {
                                            if (price > 0) {
                                                EconomyResponse cost = plugin.eco.bankWithdraw(player.getName(), price);
                                                if (cost.transactionSuccess()) {
                                                    player.sendMessage(ChatColor.GREEN + "Successfully bought the book " + bookn + " for $" + price + ".");
                                                } else {
                                                    player.sendMessage(ChatColor.RED + "You do not have enough funds to buy that book.");
                                                    return;
                                                }
                                            }
                                            player.getInventory().addItem(item);
                                            player.updateInventory();
                                            return;
                                        }
                                    }
                                }
                                player.sendMessage(ChatColor.RED + "This BookShop is out of stock.");
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                            }
                        } else {
                            if (player.hasPermission("wbr.useshop.normal")) {
                                Player seller = Bukkit.getPlayer(PlainComponentSerializer.plain().serialize(sign.line(3)));
                                for (ItemStack item : chest.getInventory().getContents()) {
                                    if (item.getType() == Material.WRITTEN_BOOK) {
                                        BookMeta meta = (BookMeta) item.getItemMeta();
                                        if (bookn.equals(meta.title())) {
                                            if (price > 0) {
                                                EconomyResponse cost = plugin.eco.withdrawPlayer(player.getName(), price);
                                                if (cost.transactionSuccess()) {
                                                    plugin.eco.depositPlayer(seller, price);
                                                    player.sendMessage(Component.text("Successfully bought the book " + bookn + " for $" + price + ".", NamedTextColor.GREEN));
                                                    seller.sendMessage(Component.text(player.getName() + " bought the book " + bookn + " for $" + price + " from you.", NamedTextColor.GREEN));
                                                } else {
                                                    player.sendMessage(ChatColor.RED + "You do not have enough funds to buy that book.");
                                                    return;
                                                }
                                            }
                                            chest.getInventory().removeItem(item);
                                            player.getInventory().addItem(item);
                                            player.updateInventory();
                                            return;
                                        }
                                    }
                                }
                                player.sendMessage(ChatColor.RED + "This BookShop is out of stock.");
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "This BookShop is missing its chest.");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShopAccess(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.CHEST) {
                Location signloc = event.getClickedBlock().getLocation().add(0, 1, 0);
                if (signloc.getBlock().getState() instanceof Sign) {
                    Sign sign = (Sign) signloc.getBlock().getState();
                    if (sign.line(0).equals(shopPrefix)) {
                        if (isProtected(sign, event.getPlayer())) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShopBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST) {
            Location signLoc = block.getLocation().add(0, 1, 0);
            if (signLoc.getBlock().getState() instanceof Sign) {
                Sign sign = (Sign) signLoc.getBlock().getState();
                if (sign.line(0).equals(shopPrefix)) {
                    if (isProtected(sign, event.getPlayer())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (sign.line(0).equals(shopPrefix)) {
                if (isProtected(sign, event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        List<Block> relatives = Arrays.asList(block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH), block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST));
        for (Block b : relatives) {
            if (MaterialTags.SIGNS.isTagged(b.getType())) {
                Sign sign = (Sign) b.getState();
                if (sign.line(0).equals(shopPrefix)) {
                    if (isProtected(sign, event.getPlayer())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (MaterialTags.SIGNS.isTagged(block.getRelative(BlockFace.UP).getType())) {
            Sign sign = (Sign) block.getRelative(BlockFace.UP).getState();
            if (sign.line(0).equals(shopPrefix)) {
                if (isProtected(sign, event.getPlayer())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isProtected(Sign sign, Player player) {
        Component owner = sign.line(3);
        if (!owner.equals(PlainComponentSerializer.plain().deserialize(player.getName()))) {
            if (!player.hasPermission("wbr.breakshop")) {
                player.sendMessage(ChatColor.RED + "That BookShop is protected.");
                return true;
            }
        }
        return false;
    }

}
