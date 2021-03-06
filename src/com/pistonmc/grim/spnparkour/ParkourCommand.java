package com.pistonmc.grim.spnparkour;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ParkourCommand implements CommandExecutor{
	
	private final Parkour plugin;
	public ParkourCommand(Parkour plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		String PREFIX = plugin.getPrefix();
		String APREFIX = plugin.getAPrefix();
		Player p = null;
		UnlockFunctions unlockFuncs = plugin.pkUnlockFuncs;
		ParkourStrings strings = plugin.pkStrings;
		
			if (sender instanceof Player) {
				p = (Player) sender;
			} else {
				sender.sendMessage(ChatColor.RED + "You need to be a player to use parkour commands");
			}
			
			if (cmd.getName().equalsIgnoreCase("pk") && p != null) {
				if (args.length == 0) {
					p.sendMessage(ChatColor.GOLD + "---------=[ " + ChatColor.DARK_AQUA + "Citi-Build Parkour Commands" + ChatColor.GOLD + " ]=---------");

					if (Parkour.permission.has(p, "parkour.mapeditor") || Parkour.permission.has(p, "parkour.admin")) {
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " create <mapName> <previous mapID> <next mapID>" + ChatColor.WHITE + " - Create a new map");
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " done" + ChatColor.WHITE + " - Confirm and create the map");
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " delete <mapID>" + ChatColor.WHITE + " - Delete a map");
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " changeMapName <mapID> <newMapName>" + ChatColor.WHITE + " - Change the map name");
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " changePrevious <mapID> <previous mapID>" + ChatColor.WHITE + " - Change the previous map");
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " changeNext <mapID> <next mapID>" + ChatColor.WHITE + " - Change the next map");
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " setSpawn <mapID>" + ChatColor.WHITE + " - Set the map spawn");
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " toggleWater <mapID>" + ChatColor.WHITE + " - Toggles Water repsawn on this Map");
						p.sendMessage(APREFIX + ChatColor.GREEN + "/" + CommandLabel + " toggleLava <mapID>" + ChatColor.WHITE + " - Toggles Lava Respawn on this Map");
					}
					if (Parkour.permission.has(p, "parkour.admin")) {
						p.sendMessage(APREFIX + ChatColor.DARK_GREEN + "/" + CommandLabel + " toggle <mapID>" + ChatColor.WHITE + " - toggle ON/OFF a parkour");
						p.sendMessage(APREFIX + ChatColor.DARK_GREEN + "/" + CommandLabel + " mapinfo <mapID>" + ChatColor.WHITE + " - Show all the information about a map");
						p.sendMessage(APREFIX + ChatColor.DARK_GREEN + "/" + CommandLabel + " setLobby" + ChatColor.WHITE + " - Set the lobby spawn");
						p.sendMessage(APREFIX + ChatColor.DARK_GREEN + "/" + CommandLabel + " resetScores <mapID>" + ChatColor.WHITE + "- Reset All scores for a map");
						p.sendMessage(APREFIX + ChatColor.DARK_GREEN + "/" + CommandLabel + " pReset <Player> <mapID> | all" + ChatColor.WHITE + " - Reset scores for a player");
						p.sendMessage(APREFIX + ChatColor.DARK_GREEN + "/" + CommandLabel + " reload " + ChatColor.WHITE + " - Reloads the config for " + plugin.getName());
						p.sendMessage(APREFIX + ChatColor.DARK_GREEN + "/" + CommandLabel + " unlock <username> <mapID>" + ChatColor.WHITE + " - Unlocks a level for a player");
						p.sendMessage(APREFIX + ChatColor.DARK_GREEN + "/" + CommandLabel + " transfer <user1> <user2>" + ChatColor.WHITE + " - Transfers user1's unlocks to user2");
					}
					p.sendMessage(PREFIX + ChatColor.GRAY + "/" + CommandLabel + " join <mapID>" + ChatColor.WHITE + " - Join a map");
					p.sendMessage(PREFIX + ChatColor.GRAY + "/" + CommandLabel + " leave" + ChatColor.WHITE + " - Leave the map");
					p.sendMessage(PREFIX + ChatColor.GRAY + "/" + CommandLabel + " lobby" + ChatColor.WHITE + " - Return to the lobby");
					p.sendMessage(PREFIX + ChatColor.GRAY + "/" + CommandLabel + " cp | checkpoint" + ChatColor.WHITE + " - Teleport to your last checkpoint");
					p.sendMessage(PREFIX + ChatColor.GRAY + "/" + CommandLabel + " maplist" + ChatColor.WHITE + " - Show all the maps");
					p.sendMessage(PREFIX + ChatColor.GRAY + "/" + CommandLabel + " best <mapID>" + ChatColor.WHITE + " - Show the best score of a map");
					p.sendMessage(PREFIX + ChatColor.GRAY + "/" + CommandLabel + " scores <username>" + ChatColor.WHITE + " - Show all a users times");
					p.sendMessage(PREFIX + ChatColor.GRAY + "/" + CommandLabel + " time" + ChatColor.WHITE + " - Show your current time");
				} 
				
				else {
	/*
	 * User Commands | parkour.use
	 * Join, Leave, Lobby, Checkpoint, Maplist, Best
	 */
					if (args[0].equalsIgnoreCase("test")) {
						if(p.getName().equalsIgnoreCase("Isklar") || p.getName().equalsIgnoreCase("bsquidwrd")){
							
							
						}
					}
					
					if (args[0].equalsIgnoreCase("join")) {
						if (Parkour.permission.has(p, "parkour.use")) {
							if (args.length == 2) {
								if (plugin.pkFuncs.isNumber(args[1])) {
									if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
										int mapID = plugin.pkFuncs.toInt(args[1]);

										if (plugin.pkVars.toggleParkour.get(mapID)) {
											
											if(unlockFuncs.levelUnlocked(p, mapID)) {
													
													if (plugin.pkFuncs.isPlayerInParkour(p)) {
														plugin.pkVars.ParkourContainer.remove(p.getName());
													}
					
								
													FileConfiguration cfg = plugin.getConfig();
					
													if (cfg.contains("Parkour.map" + args[1] + ".spawn")) {
														Location loc = new Location(plugin.getServer().getWorld(
																plugin.getConfig().get("Parkour.map" + args[1] + ".world").toString()),
																cfg.getDouble("Parkour.map" + args[1] + ".spawn.posX"),
																cfg.getDouble("Parkour.map" + args[1] + ".spawn.posY"),
																cfg.getDouble("Parkour.map" + args[1] + ".spawn.posZ"));
														loc.setPitch((float) cfg.getDouble("Parkour.map" + args[1] + ".spawn.posPitch"));
														loc.setYaw((float) cfg.getDouble("Parkour.map" + args[1] + ".spawn.posYaw"));
					
														p.teleport(loc);
													} 
													else {
														p.sendMessage(PREFIX + plugin.pkStrings.defaultError + "The spawn for map " + plugin.pkStrings.highlightTwo + args[1] + plugin.pkStrings.defaultError + " is not set");
													}	
											}
											else{
												p.sendMessage(PREFIX + plugin.pkStrings.defaultError + "You have not unlocked this parkour, complete " + plugin.pkStrings.highlightTwo + plugin.getMapName(plugin.getMapPrevious(mapID)) + plugin.pkStrings.defaultError +" to progress");
											}
										}
										else{
											plugin.pkFuncs.sendError("parkourDisabled", p, plugin);
										}
										
									} else {
										p.sendMessage(PREFIX + plugin.pkStrings.defaultError + args[1] +" is not a valid map ID");
									}
								} else {
									p.sendMessage(PREFIX + plugin.pkStrings.defaultError + args[1] +" is not a valid number");
								}
							} else {
								plugin.pkFuncs.sendError("specifymapid", p, plugin);
							}
						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					} 
	/* Leave && Lobby*/
					else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("lobby")) {
						if (Parkour.permission.has(p,"parkour.use")) {
							if (plugin.pkFuncs.isPlayerInParkour(p)) {
								p.sendMessage(PREFIX + plugin.pkStrings.defaultColor + "You have left the parkour");
								plugin.pkVars.ParkourContainer.remove(p.getName());
								plugin.pkVars.loadedUsers.get(p.getName()).setMapID(0);
								plugin.pkFuncs.savePlayerInfo(p);
								if (plugin.pkVars.getLobby() != null) {
									p.teleport(plugin.pkVars.getLobby());
									p.setGameMode(plugin.pkVars.loadedUsers.get(p.getName()).getPrevGM());
								}
		
							} else {
								if (plugin.pkVars.getLobby() != null) {
									p.teleport(plugin.pkVars.getLobby());
									p.setGameMode(plugin.pkVars.loadedUsers.get(p.getName()).getPrevGM());
									p.sendMessage(PREFIX + plugin.pkStrings.defaultColor + "You have returned to the lobby");
								}
							}
						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					}
	/* Restart */
					else if (args[0].equalsIgnoreCase("restart")) {
						if (Parkour.permission.has(p,"parkour.use")) {
							if (plugin.pkFuncs.isPlayerInParkour(p)) {
								plugin.pkFuncs.teleportFirstCheckpoint(p);
								
							} else {
								plugin.pkFuncs.sendError("notinpk", p, plugin);
							}
						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					}
	/* Checkpoint */
					else if ((args[0].equalsIgnoreCase("cp")) || (args[0].equalsIgnoreCase("checkpoint"))) {
						if (Parkour.permission.has(p, "parkour.use")) {
							if (plugin.pkFuncs.isPlayerInParkour(p)) {
								plugin.pkFuncs.teleportLastCheckpoint(p);
							} else {
								plugin.pkFuncs.sendError("notinpk", p, plugin);
							}
						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					}
	/* time */
					else if(args[0].equalsIgnoreCase("time")){
						if (Parkour.permission.has(p, "parkour.use")) {
							if (plugin.pkFuncs.isPlayerInParkour(p)) {
								long totalTime = System.currentTimeMillis()
										- plugin.pkFuncs.getPlTime(plugin.pkVars.ParkourContainer.get(p.getName()));
								p.sendMessage(PREFIX + plugin.pkStrings.defaultColor + "Current time: " + plugin.pkStrings.highlightOne + plugin.convertTime(totalTime));
							} else {
								plugin.pkFuncs.sendError("notinpk", p, plugin);
							}
						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					}
	/* Maplist */				
					else if (args[0].equalsIgnoreCase("MapList")) {
						if (Parkour.permission.has(p, "parkour.use")) {
							p.sendMessage(ChatColor.GOLD + "---------=[ " + plugin.pkStrings.defaultColor + "Parkour Map List" + ChatColor.GOLD + " ]=---------");
							p.sendMessage(ChatColor.GOLD + "-------=[ " + plugin.pkStrings.highlightOne + "Enabled:" + ChatColor.GREEN + "■" + ChatColor.GRAY + " | " + plugin.pkStrings.highlightOne + "Disabled:" + ChatColor.GRAY + "■" + ChatColor.GOLD + " ]=-------");
							for (int i : plugin.pkVars.maps) {
								String mapID = "" + i;
								
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(mapID))) {
									FileConfiguration cfg = plugin.getConfig();
			
									String mode = ChatColor.RED + "■";
									boolean isToggled = false;
									if (plugin.pkVars.toggleParkour.get(i)) {
										mode = ChatColor.GREEN + "■";
										isToggled = true;
									}
									String waterActive = plugin.pkStrings.highlightOne + " Water-Respawn:" + ChatColor.GRAY + "■";
									String lavaActive = plugin.pkStrings.highlightOne + " Lava-Respawn:" + ChatColor.GRAY + "■";
									boolean isWaterActive = cfg.getBoolean("Parkour.map" + mapID + ".waterrespawn");
									boolean isLavaActive = cfg.getBoolean("Parkour.map" + mapID + ".lavarespawn");
									if (isWaterActive){
										waterActive = plugin.pkStrings.highlightOne + " Water-Respawn:" + ChatColor.GREEN + "■";
									}
									if (isLavaActive){
										lavaActive = plugin.pkStrings.highlightOne + " Lava-Respawn:" + ChatColor.GREEN + "■";
									}
									
									if (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor")){
										p.sendMessage(plugin.pkStrings.highlightTwo + "" + i + mode + ChatColor.GRAY + " | " + plugin.pkStrings.highlightTwo + plugin.getMapName(i) + ChatColor.GRAY 
												+ " (" + plugin.pkFuncs.getCfgTotalCheckpoints(i) + " CPs)" + waterActive + ChatColor.GRAY + " |" + lavaActive);
									}
									else if (Parkour.permission.has(p, "parkour.use") && isToggled){
									p.sendMessage(plugin.pkStrings.highlightTwo + "" + i + ChatColor.GRAY + "- " + plugin.pkStrings.highlightTwo + plugin.getMapName(i) + ChatColor.GRAY + " (" + (plugin.pkFuncs.getCfgTotalCheckpoints(i)-2) 
											+ " CPs)" +  waterActive + ChatColor.GRAY + " |" + lavaActive);
									}
								}
							}
						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					}
	/* Best */				
					else if (args[0].equalsIgnoreCase("best")) {
						if (Parkour.permission.has(p, "parkour.use")) {
							if (args.length == 2) {
								if (plugin.pkFuncs.isNumber(args[1])) {
									if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
										plugin.displayHighscores(plugin.pkFuncs.toInt(args[1]), p);
									} else {
										p.sendMessage(PREFIX + plugin.pkStrings.defaultError + args[1] +" is not a valid map ID");
									}
								} else {
									p.sendMessage(PREFIX + plugin.pkStrings.defaultError + args[1] +" is not a valid number");
								}
							} else {
								p.sendMessage(APREFIX + plugin.pkStrings.defaultError + "Correct usage : /pk create <mapID>");
							}
						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					}
					
	/* Scores */
					else if (args[0].equalsIgnoreCase("scores")) {
						if (Parkour.permission.has(p, "parkour.use")) {
							if (args.length == 2) {
								String playerName = args[1];
								boolean playerFound = false;
								if (playerName.charAt(playerName.length()-1) == 's'){
									p.sendMessage(ChatColor.GOLD + "---------=[ " + plugin.pkStrings.defaultColor + playerName+"' Scores" + ChatColor.GOLD + " ]=---------");
								} else {
									p.sendMessage(ChatColor.GOLD + "---------=[ " + plugin.pkStrings.defaultColor + playerName+"'s Scores" + ChatColor.GOLD + " ]=---------");
								}

								plugin.pkFuncs.loadScore();
								for (Entry<String, Long> recordEntry : plugin.pkVars.getRecords().entrySet()){ // MapID:Player,Time
									String Map_Player = recordEntry.getKey();
									String[] Map_PlayerSplit = Map_Player.split(":");
									
									String recordPlayer = Map_PlayerSplit[1];
									
									if (recordPlayer.equalsIgnoreCase(playerName)) {
										playerFound = true;
										int mapID = plugin.pkFuncs.toInt(Map_PlayerSplit[0]);
										int playerRank = plugin.getRank(playerName, mapID);
										String mapName = plugin.getMapName(mapID);
										String playerTime = plugin.convertTime(recordEntry.getValue());
										
										p.sendMessage(plugin.pkStrings.defaultColor + mapName + ChatColor.GRAY + " - " 
														+ plugin.pkStrings.highlightOne + "#"+playerRank 
														+ ChatColor.GRAY + " - " + plugin.pkStrings.highlightOne + playerTime);
									}
								}
								if (!playerFound){
									p.sendMessage(PREFIX + plugin.pkStrings.defaultError + "No scores found for " + playerName);
								}
							} else {
								p.sendMessage(PREFIX + plugin.pkStrings.defaultError + "Correct usage : /pk scores <username>");
							}
						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					}
	/*
	 * Map Commands | parkour.mapeditor
	 * Create, Done, Delete, changeMapName, changePrevious, changeNext, setSpawn, toggleWater, toggleLava
	 */
					else if (args[0].equalsIgnoreCase("Create")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 4) {
							if (args[1] != null && args[2] != null && args[3] != null) {
								if (plugin.pkFuncs.isNumber(args[2]) && plugin.pkFuncs.isNumber(args[3])) {
									if (!plugin.pkVars.isNewMap()) {
										ItemStack stick = new ItemStack(Material.STICK, 1);
										p.sendMessage(PREFIX + "MapEditor: " + ChatColor.GREEN + "ON " + ChatColor.GRAY + "(Use the stick and right click on all checkpoint in order)");
										p.getInventory().addItem(stick);
										plugin.pkVars.newMapPlayerEditor = p.getName();
										plugin.pkVars.setNewMap(true);
										plugin.pkVars.setCheckpointNumber(1);
										plugin.pkVars.setNewMapName(args[1]);
										plugin.pkVars.newMapPrevious = Integer.parseInt(args[2]);
										plugin.pkVars.setNewMapNext(Integer.parseInt(args[3]));
										plugin.pkVars
												.setNewMapNumber((plugin.pkFuncs.maxMapNumber() + 1));
									} else {
										p.sendMessage(APREFIX + ChatColor.RED + "A player is already using the MapEditor (" + plugin.pkVars.newMapPlayerEditor + ")");
									}
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[2] + " or " + args[3] + " is not a valid ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + "Correct usage : /pk create <map name> <previous map> <next mapID>");
							}
						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage : /pk create <map name> <previous mapID> <next mapID>");
						}
					} 
	/* Done */			
					else if (args[0].equalsIgnoreCase("done")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (!plugin.pkVars.isNewMap()) {
							p.sendMessage(APREFIX + ChatColor.RED + "MapEditor is not ON");
						} else {
							if (p.getName().equalsIgnoreCase(plugin.pkVars.newMapPlayerEditor)) {
								if (plugin.pkVars.getCheckpointNumber() >= 2) {
									p.sendMessage(APREFIX + ChatColor.AQUA + plugin.pkVars.getNewMapName() + " (" + ChatColor.GREEN+ "map "  + plugin.pkVars.getNewMapNumber() + ChatColor.AQUA + ") created" + ChatColor.GRAY + " | MapEditor: " + ChatColor.RED + "OFF");
									p.sendMessage(APREFIX + ChatColor.AQUA + "Remember to set a spawn using /pk setspawn <map ID>");
									FileConfiguration cfg = plugin.getConfig();
									cfg.set("Parkour.mapsnumber", (plugin.getConfig().getInt("Parkour.mapsnumber")) + 1);
									cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".world", p.getWorld().getName());
									cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".mapName", plugin.pkVars.getNewMapName());
									cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".mapPrevious", plugin.pkVars.newMapPrevious);
									cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".mapNext", plugin.pkVars.getNewMapNext());
									cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".numberCp", (plugin.pkVars.getCheckpointNumber() - 1));
									cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".toggle", true);
									cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".waterrespawn", false);
									cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".lavarespawn", false);

									plugin.saveConfig();
									plugin.pkFuncs.intMaps();
									plugin.pkFuncs.loadToggleMap();

									plugin.pkVars.setNewMapName(null);
									plugin.pkVars.newMapPrevious = 0;
									plugin.pkVars.setNewMapNext(0);
									plugin.pkVars.setCheckpointNumber(0);
									plugin.pkVars.setNewMap(false);
									plugin.pkFuncs.intCheckpointsLoc();
									plugin.pkVars.getNewMapCheckpoints().clear();

									plugin.pkVars.newMapPlayerEditor = null;
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + "A parkour need at least 3 checkpoints" + ChatColor.GRAY + " | MapEditor: " + ChatColor.RED + "OFF");
									plugin.pkVars.newMapPlayerEditor = null;
									plugin.pkVars.setNewMapName(null);
									plugin.pkVars.newMapPrevious = 0;
									plugin.pkVars.setNewMapNext(0);
									plugin.pkVars.getNewMapCheckpoints().clear();
									plugin.pkVars.setCheckpointNumber(0);
									plugin.pkVars.setNewMapNumber(0);
									plugin.pkVars.setNewMap(false);
								}

							} else {
								p.sendMessage(APREFIX + ChatColor.RED + "A player is already using the Map Editor (" + plugin.pkVars.newMapPlayerEditor + ") You must wait a bit");
							}
						}
					}
	/* Delete */				
					else if (args[0].equalsIgnoreCase("delete")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.pkFuncs.isNumber(args[1])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									String mapID = args[1];
									plugin.getConfig().getConfigurationSection("Parkour").set("map" + mapID, null);
									plugin.getConfig().set("Parkour.mapsnumber",
											Integer.valueOf(plugin.getConfig().getInt("Parkour.mapsnumber") - 1));
									plugin.saveConfig();
									p.sendMessage(APREFIX + ChatColor.AQUA + "map " + ChatColor.GREEN + mapID + ChatColor.AQUA + " is now deleted");

									for (Iterator<String> it = plugin.pkVars.getRecords().keySet().iterator(); it.hasNext();) {
										String key = it.next();
										String[] KeySplit = key.split(":");
										if (KeySplit[0].equals(args[1])) {
											it.remove();
										}
									}
									plugin.pkFuncs.saveScore();
									plugin.pkFuncs.intCheckpointsLoc();
									plugin.pkFuncs.intMaps();
									plugin.pkFuncs.loadToggleMap();
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage : /pk delete <map ID>");
						}
					}
	/* ChangeMapName */				
					else if (args[0].equalsIgnoreCase("changeMapName")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 3) {
							if (plugin.pkFuncs.isNumber(args[1])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									plugin.getConfig().set("Parkour.map" + args[1] + ".mapName", args[2]);
									plugin.saveConfig();
									p.sendMessage(APREFIX + ChatColor.AQUA + "Map name set to '" + ChatColor.AQUA + args[2] + "' for map " + ChatColor.GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage : /pk changeMapName <map ID> <new map name>");
						}
					}
	/* ChangeMapPrevious */
					else if (args[0].equalsIgnoreCase("changePrevious")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 3) {
							if (plugin.pkFuncs.isNumber(args[1]) && plugin.pkFuncs.isNumber(args[2])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									plugin.getConfig().set("Parkour.map" + args[1] + ".mapPrevious", Integer.parseInt(args[2]));
									plugin.saveConfig();
									p.sendMessage(APREFIX + ChatColor.AQUA + "Previous map set to '" + ChatColor.AQUA + args[2] + "' for map " + ChatColor.GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " or " + args[2] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk changePrevious <map ID> <previous map>");
						}
					}
	/* ChangeMapNext */
					else if (args[0].equalsIgnoreCase("changeNext")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 3) {
							if (plugin.pkFuncs.isNumber(args[1]) && plugin.pkFuncs.isNumber(args[2])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									plugin.getConfig().set("Parkour.map" + args[1] + ".mapNext", Integer.parseInt(args[2]));
									plugin.saveConfig();
									p.sendMessage(APREFIX + ChatColor.AQUA + "Next map set to '" + ChatColor.AQUA + args[2] + "' for map " + ChatColor.GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " or " + args[2] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk changePrevious <map ID> <new ID>");
						}
					}
	/* SetSpawn */				
					else if (args[0].equalsIgnoreCase("setspawn")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.pkFuncs.isNumber(args[1])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapID = args[1];
									cfg.set("Parkour.map" + mapID + ".spawn.posX", p.getLocation().getX());
									cfg.set("Parkour.map" + mapID + ".spawn.posY", p.getLocation().getY());
									cfg.set("Parkour.map" + mapID + ".spawn.posZ", p.getLocation().getZ());
									cfg.set("Parkour.map" + mapID + ".spawn.posPitch", p.getLocation().getPitch());
									cfg.set("Parkour.map" + mapID + ".spawn.posYaw", p.getLocation().getYaw());
									plugin.saveConfig();
									p.sendMessage(APREFIX + ChatColor.AQUA + "Parkour spawn set for map " + ChatColor.GREEN + mapID);
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk setspawn <map ID>");
						}
					} 

	/* ToggleWater */				
					else if (args[0].equalsIgnoreCase("toggleWater")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.pkFuncs.isNumber(args[1])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapID = args[1];
									boolean isActive = !cfg.getBoolean("Parkour.map" + mapID + ".waterrespawn");
									cfg.set("Parkour.map" + mapID + ".waterrespawn", isActive);
									plugin.saveConfig();
									if (isActive) p.sendMessage(APREFIX + ChatColor.AQUA + "Waterrespawn is now " + ChatColor.GREEN +"ON" + ChatColor.AQUA + " for map " + ChatColor.GREEN + mapID);
									else p.sendMessage(APREFIX + ChatColor.AQUA + "Waterrespawn is now " + ChatColor.RED +"OFF" + ChatColor.AQUA + " for map " + ChatColor.GREEN + mapID);
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk toggleWater <map ID>");
						}
					} 
	/* ToggleLava */			
					else if (args[0].equalsIgnoreCase("toggleLava")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.pkFuncs.isNumber(args[1])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapID = args[1];
									boolean isActive = !cfg.getBoolean("Parkour.map" + mapID + ".lavarespawn");
									cfg.set("Parkour.map" + mapID + ".lavarespawn", isActive);
									plugin.saveConfig();
									if (isActive) p.sendMessage(APREFIX + ChatColor.AQUA + "Lavarespawn is now " + ChatColor.GREEN +"ON" + ChatColor.AQUA + " for map " + ChatColor.GREEN + mapID);
									else p.sendMessage(APREFIX + ChatColor.AQUA + "Lavarespawn is now " + ChatColor.RED +"OFF" + ChatColor.AQUA + " for map " + ChatColor.GREEN + mapID);
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /plugin toggleLava <map ID>");
						}
					}
					
	/*
	 * Admin Commands | parkour.admin
	 * Toggle, SetLobby, ResetScores, PReset, Mapinfo
	 */				
					else if (args[0].equalsIgnoreCase("toggle") && Parkour.permission.has(p, "parkour.admin")) {
						if (args.length == 2) {
							if (plugin.pkFuncs.isNumber(args[1])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									if (plugin.getConfig().getBoolean("Parkour.map" + args[1] + ".toggle")) {
										p.sendMessage(APREFIX + ChatColor.AQUA + "Map " + args[1] + " toggled " + ChatColor.RED + "OFF");
										plugin.getConfig().set("Parkour.map" + args[1] + ".toggle", false);
										plugin.saveConfig();
									} else {
										p.sendMessage(APREFIX + ChatColor.AQUA + "Map " + args[1] + " toggled " + ChatColor.GREEN + "ON");
										plugin.getConfig().set("Parkour.map" + args[1] + ".toggle", true);
										plugin.saveConfig();
									}
									plugin.pkFuncs.loadToggleMap();
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk toggle <map ID>");
						}
					}
	/* SetLobby */			
					else if (args[0].equalsIgnoreCase("setLobby") && Parkour.permission.has(p, "parkour.admin")) {
						FileConfiguration cfg = plugin.getConfig();
						cfg.set("Lobby.world", p.getWorld().getName());
						cfg.set("Lobby.posX", p.getLocation().getX());
						cfg.set("Lobby.posY", p.getLocation().getY());
						cfg.set("Lobby.posZ", p.getLocation().getZ());
						cfg.set("Lobby.posPitch", p.getLocation().getPitch());
						cfg.set("Lobby.posYaw", p.getLocation().getYaw());
						plugin.saveConfig();
						p.sendMessage(APREFIX + ChatColor.AQUA + "Lobby set");
						plugin.pkFuncs.loadLobby();
					}
	/* PlayerReset */
					else if (args[0].equalsIgnoreCase("pReset") && Parkour.permission.has(p, "parkour.admin")) {
						if (args.length == 3) {
							boolean DeleteOnAllMaps = false;
							if (args[2].equalsIgnoreCase("all")) {
								DeleteOnAllMaps = true;
							}

							if (plugin.pkFuncs.isNumber(args[2]) || DeleteOnAllMaps) {
								if ((plugin.pkFuncs.isNumber(args[2]) && plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[2]))) || DeleteOnAllMaps) {
									boolean PlayerFound = false;
									String playerName = args[1];
									Player targetPlayer = Bukkit.getServer().getPlayerExact(playerName);
									String mapID = args[2];

									Iterator<String> it = plugin.pkVars.getRecords().keySet().iterator();
									
									while (it.hasNext()) {
										String key = it.next();
										String[] KeySplit = key.split(":");

										if (KeySplit[1].equalsIgnoreCase(playerName)) {
											if (DeleteOnAllMaps) {
												unlockFuncs.resetPlayerUnlocks(playerName);
												it.remove();
												PlayerFound = true;
											} else if (Integer.parseInt(KeySplit[0]) == Integer.parseInt(mapID)) {
												PlayerFound = true;
												it.remove();
												unlockFuncs.lockLevel(targetPlayer, Integer.getInteger(mapID));
											}
										}
									}
									plugin.pkFuncs.saveScore();

									if (!PlayerFound) {
										p.sendMessage(APREFIX + ChatColor.RED + "Player not found in this scoreboard");
										return true;
									}

									if (DeleteOnAllMaps) {
										p.sendMessage(APREFIX + ChatColor.AQUA + "Scores and unlocks reset for player " + ChatColor.GREEN + playerName + ChatColor.AQUA+ " on all maps");
									} else {
										p.sendMessage(APREFIX + ChatColor.AQUA + "Scores and unlocks reset for player " + ChatColor.GREEN + playerName + ChatColor.AQUA + " on map " + ChatColor.GREEN+ mapID);
									}

									plugin.pkFuncs.loadScore();
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[2] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[2] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk pReset <username> <map ID | all>");
						}
					}
	/* ResetScores */
					else if (args[0].equalsIgnoreCase("resetScores") && Parkour.permission.has(p, "parkour.admin")) {
						if (args.length == 2) {
							if (plugin.pkFuncs.isNumber(args[1])) {
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									int mapID = Integer.parseInt(args[1]);
									p.sendMessage(PREFIX + ChatColor.AQUA + "Scores reset for map " + ChatColor.GREEN + mapID);
									for (Iterator<String> it = plugin.pkVars.getRecords().keySet().iterator(); it.hasNext();) {
										String key = it.next();
										String[] pName = key.split(":");
										int pMap = Integer.parseInt(pName[0]);
										if (pMap == mapID) {
											it.remove();
										}
									}
									plugin.pkFuncs.saveScore();
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(PREFIX + ChatColor.RED + "You must specify the map ID");
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk resetScores <map ID>");
						}
					} 
	/* Reload config */
					else if(args[0].equalsIgnoreCase("reload") && Parkour.permission.has(p, "parkour.admin")) {
						plugin.pkConfig.reloadCfg();
						p.sendMessage(APREFIX + " Configuration reloaded");
						
					}
					
	/* MapInfo */			
					else if (args[0].equalsIgnoreCase("mapInfo") && (Parkour.permission.has(p, "parkour.admin"))) {
						if (args.length == 2) {
							if (plugin.pkFuncs.isNumber(args[1])) {
								int mapID = Integer.parseInt(args[1]);
								if (plugin.pkVars.maps.contains(plugin.pkFuncs.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									
									String mode = ChatColor.RED + "■";
									@SuppressWarnings("unused")
									boolean isToggled = false;
									if (plugin.pkVars.toggleParkour.get(mapID)) {
										mode = ChatColor.GREEN + "■";
										isToggled = true;
									}
							
									String waterActive = ChatColor.AQUA + " Water:" + ChatColor.GRAY + "■";
									String lavaActive = ChatColor.AQUA + " Lava:" + ChatColor.GRAY + "■";
									boolean isWaterActive = cfg.getBoolean("Parkour.map" + mapID + ".waterrespawn");
									boolean isLavaActive = cfg.getBoolean("Parkour.map" + mapID + ".lavarespawn");
									if (isWaterActive){
										waterActive = ChatColor.AQUA + " Water:" + ChatColor.GREEN + "■";
									}
									if (isLavaActive){
										lavaActive = ChatColor.AQUA + " Lava:" + ChatColor.GREEN + "■";
									}
									
									p.sendMessage(ChatColor.GOLD + "---------=[ " + ChatColor.DARK_AQUA + " Map Info " + ChatColor.GOLD + " ]=---------");
									p.sendMessage(PREFIX + ChatColor.AQUA + "Map ID: " + ChatColor.GRAY +args[1]);
									p.sendMessage(PREFIX + ChatColor.AQUA + "Map Name: " + ChatColor.GRAY + plugin.getMapName(mapID));
									p.sendMessage(PREFIX + ChatColor.AQUA + "Enabled: " + mode);
									p.sendMessage(PREFIX + ChatColor.AQUA + "Previous Map: " + ChatColor.GRAY + plugin.getMapPrevious(mapID));
									p.sendMessage(PREFIX + ChatColor.AQUA + "Next Map: " + ChatColor.GRAY + plugin.getMapNext(mapID));
									p.sendMessage(PREFIX + ChatColor.AQUA + "Checkpoints: " + ChatColor.GRAY +(plugin.pkFuncs.getCfgTotalCheckpoints(mapID)-2) );
									p.sendMessage(PREFIX + ChatColor.AQUA + "Respawns: " + waterActive + lavaActive);
									Entry<String, Long> topScore = plugin.getHighscore(mapID);
									p.sendMessage(PREFIX + ChatColor.AQUA + "Best Time: " + ChatColor.GRAY + topScore.getKey() + " | " + plugin.convertTime(topScore.getValue()));
									
								} else {
									p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk mapinfo <map ID>");
						}
					} 
	/* Unlock */
					else if (args[0].equalsIgnoreCase("unlock")) {
						if (Parkour.permission.has(p,"parkour.admin")) {
							if(args.length == 3) {
								if(plugin.pkFuncs.isNumber(args[2]) || args[2].equals("*")) {
									UnlockFunctions uFuncs = unlockFuncs;
									PlayerUnlocks pUnlocks = new PlayerUnlocks(p);
									
									if(Bukkit.getPlayer(args[1]).isOnline()) {
										Player tPlayer = Bukkit.getPlayer(args[1]);
										uFuncs.loadPlayer(tPlayer);
										pUnlocks = plugin.pkVars.loadedPUnlocks.get(tPlayer.getName().toLowerCase());
									} else {
										OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
										pUnlocks = uFuncs.loadOfflinePlayer(oPlayer);

									}

									if(pUnlocks.getUnlocks().contains(args[2])) {
										p.sendMessage(APREFIX + strings.defaultError + "User " + strings.highlightOne + pUnlocks.getUsername() 
												+ strings.defaultError + " already has " + strings.highlightTwo + ((args[2].equals("*") ? "*" : plugin.getMapName(plugin.pkFuncs.toInt(args[2])))) 
												+ strings.defaultError + " unlocked");

									} else {

										ArrayList<String> unlocks = pUnlocks.getUnlocks();
										if(!unlocks.contains(args[2]) && !unlocks.contains("*")) {
											unlocks.add(args[2]);
											pUnlocks.setUnlocks(unlocks);
										}
										
										if(pUnlocks.getUnlocks().contains(args[2])) {

											if(Bukkit.getPlayerExact(args[1]).isOnline()) {
												Player player = Bukkit.getPlayerExact(args[1]);
												uFuncs.savePlayer(player);
											} else {
												OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
												uFuncs.saveOfflinePlayer(oPlayer, pUnlocks);
											}

											p.sendMessage(APREFIX + strings.defaultColor + "Map " + strings.highlightTwo + 
													//Yup, one liner IF statement right there. I finally found a use for them
													((args[2].equals("*") ? "*" : plugin.getMapName(plugin.pkFuncs.toInt(args[2])))) + strings.defaultColor 
													+ " unlocked for " + strings.highlightOne + pUnlocks.getUsername());

										} else if(pUnlocks.getUnlocks().contains(("*"))) {
											p.sendMessage(APREFIX + strings.defaultError + "User " + strings.highlightOne + pUnlocks.getUsername() 
													+ strings.defaultError + " already has all levels unlocked");

										} else {
											p.sendMessage(APREFIX + strings.defaultError + "Could not unlock " + strings.highlightTwo + 
													((args[2].equals("*") ? "*" : plugin.getMapName(plugin.pkFuncs.toInt(args[2])))) + strings.defaultError
													+ " for " + strings.highlightOne + pUnlocks.getUsername());
										}
									}

								} else {
									p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk unlock <username> <map ID>");
								}

							} else {
								p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk unlock <username> <map ID>");
							}

						} else{
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
					}  
	/* Transfer Unlocks */
					else if(args[0].equalsIgnoreCase("transfer")) {
						if(Parkour.permission.has(p, "parkour.admin")) {
							if(args.length == 3) {
								PlayerUnlocks pUnlocks = null;
								PlayerUnlocks tUnlocks = null;
								boolean tOnline = false;
								//Try to perform the transfer
								try {

									//Check if user1 is online
									for(Player player: Bukkit.getOnlinePlayers()) {
										if(player.getName().equalsIgnoreCase(args[1])) {
											pUnlocks = plugin.pkVars.loadedPUnlocks.get(args[1].toLowerCase());
										}
									}
									
									if(pUnlocks != null) {
										//Stupid check only working one way :/
									} else {
										OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
										pUnlocks = plugin.pkUnlockFuncs.loadOfflinePlayer(oPlayer);
									}

									//Check if user2 is online
									for(Player player: Bukkit.getOnlinePlayers()) {
										if(player.getName().equalsIgnoreCase(args[2])) {
											tUnlocks = plugin.pkVars.loadedPUnlocks.get(args[2].toLowerCase());
											tOnline = true;
										}
									}
									
									if(tUnlocks != null) {
										//Stupid check only working one way :/
									} else {
										OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[2]);
										tUnlocks = plugin.pkUnlockFuncs.loadOfflinePlayer(oPlayer);
									}


									tUnlocks.setUnlocks(pUnlocks.getUnlocks());

									if(tOnline) {
										unlockFuncs.savePlayer(Bukkit.getPlayer(args[2]));
									} else {
										OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[2]);
										unlockFuncs.saveOfflinePlayer(oPlayer, tUnlocks);
									}

									unlockFuncs.resetPlayerUnlocks(args[1].toLowerCase());

									p.sendMessage(APREFIX + strings.defaultColor + "Transfer from " + strings.highlightOne + args[1].toLowerCase() + strings.defaultColor 
											+ " to " + strings.highlightOne + args[2].toLowerCase() + strings.defaultColor + " was successful!");

								} catch (NullPointerException e) {
									//Custom error handling
									Logger logger = plugin.getLogger();
									String errorRef = plugin.pkFuncs.generateErrorString(new Random(), "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", 5);
									logger.severe("Begin Reference: " + errorRef);
									logger.severe("Message: " + e.getMessage());
									logger.severe("Class: " + e.getClass());
									logger.severe("Caused by: " + e.getCause());
									logger.severe("StackTrace: ");
									for(StackTraceElement st: e.getStackTrace()) {
										logger.severe("    at " + st.getClassName() + "(" + st.getFileName() + ":" + st.getLineNumber() + ")");
									}
									logger.severe("End Reference: " + errorRef);
									p.sendMessage(APREFIX + strings.defaultError + "There was an error when trying to perform the transfer.");
									p.sendMessage(APREFIX + strings.defaultError + "Please contact someone with Console access with the");
									p.sendMessage(APREFIX + strings.defaultError + "Error Reference: " + errorRef);
								}
								
								
							} else {
								p.sendMessage(APREFIX + ChatColor.RED + "Correct usage /pk transfer <user1> <user2>");
							}
							
						} else {
							plugin.pkFuncs.sendError("noPermission", p, plugin);
						}
						
					}

					else {
						p.sendMessage(APREFIX + ChatColor.RED + "Unrecognised command, use /pk for help");
					}
				}
			}
			return true;
		}

}
