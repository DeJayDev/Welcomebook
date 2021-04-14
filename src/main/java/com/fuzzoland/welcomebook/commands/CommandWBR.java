package com.fuzzoland.welcomebook.commands;

import com.fuzzoland.welcomebook.Book;
import com.fuzzoland.welcomebook.WelcomeBook;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.jetbrains.annotations.NotNull;

public class CommandWBR implements CommandExecutor {

    private final WelcomeBook plugin;

    public CommandWBR(WelcomeBook plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (args.length == 0) {
            /*
            sender.sendMessage(Component.text("/wbr list - list all books\n", NamedTextColor.GOLD)
                .append(Component.text("/wbr export <codename> - export held book\n", DARK_GREEN))
                .append(Component.text("/wbr get <codename> - get book\n", GOLD))
                .append(Component.text("/wbr delete <codename> - delete book\n", DARK_GREEN))
                .append(Component.text("/wbr preview <codename> - view book preview\n", GOLD))
                .append(Component.text("/wbr give <player> <codename> - give book\n", DARK_GREEN))
                .append(Component.text("/wbr settitle <title...> - change held book's title\n", GOLD))
                .append(Component.text("/wbr setauthor <author...> - change held book's author\n", DARK_GREEN))
                .append(Component.text("/wbr undo - make held book editable again\n", GOLD))
                .append(Component.text("/wbr reload - reload settings file\n", DARK_GREEN))
                .append(Component.text("/wbr import - import old config.yml from WelcomeBook", GOLD)));*/
            return false;
        }
        if (args[0].equalsIgnoreCase("list")) {
            if (sender.hasPermission("wbr.list")) {
                if (args.length == 1) {
                    sender.sendMessage(ChatColor.BLUE + "Books:");
                    for (String codename : plugin.bh.getBookNames()) {
                        sender.sendMessage(ChatColor.GREEN + "- " + codename);
                    }
                    return false;
                }
                sender.sendMessage(ChatColor.RED + "/wbr list");
                return false;
            }
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
        } else if (args[0].equalsIgnoreCase("export")) {
            if (sender.hasPermission("wbr.export")) {
                if (sender instanceof Player) {
                    if (args.length == 2) {
                        Player player = (Player) sender;
                        ItemStack item = player.getInventory().getItemInMainHand().clone();
                        if (item.getType() == Material.WRITTEN_BOOK) {
                            if (!plugin.bh.isBook(args[1])) {
                                BookMeta meta = (BookMeta) item.getItemMeta();
                                List<Component> pages = new ArrayList<>(meta.pages());
                                List<String> stringPages = new ArrayList<>();
                                pages.forEach(page -> stringPages.add(LegacyComponentSerializer.legacyAmpersand().serialize(page)));
                                plugin.bh.updateBook(args[1], new Book(meta.getTitle(), Bukkit.getServer().getPlayerExact(meta.getAuthor()).getUniqueId(), stringPages, new HashMap<>()));
                                sender.sendMessage(ChatColor.GREEN + "Book successfully exported!");
                            } else {
                                sender.sendMessage(ChatColor.RED + "A book by this name already exists.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "The item must be a written book.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "/wbr export <codename>");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("get")) {
            if (sender.hasPermission("wbr.get")) {
                if (sender instanceof Player) {
                    if (args.length == 2) {
                        if (plugin.bh.isBook(args[1])) {
                            Player player = (Player) sender;
                            String pname = player.getName();
                            Book book = plugin.bh.getBook(args[1]);
                            long cooldown = plugin.settings.getLong("BookCooldown");
                            if (!book.hasCooldown(pname) || System.currentTimeMillis() - book.getCooldown(pname) > cooldown * 1000) {
                                player.getInventory().addItem(book.toItemStack());
                                player.updateInventory();
                                book.updateCooldown(pname, System.currentTimeMillis());
                                plugin.bh.updateBook(args[1], book);
                                sender.sendMessage(ChatColor.GREEN + "Book added to inventory!");
                            } else {
                                Long timeleft = (cooldown * 1000) - (Math.round(System.currentTimeMillis() - book.getCooldown(pname)));
                                player.sendMessage(ChatColor.RED + "You can't get that book for another " + plugin.convertTime(timeleft) + ".");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "A book by that name does not exist.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "/wbr get <codename>");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (sender.hasPermission("wbr.delete")) {
                if (args.length == 2) {
                    if (plugin.bh.isBook(args[1])) {
                        plugin.bh.updateBook(args[1], null);
                        sender.sendMessage(ChatColor.GREEN + "Book successfully deleted!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "A book by that name does not exist.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "/wbr delete <codename>");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("preview")) {
            if (sender.hasPermission("wbr.preview")) {
                if (args.length == 2) {
                    if (!plugin.bh.isBook(args[1])) {
                        sender.sendMessage(ChatColor.RED + "A book by that name does not exist.");
                        return false;
                    }
                    Book book = plugin.bh.getBook(args[1]);
                    sender.sendMessage(Component.text("Title: ", NamedTextColor.BLUE).append(
                        Component.text(book.getTitle(), NamedTextColor.GREEN)
                    ));
                    sender.sendMessage(Component.text("Author: ", NamedTextColor.BLUE).append(
                        Component.text(book.getTitle(), NamedTextColor.GREEN)
                    ));
                    sender.sendMessage(Component.text("Preview: ", NamedTextColor.BLUE)
                        .append(LegacyComponentSerializer.legacyAmpersand().deserialize(book.getPages().get(0))));
                } else {
                    sender.sendMessage(ChatColor.RED + "/wbr preview <codename>");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("give")) {
            if (sender.hasPermission("wbr.give")) {
                if (args.length == 3) {
                    if (Bukkit.getPlayerExact(args[1]) != null) {
                        if (plugin.bh.isBook(args[2])) {
                            Player player = Bukkit.getPlayer(args[1]);
                            Book book = plugin.bh.getBook(args[2]);
                            ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
                            BookMeta meta = (BookMeta) item.getItemMeta();
                            meta.setTitle(book.getTitle());
                            meta.setAuthor(Bukkit.getServer().getPlayer(book.getAuthor()).getName());
                            for (String page : book.getPages()) {
                                meta.addPages(LegacyComponentSerializer.legacyAmpersand().deserialize(page));
                            }
                            item.setItemMeta(meta);
                            player.getInventory().addItem(item);
                            player.updateInventory();
                            sender.sendMessage(ChatColor.GREEN + "Book added to " + args[1] + "'s inventory!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "A book by this name does not exist.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "That player is not online.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "/wbr give <player> <codename>");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("settitle")) {
            if (sender.hasPermission("wbr.settitle")) {
                if (sender instanceof Player) {
                    if (args.length >= 2) {
                        Player player = (Player) sender;
                        ItemStack item = player.getInventory().getItemInMainHand().clone();
                        if (item.getType() == Material.WRITTEN_BOOK) {
                            BookMeta meta = (BookMeta) item.getItemMeta();
                            StringBuilder title = new StringBuilder();
                            for (int i = 1; i < args.length; i++) {
                                title.append(" ").append(args[i]);
                            }
                            meta.setTitle(title.toString().replaceFirst(" ", ""));
                            item.setItemMeta(meta);
                            player.getInventory().setItemInMainHand(item);
                            player.updateInventory();
                            sender.sendMessage(ChatColor.GREEN + "Book's name successfully changed!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "The item must be a written book.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "/wbr settitle <title...>");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("setauthor")) {
            if (sender.hasPermission("wbr.setauthor")) {
                if (sender instanceof Player) {
                    if (args.length >= 2) {
                        Player player = (Player) sender;
                        ItemStack item = player.getItemInHand().clone();
                        if (item.getType() == Material.WRITTEN_BOOK) {
                            BookMeta meta = (BookMeta) item.getItemMeta();
                            StringBuilder author = new StringBuilder();
                            for (int i = 1; i < args.length; i++) {
                                author.append(" ").append(args[i]);
                            }
                            meta.setAuthor(author.toString().replaceFirst(" ", ""));
                            item.setItemMeta(meta);
                            player.getInventory().setItemInMainHand(item);
                            player.updateInventory();
                            sender.sendMessage(ChatColor.GREEN + "Book's author successfully changed!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "The item must be a written book.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "/wbr setauthor <author...>");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("undo")) {
            if (sender.hasPermission("wbr.undo")) {
                if (sender instanceof Player) {
                    if (args.length == 1) {
                        Player player = (Player) sender;
                        ItemStack writtenbook = player.getInventory().getItemInMainHand().clone();
                        if (writtenbook.getType() == Material.WRITTEN_BOOK) {
                            ItemStack writablebook = new ItemStack(Material.WRITABLE_BOOK, 1);
                            BookMeta oldmeta = (BookMeta) writtenbook.getItemMeta();
                            BookMeta newmeta = (BookMeta) writablebook.getItemMeta();
                            newmeta.pages(oldmeta.pages());
                            writablebook.setItemMeta(newmeta);
                            player.getInventory().setItemInMainHand(writablebook);
                            player.updateInventory();
                            sender.sendMessage(ChatColor.GREEN + "Book has been undone!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "The item must be a written book.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "/wbr undo");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("wbr.reload")) {
                if (args.length == 1) {
                    plugin.settings = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "settings.yml"));
                    sender.sendMessage(ChatColor.GREEN + "Settings file reloaded!");
                } else {
                    sender.sendMessage(ChatColor.RED + "/wbr reload");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        } else if (args[0].equalsIgnoreCase("import")) {
            if (sender.hasPermission("wbr.import")) {
                if (args.length == 1) {
                    File file = new File(plugin.getDataFolder(), "config.yml");
                    if (file.exists()) {
                        sender.sendMessage(ChatColor.GREEN + "Importing books...");
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        if (config.isSet("Books")) {
                            Set<String> books = config.getConfigurationSection("Books").getKeys(false);
                            List<String> blist = new ArrayList<>(books);
                            for (String b : blist) {
                                if (plugin.bh.isBook(b)) {
                                    sender.sendMessage(ChatColor.RED + "Failed to import book " + b + " as another book with its name already exists.");
                                } else {
                                    List<Component> pages = new ArrayList<>();
                                    for (String page : config.getStringList("Books." + b + ".BookPages")) {
                                        pages.add(LegacyComponentSerializer.legacyAmpersand().deserialize(page));
                                    }
                                    String title = config.getString("Books." + b + ".BookName");
                                    UUID author = UUID.fromString(config.getString("Books." + b + ".AuthorName"));
                                    List<String> stringPages = new ArrayList<>();
                                    pages.forEach(page -> stringPages.add(LegacyComponentSerializer.legacyAmpersand().serialize(page)));
                                    plugin.bh.updateBook(b, new Book(title, author, stringPages, new HashMap<>()));
                                    sender.sendMessage(ChatColor.GREEN + "Successfully imported book " + b + " with title " + title + " by " + author + ".");
                                }
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "No books were found to import.");
                        }
                        sender.sendMessage(ChatColor.GREEN + "You may now delete the config.yml.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "A config.yml was not found in the WelcomeBook folder.");
                        sender.sendMessage(ChatColor.RED + "Make sure to transfer it to the new plugin folder.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "/wbr import");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        }
        return true;
    }
}
