package com.fuzzoland.welcomebook.commands;

import com.fuzzoland.welcomebook.Book;
import com.fuzzoland.welcomebook.WelcomeBook;
import dev.dejay.reactor.commands.BaseCommand;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CopyrightCommand extends BaseCommand {

    public CopyrightCommand(WelcomeBook plugin) {
        super(plugin, "copyright", "Claim a book, prevent modifications!", "wbr.copyright");
    }

    @Override
    public TextComponent run(JavaPlugin plugin, CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            return errorMessage("You must provide a book name.");
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.WRITTEN_BOOK && !WelcomeBook.get().bh.isBook(item.getItemMeta().getDisplayName())) {
            return errorMessage("You are not holding a valid book.");
        }
        Book book = WelcomeBook.get().bh.getBook(args[0]);
        if (book.getAuthor() != player.getUniqueId() || !player.hasPermission("wbr.copyright.others")) {
            return errorMessage("You cannot modify books that do not belong to you.");
        }
        book.setLocked(true);
        WelcomeBook.get().bh.updateBook(args[0], book);
        return successMessage("The book " + book.getTitle() + " has been locked.");
    }
}
