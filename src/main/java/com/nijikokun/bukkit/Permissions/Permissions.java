package com.nijikokun.bukkit.Permissions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
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
import com.nijiko.data.GroupWorld;
//import com.nijiko.permissions.Group;
import com.nijiko.permissions.Entry;
import com.nijiko.permissions.Group;
import com.nijiko.permissions.ModularControl;
import com.nijiko.permissions.PermissionHandler;
//import com.nijiko.permissions.User;
import com.nijiko.permissions.User;

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

        File storageOpt = new File("plugins" + File.separator + "Permissions" + File.separator, "storageconfig.yml");
        if (!storageOpt.isFile())
            System.err.println("[Permissions] storageconfig.yml is not a file.");
        if (!storageOpt.canRead())
            System.err.println("[Permissions] storageconfig.yml cannot be read.");
        if (!storageOpt.exists())
            try {
                System.out.println("[Permissions] Creating storageconfig.yml.");
                storageOpt.createNewFile();
            } catch (IOException e) {
                System.err.println("[Permissions] storageconfig.yml could not be created.");
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
        log.info("[" + description.getName() + "] version [" + description.getVersion() + "] (" + codename + ")  loaded");

        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, l, Priority.High, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, l, Priority.High, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player player = null;
        // String commandName = command.getName().toLowerCase();
        PluginDescriptionFile pdfFile = this.getDescription();

        Messaging.save(sender);
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (args.length == 0) {
            if (player != null) {
                Messaging.send("&7-------[ &fPermissions&7 ]-------");
                Messaging.send("&7Currently running version: &f[" + pdfFile.getVersion() + "] (" + codename + ")");

                if (Security.has(player.getWorld().getName(), player.getName(), "permissions.reload")) {
                    Messaging.send("&7Reload with: &f/permissions &a-reload &e<world>");
                    Messaging.send("&fLeave &e<world> blank to reload default world.");
                }

                Messaging.send("&7-------[ &fPermissions&7 ]-------");
                return true;
            } else {
                sender.sendMessage("[" + pdfFile.getName() + "] version [" + pdfFile.getVersion() + "] (" + codename + ")  loaded");
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("-reload") && args.length >= 2) {
            return reload(sender, args[1]);
        } else if (args[0].equalsIgnoreCase("-list")) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("worlds")) {
                    Set<String> worlds = Security.getWorlds();
                    String text = "";
                    if (worlds.isEmpty()) {
                        text = "&4[Permissions] No worlds loaded.";
                    } else {
                        text = "&a[Permissions] Loaded worlds: &b";
                        for (String world : worlds) {
                            text = text + world + " ,";
                        }
                        text = text.substring(0, text.length() - 2);
                    }
                    Messaging.send(text);
                    return true;
                }
                if (args.length > 2) {
                    String world = args[2];
                    if (args[1].equalsIgnoreCase("users")) {
                        Collection<User> users = Security.getUsers(world);
                        Messaging.send(listEntries(users, "Users"));
                        return true;
                    } else if (args[1].equalsIgnoreCase("groups")) {
                        Collection<Group> groups = Security.getGroups(world);
                        Messaging.send(listEntries(groups, "Groups"));
                        return true;
                    }
                }
            }
            Messaging.send("&7[Permissions] Syntax: ");
            Messaging.send("&b/permissions &a-list &eworlds.");
            Messaging.send("&b/permissions &a-list &e[users|groups] &d<world>.");
            return true;
        }

        // This part is for selecting the appropriate entry (/pr (g:)<entryname>
        // (w:<world>) ...)
        int currentArg = 0;
        boolean isGroup = args[0].startsWith("g:");
        String name = isGroup ? args[0].substring(2) : args[0];
        currentArg++;
        String world = sender instanceof Player ? ((Player) sender).getWorld().getName() : null;
        if (args.length > currentArg && args[currentArg].startsWith("w:")) {
            world = args[currentArg].substring(2);
            currentArg++;
        }
        if (world == null) {
            Messaging.send("&4[Permissions] No world specified.");
            return true;
        }
        Entry entry = isGroup ? Security.getGroupObject(world, name) : Security.getUserObject(world, name);
        // Note that entry may be null if the user/group doesn't exist
        if (args.length > currentArg) {
            if (args[currentArg].equalsIgnoreCase("create")) {
                if (entry != null) {
                    Messaging.send("&4[Permissions] User/Group already exists.");
                    return true;
                }
                try {
                    entry = isGroup ? Security.safeGetGroup(world, name) : Security.safeGetUser(world, name);
                } catch (Exception e) {
                    e.printStackTrace();
                    Messaging.send("&4[Permissions] Error creating user/group.");
                    return true;
                }
                Messaging.send("&7[Permissions] User/Group created.");
                return true;
            } else if (entry == null) {
                Messaging.send("&4[Permissions] User/Group does not exist.");
                return true;
            } else if (args[currentArg].equalsIgnoreCase("perms")) {
                currentArg++;
                if (args.length > currentArg) {
                    if (args[currentArg].equalsIgnoreCase("list")) {
                        Set<String> perms = entry.getPermissions();
                        String text = "";
                        if (perms == null || perms.isEmpty()) {
                            text = "&4[Permissions] User/Group has no non-inherited permissions.";
                        } else {
                            for (String perm : perms) {
                                text = text + perm + ",";
                            }
                            text = text.substring(0, text.length() - 1);
                        }
                        Messaging.send(text);
                        return true;
                    } else if (args[currentArg].equalsIgnoreCase("add") || args[currentArg].equalsIgnoreCase("remove")) {
                        boolean add = args[currentArg].equalsIgnoreCase("add");
                        currentArg++;
                        String text = add ? "&7[Permissions]&b Permission added successfully." : "&7[Permissions]&b Permission removed successfully.";
                        if (args.length > currentArg) {
                            String permission = args[currentArg];
                            Set<String> perms = entry.getPermissions();
                            if (!(perms.contains(permission) ^ add))
                                text = "&4[Permissions] User/Group already has that permission.";
                            else
                                entry.setPermission(permission, add);
                        }
                        Messaging.send(text);
                        return true;
                    }
                }
                Messaging.send("&7[Permissions] Syntax: ");
                Messaging.send("&b/permissions &a(g:)<target> (w:<world>) perms list.");
                Messaging.send("&b/permissions &a(g:)<target> (w:<world>) perms [add|remove] <node>.");
                return true;
            } else if (args[currentArg].equalsIgnoreCase("parents")) {
                currentArg++;
                if (args.length > currentArg) {
                    if (args[currentArg].equalsIgnoreCase("list")) {
                        LinkedHashSet<GroupWorld> parents = entry.getParents();
                        String text = "&7[Permissions]&b Parents: ";
                        if (parents == null || parents.isEmpty()) {
                            text = "&4[Permissions] User/Group has no parents.";
                        } else {
                            for (GroupWorld parent : parents) {
                                text = text + parent.toString() + " ,";
                            }
                            text = text.substring(0, text.length() - 2);
                        }
                        Messaging.send(text);
                        return true;
                    } else if (args[currentArg].equalsIgnoreCase("add") || args[currentArg].equalsIgnoreCase("remove")) {
                        boolean add = args[currentArg].equalsIgnoreCase("add");
                        currentArg++;
                        String text = add ? "&7[Permissions]&b Parent added successfully." : "&7[Permissions]&b Parent removed successfully.";
                        if (args.length > currentArg) {
                            String parentName = args[currentArg];
                            String parentWorld = world;
                            if (args.length > (++currentArg)) {
                                parentWorld = args[currentArg];
                            }
                            LinkedHashSet<GroupWorld> parents = entry.getParents();
                            if (add && parents.contains(new GroupWorld(parentWorld, parentName)))
                                text = "&4[Permissions] User/Group already has that parent.";
                            if (!add && !parents.contains(new GroupWorld(parentWorld, parentName)))
                                text = "&4[Permissions] User/Group does not have such a parent.";
                            else {
                                Group parent = Security.getGroupObject(parentWorld, parentName);
                                if (parent == null) {
                                    text = "&4[Permissions] No such group exists.";
                                } else {
                                    if (add)
                                        entry.addParent(parent);
                                    else
                                        entry.removeParent(parent);
                                }
                            }
                        }
                        Messaging.send(text);
                        return true;
                    }
                }
                Messaging.send("&7[Permissions] Syntax: ");
                Messaging.send("&b/permissions &a(g:)<target> (w:<world>) perms list.");
                Messaging.send("&b/permissions &a(g:)<target> (w:<world>) perms [add|remove] <node>.");
                return true;
            }
            if (isGroup && entry instanceof Group)// Just in case
            {
                Group group = (Group) entry;
                if (args[currentArg].equalsIgnoreCase("prefix") || args[currentArg].equalsIgnoreCase("suffix")) {
                    boolean isPrefix = args[currentArg].equalsIgnoreCase("prefix");
                    currentArg++;
                    if (args.length > currentArg) {
                        if (args[currentArg].equalsIgnoreCase("get")) {
                            String text = isPrefix ? "&7[Permissions]&b " + group.getName() + "'s prefix:" : "&7[Permissions]&b " + group.getName() + "'s suffix:";
                            Messaging.send(text);
                            text = isPrefix ? group.getPrefix() : group.getSuffix();
                            Messaging.send( "\""+text+"\"");
                            return true;
                        } else if (args[currentArg].equalsIgnoreCase("set")) {
                            currentArg++;
                            String newFix = "";
                            if (args.length > currentArg) {
                                String[] fullFix = Arrays.copyOfRange(args, currentArg, args.length);
                                for (String part : fullFix) {
                                    newFix = newFix + part + " ";
                                }
                                newFix = newFix.substring(0, newFix.length() - 1); //Possible bug
                            }
                            if (isPrefix)
                                group.setPrefix(newFix);
                            else
                                group.setSuffix(newFix);
                            String text = isPrefix ? "&7[Permissions]&b Group's prefix set to " + newFix + ".": "&7[Permissions]&7 Group's suffix set to " + newFix + ".";
                            Messaging.send(text);
                            return true;
                        }
                    }
                } else if (args[currentArg].equalsIgnoreCase("build")) {
                    currentArg++;
                    if (args.length > currentArg) {
                        if (args[currentArg].equalsIgnoreCase("get")) {
                            if (group.canBuild())
                                Messaging.send("&7[Permissions]&b " + group.getName() + " can build.");
                            else
                                Messaging.send("&7[Permissions]&b "+group.getName() + " cannot build.");
                            return true;
                        } else if (args[currentArg].equalsIgnoreCase("set")) {
                            currentArg++;
                            if (args.length > currentArg) {
                                String bool = args[currentArg];
                                boolean build = Boolean.parseBoolean(bool);
                                group.setBuild(build);
                                Messaging.send("&7[Permissions]&b" + group.getName() + "'s build setting was set to " + Boolean.toString(build));
                                return true;
                            }

                            Messaging.send("&7[Permissions] Syntax: &b/permissions &ag:<target> (w:<world>) build set <true|false>.");
                            return true;
                        }
                    }
                }
            } else if (entry instanceof User) {
                User user = (User) entry;
                if (args[currentArg].equalsIgnoreCase("promote") || args[currentArg].equalsIgnoreCase("demote")) {
                    boolean isPromote = args[currentArg].equalsIgnoreCase("promote");
                    currentArg++;
                    if(args.length > currentArg)
                    {
                        String parentName = args[currentArg];
                        String parentWorld = world;
                        if (args.length > currentArg && args[currentArg].startsWith("w:")) {
                            world = args[currentArg].substring(2);
                            currentArg++;
                        }
                        Group group = Security.getGroupObject(parentWorld, parentName);
                        if(group==null)
                        {
                            Messaging.send("&4[Permissions] No such group.");
                            return true;
                        }
                        if(!user.inGroup(parentWorld, parentName))
                        {
                            Messaging.send("&4[Permissions] User not in specified group.");
                            return true;
                        }
                        if(args.length > currentArg)
                        {
                            String track = args[currentArg];
                            if(!group.getTracks().contains(track))
                            {
                                Messaging.send("&4[Permissions] Specified track does not exist.");
                                return true;
                            }
                            if(isPromote) user.promote(group, track);
                            else user.demote(group, track);
                            String text = isPromote ? "&7[Permissions]&b User promoted along track " + track + ".": "&7[Permissions]&7 User demoted along track " + track + ".";
                            Messaging.send(text);
                        }
                    }
                    // /pr <target> (w:<world>) [promote|demote] <parent> (w:<parentworld>) <track>
                }
            }
        }

        return false;
    }

    private boolean reload(CommandSender sender, String arg) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (Security.has(p.getWorld().getName(), p.getName(), "permissions.reload")) {
                p.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
                return true;
            }
        }

        if (arg == null || arg.equals("")) {
            Security.reload(DefaultWorld);
            sender.sendMessage(ChatColor.GRAY + "[Permissions] Default world reloaded.");
            return true;
        }

        if (arg.equalsIgnoreCase("all")) {
            Security.reload();
            sender.sendMessage(ChatColor.GRAY + "[Permissions] All worlds reloaded.");
            return true;
        }

        if (Security.reload(arg))
            sender.sendMessage(ChatColor.GRAY + "[Permissions] Reload of World " + arg + " completed.");
        else
            sender.sendMessage(ChatColor.GRAY + "[Permissions] World " + arg + " does not exist.");
        return true;

    }

    @Override
    public String toString() {
        return name + " version " + version + " (" + codename + ")";
    }

    private String listEntries(Collection<? extends Entry> entries, String type) {
        String text = "";
        if (entries == null) {
            text = "&4[Permissions] World does not exist.";
        } else if (entries.isEmpty()) {
            text = "&4[Permissions] No " + type.toLowerCase() + " in that world.";
        } else {
            text = "&a[Permissions] " + type + ": &b";
            for (Entry entry : entries) {
                text = text + entry.getName() + ", ";
            }
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }
}
