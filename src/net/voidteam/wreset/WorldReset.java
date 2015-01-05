package net.voidteam.wreset;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Robby Duke on 1/4/15.
 * Copyright (c) 2015
 */
public class WorldReset extends JavaPlugin {
    public static MultiverseCore mvCore;

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();

        /**
         * Save the Multiverse Plugin for easy access...
         */
        mvCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

        /**
         * Schedule the reset tasks.
         */
        LinkedHashSet<String> scheduledKeys = (LinkedHashSet) getConfig().getConfigurationSection("scheduledWorlds").getKeys(false);
        for (String key : scheduledKeys) {
            List<Integer> days = getConfig().getIntegerList("scheduledWorlds." + key + ".scheduledDays");
            int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

            long lastBackup = getConfig().getLong("scheduledWorlds." + key + ".lastBackup");

            for (Integer day : days) {
                if (day < currentDay) {
                    continue;
                } else {
                    if (day == currentDay) {
                        if (System.currentTimeMillis() - 86400000 > lastBackup) {
                            Bukkit.getLogger().info("PASS 1");
                            /**
                             * Check if the world exists so we don't get a NullPointer, if it does then we just go ahead and run the
                             * scheduler so it does not lock the main thread.
                             */
                            if (!WorldReset.mvCore.getMVWorldManager().isMVWorld(key)) {
                                Bukkit.getLogger().severe(ChatColor.RED + "The world " + key + " is not owned by Multiverse!");
                            } else if (!getConfig().getStringList("whitelistedWorlds").contains(key)) {
                                Bukkit.getLogger().severe(ChatColor.RED + "The world " + key + " is not whitelisted!");
                            } else {
                                Bukkit.broadcastMessage(key + " is being reset, please wait...");
                                Bukkit.getScheduler().runTaskAsynchronously(this, new ResetTask(key, null));
                            }
                        } else {
                            Bukkit.getLogger().info("Already ran a reset on " + key + " today...");
                        }
                    } else {
                        Bukkit.getLogger().info("PASS 2");
                        /**
                         * Check if the world exists so we don't get a NullPointer, if it does then we just go ahead and run the
                         * scheduler so it does not lock the main thread.
                         */
                        if (!WorldReset.mvCore.getMVWorldManager().isMVWorld(key)) {
                            Bukkit.getLogger().severe(ChatColor.RED + "The world " + key + " is not owned by Multiverse!");
                        } else if (!getConfig().getStringList("whitelistedWorlds").contains(key)) {
                            Bukkit.getLogger().severe(ChatColor.RED + "The world " + key + " is not whitelisted!");
                        } else {
                            Bukkit.broadcastMessage(key + " is being reset, please wait...");

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            Date d = null;
                            try {
                                d = sdf.parse(day + "/" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "/" + (Calendar.getInstance().get(Calendar.YEAR) + 1));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            Bukkit.getScheduler().runTaskAsynchronously(this, new ResetTask(key, d));
                        }
                    }
                }
            }
        }
    }

    /**
     * It's only one command, so let me just nonchalantly shove this bad boy into the plugin class.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Used to unload, then save an archive, then delete, and finally create a new world with the same name.");
        } else if (args.length == 1) {
            /**
             * Quick little permissions check, they must be an operator to use this command.
             */
            if (!sender.hasPermission("worldreset." + args[0])) {
                sender.sendMessage(ChatColor.RED + "Sorry, you must be a server operator to run this command.");
            } else {
                /**
                 * Check if the world exists so we don't get a NullPointer, if it does then we just go ahead and run the
                 * scheduler so it does not lock the main thread.
                 */
                if (!WorldReset.mvCore.getMVWorldManager().isMVWorld(args[0])) {
                    sender.sendMessage(ChatColor.RED + "Sorry, but this world is not owned by Multiverse.");
                } else if (!getConfig().getStringList("whitelistedWorlds").contains(args[0])) {
                    sender.sendMessage(ChatColor.RED + "This world is protected and cannot be reset.");
                } else {
                    Bukkit.broadcastMessage(args[0] + " is being reset, please wait...");
                    Bukkit.getScheduler().runTaskAsynchronously(this, new ResetTask(args[0], null));
                }
            }
        } else {
            return false;
        }

        return true;
    }
}
