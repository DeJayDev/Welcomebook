package com.fuzzoland.welcomebook;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

public class Book {

    private String title;
    private UUID author;
    private List<String> pages;
    private final Map<String, Long> cooldowns;
    private boolean locked;

    public Book(String title, UUID author, List<String> pages, Map<String, Long> cooldowns) {
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.cooldowns = cooldowns;
    }

    public Book(String title, UUID author, List<String> pages, Map<String, Long> cooldowns, Boolean locked) {
        this(title, author, pages, cooldowns);
        this.locked = locked;
    }

    public void save(String codename) throws IOException {
        YamlConfiguration book = new YamlConfiguration();
        book.set("Title", this.title);
        book.set("Author", this.author);
        book.set("Pages", this.pages);
        for (Entry<String, Long> entrySet : cooldowns.entrySet()) {
            book.set("Cooldowns." + entrySet.getKey(), entrySet.getValue());
        }
        book.save(new File(WelcomeBook.datafolder + "/books", codename + ".yml"));
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getAuthor() {
        return this.author;
    }

    public void setAuthor(UUID author) {
        this.author = author;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<String> getPages() {
        return pages;
    }

    public void setPages(List<String> pages) {
        this.pages = pages;
    }

    public boolean hasCooldown(String player) {
        return cooldowns.containsKey(player);
    }

    public long getCooldown(String player) {
        return cooldowns.get(player);
    }

    public void updateCooldown(String player, Long cooldown) {
        cooldowns.put(player, cooldown);
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle(getTitle());
        meta.setAuthor(Bukkit.getServer().getPlayer(getAuthor()).getName());
        meta.setGeneration(Generation.COPY_OF_ORIGINAL);
        if (getPages().size() == 0) {
            meta.addPages(Component.text("Your server admin defined this book without pages! Have them modify: " + getTitle()));
        }
        for (String page : getPages()) {
            meta.addPages(LegacyComponentSerializer.legacyAmpersand().deserialize(page));
        }
        item.setItemMeta(meta);
        return item;
    }

}
