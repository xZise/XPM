package com.nijikokun.bukkit.Permissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.Messaging;
import com.nijiko.configuration.NotNullConfiguration;
import com.nijiko.data.GroupWorld;
import com.nijiko.permissions.Entry;
import com.nijiko.permissions.Group;
import com.nijiko.permissions.ModularControl;
import com.nijiko.permissions.PermissionHandler;
import com.nijiko.permissions.User;

/**
 * Permissions 3.x Copyright (C) 2011 Matt 'The Yeti' Burnett <admin@theyeticave.net> 
 * Original Credit & Copyright (C) 2010 Nijikokun <nijikokun@gmail.com>
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

    public static Logger log;
    public static Plugin instance;
    private Configuration storageConfig;
    public static final String name = "Permissions";
    public static final String version = "3.0";
    public static final String codename = "Yeti";

    public Listener l = new Listener(this);

    /**
     * Controller for permissions and security.
     */
    public static PermissionHandler Security;

//    /**
//     * Miscellaneous object for various functions that don't belong anywhere
//     * else
//     */
//    public static Misc Misc = new Misc();

    private String defaultWorld = "";

//    public Permissions() {
//    }

    @Override
    public void onLoad() {
        instance = this;
        log = Logger.getLogger("Minecraft");
        Properties prop = new Properties();
        FileInputStream in;
        try {
            in = new FileInputStream(new File("server.properties"));
            prop.load(in);
            defaultWorld = prop.getProperty("level-name");
        } catch (IOException e) {
            System.err.println("[Permissions] Unable to read default world's name from server.properties.");
            e.printStackTrace();
            defaultWorld = "world";
        }
//        PropertyHandler server = new PropertyHandler("server.properties");
//        defaultWorld = server.getString("level-name");

        File storageOpt = new File("plugins" + File.separator + "Permissions" + File.separator, "storageconfig.yml");
        storageOpt.getParentFile().mkdirs();
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

        // Setup Permission
        getDataFolder().mkdirs();
        setupPermissions();

        // Enabled
        log.info("[Permissions] (" + codename + ") was initialized.");
    }

    @Override
    public void onDisable() {
        // Addition by rcjrrjcr
        Security.closeAll();
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
        Security.setDefaultWorld(defaultWorld);
        try {
            Security.load();
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
        getServer().getServicesManager().register(PermissionHandler.class, Security, this, ServicePriority.Normal);
    }

    @Override
    public void onEnable() {

        PluginDescriptionFile description = getDescription();
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
        Messaging msg = new Messaging(sender);
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (args.length == 0) {
            if (player != null) {
                msg.send("&7-------[ &fPermissions&7 ]-------");
                msg.send("&7Currently running version: &f[" + pdfFile.getVersion() + "] (" + codename + ")");

                if (Security.has(player.getWorld().getName(), player.getName(), "permissions.reload")) {
                    msg.send("&7Reload with: &f/permissions &a-reload &e<world>");
                    msg.send("&fLeave &e<world> blank to reload default world.");
                }

                msg.send("&7-------[ &fPermissions&7 ]-------");
                return true;
            } else {
                sender.sendMessage("[" + pdfFile.getName() + "] version [" + pdfFile.getVersion() + "] (" + codename + ")  loaded");
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("-reload")) {
            String tempWorld = "";

            if(args.length > 1)
                tempWorld = args[1];

            int currentArg = 1;
            if(tempWorld.startsWith("\"")) {
                boolean closed = false;
                tempWorld = tempWorld.substring(1);
                if(tempWorld.endsWith("\"")) {
                    tempWorld = tempWorld.substring(0, tempWorld.length() - 1);                    
                } else {
                    currentArg++;
                    while (args.length > currentArg) {
                        String part = args[currentArg];
                        closed = part.endsWith("\"");
                        if(closed) {
                            part = part.substring(0, part.length() - 1);
                        }
                        tempWorld = tempWorld + " " + part;
                        if(closed) break;
                        currentArg++;
                    }
                    if(!closed) {
                        msg.send("&4[Permissions] No ending quote found for world string.");
                        return true;
                    }
                }
            }
            String world = tempWorld;
            return reload(sender, world);
        } else if (args[0].equalsIgnoreCase("-load")) {
            String tempWorld = args[1];
            int currentArg = 1;
            if(tempWorld.startsWith("\"")) {
                boolean closed = false;
                tempWorld = tempWorld.substring(1);
                if(tempWorld.endsWith("\"")) {
                    tempWorld = tempWorld.substring(0, tempWorld.length() - 1);                    
                } else {
                    currentArg++;
                    while (args.length > currentArg) {
                        String part = args[currentArg];
                        closed = part.endsWith("\"");
                        if(closed) {
                            part = part.substring(0, part.length() - 1);
                        }
                        tempWorld = tempWorld + " " + part;
                        if(closed) break;
                        currentArg++;
                    }
                    if(!closed) {
                        msg.send("&4[Permissions] No ending quote found for world string.");
                        return true;
                    }
                }
            }
            String world = tempWorld;
            try {
                Security.forceLoadWorld(world);
            } catch (Exception e) {
                msg.send("&4[Permissions] Error occured while loading world.");
                e.printStackTrace();
                return true;
            }
            msg.send("&7[Permissions] World loaded.");
            return true;
        } else if (args[0].equalsIgnoreCase("-list")) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("worlds")) {
                    if (player != null && !Security.has(player, "permissions.list.worlds")) {
                        msg.send("&4[Permissions] You do not have permissions to use this command.");
                        return true;
                    }
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
                    msg.send(text);
                    return true;
                }
                if (args.length > 2) {
                    String tempWorld = args[2];
                    int currentArg = 2;
                    if(tempWorld.startsWith("\"")) {
                        boolean closed = false;
                        tempWorld = tempWorld.substring(1);
                        if(tempWorld.endsWith("\"")) {
                            tempWorld = tempWorld.substring(0, tempWorld.length() - 1);                    
                        } else {
                            currentArg++;
                            while (args.length > currentArg) {
                                String part = args[currentArg];
                                closed = part.endsWith("\"");
                                if(closed) {
                                    part = part.substring(0, part.length() - 1);
                                }
                                tempWorld = tempWorld + " " + part;
                                if(closed) break;
                                currentArg++;
                            }
                            if(!closed) {
                                msg.send("&4[Permissions] No ending quote found for world string.");
                                return true;
                            }
                        }
                    }
                    String world = tempWorld;
                    if (args[1].equalsIgnoreCase("users")) {
                        if (player != null && !Security.has(player, "permissions.list.users")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        Collection<User> users = Security.getUsers(world);
                        msg.send(listEntries(users, "Users"));
                        return true;
                    } else if (args[1].equalsIgnoreCase("groups")) {
                        if (player != null && !Security.has(player, "permissions.list.groups")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        Collection<Group> groups = Security.getGroups(world);
                        msg.send(listEntries(groups, "Groups"));
                        return true;
                    }
                }
            }
            msg.send("&7[Permissions] Syntax: ");
            msg.send("&b/permissions &a-list &eworlds.");
            msg.send("&b/permissions &a-list &e[users|groups] &d<world>.");
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
            String tempWorld = args[currentArg].substring(2);
            if(tempWorld.startsWith("\"")) {
                boolean closed = false;
                tempWorld = tempWorld.substring(1);
                if(tempWorld.endsWith("\"")) {
                    tempWorld = tempWorld.substring(0, tempWorld.length() - 1);                    
                } else {
                    currentArg++;
                    while (args.length > currentArg) {
                        String part = args[currentArg];
                        closed = part.endsWith("\"");
                        if(closed) {
                            part = part.substring(0, part.length() - 1);
                        }
                        tempWorld = tempWorld + " " + part;
                        if(closed) break;
                        currentArg++;
                    }
                    if(!closed) {
                        msg.send("&4[Permissions] No ending quote found for world string.");
                        return true;
                    }
                }
            }
            world = tempWorld;
            currentArg ++;
        }
        if (world == null) {
            msg.send("&4[Permissions] No world specified.");
            return true;
        }
        Entry entry = isGroup ? Security.getGroupObject(world, name) : Security.getUserObject(world, name);
        // Note that entry may be null if the user/group doesn't exist
        if (args.length > currentArg) {
            if (args[currentArg].equalsIgnoreCase("create")) {
                if (player != null && !Security.has(player, "permissions.create")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return true;
                }
                if (entry != null) {
                    msg.send("&4[Permissions] User/Group already exists.");
                    return true;
                }
                try {
                    entry = isGroup ? Security.safeGetGroup(world, name) : Security.safeGetUser(world, name);
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.send("&4[Permissions] Error creating user/group.");
                    return true;
                }
                msg.send("&7[Permissions] User/Group created.");
                return true;
            } else if (entry == null) {
                msg.send("&4[Permissions] User/Group does not exist.");
                return true;
            } else if (args[currentArg].equalsIgnoreCase("has")) {
                currentArg++;
                if (player != null && !Security.has(player, "permissions.has")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return true;
                }
                if(args.length > currentArg) {
                    String permission = args[currentArg];
                    boolean has = entry.hasPermission(permission);
                    msg.send("&7[Permissions]&b User/Group " + (has ? "has" : "does not have") + " that permission.");
                    return true;
                }
                msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) has <permission>");
                return true;
            } else if (args[currentArg].equalsIgnoreCase("perms")) {
                currentArg++;
                if (args.length > currentArg) {
                    if (args[currentArg].equalsIgnoreCase("list")) {
                        if (player != null && !Security.has(player, "permissions.perms.list")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        Set<String> perms = entry.getPermissions();
                        String text = "&7[Permissions]&b Permissions: &c";
                        if (perms == null || perms.isEmpty()) {
                            text = "&4[Permissions] User/Group has no non-inherited permissions.";
                        } else {
                            for (String perm : perms) {
                                text = text + perm + "&b,&c ";
                            }
                            text = text.substring(0, text.length() - 6);
                        }
                        msg.send(text);
                        return true;
                    } else if (args[currentArg].equalsIgnoreCase("listall")) {
                        if (player != null && !Security.has(player, "permissions.perms.listall")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        Set<String> perms = entry.getAllPermissions();
                        String text = "&7[Permissions]&b Permissions: &c";
                        if (perms == null || perms.isEmpty()) {
                            text = "&4[Permissions] User/Group has no permissions.";
                        } else {
                            for (String perm : perms) {
                                text = text + perm + "&b,&c ";
                            }
                            text = text.substring(0, text.length() - 6);
                        }
                        msg.send(text);
                        return true;
                    }  else if (args[currentArg].equalsIgnoreCase("add") || args[currentArg].equalsIgnoreCase("remove")) {
                        boolean add = args[currentArg].equalsIgnoreCase("add");

                        String permNode = add ? "permissions.perms.add" : "permissions.perms.remove";
                        if (player != null && !Security.has(player, permNode)) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }

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
                        msg.send(text);
                        return true;
                    }
                }
                msg.send("&7[Permissions] Syntax: ");
                msg.send("&b/permissions &a(g:)<target> (w:<world>) perms list");
                msg.send("&b/permissions &a(g:)<target> (w:<world>) perms [add|remove] <node>");
                return true;
            } else if (args[currentArg].equalsIgnoreCase("parents")) {
                currentArg++;
                if (args.length > currentArg) {
                    if (args[currentArg].equalsIgnoreCase("list")) {
                        if (player != null && !Security.has(player, "permissions.parents.list")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
//                        LinkedHashSet<GroupWorld> parents = entry.getRawParents();
                        LinkedHashSet<Entry> parents = entry.getParents();
                        String text = "&7[Permissions]&b Parents: &c";
                        if (parents == null || parents.isEmpty()) {
                            text = "&4[Permissions] User/Group has no parents.";
                        } else {
                            for (Entry parent : parents) {
                                text = text + parent.toString() + "&b,&c ";
                            }
                            text = text.substring(0, text.length() - 6);
                        }
                        msg.send(text);
                        return true;
                    } else if (args[currentArg].equalsIgnoreCase("add") || args[currentArg].equalsIgnoreCase("remove")) {
                        boolean add = args[currentArg].equalsIgnoreCase("add");
                        String permNode = add ? "permissions.perms.add" : "permissions.perms.remove";
                        if (player != null && !Security.has(player, permNode)) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        currentArg++;
                        String text = add ? "&7[Permissions]&b Parent added successfully." : "&7[Permissions]&b Parent removed successfully.";
                        if (args.length > currentArg) {
                            String parentName = args[currentArg];
                            String parentWorld = world;
                            if (args.length > (++currentArg)) {
                                String tempWorld = args[currentArg];
                                if(tempWorld.startsWith("\"")) {
                                    boolean closed = false;
                                    tempWorld = tempWorld.substring(1);
                                    if(tempWorld.endsWith("\"")) {
                                        tempWorld = tempWorld.substring(0, tempWorld.length() - 1);                    
                                    } else {
                                        currentArg++;
                                        while (args.length > currentArg) {
                                            String part = args[currentArg];
                                            closed = part.endsWith("\"");
                                            if(closed) {
                                                part = part.substring(0, part.length() - 1);
                                            }
                                            tempWorld = tempWorld + " " + part;
                                            if(closed) break;
                                            currentArg++;
                                        }
                                        if(!closed) {
                                            msg.send("&4[Permissions] No ending quote found for world string.");
                                            return true;
                                        }
                                    }
                                }
                                parentWorld = tempWorld;
                                currentArg ++;
                            }
                            LinkedHashSet<GroupWorld> parents = entry.getRawParents();
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
                        msg.send(text);
                        return true;
                    }
                }
                msg.send("&7[Permissions] Syntax: ");
                msg.send("&b/permissions &a(g:)<target> (w:<world>) parents list");
                msg.send("&b/permissions &a(g:)<target> (w:<world>) parents [add|remove] <parentname> (parentworld)");
                return true;
            } else if (args[currentArg].equalsIgnoreCase("info")) {
                currentArg++;
                if (args.length > currentArg) {
                    String choice = args[currentArg];
                    if (choice.equalsIgnoreCase("get")) {
                        currentArg++;
                        if (player != null && !Security.has(player, "permissions.info.get")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        if (args.length > currentArg) {
                            String path = args[currentArg];
                            msg.send("&7[Permissions]&b " + entry.getString(path));
                            return true;
                        }
                    } else if (choice.equalsIgnoreCase("set")) {
                        currentArg++;
                        if (player != null && !Security.has(player, "permissions.info.set")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        if (args.length > currentArg) {
                            String path = args[currentArg];
                            currentArg++;
                            if (args.length > currentArg) {
                                String newValueString = args[currentArg];
                                Object newValue;
                                String type = "";
                                if (newValueString.startsWith("b:")) {
                                    newValue = Boolean.parseBoolean(newValueString.substring(2));
                                    type = "Boolean";
                                } else if (newValueString.startsWith("d:")) {
                                    newValue = Double.parseDouble(newValueString.substring(2));
                                    type = "Double";
                                } else if (newValueString.startsWith("i:")) {
                                    newValue = Integer.parseInt(newValueString.substring(2));
                                    type = "Integer";
                                } else {
                                    newValue = newValueString;
                                    type = "String";
                                }
                                entry.setData(path, newValue);
                                msg.send("&7[Permissions]&b &a" + path + "&b set to &a" + type + " &c" + newValue.toString());
                                return true;
                            }
                        }
                    } else if (choice.equalsIgnoreCase("remove")) {
                        currentArg++;
                        if (player != null && !Security.has(player, "permissions.info.remove")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        if (args.length > currentArg) {
                            String path = args[currentArg];
                            entry.removeData(path);
                            msg.send("&7[Permissions]&b &a" + path + "&b cleared.");
                            return true;
                        }
                    }
                }
                msg.send("&7[Permissions] Syntax: ");
                msg.send("&b/permissions &a(g:)<target> (w:<world>) info get <path>");
                msg.send("&b/permissions &a(g:)<target> (w:<world>) info set <path> (i:|d:|b:)<data>");
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
                            String node = isPrefix ? "permissions.prefix.get" : "permissions.suffix.get";
                            if (player != null && !Security.has(player, node)) {
                                msg.send("&4[Permissions] You do not have permissions to use this command.");
                                return true;
                            }
                            String text = isPrefix ? "&7[Permissions]&b " + group.getName() + "'s prefix:" : "&7[Permissions]&b " + group.getName() + "'s suffix:";
                            msg.send(text);
                            text = isPrefix ? group.getRawPrefix() : group.getRawSuffix();
                            msg.send("\"" + text + "\"");
                            return true;
                        } else if (args[currentArg].equalsIgnoreCase("set")) {
                            String node = isPrefix ? "permissions.prefix.set" : "permissions.suffix.set";
                            if (player != null && !Security.has(player, node)) {
                                msg.send("&4[Permissions] You do not have permissions to use this command.");
                                return true;
                            }
                            currentArg++;
                            String newFix = "";
                            if (args.length > currentArg) {
                                String[] fullFix = Arrays.copyOfRange(args, currentArg, args.length);
                                for (String part : fullFix) {
                                    newFix = newFix + part + " ";
                                }
                                newFix = newFix.substring(0, newFix.length() - 1); // Possible
                                                                                   // bug
                            }
                            if (isPrefix)
                                group.setPrefix(newFix);
                            else
                                group.setSuffix(newFix);
                            String text = isPrefix ? "&7[Permissions]&b Group's prefix set to " + newFix + "." : "&7[Permissions]&7 Group's suffix set to " + newFix + ".";
                            msg.send(text);
                            return true;
                        }
                    }
                    msg.send("&7[Permissions] Syntax: ");
                    msg.send("&b/permissions &ag:<target> (w:<world>) [prefix|suffix] get");
                    msg.send("&b/permissions &ag:<target> (w:<world>) [prefix|suffix] set <newfix>");
                    return true;
                } else if (args[currentArg].equalsIgnoreCase("build")) {
                    currentArg++;
                    if (args.length > currentArg) {
                        if (args[currentArg].equalsIgnoreCase("get")) {
                            if (player != null && !Security.has(player, "permissions.build.get")) {
                                msg.send("&4[Permissions] You do not have permissions to use this command.");
                                return true;
                            }
                            if (group.canBuild())
                                msg.send("&7[Permissions]&b " + group.getName() + " can build.");
                            else
                                msg.send("&7[Permissions]&b " + group.getName() + " cannot build.");
                            return true;
                        } else if (args[currentArg].equalsIgnoreCase("set")) {
                            if (player != null && !Security.has(player, "permissions.build.set")) {
                                msg.send("&4[Permissions] You do not have permissions to use this command.");
                                return true;
                            }
                            currentArg++;
                            if (args.length > currentArg) {
                                String bool = args[currentArg];
                                boolean build = Boolean.parseBoolean(bool);
                                group.setBuild(build);
                                msg.send("&7[Permissions]&b" + group.getName() + "'s build setting was set to " + Boolean.toString(build));
                                return true;
                            }

                            msg.send("&7[Permissions] Syntax: &b/permissions &ag:<target> (w:<world>) build set <true|false>.");
                            return true;
                        }
                    }
                    msg.send("&7[Permissions] Syntax: ");
                    msg.send("&b/permissions &ag:<target> (w:<world>) build get");
                    msg.send("&b/permissions &ag:<target> (w:<world>) build set <newfix>");
                    return true;
                }

                msg.send("&7[Permissions] Syntax: ");
                msg.send("&b/permissions &ag:<target> (w:<world>) [prefix|suffix|build] [get|set] ...");
            } else if (entry instanceof User) {
                User user = (User) entry;
                if (args[currentArg].equalsIgnoreCase("promote") || args[currentArg].equalsIgnoreCase("demote")) {
                    boolean isPromote = args[currentArg].equalsIgnoreCase("promote");
                    currentArg++;
                    if (args.length > currentArg) {
                        String parentName = args[currentArg];
                        String parentWorld = world;
                        if (args.length > currentArg && args[currentArg].startsWith("w:")) {
                            String tempWorld = args[currentArg];
                            if(tempWorld.startsWith("\"")) {
                                boolean closed = false;
                                tempWorld = tempWorld.substring(1);
                                if(tempWorld.endsWith("\"")) {
                                    tempWorld = tempWorld.substring(0, tempWorld.length() - 1);                    
                                } else {
                                    currentArg++;
                                    while (args.length > currentArg) {
                                        String part = args[currentArg];
                                        closed = part.endsWith("\"");
                                        if(closed) {
                                            part = part.substring(0, part.length() - 1);
                                        }
                                        tempWorld = tempWorld + " " + part;
                                        if(closed) break;
                                        currentArg++;
                                    }
                                    if(!closed) {
                                        msg.send("&4[Permissions] No ending quote found for world string.");
                                        return true;
                                    }
                                }
                            }
                            parentWorld = tempWorld;
                            currentArg++;
                        }
                        Group group = Security.getGroupObject(parentWorld, parentName);
                        if (group == null) {
                            msg.send("&4[Permissions] No such group.");
                            return true;
                        }
                        if (!user.inGroup(parentWorld, parentName)) {
                            msg.send("&4[Permissions] User not in specified group.");
                            return true;
                        }
                        if (args.length > currentArg) {
                            String track = args[currentArg];
                            if (!group.getTracks().contains(track)) {
                                msg.send("&4[Permissions] Specified track does not exist.");
                                return true;
                            }
                            String permNode = isPromote ? "permissions.promote." + track : "permission.demote." + track;
                            if (player != null && !Security.has(player, permNode)) {
                                msg.send("&4[Permissions] You do not have permissions to use this command.");
                                return true;
                            }
                            if (isPromote)
                                user.promote(group, track);
                            else
                                user.demote(group, track);
                            String text = isPromote ? "&7[Permissions]&b User promoted along track " + track + "." : "&7[Permissions]&7 User demoted along track " + track + ".";
                            msg.send(text);
                            return true;
                        }
                        msg.send("&4[Permissions] Syntax: /permissions <target> (w:<world>) [promote|demote] <parent> (w:<parentworld>) <track>");
                        return true;
                    }
                }

                msg.send("&7[Permissions] Syntax: ");
                msg.send("&b/permissions &a<target> (w:<world>) [promote|demote] ...");
            }

            msg.send("&b/permissions &a(g:)<target> (w:<world>) [perms|parents] [list|add|remove] ...");
            msg.send("&b/permissions &a(g:)<target> (w:<world>) info [get|set|remove] ...");
        }

        return false;
    }

    private boolean reload(CommandSender sender, String arg) {
        Player p = null;
        if (sender instanceof Player) {
            p = (Player) sender;
        }

        if (arg == null || arg.equals("")) {

            if (p!=null&&!Security.has(p.getWorld().getName(), p.getName(), "permissions.reload.default")) {
                p.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
                return true;
            }
            
            Security.reload(defaultWorld);
            sender.sendMessage(ChatColor.GRAY + "[Permissions] Default world reloaded.");
            return true;
        }

        if (arg.equalsIgnoreCase("all")) {

            if (p!=null&&Security.has(p.getWorld().getName(), p.getName(), "permissions.reload.all")) {
                p.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
                return true;
            }
            
            Security.reload();
            sender.sendMessage(ChatColor.GRAY + "[Permissions] All worlds reloaded.");
            return true;
        }


        if (p!=null&&!Security.has(p.getWorld().getName(), p.getName(), "permissions.reload."+arg)) {
            p.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
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
