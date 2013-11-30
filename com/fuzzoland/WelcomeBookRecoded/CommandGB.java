package com.fuzzoland.WelcomeBookRecoded;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class CommandGB implements CommandExecutor{

	private WBR plugin;

	public CommandGB(WBR plugin){
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender.hasPermission("wbr.gb")){
			if(sender instanceof Player){
				if(args.length == 0){
					List<String> books = plugin.settings.getStringList("CmdGB.Books");
					if(!books.isEmpty()){
						sender.sendMessage(ChatColor.BLUE + "Getting books...");
						Player player = (Player) sender;
						for(String b : books){
							if(plugin.bh.isBook(b)){
								String pname = sender.getName();
								Book book = plugin.bh.getBook(b);
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
									plugin.bh.updateBook(b, book);
									sender.sendMessage(ChatColor.GREEN + "You got the book " + book.getTitle() + "!");
								}else{
									Long timeleft = (cooldown * 1000) - (Math.round(System.currentTimeMillis() - book.getCooldown(pname)));
									sender.sendMessage(ChatColor.RED + "You can't get the book " + book.getTitle() + " for another " + plugin.convertTime(timeleft) + ".");
								}
							}
						}
					}else{
						sender.sendMessage(ChatColor.RED + "Sorry, there are no books available.");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "/gb");
				}
			}else{
				sender.sendMessage(ChatColor.RED + "Only players can use that command.");
			}
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
		}
		return false;
	}
}
