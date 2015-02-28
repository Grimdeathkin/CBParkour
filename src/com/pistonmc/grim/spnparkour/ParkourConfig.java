package com.pistonmc.grim.spnparkour;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ParkourConfig {

	/*
	 * Where all the settings from the config get loaded and stored.
	 */
	public Parkour pk;

	public ParkourConfig(Parkour plugin) {
		this.pk = plugin;
	}

	public void onEnable() {

		LoadCfg();
		
		pk.pkUnlockFuncs.saveDefaultUnlocksConfig();
		pk.pkUnlockFuncs.reloadUnlocksConfig();
		if(Bukkit.getServer().getOnlinePlayers().size() != 0) {
			pk.pkUnlockFuncs.loadAllPlayerUnlocks();
		}
		

		if (!pk.pkVars.scores.getAbsoluteFile().exists()) {
			try {
				pk.pkVars.scores.createNewFile();
				pk.pkFuncs.saveScore();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}

		if (!pk.pkVars.playerInfoFile.getAbsoluteFile().exists()) {
			pk.pkFuncs.saveDefaultPlayerInfo();
		}
		pk.pkFuncs.loadPlayerInfoFile();
		pk.pkFuncs.loadUsersPlayerInfo();

		pk.pkFuncs.intMaps();
		pk.pkFuncs.loadScore();
		pk.pkFuncs.loadToggleMap();
		pk.pkFuncs.loadLobby();
		pk.pkFuncs.intCheckpointsLoc();

		pk.pkItems.loadStartItems();
		
		Logger log = pk.getLogger();
		String space = "* ";
		String dashes = "-------------------------------------";
		
		log.info(dashes);
		int totalUnlocks = pk.pkUnlockFuncs.getUnlocksConfig().getConfigurationSection("username").getKeys(false).size();
		int totalPInfo = pk.pkFuncs.getPlayerInfoConfig().getConfigurationSection("username").getKeys(false).size();
		if(Bukkit.getServer().getOnlinePlayers().size() > 0) {
			log.info("There are " + Bukkit.getOnlinePlayers().size() + " players online.");
			log.info(space + "Player Info's Loaded: " + pk.pkVars.loadedUsers.size() + "/" + totalPInfo);
			log.info(space + "Player Unlock's Loaded: " + pk.pkVars.loadedPUnlocks.size() + "/" + totalUnlocks);
		} else {
			log.info("There are no players online.");
			log.info(space + "Total Player Info's: " + totalPInfo);
			log.info(space + "Total Player Unlocks's: " + totalUnlocks);
		}
		log.info(dashes);
		
	}

	public void LoadCfg() {
		FileConfiguration cfg = pk.getConfig();

		// Options
		cfg.addDefault("options.InvincibleWhileParkour", true);
		cfg.addDefault("options.RespawnOnLava", true);
		cfg.addDefault("options.RespawnOnWater", true);
		cfg.addDefault("options.CheckpointEffect", true);
		cfg.addDefault("options.removePotionsEffectsOnParkour", false);
		cfg.addDefault("options.setFullHungerOnParkour", false);
		cfg.addDefault("options.LastCheckpointTeleport", true);
		cfg.addDefault("options.slime_cmd", "pk cp");
		cfg.addDefault("options.musicdisk_cmd", "pk time");

		// Rewards
		cfg.addDefault("rewards.enable", false);
		cfg.addDefault("rewards.cooldown", 300);
		cfg.addDefault("rewards.cooldownMessage", "You will receive your next reward on this map in TIME");
		cfg.addDefault("rewards.rewardIfBetterScore", true);

		cfg.addDefault("rewards.money.enable", false);
		cfg.addDefault("rewards.money.amount", 10);
		cfg.addDefault("rewards.money.message", "&bYou have received MONEYAMOUNT Dollars");
		cfg.addDefault("rewards.command.enable", false);
		cfg.addDefault("rewards.command.cmd", "give PLAYER 5 10");
		cfg.addDefault("rewards.command.message", "&bYou have received 5 wood!");

		cfg.addDefault("options.BroadcastOnRecord.enable", true);
		cfg.addDefault("options.BroadcastOnRecord.message", "&7&oPLAYER &aset a new record of &7&oTIME &aon &7&oMAPNAME");
		cfg.addDefault("options.PrefixString", "PK");
		
		ArrayList<String> newAllow = new ArrayList<String>();
		newAllow.add("pk");
		cfg.addDefault("options.AllowedCommands", newAllow);

		cfg.addDefault("Parkour.mapsnumber", 0);

		cfg.options().copyDefaults(true);
		pk.saveDefaultConfig();
		pk.saveConfig();

		pk.pkVars.setRemovePotionsEffectsOnParkour(cfg.getBoolean("options.removePotionsEffectsOnParkour"));
		pk.pkVars.setInvincibleWhileParkour(cfg.getBoolean("options.InvincibleWhileParkour"));
		pk.pkVars.setCheckpointEffect(cfg.getBoolean("options.CheckpointEffect"));
		pk.pkVars.setBroadcastMessage(cfg.getBoolean("options.BroadcastOnRecord.enable"));
		pk.pkVars.setFullHunger(cfg.getBoolean("options.setFullHungerOnParkour"));
		pk.pkVars.LastCheckpointTeleport = cfg.getBoolean("options.LastCheckpointTeleport");

		pk.pkVars.setRewardIfBetterScore(cfg.getBoolean("rewards.rewardIfBetterScore"));
		pk.pkVars.rewardEnable = cfg.getBoolean("rewards.enable");

		if (pk.pkVars.isBroadcastMessage()) {
			pk.pkVars.setBroadcastMsg(cfg.getString("options.BroadcastOnRecord.message"));
		}

		pk.pkVars.PrefixString = cfg.getString("options.PrefixString");
		
		pk.pkVars.allowedCommands = new ArrayList<String>();
		for(Object item: cfg.getList("options.AllowedCommands")) {
			pk.pkVars.allowedCommands.add(item.toString().toLowerCase());
		}

		//ParkourItems Options
		pk.pkItems.slime_cmd = cfg.getString("options.slime_cmd");
		pk.pkItems.musicdisk_cmd = cfg.getString("options.musicdisk_cmd");

		loadStrings();


	}

	public void reloadCfg() {
		pk.reloadConfig();
		LoadCfg();
		loadStrings();
		pk.pkUnlockFuncs.reloadUnlocksConfig();
	}

	//Load in Strings.yml
	public void loadStrings() {
		saveDefaultStrings();
		File stringsFile = pk.pkVars.stringsFile;
		if (stringsFile == null) {
			stringsFile = pk.pkVars.stringsFile;
		}
		pk.pkVars.stringsConfig = YamlConfiguration.loadConfiguration(stringsFile);

		// Look for defaults in the jar
		InputStream defConfigStream = pk.getResource("Strings.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			pk.pkVars.stringsConfig.setDefaults(defConfig);
			saveDefaultStrings();
		}

		storeStrings();
	}

	//Store Strings
	public void storeStrings() {
		FileConfiguration stringsConfig = getStringsConfig();
		ParkourStrings strings = pk.pkStrings;

		//Load Prefixes
		strings.PREFIX = colorParseString(stringsConfig.getString("Prefix"));		
		strings.APREFIX = colorParseString(stringsConfig.getString("AdminPrefix"));

		//Load Default Color
		strings.defaultColor = colorParseString(stringsConfig.getString("DefaultColor"));
		strings.defaultError = colorParseString(stringsConfig.getString("DefaultError"));
		strings.highlightOne = colorParseString(stringsConfig.getString("HightlightOne"));
		strings.highlightTwo = colorParseString(stringsConfig.getString("HightlightTwo"));


	}

	//Get Strings FileConfig
	public FileConfiguration getStringsConfig() {
		FileConfiguration stringsConfig = pk.pkVars.stringsConfig;
		if (stringsConfig == null) {
			loadStrings();
		}
		return stringsConfig;
	}

	//Save Default Strings
	public void saveDefaultStrings() {
		File stringsFile = pk.pkVars.stringsFile;
		if (!stringsFile.exists()) {            
			pk.saveResource(stringsFile.getName(), false);
		}
	}

	//Parse a string for ChatColor. Replaces & with ChatColor
	public String colorParseString(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}


}
