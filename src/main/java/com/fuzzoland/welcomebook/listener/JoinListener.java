package com.fuzzoland.welcomebook.listener;

import com.fuzzoland.welcomebook.WelcomeBook;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final WelcomeBook plugin;

    public JoinListener(WelcomeBook plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            if (plugin.settings.getBoolean("EveryJoin.Enabled")) {
                List<String> books = plugin.settings.getStringList("EveryJoin.Books");
                WelcomeBook.get().giveBooks(player, books);
            }
        } else {
            if (plugin.settings.getBoolean("FirstJoin.Enabled")) {
                List<String> books = plugin.settings.getStringList("FirstJoin.Books");
                WelcomeBook.get().giveBooks(player, books);
            }
        }
    }

}
