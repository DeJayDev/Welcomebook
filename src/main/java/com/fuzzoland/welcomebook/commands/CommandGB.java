package com.fuzzoland.welcomebook.commands;

import com.fuzzoland.welcomebook.Book;
import com.fuzzoland.welcomebook.WelcomeBook;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandGB implements CommandExecutor {

    private final WelcomeBook plugin;

    public CommandGB(WelcomeBook plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (sender.hasPermission("wbr.gb")) {
            if (sender instanceof Player) {
                if (args.length == 0) {
                    List<String> books = plugin.settings.getStringList("CmdGB.Books");
                    if (!books.isEmpty()) {
                        sender.sendMessage(ChatColor.BLUE + "Getting books...");
                        Player player = (Player) sender;
                        for (String b : books) {
                            if (plugin.bh.isBook(b)) {
                                String pname = sender.getName();
                                Book book = plugin.bh.getBook(b);
                                long cooldown = plugin.settings.getLong("BookCooldown");
                                if (!book.hasCooldown(pname) || System.currentTimeMillis() - book.getCooldown(pname) > cooldown * 1000) {
                                    plugin.giveBook(player, b);
                                    book.updateCooldown(pname, System.currentTimeMillis());
                                    plugin.bh.updateBook(b, book);
                                    sender.sendMessage(ChatColor.GREEN + "You got the book " + book.getTitle() + "!");
                                } else {
                                    long timeleft = (cooldown * 1000) - (Math.round(System.currentTimeMillis() - book.getCooldown(pname)));
                                    sender.sendMessage(ChatColor.RED + "You can't get the book " + book.getTitle() + " for another " + plugin.convertTime(timeleft) + ".");
                                }
                            }
                        }
                        return false;
                    }
                    sender.sendMessage(ChatColor.RED + "Sorry, there are no books available.");
                    return false;
                }
                sender.sendMessage(ChatColor.RED + "/gb");
                return false;
            }
            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
            return false;
        }
        sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
        return false;
    }

}
