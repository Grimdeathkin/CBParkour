package com.citibuild.cbparkour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.citibuild.cbparkour.Listeners.PlayerListener;
import com.citibuild.cbparkour.Listeners.SignListener;


/*
 * TODO
 * - Add userInfo command to get unlocks and if they hold map records.
 * - Add sign leaderboard system.
 * - Add ranking system and chat prefixing (like SkyWars).
 * - Figure out how to seduce bsquid... beer!.
 */

public class Parkour extends JavaPlugin implements Listener {

	// Vault initiation
	public static Economy economy = null;
	public static Permission permission = null;
	private boolean vault;
	
	//Class definitions
	public ParkourItems pkItems;
	public ParkourFunctions pkFuncs;
	public ParkourVariables pkVars;
	public ParkourStrings pkStrings;
	public ParkourConfig pkConfig;
	public UnlockFunctions pkUnlockFuncs;
	
/*
 * 	Setup
 */
	private static final Logger log = Logger.getLogger("Minecraft");

        @Override
	public void onEnable() {
        pkItems = new ParkourItems(this);
        pkFuncs = new ParkourFunctions(this);
        pkVars = new ParkourVariables(this);
        pkStrings = new ParkourStrings(this);
        pkConfig = new ParkourConfig(this);
        pkUnlockFuncs = new UnlockFunctions(this);
        
        pkConfig.onEnable();
		
		if (!setupPermissions() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		setupEconomy();

		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new SignListener(this), this);
		this.getCommand("pk").setExecutor(new ParkourCommand(this));
	}

	@Override
	public void onDisable() {
		
		pkFuncs.saveAllPlayerInfo();
		pkFuncs.savePlayerInfoFile();
		pkUnlockFuncs.saveAllPlayerUnlocks();
		
		// Reset everything
		pkVars.setNewMap(false);
		pkVars.getNewMapCheckpoints().clear();
		pkVars.newMapPrevious = 0;
		pkVars.setNewMapNext(0);
		pkVars.setNewMapName("");
		pkVars.newMapPlayerEditor = "";
		pkVars.setNewMapNumber(0);
		pkVars.setCheckpointNumber(0);

		pkVars.maps.clear();
		pkVars.toggleParkour.clear();
		pkVars.getcLoc().clear();
		pkVars.ParkourContainer.clear();
		pkVars.getRecords().clear();
		pkVars.rewardPlayersCooldown.clear();
		
		pkFuncs.intMaps();
		pkFuncs.loadScore();
		pkFuncs.loadToggleMap();
		pkFuncs.loadLobby();
		pkFuncs.intCheckpointsLoc();
	}

