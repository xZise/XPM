package com.nijikokun.bukkit.Permissions;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.Messaging;
import com.nijiko.Misc;
import com.nijiko.configuration.NotNullConfiguration;
//import com.nijiko.permissions.Group;
import com.nijiko.permissions.ModularControl;
import com.nijiko.permissions.PermissionHandler;
//import com.nijiko.permissions.User;

/**
 * Permissions 2.x Copyright (C) 2011 Matt 'The Yeti' Burnett
 * <admin@theyeticave.net> Original Credit & Copyright (C) 2010 Nijikokun
 * <nijikokun@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Permissions Public License as published by the Free
 * Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Permissions Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Permissions Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class Permissions extends JavaPlugin {

    public static Logger log = Logger.getLogger("Minecraft");
    public static PluginDescriptionFile description;
    public static Plugin instance;
    public static Server Server = null;
    public File directory;
    private Configuration storageConfig;
    public static String name = "Permissions";
    public static String version = "3.0";
    public static String codename = "Yeti";

    public Listener l = new Listener(this);

    /**
     * Controller for permissions and security.
     */
    public static PermissionHandler Security;

    /**
     * Miscellaneous object for various functions that don't belong anywhere
     * else
     */
    public static Misc Misc = new Misc();

    private String DefaultWorld = "";

    public Permissions() {

        PropertyHandler server = new PropertyHandler("server.properties");
        DefaultWorld = server.getString("level-name");

        File storageOpt = new File("plugins" + File.separator + "Permissions"
                + File.separator, "storageconfig.yml");
        if (!storageOpt.isFile())
            System.err
                    .println("[Permissions] storageconfig.yml is not a file.");
        if (!storageOpt.canRead())
            System.err
                    .println("[Permissions] storageconfig.yml cannot be read.");
        if (!storageOpt.exists())
            try {
                System.out.println("[Permissions] Creating storageconfig.yml.");
                storageOpt.createNewFile();
            } catch (IOException e) {
                System.err
                        .println("[Permissions] storageconfig.yml could not be created.");
                e.printStackTrace();
            }
        Configuration storageConfig = new NotNullConfiguration(storageOpt);
        storageConfig.load();
        this.storageConfig = storageConfig;

        instance = this;

        // Enabled
        log.info("[Permissions] (" + codename + ") was Initialized.");
    }

    @Override
    public void onLoad() {
        // Setup Permission
        getDataFolder().mkdirs();
        setupPermissions();
    }

    @Override
    public void onDisable() {
        // Addition by rcjrrjcr
        Security.saveAll();
        log.info("[Permissions] (" + codename + ") saved all data.");
        // End of addition by rcjrrjcr

        log.info("[Permissions] (" + codename + ") disabled successfully.");
        return;
    }

    /**
     * Alternative method of grabbing Permissions.Security <br />
     * <br />
     * <blockquote>
     * 
     * <pre>
     * Permissions.getHandler()
     * </pre>
     * 
     * </blockquote>
     * 
     * @return PermissionHandler
     */
    public PermissionHandler getHandler() {
        return Permissions.Security;
    }

    public void setupPermissions() {
        Security = new ModularControl(storageConfig);
        Security.setDefaultWorld(DefaultWorld);
        try {
            Security.load();
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        Server = this.getServer();
        description = this.getDescription();
        directory = getDataFolder();

        // Enabled
        log.info("[" + description.getName() + "] version ["
                + description.getVersion() + "] (" + codename + ")  loaded");

        this.getServer().getPluginManager()
                .registerEvent(Event.Type.BLOCK_PLACE, l, Priority.High, this);
        this.getServer().getPluginManager()
                .registerEvent(Event.Type.BLOCK_BREAK, l, Priority.High, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args) {
        Player player = null;
        String commandName = command.getName().toLowerCase();
        PluginDescriptionFile pdfFile = this.getDescription();

        if (sender instanceof Player) {
            player = (Player) sender;

            Messaging.save(player);
        }

        if (commandName.compareToIgnoreCase("permissions") == 0) {
            if (args.length < 1) {
                if (player != null) {
                    Messaging.send("&7-------[ &fPermissions&7 ]-------");
                    Messaging.send("&7Currently running version: &f["
                            + pdfFile.getVersion() + "] (" + codename + ")");

                    if (Security.has(player, "permissions.reload")) {
                        Messaging
                                .send("&7Reload with: &f/permissions -reload [World]");
                        Messaging
                                .send("&fLeave [World] blank to reload default world.");
                    }

                    Messaging.send("&7-------[ &fPermissions&7 ]-------");
                    return true;
                } else {
                    sender.sendMessage("[" + pdfFile.getName() + "] version ["
                            + pdfFile.getVersion() + "] (" + codename
                            + ")  loaded");
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("-reload") && args.length >= 2) {
                return reload(sender, args[1]);
            }
            else if(args[0].equalsIgnoreCase("has") && args.length == 4)
            {
                String world = args[1];
                String user = args[2];
                String node = args[3];
                sender.sendMessage(String.valueOf(Security.permission(world, user, node)));
                return true;
            }
//            else if(args[0].equals("user") && args.length > 3)
//            {
//                User user = null;
//                try {
//                    user = Security.safeGetUser(args[2], args[1]);
//                } catch (Exception e) {
//                    sender.sendMessage(ChatColor.RED + "Error retrieving user data.");
//                    e.printStackTrace();
//                    return true;
//                }
//                //Process the rest of the command
//            }
//            else if(args[0].equals("group") && args.length > 3)
//            {
//                Group group = null;
//                try {
//                    group = Security.safeGetGroup(args[2], args[1]);
//                } catch (Exception e) {
//                    sender.sendMessage(ChatColor.RED + "Error retrieving user data.");
//                    e.printStackTrace();
//                    return true;
//                }
//                //Process the rest of the command
//            }
            //Add new commands here
        }
        return false;
    }

    private boolean reload(CommandSender sender, String arg) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (Security.has(p, "permissions.reload")) {
                p.sendMessage(ChatColor.RED
                        + "[Permissions] You lack the necessary permissions to perform this action.");
                return true;
            }
        }

        if (arg == null || arg.equals("")) {
            Security.reload(DefaultWorld);
            sender.sendMessage(ChatColor.GRAY
                    + "[Permissions] Default world reloaded.");
            return true;
        }

        if (arg.equalsIgnoreCase("all")) {
            sender.sendMessage(ChatColor.GRAY
                    + "[Permissions] All worlds reloaded.");
            return true;
        }

        if (Security.reload(arg))
            sender.sendMessage(ChatColor.GRAY
                    + "[Permissions] Reload of World " + arg + " completed.");
        else
            sender.sendMessage(ChatColor.GRAY + "[Permissions] World " + arg
                    + " does not exist.");
        return true;

    }
    
    @Override
    public String toString()
    {
        return name + " version " + version + " (" + codename + ")";
    }
    
    
}
