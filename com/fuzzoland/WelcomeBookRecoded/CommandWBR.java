package com.fuzzoland.WelcomeBookRecoded;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class CommandWBR implements CommandExecutor{

	private WBR plugin;
	
	public CommandWBR(WBR plugin){
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(args.length == 0){
			sender.sendMessage(ChatColor.GOLD + "/wbr list - list all books");
			sender.sendMessage(ChatColor.DARK_GREEN + "/wbr export <codename> - export held book");
			sender.sendMessage(ChatColor.GOLD + "/wbr get <codename> - get book");
			sender.sendMessage(ChatColor.DARK_GREEN + "/wbr delete <codename> - delete book");
			sender.sendMessage(ChatColor.GOLD + "/wbr preview <codename> - view book preview");
			sender.sendMessage(ChatColor.DARK_GREEN + "/wbr give <player> <codename> - give book");
			sender.sendMessage(ChatColor.GOLD + "/wbr settitle <title...> - change held book's title");
			sender.sendMessage(ChatColor.DARK_GREEN + "/wbr setauthor <author...> - change held book's author");
			sender.sendMessage(ChatColor.GOLD + "/wbr undo - make held book editable again");
			sender.sendMessage(ChatColor.DARK_GREEN + "/wbr reload - reload settings file");
			sender.sendMessage(ChatColor.GOLD + "/wbr import - import old config.yml from WelcomeBook");
		}else if(args.length >= 1){
			if(args[0].equalsIgnoreCase("list")){
				if(sender.hasPermission("wbr.list")){
					if(args.length == 1){
						sender.sendMessage(ChatColor.BLUE + "Books:");
						for(String codename : plugin.bh.getBookNames()){
							sender.sendMessage(ChatColor.GREEN + "- " + codename);
						}
					}else{
						sender.sendMessage(ChatColor.RED + "/wbr list");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("export")){
				if(sender.hasPermission("wbr.export")){
					if(sender instanceof Player){
						if(args.length == 2){
							Player player = (Player) sender;
							ItemStack item = player.getItemInHand().clone();
							if(item.getType() == Material.WRITTEN_BOOK){
								if(!plugin.bh.isBook(args[1])){
									BookMeta meta = (BookMeta) item.getItemMeta();
									List<String> pages = new ArrayList<String>();
									for(String page : meta.getPages()){
										pages.add(page.replaceAll("§", "&"));
									}
									plugin.bh.updateBook(args[1], new Book(meta.getTitle(), meta.getAuthor(), pages, new HashMap<String, Long>()));
									sender.sendMessage(ChatColor.GREEN + "Book successfully exported!");
								}else{
									sender.sendMessage(ChatColor.RED + "A book by this name already exists.");
								}
							}else{
					        	sender.sendMessage(ChatColor.RED + "The item must be a written book.");
					        }
						}else{
							sender.sendMessage(ChatColor.RED + "/wbr export <codename>");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "Only players can use this command.");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("get")){
				if(sender.hasPermission("wbr.get")){
					if(sender instanceof Player){
						if(args.length == 2){
							if(plugin.bh.isBook(args[1])){
								Player player = (Player) sender;
								String pname = player.getName();
								Book book = plugin.bh.getBook(args[1]);
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
									plugin.bh.updateBook(args[1], book);
									sender.sendMessage(ChatColor.GREEN + "Book added to inventory!");
								}else{
									Long timeleft = (cooldown * 1000) - (Math.round(System.currentTimeMillis() - book.getCooldown(pname)));
									player.sendMessage(ChatColor.RED + "You can't get that book for another " + plugin.convertTime(timeleft) + ".");
								}
							}else{
								sender.sendMessage(ChatColor.RED + "A book by this name does not exist.");
							}
						}else{
							sender.sendMessage(ChatColor.RED + "/wbr get <codename>");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "Only players can use this command.");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("delete")){
				if(sender.hasPermission("wbr.delete")){
					if(args.length == 2){
						if(plugin.bh.isBook(args[1])){
							plugin.bh.updateBook(args[1], null);
							sender.sendMessage(ChatColor.GREEN + "Book successfully deleted!");
						}else{
							sender.sendMessage(ChatColor.RED + "A book by this name does not exist.");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "/wbr delete <codename>");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("preview")){
				if(sender.hasPermission("wbr.preview")){
					if(args.length == 2){
						if(plugin.bh.isBook(args[1])){
							Book book = plugin.bh.getBook(args[1]);
							sender.sendMessage(ChatColor.BLUE + "Title: " + ChatColor.GREEN + book.getTitle());
							sender.sendMessage(ChatColor.BLUE + "Author: " + ChatColor.GREEN + book.getAuthor());
							String firstpage = book.getPages().get(0);
							sender.sendMessage(ChatColor.BLUE + "Preview: " + ChatColor.GREEN + firstpage.substring(0, (firstpage.length() > 100) ? 100 : firstpage.length()).replaceAll("&", "§").replaceAll("Â", "") + "...");
						}else{
							sender.sendMessage(ChatColor.RED + "A book by this name does not exist.");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "/wbr preview <codename>");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("give")){
				if(sender.hasPermission("wbr.give")){
					if(args.length == 3){
						if(Bukkit.getPlayerExact(args[1]) != null){
							if(plugin.bh.isBook(args[2])){
								Player player = Bukkit.getPlayerExact(args[1]);
								Book book = plugin.bh.getBook(args[2]);
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
								sender.sendMessage(ChatColor.GREEN + "Book added to " + args[1] + "'s inventory!");
							}else{
								sender.sendMessage(ChatColor.RED + "A book by this name does not exist.");
							}
						}else{
							sender.sendMessage(ChatColor.RED + "That player is not online.");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "/wbr give <player> <codename>");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("settitle")){
				if(sender.hasPermission("wbr.settitle")){
					if(sender instanceof Player){
						if(args.length >= 2){
							Player player = (Player) sender;
							ItemStack item = player.getItemInHand().clone();
							if(item.getType() == Material.WRITTEN_BOOK){
								BookMeta meta = (BookMeta) item.getItemMeta();
								StringBuilder title = new StringBuilder();
								for(int i = 1; i < args.length; i++){
									title.append(" ").append(args[i]);
								}
								meta.setTitle(title.toString().replaceFirst(" ", ""));
								item.setItemMeta(meta);
								player.setItemInHand(item);
								player.updateInventory();
								sender.sendMessage(ChatColor.GREEN + "Book's name successfully changed!");
							}else{
								sender.sendMessage(ChatColor.RED + "The item must be a written book.");
							}
						}else{
							sender.sendMessage(ChatColor.RED + "/wbr settitle <title...>");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "Only players can use this command.");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("setauthor")){
				if(sender.hasPermission("wbr.setauthor")){
					if(sender instanceof Player){
						if(args.length >= 2){
							Player player = (Player) sender;
							ItemStack item = player.getItemInHand().clone();
							if(item.getType() == Material.WRITTEN_BOOK){
								BookMeta meta = (BookMeta) item.getItemMeta();
								StringBuilder author = new StringBuilder();
								for(int i = 1; i < args.length; i++){
									author.append(" ").append(args[i]);
								}
								meta.setAuthor(author.toString().replaceFirst(" ", ""));
								item.setItemMeta(meta);
								player.setItemInHand(item);
								player.updateInventory();
								sender.sendMessage(ChatColor.GREEN + "Book's author successfully changed!");
							}else{
								sender.sendMessage(ChatColor.RED + "The item must be a written book.");
							}
						}else{
							sender.sendMessage(ChatColor.RED + "/wbr setauthor <author...>");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "Only players can use this command.");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("undo")){
				if(sender.hasPermission("wbr.undo")){
					if(sender instanceof Player){
						if(args.length == 1){
							Player player = (Player) sender;
							ItemStack item = player.getItemInHand().clone();
							if(item.getType() == Material.WRITTEN_BOOK){
								item.setType(Material.BOOK_AND_QUILL);
								player.setItemInHand(item);
								player.updateInventory();
								sender.sendMessage(ChatColor.GREEN + "Book has been undone!");
							}else{
								sender.sendMessage(ChatColor.RED + "The item must be a written book.");
							}
						}else{
							sender.sendMessage(ChatColor.RED + "/wbr undo");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "Only players can use this command.");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("reload")){
				if(sender.hasPermission("wbr.reload")){
					if(args.length == 1){
						plugin.settings = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "settings.yml"));
						sender.sendMessage(ChatColor.GREEN + "Settings file reloaded!");
					}else{
						sender.sendMessage(ChatColor.RED + "/wbr reload");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}else if(args[0].equalsIgnoreCase("import")){
				if(sender.hasPermission("wbr.import")){
					if(args.length == 1){
						File file = new File(plugin.getDataFolder(), "config.yml");
						if(file.exists()){
							sender.sendMessage(ChatColor.GREEN + "Importing books...");
							YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
							if(config.isSet("Books")){
								Set<String> books = config.getConfigurationSection("Books").getKeys(false);
								if(books != null){
									List<String> blist = new ArrayList<String>(books);
									for(String b : blist){
										if(plugin.bh.isBook(b)){
											sender.sendMessage(ChatColor.RED + "Failed to import book " + b + " as another book with its name already exists.");
										}else{
											List<String> pages = new ArrayList<String>();
											for(String page : config.getStringList("Books." + b + ".BookPages")){
												pages.add(page.replaceAll("§", "&"));
											}
											String title = config.getString("Books." + b + ".BookName");
											String author = config.getString("Books." + b + ".AuthorName");
											plugin.bh.updateBook(b, new Book(title, author, pages, new HashMap<String, Long>()));
											sender.sendMessage(ChatColor.GREEN + "Successfully imported book " + b + " with title " + title + " by " + author + ".");
										}
									}
								}else{
									sender.sendMessage(ChatColor.RED + "No books were found to import.");
								}
							}else{
								sender.sendMessage(ChatColor.RED + "No books were found to import.");
							}
							sender.sendMessage(ChatColor.GREEN + "You may now delete the config.yml.");
						}else{
							sender.sendMessage(ChatColor.RED + "A config.yml was not found in the WBR folder.");
							sender.sendMessage(ChatColor.RED + "Make sure to transfer it to the new plugin folder.");
						}
					}else{
						sender.sendMessage(ChatColor.RED + "/wbr import");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				}
			}
		}
		return true;
	}
}
