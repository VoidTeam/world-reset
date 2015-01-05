package net.voidteam.wreset;

import com.onarandombox.MultiverseCore.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Robby Duke on 1/4/15.
 * Copyright (c) 2015
 */
public class ResetTask implements Runnable {
    private final String worldName;
    private final Date when;

    public ResetTask(String worldName, Date when) {
        this.worldName = worldName;
        this.when = when;
    }

    @Override
    public void run() {
        boolean shouldRun = true;

        while (shouldRun) {
            if (when == null || when.before(Calendar.getInstance().getTime())) {
                if (Bukkit.getWorld(worldName).getPlayers().size() > 0) {
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("WorldReset"), new Runnable() {
                        @Override
                        public void run() {
                            String teleportWorld = Bukkit.getPluginManager().getPlugin("WorldReset").getConfig().getString("scheduledWorlds." + worldName + ".teleportLocation");
                            final Location teleportLocation = Bukkit.getWorld(teleportWorld).getSpawnLocation();
                            for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
                                player.teleport(teleportLocation);
                                player.sendMessage("Sorry, but the world you were in is being reset.");
                            }
                        }
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }

                    continue;
                }

                if (WorldReset.mvCore.getMVWorldManager().unloadWorld(worldName)) {

                    try {
                        /**
                         * Create the backup directories if they do not exist.
                         */
                        File backupsDirectory = new File(String.format("plugins%sWorldReset%sarchive%s%s", File.separator, File.separator, File.separator, worldName));

                        if (!backupsDirectory.exists()) {
                            backupsDirectory.mkdirs();
                        }

                        /**
                         * Make a ZIP file.
                         */
                        ZipDirectory.zipDirectory(
                                String.format("%s", worldName), // Source directory
                                String.format("%s%s%s.zip", backupsDirectory.getAbsolutePath(), File.separator, System.currentTimeMillis()) // Target file
                        );
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        break;
                    }

                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("WorldReset"), new Runnable() {
                        @Override
                        public void run() {
                            /**
                             * Let Multiverse cleanup.
                             */
                            WorldReset.mvCore.getMVWorldManager().deleteWorld(worldName);


                            /**
                             * Delete the world directory so Multiverse can regenerate it safely.
                             */
                            File worldDirectory = new File(String.format("%s", worldName));
                            if (worldDirectory.exists()) {
                                FileUtils.deleteFolder(worldDirectory);
                            }

                            /**
                             * Create the new world.
                             */
                            Bukkit.getServer().dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    String.format("mv create %s normal", worldName)
                            );

                            Bukkit.broadcastMessage(
                                    String.format("%s was successfully reset and is now ready to be used again!", worldName)
                            );

                            Bukkit.getPluginManager().getPlugin("WorldReset").getConfig().set("scheduledWorlds." + worldName + ".lastBackup", System.currentTimeMillis());
                            Bukkit.getPluginManager().getPlugin("WorldReset").saveConfig();
                        }
                    });
                } else {
                    Bukkit.broadcastMessage(
                            String.format("%s was not able to be reset...", worldName)
                    );
                }

                shouldRun = false;
            } else {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
