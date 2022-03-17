package xyz.derkades.spawnx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		super.getServer().getPluginManager().registerEvents(this, this);
		super.saveDefaultConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("setspawn")) {
			if (sender instanceof Player player) {
				if (player.hasPermission("spawnx.setspawn")) {
					setSpawnLocation(player.getLocation());
					player.sendMessage(ChatColor.DARK_AQUA + "The spawn location has been set!");
				} else {
					player.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
				}
			} else {
				if (args.length != 4) {
					sender.sendMessage(ChatColor.RED + "Console usage requires 4 arguments <world> <x> <y> <z>");
					return false;
				}

				String worldName = args[0];
				double x = Double.parseDouble(args[1]);
				double y = Double.parseDouble(args[2]);
				double z = Double.parseDouble(args[3]);

				World world = getServer().getWorld(worldName);

				if(world == null) {
					sender.sendMessage(ChatColor.RED + "Unknown world: " + worldName);
					return false;
				}

				setSpawnLocation(new Location(world, x, y, z));
				sender.sendMessage(ChatColor.DARK_AQUA + "The spawn location has been set!");
			}

			return true;
		} else if (command.getName().equalsIgnoreCase("spawn")) {
			if (sender instanceof Player player) {
				if (player.hasPermission("spawnx.spawn")) {
					player.teleport(getSpawnLocation());
				} else {
					player.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
				}
			} else {
				sender.sendMessage("You have to be a player in order to execute this command.");
			}

			return true;
		}

		return false;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (getConfig().getBoolean("teleport-on-join")) {
			Player player = event.getPlayer();
			player.teleport(getSpawnLocation());
		}
	}

	private Location getSpawnLocation() {
		String worldName = getConfig().getString("world-name");
		double x = getConfig().getDouble("x");
		double y = getConfig().getDouble("y");
		double z = getConfig().getDouble("z");
		float pitch = (float) getConfig().getDouble("pitch");
		float yaw = (float) getConfig().getDouble("yaw");
		return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
	}

	private void setSpawnLocation(Location loc) {
		getConfig().set("world-name", loc.getWorld().getName());
		getConfig().set("x", loc.getX());
		getConfig().set("y", loc.getY());
		getConfig().set("z", loc.getZ());
		getConfig().set("pitch", loc.getPitch());
		getConfig().set("yaw", loc.getYaw());
		super.saveConfig();
	}

}
