package xyz.derkades.spawnx;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static io.papermc.paper.command.brigadier.Commands.literal;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.blockPosition;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.world;

@SuppressWarnings("UnstableApiUsage")
public class Main extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		super.getServer().getPluginManager().registerEvents(this, this);
		super.saveDefaultConfig();

		LifecycleEventManager<Plugin> manager = getLifecycleManager();
		manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> registerCommands(event.registrar()));
	}

	private void registerCommands(Commands commands) {
		LiteralCommandNode<CommandSourceStack> spawnCommand = literal("spawn")
				.requires(source -> source.getSender() instanceof Player player
						&& player.hasPermission("spawnx.spawn"))
				.executes(ctx -> {
					Player player = (Player) ctx.getSource().getSender();
					player.teleport(getSpawnLocation());

					return Command.SINGLE_SUCCESS;
				}).build();

		Command<CommandSourceStack> setSpawnExecutor = ctx -> {
			BlockPosition pos = ctx.getArgument("position", BlockPositionResolver.class)
					.resolve(ctx.getSource());
			float yaw = ctx.getArgument("yaw", Float.class);
			World world = ctx.getArgument("world", World.class);

			Location location = new Location(world, pos.x(), pos.y(), pos.z(), yaw, 0);
			return onSetSpawn(ctx.getSource().getSender(), location);
		};

		LiteralCommandNode<CommandSourceStack> setSpawnCommand = literal("setspawn")
				.requires(source -> source.getSender().hasPermission("spawnx.setspawn"))
				.executes(ctx -> {
					if(ctx.getSource().getSender() instanceof ConsoleCommandSender) {
						throw new SimpleCommandExceptionType(
								new LiteralMessage("You must provide a location from the console")).create();
					}

					Player player = (Player) ctx.getSource().getSender();
					return onSetSpawn(player, player.getLocation());
				})
				//setspawn <world> <x> <y> <z> <yaw>
				.then(argument("world", world())
							  .then(argument("position", blockPosition())
											.then(argument("yaw", floatArg(-180, 180))
														  .executes(setSpawnExecutor)))).build();


		commands.register(spawnCommand, "Teleport to spawn");
		commands.register(setSpawnCommand, "Set the spawn location");
	}

	public int onSetSpawn(CommandSender sender, Location location) {
		getConfig().set("world-name", location.getWorld().getName());
		getConfig().set("x", location.getX());
		getConfig().set("y", location.getY());
		getConfig().set("z", location.getZ());
		getConfig().set("pitch", location.getPitch());
		getConfig().set("yaw", location.getYaw());
		super.saveConfig();

		sender.sendMessage(Component.text("The spawn location has been set!").color(NamedTextColor.DARK_AQUA));

		return Command.SINGLE_SUCCESS;
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
}