	private boolean setupPermissions(){
		 if (getServer().getPluginManager().getPlugin("Vault") == null) {
	            return false;
	        }
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
	
	private boolean setupEconomy() {
		try {
			Class.forName("net.milkbowl.vault.economy.Economy");
		} catch (ClassNotFoundException e) {
			pkFuncs.debug("Vault not found. Disabling money reward.");
			getConfig().set("rewards.money.enable", false);
			saveConfig();
			return false;
		}
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(
				net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}
	
/*
 * 	Functions
 */
	
 	//bsquidwrd moved to ParkourFunctions 8:21 PM PST November 30, 2013

/*
 *  Public API
 */

	/**
	 * Gets a players unlocked maps
	 * @param p
	 */
	public ArrayList<Integer> getUnlocks(Player p){
		ArrayList<Integer> unlockedMaps = new ArrayList<>();
		for (int i : pkVars.maps) {
			//if(permission.has(p, "parkour.completed.map"+i)){
			if(pkUnlockFuncs.levelUnlocked(p, i)) {
				unlockedMaps.add(i);
			}
		}
		return unlockedMaps;
	}
	
	public int getRank(String playerName, int mapID){
		Map<String, Long> records = getRecords(mapID); //Player, Time
		int counter = 1;
		for (String s : records.keySet()){
			if (s.equalsIgnoreCase(playerName)) break;
			counter++;
		}
		return counter;
	}
	
	/**
	 * Returns all Records on the given Map - <Playername, Time>
	 * @param map
	 * @return a map of records sorted by value
	 */
	public Map<String, Long> getRecords(int map) {
		Map<String, Long> records = new HashMap<>();
		for (String m : pkVars.getRecords().keySet()) {
			String[] s = m.split(":");
			if (pkFuncs.toInt(s[0]) == map) {
				records.put(s[1], pkVars.getRecords().get(m));
			}
		}
		return pkFuncs.sortByValue(records);
	}

	/**
	 * Converts a time in ms into a user readable format
	 * @param ms
	 * @return readable time values
	 */
	public String convertTime(long ms) {
		int ms1 = (int) ms;
		int secs = ms1 / 1000;
		int mins = secs / 60;
		int hours = mins / 60;

		hours %= 24;
		secs %= 60;
		mins %= 60;
		ms1 %= 1000;

		String hoursS = Integer.toString(hours);
		String secsS = Integer.toString(secs);
		String minsS = Integer.toString(mins);
		String ms2 = Integer.toString(ms1);

		if (secs < 10) {
			secsS = "0" + secsS;
		}
		if (mins < 10) {
			minsS = "0" + minsS;
		}
		if (hours < 10) {
			hoursS = "0" + hoursS;
		}

		return hoursS + "h : " + minsS + "m : " + secsS + "s : " + ms2 + "ms";
	}

	/**
	 * Displays the high-scores of a map to a player
	 * @param mapID
	 * @param player
	 */
	public void displayHighscores(int mapID, Player player) {
		Map<String, Long> records = getRecords(mapID);
		player.sendMessage(ChatColor.GOLD + "---------=[ " + pkStrings.defaultColor + "Best Times for " + pkStrings.highlightTwo + getMapName(mapID) + ChatColor.GOLD + " ]=---------");
		boolean inTopTen = false;
		int counter = 1;
		for (String p : records.keySet()) {
			if (p.equals(player.getName())) inTopTen = true;
			player.sendMessage(pkStrings.defaultColor + "#" + counter + " " + pkStrings.highlightOne + p + ChatColor.GRAY + " - " + pkStrings.highlightOne + convertTime(records.get(p)));
			counter++;
			if (counter == 11) break;
		}
		if (!inTopTen && records.containsKey(player.getName())) {
			player.sendMessage(pkStrings.defaultColor + "-- Your time --");

			counter = 1;
			for (String p : records.keySet()){
				if (p.equals(player.getName())) break;
				counter++;
			}
			player.sendMessage(pkStrings.defaultColor + "#" + counter + " " + pkStrings.highlightOne + player.getName() + ChatColor.GRAY + " - "
					+ pkStrings.highlightOne + convertTime(records.get(player.getName())));
		}
	}

	/**
	 * Gets the top score for a given mapID
	 * @param mapID
	 * @return first entry in records for a map (shortest time)
	 */
	public Entry<String, Long>  getHighscore(int mapID){
		Map<String, Long> records = getRecords(mapID);
		Map.Entry<String, Long> entry = records.entrySet().iterator().next();
		return entry;
	}
	
	/**
	 * Gets the user friendly map name from a given mapID
	 * @param mapID
	 * @return the user friendly map name or "unknownMapName" if none found
	 */
	public String getMapName(int mapID) {
		if (getConfig().contains("Parkour.map" + mapID + ".mapName")) {
			return getConfig().getString("Parkour.map" + mapID + ".mapName");
		} else {
			return "unknownMapName";
		}
	}

	/**
	 * Gets the previous mapID in the unlock chain for a given mapID
	 * @param mapID
	 * @return the mapID of the previous map from config or 0 if none found
	 */
	public int getMapPrevious(int mapID) {
		if (getConfig().contains("Parkour.map" + mapID + ".mapPrevious")) {
			return getConfig().getInt("Parkour.map" + mapID + ".mapPrevious");

		} else {
			return 0;
		}
	}
	
	/**
	 * Gets the next mapID in the unlock chain for a given mapID
	 * @param mapID
	 * @return the mapID of the next map from config or 0 if none found
	 */
	public int getMapNext(int mapID) {
		if (getConfig().contains("Parkour.map" + mapID + ".mapNext")) {
			return getConfig().getInt("Parkour.map" + mapID + ".mapNext");

		} else {
			return 0;
		}
	}

	/**
	 * Gets a mapID from a user friendly input string
	 * @param mapName
	 * @return the mapID of the given map or 0 if not found
	 */
	public int getMapNumber(String mapName) {
		for (int i : pkVars.maps) {
			if (getConfig().getString("Parkour.map" + i + ".mapName").equals(mapName)) { 
				return i; 
			}
		}
		return 0;
	}
	
	/**
	 * Gets the current configured messaging prefix for users
	 * @return the prefix string
	 */
	public String getPrefix(){
		return pkStrings.PREFIX;
	}

	/**
	 * Gets the current configured messaging prefix for admins
	 * @return the prefix string
	 */
	public String getAPrefix(){
		return pkStrings.APREFIX;
	}

	public boolean isVault() {
		return vault;
	}

	public void setVault(boolean vault) {
		this.vault = vault;
	}
}