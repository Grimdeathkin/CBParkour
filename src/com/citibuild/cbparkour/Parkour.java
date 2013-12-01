package com.citibuild.cbparkour;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * TODO
 * - Begin getUnlock function to return a list of map IDs a player has unlocked.
 * - Add configurable strings (or perhaps add a main colour choice for strings). NB: Josh wants all server plugin colour schemes to be GREEN / WHITE.
 * - Extension on previous point, separate config into separate class.
 * - Add userInfo command to get unlocks and if they hold map records.
 * - Add sign leaderboard system.
 * - Add system to save progress on disconnect or reload.
 * - Add ranking system and chat prefixing (like SkyWars).
 * - Figure out how to seduce bsquid... beer!.
 */

public class Parkour extends JavaPlugin implements Listener {

	// Vault initiation
	public static Economy economy = null;
	public static Permission permission = null;
	boolean vault;
	
	// Used for parkour creation
	ArrayList<Location> newMapCheckpoints = new ArrayList<>();
	boolean newMap = false;
	public String newMapPlayerEditor = "";
	int CheckpointNumber = 0;
	int NewMapNumber = 0;
    String newMapName = null;
	int newMapPrevious = 0;
	int newMapNext = 0;

	// Options
	boolean removePotionsEffectsOnParkour = false;
	boolean BroadcastMessage = false;
	String BroadcastMsg = "&7&oPLAYER &aset a new record of &7&oTIME &aon &7&oMAPNAME";
	String PrefixString = "PK";
	boolean CheckpointEffect = true;
	boolean InvincibleWhileParkour = true;
	boolean FullHunger = false;
	boolean LastCheckpointTeleport = false;
	boolean rewardEnable = false;
	boolean rewardIfBetterScore = true;

	// Used for player parkour management
	Location lobby = null;
	public ArrayList<Integer> maps = new ArrayList<>();
	public HashMap<Integer, Boolean> toggleParkour = new HashMap<>(); // Parkour active or not
	HashMap<Location, String> cLoc = new HashMap<>(); // HashMap infos> Location : mapNumber_Checkpoint
	public HashMap<String, String> ParkourContainer = new HashMap<>(); // HashMap infos> playerName :
																		// mapNumber_parkourStartTime_Chekcpoint
	HashMap<String, Long> Records = new HashMap<>(); // Map:Player, Time
	HashMap<String, Long> rewardPlayersCooldown = new HashMap<>(); // HashMap infos> playerName :
																				// LastRewardTime

	// Used for saving/loading scores
	String path = "plugins" + File.separator + "CBParkour" + File.separator + "PlayersScores.scores";
	File scores = new File(path);

	// Chat colours
	static ChatColor BLACK = ChatColor.BLACK;				//\u00A70
	static ChatColor D_BLUE = ChatColor.DARK_BLUE;			//\u00A71
	static ChatColor D_GREEN = ChatColor.DARK_GREEN;		//\u00A72
	static ChatColor D_AQUA = ChatColor.DARK_AQUA;			//\u00A73
	static ChatColor D_RED = ChatColor.DARK_RED;			//\u00A74
	static ChatColor D_PURPLE = ChatColor. DARK_PURPLE;	//\u00A75
	static ChatColor GOLD = ChatColor.GOLD;				//\u00A76
	static ChatColor D_GRAY = ChatColor.DARK_GRAY;			//\u00A77
	static ChatColor GRAY = ChatColor.GRAY;				//\u00A78
	static ChatColor BLUE = ChatColor.BLUE;				//\u00A79
	static ChatColor GREEN = ChatColor.GREEN;				//\u00A7a
	static ChatColor AQUA = ChatColor.AQUA;				//\u00A7b
	static ChatColor RED = ChatColor.RED;					//\u00A7c
	static ChatColor LIGHT_PURPLE = ChatColor.LIGHT_PURPLE;//\u00A7d
	static ChatColor YELLOW = ChatColor.YELLOW;			//\u00A7e
	static ChatColor WHITE = ChatColor.WHITE;				//\u00A7f	
	// Chat effects
	static ChatColor BOLD = ChatColor.BOLD;				//\u00A7l
	static ChatColor STRIKE = ChatColor.STRIKETHROUGH;		//\u00A7m
	static ChatColor ULINE = ChatColor.UNDERLINE;			//\u00A7n
	static ChatColor ITALIC = ChatColor.ITALIC;			//\u00A7o
	static ChatColor RESET = ChatColor.RESET;				//\u00A7r
	// Prefixes, user and admin
	public String PREFIX;
	public String APREFIX;
	
	//GameMode Variable
	public GameMode prePKGM = GameMode.SURVIVAL;
	
	//Parkour Items and Config Class definition
	public ParkourItems pkItems;
	public ParkourFunctions pkFuncs;
	
/*
 * 	Setup
 */
	private static final Logger log = Logger.getLogger("Minecraft");

        @Override
	public void onEnable() {
        pkItems = new ParkourItems(this);
        pkFuncs = new ParkourFunctions(this);
        	
		pkFuncs.LoadCfg();
		PREFIX = (GRAY+ "[" + D_AQUA + PrefixString + GRAY + "] ");
		APREFIX = (GRAY+ "[" + RED + PrefixString + GRAY + "] ");
		
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
		
		if (!scores.getAbsoluteFile().exists()) {
			try {
				scores.createNewFile();
				pkFuncs.saveScore();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}

		pkFuncs.intMaps();
		pkFuncs.loadScore();
		pkFuncs.loadToggleMap();
		pkFuncs.loadLobby();
		pkFuncs.intCheckpointsLoc();

		pkItems.loadStartItems();
	}

	@Override
	public void onDisable() {
		// Reset everything
		newMap = false;
		newMapCheckpoints.clear();
		newMapPrevious = 0;
		newMapNext = 0;
		newMapName = "";
		newMapPlayerEditor = "";
		NewMapNumber = 0;
		CheckpointNumber = 0;

		maps.clear();
		toggleParkour.clear();
		cLoc.clear();
		ParkourContainer.clear();
		Records.clear();
		rewardPlayersCooldown.clear();
		
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
		for (int i : maps) {
			if(permission.has(p, "parkour.completed.map"+i)){
				unlockedMaps.add(i);
			}
		}
		return unlockedMaps;
	}
	
	/**
	 * Returns all Records on the given Map - <Playername, Time>
	 * @param map
	 * @return a map of records sorted by value
	 */
	public Map<String, Long> getRecords(int map) {
		Map<String, Long> records = new HashMap<>();
		for (String m : Records.keySet()) {
			String[] s = m.split(":");
			if (pkFuncs.toInt(s[0]) == map) {
				records.put(s[1], Records.get(m));
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
		player.sendMessage(GOLD + "---------=[ " + D_AQUA + "Best Times for " + getMapName(mapID) + GOLD + " ]=---------");
		boolean inTopTen = false;
		int counter = 1;
		for (String p : records.keySet()) {
			if (p.equals(player.getName())) inTopTen = true;
			player.sendMessage(WHITE + "#" +AQUA + counter + " " + p + " - " + convertTime(records.get(p)));
			counter++;
			if (counter == 11) break;
		}
		if (!inTopTen && records.containsKey(player.getName())) {
			player.sendMessage(GREEN + "-- Your time --");

			player.sendMessage(WHITE + "#" +AQUA + "x " + player.getName() + " - "
					+ convertTime(records.get(player.getName())));
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
		for (int i : maps) {
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
		return PREFIX;
	}

	/**
	 * Gets the current configured messaging prefix for admins
	 * @return the prefix string
	 */
	public String getAPrefix(){
		return APREFIX;
	}
}