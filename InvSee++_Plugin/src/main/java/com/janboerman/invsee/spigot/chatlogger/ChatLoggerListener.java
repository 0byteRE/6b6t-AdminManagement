package com.janboerman.invsee.spigot.chatlogger;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs player chat messages with rich context for LORA AI training.
 */
public class ChatLoggerListener implements Listener {

    private final InvseePlusPlus plugin;
    private final Scheduler scheduler;
    private final File logFile;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ChatLoggerListener(InvseePlusPlus plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;

        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.logFile = new File(folder, "LoraMemory.txt");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        scheduler.executeSyncPlayer(player.getUniqueId(), () -> log(player, message), null);
    }

    private void log(Player player, String message) {
        String timestamp = formatter.format(new Date());
        World world = player.getWorld();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        GameMode gamemode = player.getGameMode();
        String username = player.getName();

        PlayerInventory inv = player.getInventory();
        String held = formatItem(inv.getItemInMainHand());
        String helmet = formatItem(inv.getHelmet());
        String chest = formatItem(inv.getChestplate());
        String legs = formatItem(inv.getLeggings());
        String boots = formatItem(inv.getBoots());

        String line = String.format(
                "[Time: %s] [World: %s] [X: %d, Y: %d, Z: %d] [Gamemode: %s] [Player: %s] [Held: %s] [Armor: %s, %s, %s, %s] %s",
                timestamp, world.getName(), x, y, z, gamemode.name(), username,
                held, helmet, chest, legs, boots, message);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            plugin.getLogger().warning("Could not write chat log: " + e.getMessage());
        }
    }

    private String formatItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "None";
        }
        return item.getType().toString();
    }
}

