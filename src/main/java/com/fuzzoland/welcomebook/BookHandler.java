package com.fuzzoland.welcomebook;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

public class BookHandler {

    private final Map<String, Book> books = new HashMap<>();
    private final File dir;
    private final Logger logger;

    public BookHandler(File dir, Logger logger) {
        this.dir = dir;
        this.logger = logger;
    }

    public void loadBooks() throws IOException {
        int count = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (file.getCanonicalPath().endsWith(".yml")) {
                    YamlConfiguration book = YamlConfiguration.loadConfiguration(file);
                    String bookn = file.getName();
                    Map<String, Long> cooldowns = new HashMap<>();
                    if (book.isSet("Cooldowns")) {
                        for (String player : book.getConfigurationSection("Cooldowns").getKeys(false)) {
                            cooldowns.put(player, book.getLong("Cooldowns." + player));
                        }
                    }
                    books.put(bookn.substring(0, bookn.lastIndexOf('.')), new Book(book.getString("Title"), UUID.fromString(book.getString("Author")), (List<String>) book.getList("Pages"), cooldowns));
                    count++;
                }
            }
        }
        logger.log(Level.INFO, "[WelcomeBook] " + count + " book(s) loaded!");
    }

    public void saveBooks() {
        for (File file : dir.listFiles()) {
            try {
                if (file.isFile()) {
                    if (file.getCanonicalPath().endsWith(".yml")) {
                        file.delete();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int count = 0;
        for (Entry<String, Book> entrySet : books.entrySet()) {
            try {
                entrySet.getValue().save(entrySet.getKey());
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        }
        logger.log(Level.INFO, "[WelcomeBook] " + count + " book(s) saved!");
    }

    public Boolean isBook(String codename) {
        return books.containsKey(codename);
    }

    public Set<String> getBookNames() {
        return books.keySet();
    }

    public Collection<Book> getBooks() {
        return books.values();
    }

    public Book getBook(String codename) {
        return books.getOrDefault(codename, null);
    }

    public void updateBook(String codename, Book book) {
        if (book == null) {
            books.remove(codename);
        } else {
            books.put(codename, book);
        }
    }

}
