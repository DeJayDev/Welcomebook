package com.fuzzoland.WelcomeBookRecoded;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

public class JoinListener implements Listener{

	private WBR plugin;
	
	public JoinListener(WBR plugin){
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		if(player.hasPlayedBefore()){
			if(plugin.settings.getBoolean("EveryJoin.Enabled")){
				List<String> books = plugin.settings.getStringList("EveryJoin.Books");
				PlayerInventory inv = player.getInventory();
				for(String b : books){
					if(plugin.bh.isBook(b)){
						Book book = plugin.bh.getBook(b);
						ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
						BookMeta meta = (BookMeta) item.getItemMeta();
						meta.setTitle(book.getTitle());
						meta.setAuthor(book.getAuthor());
						for(String page : book.getPages()){
							meta.addPage(page.replaceAll("&", "§").replaceAll("Â", ""));
						}
						item.setItemMeta(meta);
						inv.addItem(item);
						player.updateInventory();
					}
				}
			}
		}else{
			if(plugin.settings.getBoolean("FirstJoin.Enabled")){
				List<String> books = plugin.settings.getStringList("FirstJoin.Books");
				PlayerInventory inv = player.getInventory();
				for(String b : books){
					if(plugin.bh.isBook(b)){
						Book book = plugin.bh.getBook(b);
						ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
						BookMeta meta = (BookMeta) item.getItemMeta();
						meta.setTitle(book.getTitle());
						meta.setAuthor(book.getAuthor());
						for(String page : book.getPages()){
							meta.addPage(page.replaceAll("&", "§").replaceAll("Â", ""));
						}
						item.setItemMeta(meta);
						inv.addItem(item);
						player.updateInventory();
					}
				}
			}
		}
	}
}
