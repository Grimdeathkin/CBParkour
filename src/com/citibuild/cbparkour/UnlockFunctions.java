package com.citibuild.cbparkour;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class UnlockFunctions {

	Parkour pk;

	private FileConfiguration playerUnlocksConfig = null;
	private File playerUnlocksFile = null;

	public UnlockFunctions(Parkour plugin) {
		this.pk = plugin;
	}

	public void reloadUnlocksConfig() {
		if (playerUnlocksFile == null) {
			playerUnlocksFile = new File(pk.getDataFolder(), "PlayerUnlocks.yml");
		}
		playerUnlocksConfig = YamlConfiguration.loadConfiguration(playerUnlocksFile);

		// Look for defaults in the jar
		InputStream defConfigStream = pk.getResource("PlayerUnlocks.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			playerUnlocksConfig.setDefaults(defConfig);
		}
	}

	private FileConfiguration getUnlocksConfig() {
		if (playerUnlocksConfig == null) {
			reloadUnlocksConfig();
		}
		return playerUnlocksConfig;
	}

	public void saveUnlocksConfig() {
		if (playerUnlocksConfig == null || playerUnlocksFile == null) {
			return;
		}
		try {
			getUnlocksConfig().save(playerUnlocksFile);
		} catch (IOException ex) {
			pk.getLogger().log(Level.SEVERE, "Could not save config to " + playerUnlocksFile, ex);
		}
	}

	public void saveDefaultUnlocksConfig() {
		if (playerUnlocksFile == null) {
			playerUnlocksFile = new File(pk.getDataFolder(), "PlayerUnlocks.yml");
		}
		if (!playerUnlocksFile.exists()) {            
			pk.saveResource("PlayerUnlocks.yml", false);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadPlayer(Player player) {
		PlayerUnlocks pUnlocks = new PlayerUnlocks(player);
		String unlocksPath = "username." + player.getName().toLowerCase() + ".unlocks";
		ArrayList<String> unlocks = new ArrayList<String>();
		
		if(!getUnlocksConfig().contains(unlocksPath)) {
			getUnlocksConfig().createSection(unlocksPath);
			unlocks.add("0");
		} else {
			unlocks = (ArrayList<String>) getUnlocksConfig().getList(unlocksPath);
		}
		
		pUnlocks.setUnlocks(unlocks);
		pk.pkVars.loadedPUnlocks.put(pUnlocks.getUsername(), pUnlocks);

	}

	public void savePlayer(Player player) {
		if(pk.pkVars.loadedPUnlocks.containsKey(player.getName().toLowerCase())) {
			HashMap<String, PlayerUnlocks> loadedPUnlocks = pk.pkVars.loadedPUnlocks;
			PlayerUnlocks pUnlocks = loadedPUnlocks.get(player.getName().toLowerCase());
			FileConfiguration config = getUnlocksConfig();
			String userPath = "username." + pUnlocks.getUsername();
			config.createSection(userPath);
			config.set(userPath + ".unlocks", pUnlocks.getUnlocks());

			saveUnlocksConfig();
		}

	}

	public void saveAllPlayerUnlocks() {
		for(Player player: pk.getServer().getOnlinePlayers()) {
			savePlayer(player);
		}
	}

	public void loadAllPlayerUnlocks() {
		for(Player player: pk.getServer().getOnlinePlayers()) {
			loadPlayer(player);
		}
	}

	public PlayerUnlocks getPlayerUnlocks(Player player) {
		if(!pk.pkVars.loadedPUnlocks.containsKey(player.getName().toLowerCase())) {
			loadPlayer(player);
		}

		return pk.pkVars.loadedPUnlocks.get(player.getName().toLowerCase());
	}

	@SuppressWarnings("unchecked")
	public PlayerUnlocks loadOfflinePlayer(OfflinePlayer oPlayer) {
		PlayerUnlocks pUnlocks = new PlayerUnlocks((Player) oPlayer);
		String userPath = "username." + pUnlocks.getUsername();
		getUnlocksConfig().createSection((userPath + ".unlocks"));
		ArrayList<String> unlocks = new ArrayList<String>();
		if(unlocks.isEmpty() || unlocks == null) {
			unlocks.add("0");

		}
		unlocks = (ArrayList<String>) getUnlocksConfig().getList(userPath + ".unlocks");

		pUnlocks.setUnlocks(unlocks);

		return pUnlocks;
	}

	@SuppressWarnings("unchecked")
	public List<String> getPlayerUnlocks(OfflinePlayer oPlayer) {
		return (List<String>) getUnlocksConfig().getList("username." + oPlayer.getName().toLowerCase() + ".unlocks");
	}

	public void resetPlayerUnlocks(String username) {
		ArrayList<String> newUnlocks = new ArrayList<String>();
		newUnlocks.add("0");
		getUnlocksConfig().set("username." + username.toLowerCase() + ".unlocks", newUnlocks);
		saveUnlocksConfig();
	}

	public void addUnlockedLevel(Player player, String level) {
		PlayerUnlocks pUnlocks = pk.pkVars.loadedPUnlocks.get(player.getName().toLowerCase());
		if(!pUnlocks.getUnlocks().contains(level) && !pUnlocks.getUnlocks().contains("*")) {
			ArrayList<String> unlocks = pUnlocks.getUnlocks();
			unlocks.add(level);
			pUnlocks.setUnlocks(unlocks);
			savePlayer(player);
		}
	}

	public boolean levelUnlocked(Player player, Integer level) {
		PlayerUnlocks pUnlocks = pk.pkVars.loadedPUnlocks.get(player.getName().toLowerCase());
		int prevMap = pk.getMapPrevious(level);

		if(prevMap == 0) {
			return true;
		}
		if(pUnlocks.getUnlocks().contains(String.valueOf(prevMap))) {
			return true;
		}
		if(pUnlocks.getUnlocks().contains("*")) {
			return true;
		}

		return false;
	}

	public void lockLevel(Player player, Integer level) {
		PlayerUnlocks pUnlocks = pk.pkVars.loadedPUnlocks.get(player.getName().toLowerCase());
		ArrayList<String> unlocks = pUnlocks.getUnlocks();
		unlocks.remove(String.valueOf(level));
		pUnlocks.setUnlocks(unlocks);
		savePlayer(player);

	}

}
