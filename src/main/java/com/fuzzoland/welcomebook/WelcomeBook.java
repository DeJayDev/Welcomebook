package com.fuzzoland.welcomebook;

import com.fuzzoland.welcomebook.commands.CommandGB;
import com.fuzzoland.welcomebook.commands.CommandListener;
import com.fuzzoland.welcomebook.commands.CommandWBR;
import com.fuzzoland.welcomebook.commands.CopyrightCommand;
import com.fuzzoland.welcomebook.commands.UnsignCommand;
import com.fuzzoland.welcomebook.listener.JoinListener;
import com.fuzzoland.welcomebook.listener.ShopListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class WelcomeBook extends JavaPlugin {

    public static File datafolder;
    public BookHandler bh;
    public Economy eco;
    public YamlConfiguration settings;

    @Override
    public void onEnable() {
        File settingsFile = new File(getDataFolder(), "settings.yml");
        if (!settingsFile.exists()) {
            settingsFile.getParentFile().mkdirs();
            saveResource("settings.yml", false);
        }
        settings = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "settings.yml"));
        File booksFolder = new File(getDataFolder() + "/books");
        if (!booksFolder.exists()) {
            booksFolder.mkdirs();
        }
        bh = new BookHandler(booksFolder, getLogger());
        try {
            bh.loadBooks();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        if (settings.getBoolean("BookShop.Enabled")) {
            setupEconomy();
        }
        if (settings.getBoolean("CustomCommands.Enabled")) {
            getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        }
        getLogger().info("Events registered!");
        getCommand("wbr").setExecutor(new CommandWBR(this));
        getCommand("gb").setExecutor(new CommandGB(this));
        new CopyrightCommand(this);
        new UnsignCommand(this);
        getLogger().info("Commands registered!");
        datafolder = getDataFolder();
    }

    @Override
    public void onDisable() {
        bh.saveBooks();
    }

    private void setupEconomy() {
        getLogger().info("You have chosen to enable the BookShops feature.");
        getLogger().info("Setting up economy support...");
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Failed to set up economy support - Vault not found!");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Failed to set up economy support - a valid economy plugin was not found!");
            return;
        }
        eco = rsp.getProvider();
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getLogger().info("Successfully set up economy support!");
    }

    public BookHandler getBookHandler() {
        return bh;
    }

    public void giveBooks(Player player, List<String> books) {
        for (String b : books) {
            giveBook(player, b, false);
        }
        player.updateInventory();
    }

    public void giveBook(Player player, String bookString) {
        giveBook(player, bookString, true);
    }

    public void giveBook(Player player, String bookString, boolean update) {
        if (bh.isBook(bookString)) {
            Book book = bh.getBook(bookString);
            player.getInventory().addItem(book.toItemStack());
            if (update) {
                player.updateInventory();
            }
        }
    }

    public String convertTime(Long millis) {
        if (millis >= 86400000) {
            return convertToDays(millis);
        } else if (millis >= 3600000) {
            return convertToHours(millis);
        } else if (millis >= 60000) {
            return convertToMinutes(millis);
        } else if (millis >= 1000) {
            return convertToSeconds(millis);
        } else {
            return "now";
        }
    }

    private String convertToSeconds(Long millis) {
        return (int) ((millis / 1000) % 60) + "s";
    }

    private String convertToMinutes(Long millis) {
        return (int) ((millis / 60000) % 60) + "m, " + (int) ((millis / 1000) % 60) + "s";
    }

    private String convertToHours(Long millis) {
        return (int) ((millis / 3600000) % 24) + "h, " + (int) ((millis / 60000) % 60) + "m, " + (int) ((millis / 1000) % 60) + "s";
    }

    private String convertToDays(Long millis) {
        return (int) ((millis / 86400000) % 365) + "d, " + (int) ((millis / 3600000) % 24) + "h, " + (int) ((millis / 60000) % 60) + "m, " + (int) ((millis / 1000) % 60) + "s";
    }

    public static WelcomeBook get() {
        return JavaPlugin.getPlugin(WelcomeBook.class);
    }

}
