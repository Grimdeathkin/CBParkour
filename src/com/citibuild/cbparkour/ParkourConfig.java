package com.citibuild.cbparkour;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

		cfg.addDefault("Parkour.mapsnumber", 0);
		cfg.options().copyDefaults(true);
		pk.saveDefaultConfig();
		pk.saveConfig();

		pk.pkVars.removePotionsEffectsOnParkour = cfg.getBoolean("options.removePotionsEffectsOnParkour");
		pk.pkVars.InvincibleWhileParkour = cfg.getBoolean("options.InvincibleWhileParkour");
		pk.pkVars.CheckpointEffect = cfg.getBoolean("options.CheckpointEffect");
		pk.pkVars.BroadcastMessage = cfg.getBoolean("options.BroadcastOnRecord.enable");
		pk.pkVars.FullHunger = cfg.getBoolean("options.BroadcastOnRecord.enable");
		pk.pkVars.LastCheckpointTeleport = cfg.getBoolean("options.LastCheckpointTeleport");

		pk.pkVars.rewardIfBetterScore = cfg.getBoolean("rewards.rewardIfBetterScore");
		pk.pkVars.rewardEnable = cfg.getBoolean("rewards.enable");

		if (pk.pkVars.BroadcastMessage) {
			pk.pkVars.BroadcastMsg = cfg.getString("options.BroadcastOnRecord.message");
		}

		pk.pkVars.PrefixString = cfg.getString("options.PrefixString");

		//ParkourItems Options
		pk.pkItems.slime_cmd = cfg.getString("options.slime_cmd");
		pk.pkItems.musicdisk_cmd = cfg.getString("options.musicdisk_cmd");

		loadStrings();


	}

	public void reloadCfg() {
		pk.reloadConfig();
		LoadCfg();
		loadStrings();
	}

	//Load in Strings.yml
	public void loadStrings() {
		saveDefaultStrings();
		File stringsFile = pk.pkVars.stringsFile;
		if (stringsFile == null) {
			stringsFile = pk.pkVars.playerInfoFile;
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
		
		//Load Error Messages
		strings.spawnNotSet = colorParseString(stringsConfig.getString("Errors.SpawnNotSet"));
		strings.notUnlocked = colorParseString(stringsConfig.getString("Errors.NotUnlocked"));
		strings.disabled = colorParseString(stringsConfig.getString("Errors.Disabled"));
		strings.invalidMap = colorParseString(stringsConfig.getString("Errors.InvalidMap"));
		strings.notInPK = colorParseString(stringsConfig.getString("Errors.NotInPK"));
		strings.noPermission = colorParseString(stringsConfig.getString("Errors.NoPermission"));
		strings.noPKPermission = colorParseString(stringsConfig.getString("Errors.NoPKPermission"));
		strings.notExist = colorParseString(stringsConfig.getString("Errors.NotExist"));
		strings.startAtFirstCP = colorParseString(stringsConfig.getString("Errors.StartAtFirst"));
		
		//Load In-Parkour Messages
		strings.welcome = colorParseString(stringsConfig.getString("InPK.Welcome"));
		strings.leave = colorParseString(stringsConfig.getString("InPK.Leave"));
		strings.tpToLobby = colorParseString(stringsConfig.getString("InPK.Lobby"));
		strings.timerStart = colorParseString(stringsConfig.getString("InPK.TimerStart"));
		strings.restartTimer = colorParseString(stringsConfig.getString("InPK.RestartTimer"));
		strings.notInThisPK = colorParseString(stringsConfig.getString("InPK.NotInThisPK"));
		strings.firstFinish = colorParseString(stringsConfig.getString("InPK.FirstFinish"));
		strings.globalRecord = colorParseString(stringsConfig.getString("InPK.GlobalRecord"));
		strings.beatTime = colorParseString(stringsConfig.getString("InPK.BeatTime"));
		strings.notBeatTime = colorParseString(stringsConfig.getString("InPK.NotBeatTime"));
		strings.finishCourse = colorParseString(stringsConfig.getString("InPK.FinishCourse"));
		strings.newCP = colorParseString(stringsConfig.getString("InPK.Checkpoint"));
		strings.cpPrevReached = colorParseString(stringsConfig.getString("InPK.CPReached"));
		strings.cpMissing = colorParseString(stringsConfig.getString("InPK.CPMissing"));
		strings.newGlobalRecord = colorParseString(stringsConfig.getString("InPK.NewGlobalRecord"));


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
