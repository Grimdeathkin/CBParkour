package com.citibuild.cbparkour;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/*
 * TODO 
 * - Convert instances of mapNumber to mapID for legibility and clarification.
 * - Begin getUnlock function to return a list of map IDs a player has unlocked.
 * - Separate events into classes (PlayerListener, EntityListener, SignListener).
 * - Add configurable strings (or perhaps add a main colour choice for strings). NB: Josh wants all server plugin colour schemes to be GREEN / WHITE.
 * - Extension on previous point, separate config into separate class.
 * - Add userInfo command to get unlocks and if they hold map records.
 * - Add sign leaderboard system.
 * - Integrate slimeball commander into CBParkour.
 * - Add system to restore gamemode on parkour finish.
 * - Add system to stop admins changing gamemode during parkour.
 * - Add reload command for configurations.
 * - Add system to save progress on disconnect or reload.
 * - Add ranking system and chat prefixing (like SkyWars).
 * - Figure out how to seduce bsquid... beer!.
 * - Add time command to get current time.
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
	
/*
 * 	Setup
 */
	private static final Logger log = Logger.getLogger("Minecraft");

        @Override
	public void onEnable() {
		
		LoadCfg();
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
		this.getCommand("pk").setExecutor(new ParkourCommand(this));
		
		if (!scores.getAbsoluteFile().exists()) {
			try {
				scores.createNewFile();
				saveScore();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}

		intMaps();
		loadScore();
		loadToggleMap();
		loadLobby();
		intCheckpointsLoc();
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
		
		intMaps();
		loadScore();
		loadToggleMap();
		loadLobby();
		intCheckpointsLoc();
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
			debug("Vault not found. Disabling money reward.");
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
	
 	public void teleportFirstCheckpoint(Player p){

		FileConfiguration cfg = getConfig();
		Location firstCheckpoint;

		int MapNumber = getPlMapNumber(ParkourContainer.get(p.getName()));

		if (cfg.contains("Parkour.map" + MapNumber + ".spawn")) {
			firstCheckpoint = new Location(
					getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
					cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posX"), cfg.getDouble("Parkour.map"
							+ MapNumber + ".spawn.posY"), cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posZ"));

			firstCheckpoint.setPitch((float) cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posPitch"));
			firstCheckpoint.setYaw((float) cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posYaw"));

			p.teleport(firstCheckpoint);
		} else {
			firstCheckpoint = new Location(
					getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
					cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posX") + 0.5, cfg.getDouble("Parkour.map"
							+ MapNumber + ".cp.1.posY"),
					cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posZ") + 0.5);


			firstCheckpoint.setPitch(p.getLocation().getPitch());
			firstCheckpoint.setYaw(p.getLocation().getYaw());
			p.teleport(firstCheckpoint);
		}
 	}
	
	public void teleportLastCheckpoint(Player p) {
		FileConfiguration cfg = getConfig();
		Location lastCheckpoint;

		int MapNumber = getPlMapNumber(ParkourContainer.get(p.getName()));
		int PlCheckpoint = getPlCheckpoint(ParkourContainer.get(p.getName()));

		if (PlCheckpoint == 1 || !LastCheckpointTeleport) // Teleport to map spawn
		{
			teleportFirstCheckpoint(p);
		} else {
			lastCheckpoint = new Location(getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posX") + 0.5,
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posY"),
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posZ") + 0.5);

			lastCheckpoint.setPitch(p.getLocation().getPitch());
			lastCheckpoint.setYaw(p.getLocation().getYaw());
			p.teleport(lastCheckpoint);
		}
	}

	public void setPlCheckpoint(String p, int Cp) {
		String HashTableSrc = ParkourContainer.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String CpFinal = Splitter[0] + "_" + Splitter[1] + "_" + Cp;
		ParkourContainer.put(p, CpFinal);
	}

	public void setPlTime(String p, Long Time) {
		String HashTableSrc = ParkourContainer.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String TimeFinal = Splitter[0] + "_" + Time + "_" + Splitter[2];
		ParkourContainer.put(p, TimeFinal);
	}

	public Long getPlTime(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Long.valueOf(Splitter[1]);
	}

	public int getPlCheckpoint(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Integer.parseInt(Splitter[2]);
	}

	public int getPlMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Integer.parseInt(Splitter[0]);
	}

	public int getCpMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Integer.parseInt(Splitter[0]);
	}

	public int getCheckpoint(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Integer.parseInt(Splitter[1]);
	}

	public int getCfgTotalCheckpoints(int mapNumber) {
		return getConfig().getInt("Parkour.map" + mapNumber + ".numberCp");
	}

	private boolean mapExist(String MapNumber) {
            return getConfig().getInt("Parkour.map" + MapNumber + ".numberCp") != 0;
	}

	public boolean isNumber(String number) {
		try {
			Integer.parseInt(number);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void intCheckpointsLoc() {
		cLoc.clear();
		FileConfiguration cfg = getConfig();
		for (int mapNumber : maps) {
			for (int i = cfg.getInt("Parkour.map" + mapNumber + ".numberCp"); i >= 1; i--) {
				Location loc = new Location(getServer().getWorld(cfg.getString("Parkour.map" + mapNumber + ".world")),
						cfg.getInt("Parkour.map" + mapNumber + ".cp." + i + ".posX"), cfg.getInt("Parkour.map"
								+ mapNumber + ".cp." + i + ".posY"), cfg.getInt("Parkour.map" + mapNumber + ".cp." + i
								+ ".posZ"));
				String HashTable = mapNumber + "_" + i;
				cLoc.put(loc, HashTable);
			}
		}
	}

	public void intMaps() {
		maps.clear();
		String mapList = getConfig().getConfigurationSection("Parkour").getKeys(false).toString().replaceAll("\\s", "")
				.replace("[", "").replace("]", "");
		String[] mapsSplit = mapList.split(",");
		for (int i = getConfig().getInt("Parkour.mapsnumber"); i >= 0; i--) {
			if (mapExist(mapsSplit[i].substring(3))) {
				maps.add(Integer.parseInt(mapsSplit[i].substring(3)));
			}
		}
		Collections.sort(maps);
	}

	public void loadToggleMap() {
		toggleParkour.clear();
		for (int mapNumber : maps) {
			if (getConfig().contains("Parkour.map" + mapNumber + ".toggle")) {
				toggleParkour.put(mapNumber, getConfig().getBoolean("Parkour.map" + mapNumber + ".toggle"));
			}
		}
	}

	private void LoadCfg() {
		FileConfiguration cfg = getConfig();

		// Options
		cfg.addDefault("options.InvincibleWhileParkour", true);
		cfg.addDefault("options.RespawnOnLava", true);
		cfg.addDefault("options.RespawnOnWater", true);
		cfg.addDefault("options.CheckpointEffect", true);
		cfg.addDefault("options.removePotionsEffectsOnParkour", false);
		cfg.addDefault("options.setFullHungerOnParkour", false);
		cfg.addDefault("options.LastCheckpointTeleport", true);

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
		saveConfig();

		removePotionsEffectsOnParkour = cfg.getBoolean("options.removePotionsEffectsOnParkour");
		InvincibleWhileParkour = cfg.getBoolean("options.InvincibleWhileParkour");
		CheckpointEffect = cfg.getBoolean("options.CheckpointEffect");
		BroadcastMessage = cfg.getBoolean("options.BroadcastOnRecord.enable");
		FullHunger = cfg.getBoolean("options.BroadcastOnRecord.enable");
		LastCheckpointTeleport = cfg.getBoolean("options.LastCheckpointTeleport");
			
		rewardIfBetterScore = cfg.getBoolean("rewards.rewardIfBetterScore");
		rewardEnable = cfg.getBoolean("rewards.enable");

		if (BroadcastMessage) {
			BroadcastMsg = cfg.getString("options.BroadcastOnRecord.message");
		}
		
		PrefixString = cfg.getString("options.PrefixString");

	}

	public void loadLobby() {
		FileConfiguration cfg = getConfig();

		if (cfg.contains("Lobby")) {
			lobby = null;
			Location loc = new Location(getServer().getWorld(cfg.getString("Lobby.world")),
					cfg.getDouble("Lobby.posX"), cfg.getDouble("Lobby.posY"), cfg.getDouble("Lobby.posZ"));
			loc.setPitch((float) cfg.getDouble("Lobby.posPitch"));
			loc.setYaw((float) cfg.getDouble("Lobby.posYaw"));
			lobby = loc;
		}
	}
	
	/*
	 * 2notnumber = Second line is not a number
	 * badmap = Map not recognized
	 * noPermission = No permission to perform the action
	 * noParkourPermission = user does not have parkour.use
	 * badsign = Sign does not yet exist
	 * parkourDisabled = parkour is toggled to disabled
	 */
	public static void sendError(String status, Player player, Plugin plugin) {
		String APREFIX = ((Parkour) plugin).getAPrefix();
		String PREFIX = ((Parkour) plugin).getPrefix();
		if(status.equalsIgnoreCase("2notnumber")) {
			player.sendMessage(APREFIX + "The second line must be a number. Please try again.");
			
		} else if(status.equalsIgnoreCase("badmap")) {
			player.sendMessage(APREFIX + "That map is not recognized. Please try again.");
			
		} else if(status.equalsIgnoreCase("noPermission")) {
			player.sendMessage(PREFIX + "You do not have permission to do that.");
			
		} else if (status.equalsIgnoreCase("noParkourPermission")){
			player.sendMessage(PREFIX + ChatColor.RED + "You don't have permission to do this parkour");
			
		} else if(status.equalsIgnoreCase("badsign")) {
			player.sendMessage(APREFIX + "That sign is not recognized. Please try again.");
			
		} else if(status.equalsIgnoreCase("parkourDisabled")){
			player.sendMessage(PREFIX + "This parkour is" + ChatColor.RED + " disabled");
			
		}
	}
	
	/*
	 * notUnlocked - User has not unlocked this map
	 * mapUnlock - User has unlocked the next map
	 */
	public static void sendInfo(String info, Player player, int mapNumber, Plugin plugin){
		String PREFIX = ((Parkour) plugin).getPrefix();
		String nextMapName = ((Parkour) plugin).getMapName(((Parkour) plugin).getMapNext(mapNumber));
		String prevMapName = ((Parkour) plugin).getMapName(((Parkour) plugin).getMapPrevious(mapNumber));
		if(info.equalsIgnoreCase("notUnlocked")) {
			player.sendMessage(PREFIX + RED + "You have not unlocked this parkour, complete "+GREEN + prevMapName +RED+" to progress");

		} else if(info.equalsIgnoreCase("mapUnlock")) {
		player.sendMessage(PREFIX + GOLD + "Map unlocked! - "+ GREEN + nextMapName);
			
		}

	}
	
/* Note Pitch | Note Pitch
	 * f#	0.500 | F#	1.000
	 * G	0.525 | G	1.050
	 * G#	0.550 | G#	1.100
	 * A	0.600 | A	1.200
	 * A#	0.650 | A#	1.250
	 * B	0.675 | B	1.350
	 * C	0.700 | C	1.400
	 * C#	0.750 | C#	1.350
	 * D	0.800 | D	1.600
	 * D#	0.850 | D#	1.700
	 * E	0.900 | E	1.750
	 * F	0.950 | F	1.900
	 */	
	public void playJingle(final Player player){
		new BukkitRunnable(){
		    int count = 0;
                    @Override
		    public void run(){
		        if(count < 4){
		        	switch (count){
		        	case 0:
		        		player.playSound(player.getLocation(), Sound.NOTE_PIANO,1,0.700f);
		        		break;
		        	case 1:
		        		player.playSound(player.getLocation(), Sound.NOTE_PIANO,1,0.900f);

		        		break;
		        	case 2:
		        		player.playSound(player.getLocation(), Sound.NOTE_PIANO,1,1.050f);
		        		break;
		        	case 3:
		        		player.playSound(player.getLocation(), Sound.NOTE_PIANO,1,0.700f);
		        		player.playSound(player.getLocation(), Sound.NOTE_PIANO,1,1.400f);
		        		player.playSound(player.getLocation(), Sound.ORB_PICKUP,0.1f,0.700f);
		        		break;
		        	}
		        }
		        else{
		            cancel();
		        }
		        count++;
		    }
		}.runTaskTimer(this, 0L, 2L);
	}

	public int maxMapNumber() {
		return getConfig().getInt("Parkour.mapsnumber");
	}
	
/*
 *  Player Functions
 */
	public boolean isPlayerInParkour(Player player) {
		if(ParkourContainer.containsKey(player.getName())) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unused")
	private void getUnlocks(Player p){
		/* TODO
		 * getUnlocks function
		 */
		
	}
	
	public void saveScore() {
		try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream((path))))) {
                oos.writeObject(Records);
                oos.flush();
            }
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadScore() {
		try {
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)))) {
                Records.clear();
                Records = (HashMap<String, Long>) ois.readObject();
            }
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace(System.out);
		}
	}

	public int toInt(String msg) {
		return Integer.parseInt(msg);
	}

	private void debug(String msg) {
		System.out.println("[ CBParkourDebug ] " + msg);
	}

	public void giveReward(Player p) {
		if (rewardEnable) {
			FileConfiguration cfg = getConfig();

			boolean rewardMoneyEnable = cfg.getBoolean("rewards.money.enable");
			boolean rewardCommandEnable = cfg.getBoolean("rewards.command.enable");

			String rewardMoneyMsg = cfg.getString("rewards.money.message");
			String rewardCommandMsg = cfg.getString("rewards.command.message");

			String rewardCmd = cfg.getString("rewards.command.cmd");
			int rewardMoney = cfg.getInt("rewards.money.amount");

			int rewardCooldown = cfg.getInt("rewards.cooldown");
			String rewardCooldownMsg = cfg.getString("rewards.cooldownMessage");

			if (!rewardPlayersCooldown.containsKey(p.getName())) {
				if (rewardMoneyEnable && rewardMoney > 0) {
					rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					if (vault) economy.depositPlayer(p.getName(), rewardMoney);
					p.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', rewardMoneyMsg).replaceAll("MONEYAMOUNT",
							"" + rewardMoney));
				}
				if (rewardCommandEnable) {
					rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					getServer().dispatchCommand(getServer().getConsoleSender(),
							rewardCmd.replaceAll("PLAYER", p.getName()));
					p.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', rewardCommandMsg));
				}
			} else {
				if (System.currentTimeMillis() - rewardPlayersCooldown.get(p.getName()) >= rewardCooldown * 1000) {
					if (rewardMoneyEnable && rewardMoney > 0) {
						rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						if (vault) economy.depositPlayer(p.getName(), rewardMoney);
						p.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', rewardMoneyMsg).replaceAll(
								"MONEYAMOUNT", "" + rewardMoney));
					}
					if (rewardCommandEnable) {
						rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						getServer().dispatchCommand(getServer().getConsoleSender(),
								rewardCmd.replaceAll("PLAYER", p.getName()));
						p.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', rewardCommandMsg));
					}
				} else {
					long time = (System.currentTimeMillis() - rewardPlayersCooldown.get(p.getName()));

					int ms1 = (int) time;
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

					if (secs < 10) {
						secsS = "0" + secsS;
					}
					if (mins < 10) {
						minsS = "0" + minsS;
					}
					if (hours < 10) {
						hoursS = "0" + hoursS;
					}

					p.sendMessage(PREFIX + rewardCooldownMsg.replaceAll("TIME", hoursS + "h:" + minsS + "m:" + secsS + "s"));
				}
			}
		}
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
                        @Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

/*
 *  Public API
 */

	/**
	 * Returns all Records on the given Map - <Playername, Time>
	 * @param map
	 * @return a map of records sorted by value
	 */
	public Map<String, Long> getRecords(int map) {
		Map<String, Long> records = new HashMap<>();
		for (String m : Records.keySet()) {
			String[] s = m.split(":");
			if (toInt(s[0]) == map) {
				records.put(s[1], Records.get(m));
			}
		}
		return sortByValue(records);
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
	 * @param mapNumber
	 * @return the mapID of the previous map from config or 0 if none found
	 */
	public int getMapPrevious(int mapNumber) {
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapPrevious")) {
			return getConfig().getInt("Parkour.map" + mapNumber + ".mapPrevious");

		} else {
			return 0;
		}
	}
	
	/**
	 * Gets the next mapID in the unlock chain for a given mapID
	 * @param mapNumber
	 * @return the mapID of the next map from config or 0 if none found
	 */
	public int getMapNext(int mapNumber) {
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapNext")) {
			return getConfig().getInt("Parkour.map" + mapNumber + ".mapNext");

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