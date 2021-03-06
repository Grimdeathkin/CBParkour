package com.pistonmc.grim.spnparkour;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.scheduler.BukkitRunnable;

public class ParkourFunctions {

	Parkour pk;

	public ParkourFunctions(Parkour plugin) {
		this.pk = plugin;
	}

	/*
	 * Map & Checkpoints functions
	 */
	public int getCpMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
		return Integer.parseInt(Splitter[0]);
	}

	public int getCheckpoint(String HashTable) {
		String[] Splitter = HashTable.split("_");
		return Integer.parseInt(Splitter[1]);
	}

	public int getCfgTotalCheckpoints(int mapID) {
		return pk.getConfig().getInt("Parkour.map" + mapID + ".numberCp");
	}

	public boolean mapExist(String mapID) {
		return pk.getConfig().getInt("Parkour.map" + mapID + ".numberCp") != 0;
	}

	public void intCheckpointsLoc() {
		pk.pkVars.getcLoc().clear();
		FileConfiguration cfg = pk.getConfig();
		for (int mapID : pk.pkVars.maps) {
			for (int i = cfg.getInt("Parkour.map" + mapID + ".numberCp"); i >= 1; i--) {
				Location loc = new Location(pk.getServer().getWorld(cfg.getString("Parkour.map" + mapID + ".world")),
						cfg.getInt("Parkour.map" + mapID + ".cp." + i + ".posX"), cfg.getInt("Parkour.map"
								+ mapID + ".cp." + i + ".posY"), cfg.getInt("Parkour.map" + mapID + ".cp." + i
										+ ".posZ"));
				String HashTable = mapID + "_" + i;
				pk.pkVars.getcLoc().put(loc, HashTable);
			}
		}
	}

	public void intMaps() {
		pk.pkVars.maps.clear();
		String mapList = pk.getConfig().getConfigurationSection("Parkour").getKeys(false).toString().replaceAll("\\s", "")
				.replace("[", "").replace("]", "");
		String[] mapsSplit = mapList.split(",");
		for (int i = pk.getConfig().getInt("Parkour.mapsnumber"); i >= 0; i--) {
			if (mapExist(mapsSplit[i].substring(3))) {
				pk.pkVars.maps.add(Integer.parseInt(mapsSplit[i].substring(3)));
			}
		}
		Collections.sort(pk.pkVars.maps);
	}

	public void loadToggleMap() {
		pk.pkVars.toggleParkour.clear();
		for (int mapID : pk.pkVars.maps) {
			if (pk.getConfig().contains("Parkour.map" + mapID + ".toggle")) {
				pk.pkVars.toggleParkour.put(mapID, pk.getConfig().getBoolean("Parkour.map" + mapID + ".toggle"));
			}
		}
	}

	public void loadLobby() {
		FileConfiguration cfg = pk.getConfig();

		if (cfg.contains("Lobby")) {
			pk.pkVars.setLobby(null);
			Location loc = new Location(pk.getServer().getWorld(cfg.getString("Lobby.world")),
					cfg.getDouble("Lobby.posX"), cfg.getDouble("Lobby.posY"), cfg.getDouble("Lobby.posZ"));
			loc.setPitch((float) cfg.getDouble("Lobby.posPitch"));
			loc.setYaw((float) cfg.getDouble("Lobby.posYaw"));
			pk.pkVars.setLobby(loc);
		}
	}

	public int maxMapNumber() {
		return pk.getConfig().getInt("Parkour.mapsnumber");
	}

	
	/*
	 *  Player Functions
	 */
	
	public boolean isPlayerInParkour(Player player) {
		if(pk.pkVars.ParkourContainer.containsKey(player.getName())) {
			return true;
		} else {
			return false;
		}
	}

	public void saveScore() {
		try {
			try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream((pk.pkVars.scoresPath))))) {
				oos.writeObject(pk.pkVars.getRecords());
				oos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadScore() {
		try {
			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(pk.pkVars.scoresPath)))) {
				pk.pkVars.getRecords().clear();
				pk.pkVars.setRecords((HashMap<String, Long>) ois.readObject());
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace(System.out);
		}
	}

	public void giveReward(Player p) {
		if (pk.pkVars.rewardEnable) {
			FileConfiguration cfg = pk.getConfig();

			boolean rewardMoneyEnable = cfg.getBoolean("rewards.money.enable");
			boolean rewardCommandEnable = cfg.getBoolean("rewards.command.enable");

			String rewardMoneyMsg = cfg.getString("rewards.money.message");
			String rewardCommandMsg = cfg.getString("rewards.command.message");

			String rewardCmd = cfg.getString("rewards.command.cmd");
			int rewardMoney = cfg.getInt("rewards.money.amount");

			int rewardCooldown = cfg.getInt("rewards.cooldown");
			String rewardCooldownMsg = cfg.getString("rewards.cooldownMessage");

			if (!pk.pkVars.rewardPlayersCooldown.containsKey(p.getName())) {
				if (rewardMoneyEnable && rewardMoney > 0) {
					pk.pkVars.rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					if (pk.isVault()) Parkour.economy.depositPlayer(p.getName(), rewardMoney);
					p.sendMessage(pk.getPrefix() + ChatColor.translateAlternateColorCodes('&', rewardMoneyMsg).replaceAll("MONEYAMOUNT",
							"" + rewardMoney));
				}
				if (rewardCommandEnable) {
					pk.pkVars.rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					pk.getServer().dispatchCommand(pk.getServer().getConsoleSender(),
							rewardCmd.replaceAll("PLAYER", p.getName()));
					p.sendMessage(pk.getPrefix() + ChatColor.translateAlternateColorCodes('&', rewardCommandMsg));
				}
			} else {
				if (System.currentTimeMillis() - pk.pkVars.rewardPlayersCooldown.get(p.getName()) >= rewardCooldown * 1000) {
					if (rewardMoneyEnable && rewardMoney > 0) {
						pk.pkVars.rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						if (pk.isVault()) Parkour.economy.depositPlayer(p.getName(), rewardMoney);
						p.sendMessage(pk.getPrefix() + ChatColor.translateAlternateColorCodes('&', rewardMoneyMsg).replaceAll(
								"MONEYAMOUNT", "" + rewardMoney));
					}
					if (rewardCommandEnable) {
						pk.pkVars.rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						pk.getServer().dispatchCommand(pk.getServer().getConsoleSender(),
								rewardCmd.replaceAll("PLAYER", p.getName()));
						p.sendMessage(pk.getPrefix() + ChatColor.translateAlternateColorCodes('&', rewardCommandMsg));
					}
				} else {
					long time = (System.currentTimeMillis() - pk.pkVars.rewardPlayersCooldown.get(p.getName()));

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

					p.sendMessage(pk.pkStrings.PREFIX + rewardCooldownMsg.replaceAll("TIME", hoursS + "h:" + minsS + "m:" + secsS + "s"));
				}
			}
		}
	}

	public void teleportFirstCheckpoint(Player p){

		FileConfiguration cfg = pk.getConfig();
		Location firstCheckpoint;

		int mapID = getPlMapNumber(pk.pkVars.ParkourContainer.get(p.getName()));

		if (cfg.contains("Parkour.map" + mapID + ".spawn")) {
			firstCheckpoint = new Location(
					pk.getServer().getWorld(cfg.getString("Parkour.map" + mapID + ".world")),
					cfg.getDouble("Parkour.map" + mapID + ".spawn.posX"), cfg.getDouble("Parkour.map"
							+ mapID + ".spawn.posY"), cfg.getDouble("Parkour.map" + mapID + ".spawn.posZ"));

			firstCheckpoint.setPitch((float) cfg.getDouble("Parkour.map" + mapID + ".spawn.posPitch"));
			firstCheckpoint.setYaw((float) cfg.getDouble("Parkour.map" + mapID + ".spawn.posYaw"));

			p.teleport(firstCheckpoint);
		} else {
			firstCheckpoint = new Location(
					pk.getServer().getWorld(cfg.getString("Parkour.map" + mapID + ".world")),
					cfg.getDouble("Parkour.map" + mapID + ".cp.1.posX") + 0.5, cfg.getDouble("Parkour.map"
							+ mapID + ".cp.1.posY"),
							cfg.getDouble("Parkour.map" + mapID + ".cp.1.posZ") + 0.5);


			firstCheckpoint.setPitch(p.getLocation().getPitch());
			firstCheckpoint.setYaw(p.getLocation().getYaw());
			p.teleport(firstCheckpoint);
		}
	}
	
	public void teleportLastCheckpoint(Player p) {
		FileConfiguration cfg = pk.getConfig();
		Location lastCheckpoint;

		int mapID = getPlMapNumber(pk.pkVars.ParkourContainer.get(p.getName()));
		int PlCheckpoint = getPlCheckpoint(pk.pkVars.ParkourContainer.get(p.getName()));

		if (PlCheckpoint == 1 || !pk.pkVars.LastCheckpointTeleport) // Teleport to map spawn
		{
			teleportFirstCheckpoint(p);
		} else {
			lastCheckpoint = new Location(pk.getServer().getWorld(cfg.getString("Parkour.map" + mapID + ".world")),
					cfg.getDouble("Parkour.map" + mapID + ".cp." + PlCheckpoint + ".posX") + 0.5,
					cfg.getDouble("Parkour.map" + mapID + ".cp." + PlCheckpoint + ".posY"),
					cfg.getDouble("Parkour.map" + mapID + ".cp." + PlCheckpoint + ".posZ") + 0.5);

			lastCheckpoint.setPitch(p.getLocation().getPitch());
			lastCheckpoint.setYaw(p.getLocation().getYaw());
			p.teleport(lastCheckpoint);
		}
	}
	
	public void setPlCheckpoint(String p, int Cp) {
		String HashTableSrc = pk.pkVars.ParkourContainer.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String CpFinal = Splitter[0] + "_" + Splitter[1] + "_" + Cp;
		pk.pkVars.ParkourContainer.put(p, CpFinal);
	}
	
	public void setPlTime(String p, Long Time) {
		String HashTableSrc = pk.pkVars.ParkourContainer.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String TimeFinal = Splitter[0] + "_" + Time + "_" + Splitter[2];
		pk.pkVars.ParkourContainer.put(p, TimeFinal);
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
	
	/*
	 * 2notnumber = Second line is not a number
	 * badmap = Map not recognized
	 * noPermission = No permission to perform the action
	 * noParkourPermission = user does not have parkour.use
	 * badsign = Sign is not recognized as valid
	 * parkourDisabled = parkour is toggled to disabled
	 * gmChange = When a player tries to change GameMode while in a Parkour
	 * specifymapid = Map ID is not specified
	 * notinpk = Not in a parkour map/session
	 * commanddisabled = Command disabled because player is in a Parkour
	 */
	public void sendError(String status, Player player, Parkour plugin) {
		String APREFIX = plugin.getAPrefix();
		String PREFIX = plugin.getPrefix();
		if(status.equalsIgnoreCase("2notnumber")) {
			player.sendMessage(APREFIX + pk.pkStrings.defaultError + "The second line must be a number. Please try again.");

		} else if(status.equalsIgnoreCase("badmap")) {
			player.sendMessage(APREFIX + pk.pkStrings.defaultError + "That map is not recognized. Please try again.");

		} else if(status.equalsIgnoreCase("noPermission")) {
			player.sendMessage(PREFIX + pk.pkStrings.defaultError + "You do not have permission to do that.");

		} else if (status.equalsIgnoreCase("noParkourPermission")){
			player.sendMessage(PREFIX + pk.pkStrings.defaultError + "You don't have permission to do this parkour");

		} else if(status.equalsIgnoreCase("badsign")) {
			player.sendMessage(APREFIX + pk.pkStrings.defaultError + "That sign is not recognized. Please try again.");

		} else if(status.equalsIgnoreCase("parkourDisabled")){
			player.sendMessage(PREFIX + pk.pkStrings.defaultError + "This parkour is" + ChatColor.RED + " disabled");

		} else if(status.equalsIgnoreCase("gmChange")) {
			player.sendMessage(APREFIX + pk.pkStrings.defaultError + "You may not change your GameMode while in a Parkour. Please type /pk leave before trying again.");

		} else if(status.equalsIgnoreCase("mapspawnnotset")) {
			player.sendMessage(PREFIX + pk.pkStrings.defaultError + "You must specify the map ID");

		} else if(status.equalsIgnoreCase("notinpk")) {
			player.sendMessage(PREFIX + pk.pkStrings.defaultError + "You are not in a parkour, use /pk lobby to return to the lobby");

		} else if(status.equalsIgnoreCase("commanddisabled")) {
			player.sendMessage(PREFIX + pk.pkStrings.defaultError + "You are in a parkour and cannot use that command");
			
		}
	}

	/*
	 * notUnlocked - User has not unlocked this map
	 * mapUnlock - User has unlocked the next map
	 */
	public void sendInfo(String info, Player player, int mapID, Parkour plugin){
		String PREFIX = plugin.getPrefix();
		String nextMapName = plugin.getMapName((plugin.getMapNext(mapID)));
		String prevMapName = plugin.getMapName(((Parkour) plugin).getMapPrevious(mapID));
		if(info.equalsIgnoreCase("notUnlocked")) {
			player.sendMessage(PREFIX + ChatColor.RED + "You have not unlocked this parkour, complete "+ ChatColor.GREEN + prevMapName + ChatColor.RED+" to progress");

		} else if(info.equalsIgnoreCase("mapUnlock")) {
			player.sendMessage(PREFIX + ChatColor.GOLD + "Map unlocked! - "+ ChatColor.GREEN + nextMapName);

		}

	}
	
	
	/*
	 * PlayerInfo Functions
	 */

	public void loadPlayerInfoFile() {
		File playerInfoFile = pk.pkVars.playerInfoFile;
		if (playerInfoFile == null) {
			playerInfoFile = pk.pkVars.playerInfoFile;
		}
		pk.pkVars.playerInfoConfig = YamlConfiguration.loadConfiguration(playerInfoFile);

		// Look for defaults in the jar
		InputStream defConfigStream = pk.getResource("PlayerInfo.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			pk.pkVars.playerInfoConfig.setDefaults(defConfig);
		}
	}

	public void savePlayerInfoFile() {
		FileConfiguration playerInfoConfig = pk.pkVars.playerInfoConfig;
		File playerInfoFile = pk.pkVars.playerInfoFile;
		if (playerInfoConfig == null || playerInfoFile == null) {
			return;
		}
		try {
			getPlayerInfoConfig().save(playerInfoFile);
		} catch (IOException ex) {
			pk.getLogger().log(Level.SEVERE, "Could not save config to " + playerInfoFile, ex);
		}

	}

	public FileConfiguration getPlayerInfoConfig() {
		FileConfiguration playerInfoConfig = pk.pkVars.playerInfoConfig;
		if (playerInfoConfig == null) {
			loadPlayerInfoFile();
		}
		return playerInfoConfig;
	}

	public void saveDefaultPlayerInfo() {
		File playerInfoFile = pk.pkVars.playerInfoFile;
		if (!playerInfoFile.exists()) {            
			pk.saveResource(playerInfoFile.getName(), false);
		}
	}

	public void createNewPlayerInfo(Player player) {
		String username = player.getName();
		PlayerInfo newPlayer = new PlayerInfo();
		newPlayer.setUsername(username);
		newPlayer.setTime(0L);
		newPlayer.setMapID(0);
		pk.pkVars.loadedUsers.put(username, newPlayer);

	}

	public void loadPlayerInfo(Player player) {
		String username = player.getName();
		FileConfiguration pIC = pk.pkVars.playerInfoConfig;

		if(pIC.get("username." + username) == null) {
			createNewPlayerInfo(player);
		}

		HashMap<String, PlayerInfo> loadedUsers = pk.pkVars.loadedUsers;

		if(!loadedUsers.containsKey(username)) {
			PlayerInfo loadedPInfo = new PlayerInfo();
			loadedPInfo.setUsername(username);
			String userPath = "username." + username + ".";
			loadedPInfo.setMapID(pIC.getInt(userPath + "mapID"));
			loadedPInfo.setCheckpoint(pIC.getInt(userPath + "checkpoint"));
			loadedPInfo.setTime(pIC.getLong(userPath + "time"));
			String gm = pIC.getString(userPath + "gamemode");
			if(gm == null) {
				gm = "ADVENTURE";
			}
			loadedPInfo.setPrevGM(translateGM(gm));

			//Add user to loaded users list
			loadedUsers.put(username, loadedPInfo);

			if(mapExist("" + loadedPInfo.getMapID())) {
				PlayerInfo userPInfo = pk.pkVars.loadedUsers.get(username);
				pk.pkVars.ParkourContainer.put(username, loadedPInfo.getMapID() + "_" + (System.currentTimeMillis() - userPInfo.getTime()) + "_" + userPInfo.getCheckpoint());
			}

		}

	}

	public void savePlayerInfo(Player player) {
		String username = player.getName();
		
		if(pk.pkVars.ParkourContainer.containsKey(username)) {
			int mapID = pk.pkFuncs.getPlMapNumber(pk.pkVars.ParkourContainer.get(username));
			long time = System.currentTimeMillis()                                        
					- pk.pkFuncs.getPlTime(pk.pkVars.ParkourContainer.get(username));
			
			pk.pkVars.loadedUsers.get(player.getName()).setMapID(mapID);
			pk.pkVars.loadedUsers.get(player.getName()).setTime(time);
		}

		if(!pk.pkVars.loadedUsers.containsKey(username)) {
			loadPlayerInfo(player);
		}

		PlayerInfo pInfo = pk.pkVars.loadedUsers.get(username);

		FileConfiguration pIC = pk.pkVars.playerInfoConfig;

		//Save username, mapID and time
		pIC.createSection("username." + username);
		String uNamePath = "username." + username + ".";
		pIC.set(uNamePath + "mapID", pInfo.getMapID());
		pIC.set(uNamePath + "checkpoint", pInfo.getCheckpoint());
		pIC.set(uNamePath + "time", pInfo.getTime());
		pIC.set(uNamePath + "gamemode", translateGM(pInfo.getPrevGM()));

		savePlayerInfoFile();

	}

	public void loadUsersPlayerInfo() {
		if(pk.getServer().getOnlinePlayers().size() >= 1) {
			FileConfiguration pIC = pk.pkVars.playerInfoConfig;
			Set<String> keyUsers = pIC.getConfigurationSection("username").getKeys(false);
			for(Player player: pk.getServer().getOnlinePlayers()) {
				if(keyUsers.contains(player.getName())) {
					loadPlayerInfo(player);
				} else {
					continue;
				}
			}
		}
	}

	public void saveAllPlayerInfo() {
		for(Player player: pk.getServer().getOnlinePlayers()) {
			savePlayerInfo(player);
		}
	}

	//Translate GameMode depending on input

	public GameMode translateGM(String gm) {
		if(gm.equalsIgnoreCase("survival")) {
			return GameMode.SURVIVAL;
		} else if(gm.equalsIgnoreCase("creative")) {
			return GameMode.CREATIVE;
		} else {
			return GameMode.ADVENTURE;
		}
	}

	public String translateGM(GameMode gm) {
		if(gm == GameMode.SURVIVAL) {
			return "SURVIVAL";
		} else if(gm == GameMode.CREATIVE) {
			return "CREATIVE";
		} else {
			return "ADVENTURE";
		}
	}


	/*
	 * Misc Functions
	 */

	public int toInt(String msg) {
		return Integer.parseInt(msg);
	}
	
	public boolean isNumber(String number) {
		try {
			Integer.parseInt(number);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void debug(String msg) {
		System.out.println("[ CBParkourDebug ] " + msg);
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
		}.runTaskTimer(pk, 0L, 2L);
	}

	public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
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


	public void healPlayer(Player player) {
		double amount = player.getMaxHealth() - player.getHealth();
        EntityRegainHealthEvent healEvent = new EntityRegainHealthEvent(player, amount, RegainReason.CUSTOM);
        pk.getServer().getPluginManager().callEvent(healEvent);
        
        player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setFireTicks(0);
	}


	//Generates a random string of characters
	public String generateErrorString(Random rng, String characters, int length) {
		char[] text = new char[length];
		for (int i = 0; i < length; i++)
		{
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}
	
	public void playEatSound(Player player) {
		if(player.getHealth() != player.getMaxHealth() && player.getFoodLevel() != 20) {
			player.playSound(player.getLocation(), Sound.EAT, 50, 1);
			player.playSound(player.getLocation(), Sound.EAT, 50, 5);
		}
	}


}
