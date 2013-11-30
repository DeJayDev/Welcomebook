package com.fuzzoland.WelcomeBookRecoded;

import java.util.Arrays;
import java.util.List;

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

public class ShopListener implements Listener{

	private WBR plugin;
	
	public ShopListener(WBR plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onMakehop(SignChangeEvent event){
		if(event.getLine(0).equals("[BookShop]")){
			for(int i = 1; i < 4; i++){
				if(event.getLine(i) == null){
					event.setLine(0, ChatColor.RED + "[BookShop]");
					return;
				}
			}
			Player player = event.getPlayer();
			String name = event.getLine(3);
			if(name.equals(player.getName())){
				if(player.hasPermission("wbr.makeshop.normal")){
					String price = event.getLine(2);
					if(price.startsWith("$")){
						try{
							Double.parseDouble(price.replace("$", ""));
						}catch(NumberFormatException e){
							player.sendMessage(ChatColor.RED + "The price is invalid.");
							event.setLine(0, ChatColor.RED + "[BookShop]");
							return;
						}
						if(plugin.settings.getBoolean("BookShop.Fee.Enabled")){
							if(!player.hasPermission("wbr.makeshop.normal.nofee")){
								Double fee = plugin.settings.getDouble("BookShop.Fee.Amount");
								EconomyResponse cost = plugin.eco.withdrawPlayer(player.getName(), fee);
								if(cost.transactionSuccess()){
									player.sendMessage(ChatColor.GREEN + "Successfully created a normal BookShop for $" + String.valueOf(fee) + "!");
								}else{
									player.sendMessage(ChatColor.RED + "It costs $" + String.valueOf(fee) + " to create a BookShop.");
									event.setLine(0, ChatColor.RED + "[BookShop]");
									return;
								}
							}
						}else{
							player.sendMessage(ChatColor.GREEN + "Successfully created a BookShop!");
						}
						event.setLine(0, ChatColor.BLUE + "[BookShop]");
					}else{
						player.sendMessage(ChatColor.RED + "The price is invalid.");
						event.setLine(0, ChatColor.RED + "[BookShop]");
					}
				}else{
					player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
					event.setLine(0, ChatColor.RED + "[BookShop]");
				}
			}else if(name.equals(plugin.settings.getString("BookShop.AdminShopName"))){
				if(player.hasPermission("wbr.makeshop.infinite")){
					String price = event.getLine(2);
					if(price.startsWith("$")){
						try{
							Double.parseDouble(price.replace("$", ""));
						}catch(NumberFormatException e){
							player.sendMessage(ChatColor.RED + "The price is invalid.");;
							event.setLine(0, ChatColor.RED + "[BookShop]");
							return;
						}
						player.sendMessage(ChatColor.GREEN + "Successfully created an infinite BookShop!");
						event.setLine(0, ChatColor.BLUE + "[BookShop]");
					}else{
						player.sendMessage(ChatColor.RED + "The price is invalid.");
						event.setLine(0, ChatColor.RED + "[BookShop]");
					}
				}else{
					player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
					event.setLine(0, ChatColor.RED + "[BookShop]");
				}
			}else{
				player.sendMessage(ChatColor.RED + "You must put your name on the fourth line.");
				event.setLine(0, ChatColor.RED + "[BookShop]");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onUseShop(PlayerInteractEvent event){
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			Block block = event.getClickedBlock();
			if(block.getState() instanceof Sign){
				Sign sign = (Sign) block.getState();
				if(sign.getLine(0).equals(ChatColor.BLUE + "[BookShop]")){
					event.setCancelled(true);
					Player player = event.getPlayer();
					Location chestloc = block.getLocation().add(0, -1, 0);
					if(chestloc.getBlock().getType() == Material.CHEST){
						Chest chest = (Chest) chestloc.getBlock().getState();
						String bookn = sign.getLine(1);
						Double bookp = Double.parseDouble(sign.getLine(2).replace("$", ""));
						if(sign.getLine(3).equals(plugin.settings.getString("BookShop.AdminShopName"))){
							if(player.hasPermission("wbr.useshop.infinite")){
								for(ItemStack item : chest.getInventory().getContents()){
									if(item != null){
										if(item.getType() == Material.WRITTEN_BOOK){
											BookMeta meta = (BookMeta) item.getItemMeta();
											if(bookn.equals(meta.getTitle())){
												if(bookp > 0){
													EconomyResponse cost = plugin.eco.withdrawPlayer(player.getName(), bookp);
													if(cost.transactionSuccess()){
														player.sendMessage(ChatColor.GREEN + "Successfully bought the book " + bookn + " for $" + String.valueOf(bookp) + ".");
													}else{
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
								}
								player.sendMessage(ChatColor.RED + "This BookShop is out of stock.");
							}else{
								player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
							}
						}else{
							if(player.hasPermission("wbr.useshop.normal")){
								String seller = sign.getLine(3);
								for(ItemStack item : chest.getInventory().getContents()){
									if(item != null){
										if(item.getType() == Material.WRITTEN_BOOK){
											BookMeta meta = (BookMeta) item.getItemMeta();
											if(bookn.equals(meta.getTitle())){
												if(bookp > 0){
													EconomyResponse cost = plugin.eco.withdrawPlayer(player.getName(), bookp);
													if(cost.transactionSuccess()){
														plugin.eco.depositPlayer(seller, bookp);
														player.sendMessage(ChatColor.GREEN + "Successfully bought the book " + bookn + " for $" + String.valueOf(bookp) + ".");
														if(Bukkit.getPlayerExact(seller) != null){
															Bukkit.getPlayerExact(seller).sendMessage(ChatColor.GREEN + player.getName() + " bought the book " + bookn + " for $" + String.valueOf(bookp) + " from you.");
														}
													}else{
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
								}
								player.sendMessage(ChatColor.RED + "This BookShop is out of stock.");
							}else{
								player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
							}
						}
					}else{
						player.sendMessage(ChatColor.RED + "This BookShop is missing its chest.");
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onShopAccess(PlayerInteractEvent event){
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(event.getClickedBlock().getType() == Material.CHEST){
				Location signloc = event.getClickedBlock().getLocation().add(0, 1, 0);
				if(signloc.getBlock().getState() instanceof Sign){
					Sign sign = (Sign) signloc.getBlock().getState();
					if(sign.getLine(0).equals(ChatColor.BLUE + "[BookShop]")){
						if(isProtected(sign, event.getPlayer())){
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onShopBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		if(block.getType() == Material.CHEST){
			Location signloc = block.getLocation().add(0, 1, 0);
			if(signloc.getBlock().getState() instanceof Sign){
				Sign sign = (Sign) signloc.getBlock().getState();
				if(sign.getLine(0).equals(ChatColor.BLUE + "[BookShop]")){
					if(isProtected(sign, event.getPlayer())){
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		if(block.getState() instanceof Sign){
			Sign sign = (Sign) block.getState();
			if(sign.getLine(0).equals(ChatColor.BLUE + "[BookShop]")){
				if(isProtected(sign, event.getPlayer())){
					event.setCancelled(true);
					return;
				}
			}
		}
		List<Block> relatives = Arrays.asList(block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH), block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST));
		for(int i = 0; i < relatives.size(); i++){
			Block b = relatives.get(i);
			if(b.getType() == Material.WALL_SIGN){
				Sign sign = (Sign) b.getState();
				if(sign.getLine(0).equals(ChatColor.BLUE + "[BookShop]")){
					if(isProtected(sign, event.getPlayer())){
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		if(block.getRelative(BlockFace.UP).getType() == Material.SIGN_POST){
			Sign sign = (Sign) block.getRelative(BlockFace.UP).getState();
			if(sign.getLine(0).equals(ChatColor.BLUE + "[BookShop]")){
				if(isProtected(sign, event.getPlayer())){
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	private Boolean isProtected(Sign sign, Player player){
		String owner = sign.getLine(3);
		if(!owner.equals(player.getName())){
			if(!player.hasPermission("wbr.breakshop")){
				player.sendMessage(ChatColor.RED + "That BookShop is protected.");
				return true;
			}
		}
		return false;
	}
}
