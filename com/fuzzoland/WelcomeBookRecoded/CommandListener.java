package com.fuzzoland.WelcomeBookRecoded;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class CommandListener implements Listener{

	private WBR plugin;
	
	public CommandListener(WBR plugin){
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		String pcmd = event.getMessage().split(" ")[0].replaceFirst("/", "");
		for(String books : plugin.settings.getStringList("CustomCommands.Books")){
			String[] parts = books.split(";");
			String cmd = parts[0];
			if(cmd.equalsIgnoreCase(pcmd)){
				String bname = parts[1];
				if(plugin.bh.isBook(bname)){
					event.setCancelled(true);
					Player player = event.getPlayer();
					String pname = player.getName();
					Book book = plugin.bh.getBook(bname);
					Long cooldown = plugin.settings.getLong("BookCooldown");
					if(!book.hasCooldown(pname) || System.currentTimeMillis() - book.getCooldown(pname) > cooldown * 1000){
						ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
						BookMeta meta = (BookMeta) item.getItemMeta();
						meta.setTitle(book.getTitle());
						meta.setAuthor(book.getAuthor());
						for(String page : book.getPages()){
							meta.addPage(page.replaceAll("&", "§").replaceAll("Â", ""));
						}
						item.setItemMeta(meta);
						player.getInventory().addItem(item);
						player.updateInventory();
						book.updateCooldown(pname, System.currentTimeMillis());
						plugin.bh.updateBook(bname, book);
						player.sendMessage(ChatColor.GREEN + "You got the book " + book.getTitle() + "!");
					}else{
						Long timeleft = (cooldown * 1000) - (Math.round(System.currentTimeMillis() - book.getCooldown(pname)));
						player.sendMessage(ChatColor.RED + "You can't get the book " + book.getTitle() + " for another " + String.valueOf(plugin.convertTime(timeleft)) + ".");
					}
				}
			}
		}
	}
}
