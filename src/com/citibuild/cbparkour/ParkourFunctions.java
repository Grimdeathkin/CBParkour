package com.citibuild.cbparkour;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ParkourFunctions {
	
	Parkour pk;
	
	public ParkourFunctions(Parkour plugin) {
		this.pk = plugin;
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

		pk.removePotionsEffectsOnParkour = cfg.getBoolean("options.removePotionsEffectsOnParkour");
		pk.InvincibleWhileParkour = cfg.getBoolean("options.InvincibleWhileParkour");
		pk.CheckpointEffect = cfg.getBoolean("options.CheckpointEffect");
		pk.BroadcastMessage = cfg.getBoolean("options.BroadcastOnRecord.enable");
		pk.FullHunger = cfg.getBoolean("options.BroadcastOnRecord.enable");
		pk.LastCheckpointTeleport = cfg.getBoolean("options.LastCheckpointTeleport");
			
		pk.rewardIfBetterScore = cfg.getBoolean("rewards.rewardIfBetterScore");
		pk.rewardEnable = cfg.getBoolean("rewards.enable");

		if (pk.BroadcastMessage) {
			pk.BroadcastMsg = cfg.getString("options.BroadcastOnRecord.message");
		}
		
		pk.PrefixString = cfg.getString("options.PrefixString");
		
		//ParkourItems Options
		pk.pkItems.slime_cmd = cfg.getString("options.slime_cmd");
		pk.pkItems.musicdisk_cmd = cfg.getString("options.musicdisk_cmd");

	}
	
	public void reloadCfg() {
		pk.reloadConfig();
		LoadCfg();
	}
	
	public void teleportFirstCheckpoint(Player p){

		FileConfiguration cfg = pk.getConfig();
		Location firstCheckpoint;

		int mapID = getPlMapNumber(pk.ParkourContainer.get(p.getName()));

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

		int mapID = getPlMapNumber(pk.ParkourContainer.get(p.getName()));
		int PlCheckpoint = getPlCheckpoint(pk.ParkourContainer.get(p.getName()));

		if (PlCheckpoint == 1 || !pk.LastCheckpointTeleport) // Teleport to map spawn
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
		String HashTableSrc = pk.ParkourContainer.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String CpFinal = Splitter[0] + "_" + Splitter[1] + "_" + Cp;
		pk.ParkourContainer.put(p, CpFinal);
	}

	public void setPlTime(String p, Long Time) {
		String HashTableSrc = pk.ParkourContainer.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String TimeFinal = Splitter[0] + "_" + Time + "_" + Splitter[2];
		pk.ParkourContainer.put(p, TimeFinal);
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

	public int getCfgTotalCheckpoints(int mapID) {
		return pk.getConfig().getInt("Parkour.map" + mapID + ".numberCp");
	}

	private boolean mapExist(String mapID) {
            return pk.getConfig().getInt("Parkour.map" + mapID + ".numberCp") != 0;
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
		pk.cLoc.clear();
		FileConfiguration cfg = pk.getConfig();
		for (int mapID : pk.maps) {
			for (int i = cfg.getInt("Parkour.map" + mapID + ".numberCp"); i >= 1; i--) {
				Location loc = new Location(pk.getServer().getWorld(cfg.getString("Parkour.map" + mapID + ".world")),
						cfg.getInt("Parkour.map" + mapID + ".cp." + i + ".posX"), cfg.getInt("Parkour.map"
								+ mapID + ".cp." + i + ".posY"), cfg.getInt("Parkour.map" + mapID + ".cp." + i
								+ ".posZ"));
				String HashTable = mapID + "_" + i;
				pk.cLoc.put(loc, HashTable);
			}
		}
	}

	public void intMaps() {
		pk.maps.clear();
		String mapList = pk.getConfig().getConfigurationSection("Parkour").getKeys(false).toString().replaceAll("\\s", "")
				.replace("[", "").replace("]", "");
		String[] mapsSplit = mapList.split(",");
		for (int i = pk.getConfig().getInt("Parkour.mapsnumber"); i >= 0; i--) {
			if (mapExist(mapsSplit[i].substring(3))) {
				pk.maps.add(Integer.parseInt(mapsSplit[i].substring(3)));
			}
		}
		Collections.sort(pk.maps);
	}

	public void loadToggleMap() {
		pk.toggleParkour.clear();
		for (int mapID : pk.maps) {
			if (pk.getConfig().contains("Parkour.map" + mapID + ".toggle")) {
				pk.toggleParkour.put(mapID, pk.getConfig().getBoolean("Parkour.map" + mapID + ".toggle"));
			}
		}
	}

	public void loadLobby() {
		FileConfiguration cfg = pk.getConfig();

		if (cfg.contains("Lobby")) {
			pk.lobby = null;
			Location loc = new Location(pk.getServer().getWorld(cfg.getString("Lobby.world")),
					cfg.getDouble("Lobby.posX"), cfg.getDouble("Lobby.posY"), cfg.getDouble("Lobby.posZ"));
			loc.setPitch((float) cfg.getDouble("Lobby.posPitch"));
			loc.setYaw((float) cfg.getDouble("Lobby.posYaw"));
			pk.lobby = loc;
		}
	}
	
	/*
	 * 2notnumber = Second line is not a number
	 * badmap = Map not recognized
	 * noPermission = No permission to perform the action
	 * noParkourPermission = user does not have parkour.use
	 * badsign = Sign does not yet exist
	 * parkourDisabled = parkour is toggled to disabled
	 * gmChange = When a player tries to change GameMode while in a Parkour
	 */
	public void sendError(String status, Player player, Plugin plugin) {
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
			
		} else if(status.equalsIgnoreCase("gmChange")) {
			player.sendMessage(PREFIX + "You may not change your GameMode while in a Parkour. Please type /pk leave before trying again.");
			
		}
	}
	
	/*
	 * notUnlocked - User has not unlocked this map
	 * mapUnlock - User has unlocked the next map
	 */
	public void sendInfo(String info, Player player, int mapID, Plugin plugin){
		String PREFIX = ((Parkour) plugin).getPrefix();
		String nextMapName = ((Parkour) plugin).getMapName(((Parkour) plugin).getMapNext(mapID));
		String prevMapName = ((Parkour) plugin).getMapName(((Parkour) plugin).getMapPrevious(mapID));
		if(info.equalsIgnoreCase("notUnlocked")) {
			player.sendMessage(PREFIX + Parkour.RED + "You have not unlocked this parkour, complete "+ Parkour.GREEN + prevMapName + Parkour.RED+" to progress");

		} else if(info.equalsIgnoreCase("mapUnlock")) {
		player.sendMessage(PREFIX + Parkour.GOLD + "Map unlocked! - "+ Parkour.GREEN + nextMapName);
			
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
		}.runTaskTimer(pk, 0L, 2L);
	}

	public int maxMapNumber() {
		return pk.getConfig().getInt("Parkour.mapsnumber");
	}
	
/*
 *  Player Functions
 */
	public boolean isPlayerInParkour(Player player) {
		if(pk.ParkourContainer.containsKey(player.getName())) {
			return true;
		} else {
			return false;
		}
	}
	
	public void saveScore() {
		try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream((pk.path))))) {
                oos.writeObject(pk.Records);
                oos.flush();
            }
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadScore() {
		try {
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(pk.path)))) {
            	pk.Records.clear();
                pk.Records = (HashMap<String, Long>) ois.readObject();
            }
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace(System.out);
		}
	}

	public int toInt(String msg) {
		return Integer.parseInt(msg);
	}

	public void debug(String msg) {
		System.out.println("[ CBParkourDebug ] " + msg);
	}

	public void giveReward(Player p) {
		if (pk.rewardEnable) {
			FileConfiguration cfg = pk.getConfig();

			boolean rewardMoneyEnable = cfg.getBoolean("rewards.money.enable");
			boolean rewardCommandEnable = cfg.getBoolean("rewards.command.enable");

			String rewardMoneyMsg = cfg.getString("rewards.money.message");
			String rewardCommandMsg = cfg.getString("rewards.command.message");

			String rewardCmd = cfg.getString("rewards.command.cmd");
			int rewardMoney = cfg.getInt("rewards.money.amount");

			int rewardCooldown = cfg.getInt("rewards.cooldown");
			String rewardCooldownMsg = cfg.getString("rewards.cooldownMessage");

			if (!pk.rewardPlayersCooldown.containsKey(p.getName())) {
				if (rewardMoneyEnable && rewardMoney > 0) {
					pk.rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					if (pk.vault) Parkour.economy.depositPlayer(p.getName(), rewardMoney);
					p.sendMessage(pk.PREFIX + ChatColor.translateAlternateColorCodes('&', rewardMoneyMsg).replaceAll("MONEYAMOUNT",
							"" + rewardMoney));
				}
				if (rewardCommandEnable) {
					pk.rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					pk.getServer().dispatchCommand(pk.getServer().getConsoleSender(),
							rewardCmd.replaceAll("PLAYER", p.getName()));
					p.sendMessage(pk.PREFIX + ChatColor.translateAlternateColorCodes('&', rewardCommandMsg));
				}
			} else {
				if (System.currentTimeMillis() - pk.rewardPlayersCooldown.get(p.getName()) >= rewardCooldown * 1000) {
					if (rewardMoneyEnable && rewardMoney > 0) {
						pk.rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						if (pk.vault) Parkour.economy.depositPlayer(p.getName(), rewardMoney);
						p.sendMessage(pk.PREFIX + ChatColor.translateAlternateColorCodes('&', rewardMoneyMsg).replaceAll(
								"MONEYAMOUNT", "" + rewardMoney));
					}
					if (rewardCommandEnable) {
						pk.rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						pk.getServer().dispatchCommand(pk.getServer().getConsoleSender(),
								rewardCmd.replaceAll("PLAYER", p.getName()));
						p.sendMessage(pk.PREFIX + ChatColor.translateAlternateColorCodes('&', rewardCommandMsg));
					}
				} else {
					long time = (System.currentTimeMillis() - pk.rewardPlayersCooldown.get(p.getName()));

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

					p.sendMessage(pk.PREFIX + rewardCooldownMsg.replaceAll("TIME", hoursS + "h:" + minsS + "m:" + secsS + "s"));
				}
			}
		}
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

}
