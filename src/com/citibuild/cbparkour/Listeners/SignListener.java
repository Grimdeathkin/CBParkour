package com.citibuild.cbparkour.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.citibuild.cbparkour.Parkour;

public class SignListener implements Listener{
	
	private final Parkour plugin;
	public SignListener(Parkour plugin) {
		this.plugin = plugin;
	}
	
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		Player player = e.getPlayer();
		if (e.getLine(0).equalsIgnoreCase("[pk]") && !Parkour.permission.has(player, "parkour.mapeditor")) {
			e.setCancelled(true);
			e.getBlock().setType(Material.AIR);
			plugin.pkFuncs.sendError("noPermission", player, plugin);
		}

		if (Parkour.permission.has(player, "parkour.mapeditor")) {
			// 15 char max per lines (on sign)

			if (e.getLine(0).equalsIgnoreCase("[pk]")) {
				if (e.getLine(1).equalsIgnoreCase("leave")) {
					e.setLine(0, "[Parkour]");
					e.setLine(1, "Leave");
				} else if (e.getLine(1).equalsIgnoreCase("join")) {
					if (plugin.pkFuncs.isNumber(e.getLine(2))) {
						if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(e.getLine(2)))) {
							int mapID = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[Parkour]");
							e.setLine(1, "Join");
							e.setLine(2, ChatColor.AQUA + plugin.getMapName(mapID));
						} else {
							e.setCancelled(true);
							e.getBlock().setType(Material.AIR);
							plugin.pkFuncs.sendError("badmap", player, plugin);
						}
					} else {
						e.setCancelled(true);
						e.getBlock().setType(Material.AIR);
						plugin.pkFuncs.sendError("2notnumber", player, plugin);
					}
				} else if (e.getLine(1).equalsIgnoreCase("info")) {
					if (plugin.pkFuncs.isNumber(e.getLine(2))) {
						if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(e.getLine(2)))) {
							int mapID = Integer.parseInt(e.getLine(2));

							e.setLine(0, "Parkour #" + mapID);
							e.setLine(1, "---------------");
							e.setLine(2, ChatColor.AQUA + plugin.getMapName(mapID));
						} else {
							e.setCancelled(true);
							e.getBlock().setType(Material.AIR);
							plugin.pkFuncs.sendError("badmap", player, plugin);
						}
					} else {
						e.setCancelled(true);
						e.getBlock().setType(Material.AIR);
						plugin.pkFuncs.sendError("2notnumber", player, plugin);
					}
				} else if (e.getLine(1).equalsIgnoreCase("best")) {
					if (plugin.pkFuncs.isNumber(e.getLine(2))) {
						if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(e.getLine(2)))) {
							int mapID = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[Parkour]");
							e.setLine(1, "Best Times");
							e.setLine(2, ChatColor.AQUA + plugin.getMapName(mapID));
							e.setLine(3, "Click Me!");

						} else {
							e.setCancelled(true);
							e.getBlock().setType(Material.AIR);
							plugin.pkFuncs.sendError("badmap", player, plugin);
						}
					} else {
						e.setCancelled(true);
						e.getBlock().setType(Material.AIR);
						plugin.pkFuncs.sendError("2notnumber", player, plugin);
					}
				} else {
					e.setCancelled(true);
					e.getBlock().breakNaturally();
					plugin.pkFuncs.sendError("badsign", player, plugin);
				}
			}
		}
	}

}
