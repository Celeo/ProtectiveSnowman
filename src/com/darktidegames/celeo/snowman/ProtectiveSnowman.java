package com.darktidegames.celeo.snowman;

import java.io.File;
import java.util.List;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * <b>ProtectiveSnowman</b><br>
 * Snowballs do damage to mobs and snowmen can live in any biome
 * 
 * @author Celeo
 */
public class ProtectiveSnowman extends JavaPlugin implements Listener
{

	private int snowmanMaxHealth = 10;
	private int snowballDamage = 6;
	private boolean attackCreepers = false;
	private int creeperTooFar = 25;

	@Override
	public void onLoad()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
	}

	@Override
	public void onEnable()
	{
		load();
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				if (!attackCreepers)
					return;
				for (World world : getServer().getWorlds())
				{
					if (!world.getEnvironment().equals(Environment.NORMAL))
						continue;
					if (world.getEntities().isEmpty())
						continue;
					for (Entity e : world.getEntities())
					{
						if (!(e instanceof LivingEntity))
							continue;
						if (!(e instanceof Snowman))
							continue;
						Snowman snowman = (Snowman) e;
						if (snowman.getTarget() != null)
							if (snowman.getLocation().distance(snowman.getTarget().getLocation()) > creeperTooFar)
								snowman.setTarget(null);
						List<Entity> near = snowman.getNearbyEntities(8, 1, 8);
						if (near.isEmpty())
							continue;
						for (Entity n : near)
						{
							if (!(n instanceof LivingEntity))
								continue;
							if (!(n instanceof Creeper))
								continue;
							snowman.setTarget((LivingEntity) n);
							return;
						}
					}
				}
			}
		}, 100L, 100L);
		getLogger().info("Enabled");
	}

	private void load()
	{
		reloadConfig();
		snowmanMaxHealth = getConfig().getInt("snowman.maxHealth", 10);
		snowballDamage = getConfig().getInt("snowman.snowballDamage", 6);
		attackCreepers = getConfig().getBoolean("snowman.attackCreepers", true);
		creeperTooFar = getConfig().getInt("snowman.creeperTooFar", 25);
		getLogger().info("Settings loaded from configuration");
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		getLogger().info("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (player.isOp()
					|| player.hasPermission("protectivesnowman.control"))
			{
				if (args == null || args.length == 0)
				{
					player.sendMessage("§c/snowman [reload|info]");
					return true;
				}
				if (args[0].equalsIgnoreCase("reload"))
				{
					load();
					player.sendMessage("§aReloaded from configuration");
				}
				else if (args[0].equalsIgnoreCase("info"))
					player.sendMessage("§7[ProtectiveSnowman] Snowman max health: §6"
							+ snowmanMaxHealth
							+ "§7, Snowball damage: §6"
							+ snowballDamage
							+ "§7, Snowmen will attack creepers: §6"
							+ attackCreepers
							+ " §7, Snowmen giveup on creeper distance: §6"
							+ creeperTooFar);
				else
					player.sendMessage("§c/snowman [reload|info]");
			}
			else
				player.sendMessage("§cYou cannot use that command");
			return true;
		}
		if (args == null || args.length == 0)
		{
			sender.sendMessage("/snowman [reload|info]");
			return true;
		}
		if (args[0].equalsIgnoreCase("reload"))
			load();
		else if (args[0].equalsIgnoreCase("info"))
			sender.sendMessage("§7[ProtectiveSnowman] Snowman max health: "
					+ snowmanMaxHealth + ", Snowball damage:" + snowballDamage);
		else
			sender.sendMessage("/snowman [reload|info]");
		return false;
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Snowman)
		{
			((Snowman) entity).setMaxHealth(snowmanMaxHealth);
			((Snowman) entity).setHealth(snowmanMaxHealth);
		}
	}

	@EventHandler
	public void onEntityHurt(EntityDamageEvent event)
	{
		Entity hurt = event.getEntity();
		if (hurt instanceof Snowman
				&& (event.getCause().equals(DamageCause.DROWNING) || event.getCause().equals(DamageCause.MELTING)))
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event)
	{
		Entity hurt = event.getEntity();
		Entity damager = event.getDamager();
		if (damager instanceof Snowball)
			if (hurt instanceof LivingEntity && !(hurt instanceof Player))
				event.setDamage(snowballDamage);
	}

}