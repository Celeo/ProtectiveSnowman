package com.darktidegames.celeo.snowman;

import java.io.File;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

	@Override
	public void onEnable()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		reloadConfig();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Enabled");
	}

	@Override
	public void onDisable()
	{
		getLogger().info("Disabled");
	}

	@EventHandler
	public void onEntityHurt(EntityDamageEvent event)
	{
		Entity hurt = event.getEntity();
		if (hurt instanceof Snowman
				&& event.getCause().equals(DamageCause.DROWNING))
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event)
	{
		Entity hurt = event.getEntity();
		Entity damager = event.getDamager();
		if (damager instanceof Snowball)
			if (hurt instanceof LivingEntity && !(hurt instanceof Player))
				event.setDamage(6);
	}

}