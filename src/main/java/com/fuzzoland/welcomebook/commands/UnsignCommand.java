package com.fuzzoland.welcomebook.commands;

import com.fuzzoland.welcomebook.Book;
import com.fuzzoland.welcomebook.WelcomeBook;
import dev.dejay.reactor.commands.BaseCommand;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class UnsignCommand extends BaseCommand {

    public UnsignCommand(WelcomeBook plugin) {
        super(plugin, "unsign", "Unsign a book to edit it.", "wbr.undo", "bookundo");
    }

    @Override
    public TextComponent run(JavaPlugin plugin, CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack writtenbook = player.getInventory().getItemInMainHand().clone();
        if (writtenbook.getType() != Material.WRITTEN_BOOK) {
            return errorMessage("You must be holding a written book you can edit.");
        }

        Book book = WelcomeBook.get().bh.getBook(writtenbook.getItemMeta().getDisplayName());
        if (book == null) {
            return errorMessage("Failed to find that book. Has it been saved?");
        }
        if (book.isLocked() && (book.getAuthor() != player.getUniqueId()) || !player.hasPermission("wbr.undo.others")) {
            return errorMessage("You cannot modify this book as it was locked by it's creator.");
        }

        ItemStack writablebook = new ItemStack(Material.WRITABLE_BOOK, 1);
        BookMeta oldmeta = (BookMeta) writtenbook.getItemMeta();
        BookMeta newmeta = (BookMeta) writablebook.getItemMeta();
        newmeta.pages(oldmeta.pages());
        writablebook.setItemMeta(newmeta);
        player.getInventory().setItemInMainHand(writablebook);
        player.updateInventory();
        return successMessage("Book has been undone!");

    }
}
