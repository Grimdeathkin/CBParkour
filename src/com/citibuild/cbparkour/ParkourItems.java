package com.citibuild.cbparkour;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ParkourItems {
	
	Plugin plugin;
	ItemStack istack;
	ItemMeta imeta;
	String command;
	String[] items = {"checkpoint", "current time"};
	ArrayList<String> itemsList = new ArrayList<String>();
	
	public String slime_cmd;
	public String musicdisk_cmd;
	
	public ParkourItems(Plugin plugin) {
		this.plugin = plugin;
		itemsList.addAll(Arrays.asList(items));
	}
	
	public void loadStartItems() {
		for(Player player: plugin.getServer().getOnlinePlayers()) {
			giveItems(player);
		}
	}
	
	public void giveItems(Player player) {
		giveSlime(player);
		giveMusicDisk(player);
	}
	
	@SuppressWarnings("deprecation")
	public void giveSlime(Player player) {
		istack = new ItemStack(Material.SLIME_BALL, 1);
		imeta = istack.getItemMeta();
		imeta.setDisplayName(ChatColor.GOLD + "Checkpoint");
		ArrayList<String> lorelist = new ArrayList<String>();
		lorelist.add(ChatColor.WHITE + "Returns you to the last " + ChatColor.GREEN + "Checkpoint" + ChatColor.WHITE + "!");
		imeta.setLore(lorelist);
		istack.setItemMeta(imeta);
		if(!player.getInventory().contains(istack)) {
			player.getInventory().addItem(istack);
		}
		player.updateInventory();
	}
	
	@SuppressWarnings("deprecation")
	public void giveMusicDisk(Player player) {
		istack = new ItemStack(Material.RECORD_6, 1);
		imeta = istack.getItemMeta();
		imeta.setDisplayName(ChatColor.DARK_PURPLE + "Current Time");
		ArrayList<String> lorelist = new ArrayList<String>();
		lorelist.add(ChatColor.WHITE + "Shows you your current time");
		imeta.setLore(lorelist);
		istack.setItemMeta(imeta);
		if(!player.getInventory().contains(istack)) {
			player.getInventory().addItem(istack);
		}
		player.updateInventory();
	}

}
