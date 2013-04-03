package fr.ribesg.bukkit.nworld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.ribesg.bukkit.ncore.lang.MessageId;

/**
 * @author Ribesg
 */
public class NCommandExecutor implements CommandExecutor {
    
    private final NWorld plugin;
    
    public NCommandExecutor(final NWorld instance) {
        plugin = instance;
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        if (command.getName().equalsIgnoreCase("nworld")) {
            if (sender.hasPermission(Permissions.CMD_WORLD) || sender.hasPermission(Permissions.USER) || sender.hasPermission(Permissions.ADMIN) || sender.isOp()) {
                return cmdWorld(sender, args);
            } else {
                plugin.sendMessage(sender, MessageId.noPermissionForCommand);
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("spawn")) {
            if (sender.hasPermission(Permissions.CMD_SPAWN) || sender.hasPermission(Permissions.USER) || sender.hasPermission(Permissions.ADMIN) || sender.isOp()) {
                return cmdSpawn(sender);
            } else {
                plugin.sendMessage(sender, MessageId.noPermissionForCommand);
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("setspawn")) {
            if (sender.hasPermission(Permissions.CMD_SETSPAWN) || sender.hasPermission(Permissions.ADMIN) || sender.isOp()) {
                return cmdSetSpawn(sender);
            } else {
                plugin.sendMessage(sender, MessageId.noPermissionForCommand);
                return true;
            }
        } else {
            return false;
        }
    }
    
    private boolean cmdWorld(final CommandSender sender, String[] args) {
        args = parseArgs(args);
        if (args.length == 0) {
            // Lists available worlds
            plugin.sendMessage(sender, MessageId.world_availableWorlds);
            for (final String worldName : plugin.getWorldMap().keySet()) {
                final boolean isTeleportationAllowed = plugin.getWorldMap().get(worldName);
                sender.sendMessage(ChatColor.BLACK + "- " + (isTeleportationAllowed ? ChatColor.GREEN : ChatColor.RED) + worldName);
            }
            return true;
        } else if (args.length == 1) {
            // Warp to world
            return subCmdWorldWarp(sender, args[0]);
        } else { // Handle subcommands
            String subCmd = args[0].toLowerCase();
            args = Arrays.copyOfRange(args, 1, args.length);
            switch (subCmd) {
                case "create":
                    return subCmdWorldCreate(sender, args);
                case "load":
                    return subCmdWorldLoad(sender, args);
                case "unload":
                    return subCmdWorldUnload(sender, args);
                case "allow":
                case "allowwarp":
                    return subCmdWorldWarpAllow(sender, args);
                case "deny":
                case "denywarp":
                    return subCmdWorldWarpDeny(sender, args);
                default:
                    return false;
            }
        }
    }
    
    private boolean subCmdWorldWarp(CommandSender sender, String givenWorldName) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, MessageId.cmdOnlyAvailableForPlayers);
            return true;
        }
        final Player player = (Player) sender;
        final String worldName = givenWorldName.toLowerCase();
        if (plugin.getWorldMap().containsKey(worldName)) {
            if (plugin.getWorldMap().get(worldName) || player.hasPermission(Permissions.CMD_WORLD_WARP_ALL)) {
                final World world = plugin.getServer().getWorld(worldName);
                player.teleport(plugin.getSpawnMap().get(worldName));
                plugin.sendMessage(sender, MessageId.world_teleportedTo, world.getName());
                return true;
            } else {
                plugin.sendMessage(player, MessageId.world_warpToThisWorldDisallowed, plugin.getServer().getWorld(worldName).getName());
                return true;
            }
        } else {
            plugin.sendMessage(player, MessageId.world_unknownWorld, givenWorldName);
            return true;
        }
    }
    
    private boolean subCmdWorldCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CMD_WORLD_CREATE) && !sender.hasPermission(Permissions.ADMIN) && !sender.isOp()) {
            plugin.sendMessage(sender, MessageId.noPermissionForCommand);
            return true;
        }
        String realWorldName = getRealWorldName(args[0]);
        if (realWorldName != null) {
            plugin.sendMessage(sender, MessageId.world_alreadyExists, realWorldName);
            return true;
        } else {
            long seed = new Random().nextLong();
            if (args.length > 1) {
                try {
                    seed = Long.parseLong(args[1]);
                } catch (final NumberFormatException e) {
                    seed = args[1].hashCode();
                }
            }
            final WorldCreator newWorld = new WorldCreator(args[0]);
            newWorld.seed(seed);
            if (plugin.getPluginConfig().getBroadcastOnWorldCreate() == 1) {
                plugin.broadcastMessage(MessageId.world_creatingWorldMayBeLaggy);
            } else {
                plugin.sendMessage(sender, MessageId.world_creatingWorldMayBeLaggy);
            }
            final World createdWorld = plugin.getServer().createWorld(newWorld);
            if (plugin.getPluginConfig().getBroadcastOnWorldCreate() == 1) {
                plugin.broadcastMessage(MessageId.world_created);
            } else {
                plugin.sendMessage(sender, MessageId.world_created);
            }
            plugin.getWorldMap().put(createdWorld.getName().toLowerCase(), false);
            return true;
        }
    }
    
    private boolean subCmdWorldLoad(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CMD_WORLD_LOAD) && !sender.hasPermission(Permissions.ADMIN) && !sender.isOp()) {
            plugin.sendMessage(sender, MessageId.noPermissionForCommand);
            return true;
        }
        String realWorldName = getRealWorldName(args[0]);
        if (realWorldName == null) {
            plugin.sendMessage(sender, MessageId.world_unknownWorld, args[0]);
            return true;
        } else if (plugin.getServer().getWorld(realWorldName) != null) {
            plugin.sendMessage(sender, MessageId.world_alreadyLoaded, realWorldName);
            return true;
        } else {
            if (plugin.getPluginConfig().getBroadcastOnWorldLoad() == 1) {
                plugin.broadcastMessage(MessageId.world_loadingWorldMayBeLaggy);
            } else {
                plugin.sendMessage(sender, MessageId.world_loadingWorldMayBeLaggy);
            }
            final World loadedWorld = plugin.getServer().createWorld(new WorldCreator(realWorldName));
            if (plugin.getPluginConfig().getBroadcastOnWorldLoad() == 1) {
                plugin.broadcastMessage(MessageId.world_loaded);
            } else {
                plugin.sendMessage(sender, MessageId.world_loaded);
            }
            plugin.getWorldMap().put(loadedWorld.getName().toLowerCase(), false);
            return true;
        }
    }
    
    private boolean subCmdWorldUnload(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CMD_WORLD_UNLOAD) && !sender.hasPermission(Permissions.ADMIN) && !sender.isOp()) {
            plugin.sendMessage(sender, MessageId.noPermissionForCommand);
            return true;
        }
        String realWorldName = getRealWorldName(args[0]);
        if (realWorldName == null) {
            plugin.sendMessage(sender, MessageId.world_unknownWorld, args[0]);
            return true;
        } else if (plugin.getServer().getWorld(realWorldName) == null) {
            plugin.sendMessage(sender, MessageId.world_notLoaded, realWorldName);
            return true;
        } else {
            if (plugin.getPluginConfig().getBroadcastOnWorldUnload() == 1) {
                plugin.broadcastMessage(MessageId.world_unloadingWorldMayBeLaggy);
            } else {
                plugin.sendMessage(sender, MessageId.world_unloadingWorldMayBeLaggy);
            }
            plugin.getServer().unloadWorld(realWorldName, true);
            if (plugin.getPluginConfig().getBroadcastOnWorldUnload() == 1) {
                plugin.broadcastMessage(MessageId.world_unloaded);
            } else {
                plugin.sendMessage(sender, MessageId.world_unloaded);
            }
            plugin.getWorldMap().remove(realWorldName.toLowerCase());
            return true;
        }
    }
    
    private boolean subCmdWorldWarpAllow(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CMD_WORLD_WARP_EDIT) && !sender.hasPermission(Permissions.ADMIN) && !sender.isOp()) {
            plugin.sendMessage(sender, MessageId.noPermissionForCommand);
            return true;
        }
        String realWorldName = getRealWorldName(args[0]);
        if (realWorldName == null) {
            plugin.sendMessage(sender, MessageId.world_unknownWorld, args[0]);
            return true;
        } else if (plugin.getServer().getWorld(realWorldName) == null) {
            plugin.sendMessage(sender, MessageId.world_notLoaded, realWorldName);
            return true;
        } else {
            if (plugin.getWorldMap().get(realWorldName.toLowerCase())) {
                plugin.sendMessage(sender, MessageId.world_alreadyAllowed, realWorldName);
                return true;
            } else {
                plugin.getWorldMap().put(realWorldName.toLowerCase(), true);
                plugin.sendMessage(sender, MessageId.world_allowedWarp, realWorldName);
                return true;
            }
        }
    }
    
    private boolean subCmdWorldWarpDeny(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CMD_WORLD_WARP_EDIT) && !sender.hasPermission(Permissions.ADMIN) && !sender.isOp()) {
            plugin.sendMessage(sender, MessageId.noPermissionForCommand);
            return true;
        }
        String realWorldName = getRealWorldName(args[0]);
        if (realWorldName == null) {
            plugin.sendMessage(sender, MessageId.world_unknownWorld, args[0]);
            return true;
        } else if (plugin.getServer().getWorld(realWorldName) == null) {
            plugin.sendMessage(sender, MessageId.world_notLoaded, realWorldName);
            return true;
        } else {
            if (plugin.getWorldMap().get(realWorldName.toLowerCase())) {
                plugin.getWorldMap().put(realWorldName.toLowerCase(), false);
                plugin.sendMessage(sender, MessageId.world_disallowedWarp, realWorldName);
                return true;
            } else {
                plugin.sendMessage(sender, MessageId.world_alreadyDisallowed, realWorldName);
                return true;
            }
        }
    }
    
    private boolean cmdSpawn(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, MessageId.cmdOnlyAvailableForPlayers);
            return true;
        }
        final Player player = (Player) sender;
        player.teleport(plugin.getSpawnMap().get(player.getWorld().getName().toLowerCase()));
        plugin.sendMessage(player, MessageId.world_teleportingToSpawn);
        return true;
    }
    
    private boolean cmdSetSpawn(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, MessageId.cmdOnlyAvailableForPlayers);
            return true;
        }
        final Player player = (Player) sender;
        final Location loc = player.getLocation();
        player.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        plugin.getSpawnMap().put(player.getWorld().getName().toLowerCase(), loc);
        plugin.sendMessage(player, MessageId.world_settingSpawnPoint, player.getWorld().getName());
        return true;
    }
    
    // ############### //
    // ## Utilities ## //
    // ############### //
    /**
     * Parses an argument array to support spaces between "" or ''
     * Example:
     * Input= {arg1} ; {"arg2} ; {arg3} ; {arg4"} ; {arg5}
     * Output= {arg1} ; {arg2 arg3 arg4} ; {arg5}
     * 
     * @param args
     *            Original arguments
     * @return The new arguments array
     */
    private String[] parseArgs(String[] args) {
        if (args == null || args.length == 0 || args.length == 1) {
            return args;
        } else {
            List<String> newArgs = new ArrayList<String>();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("\"") || arg.startsWith("'")) {
                    StringBuilder newArg = new StringBuilder(arg.substring(1));
                    String endChar = Character.toString(arg.charAt(0));
                    while (!arg.endsWith(endChar)) {
                        i++;
                        if (i == args.length) {
                            throw new IllegalArgumentException();
                        }
                        arg = args[i];
                        newArg.append(' ');
                        newArg.append(arg);
                    }
                    newArgs.add(newArg.substring(0, newArg.length() - 1));
                } else {
                    newArgs.add(arg);
                }
            }
            return (String[]) newArgs.toArray();
        }
    }
    
    /**
     * Check if a given world is loaded, not case sensitive.
     * 
     * @param worldName
     *            The world name given by the sender
     * @return The correct name of the world if it is loaded, null otherwise
     */
    private String isLoaded(String worldName) {
        World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            return world.getName();
        } else {
            return null;
        }
    }
    
    /**
     * Check if a given unloaded world exists in world folder, not case sensitive.
     * 
     * @param worldName
     *            The world name given by the sender
     * @return The correct name of the world if it exists, null otherwise
     * @throws IOException
     *             If it was unable to iterate over the Worlds folder
     */
    private String exists(String worldName) throws IOException {
        try {
            final Path worldFolderPath = plugin.getServer().getWorldContainer().toPath();
            for (final Path p : Files.newDirectoryStream(worldFolderPath)) {
                if (p.getFileName().toString().equalsIgnoreCase(worldName)) {
                    return p.getFileName().toString();
                }
            }
        } catch (final IOException e) {
            plugin.getLogger().severe("Unable to iterate over Worlds");
            e.printStackTrace();
            throw e;
        }
        return null;
    }
    
    /**
     * Returns the real world name of the given world
     * 
     * @param worldName
     *            The world name given by the sender
     * @return The correct name of the world if it is loaded or exists, null otherwise
     */
    private String getRealWorldName(String worldName) {
        try {
            String res = isLoaded(worldName);
            if (res == null) res = exists(worldName);
            return res;
        } catch (IOException e) {
            return null;
        }
    }
}
