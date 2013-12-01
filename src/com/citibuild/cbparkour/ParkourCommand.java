package com.citibuild.cbparkour;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
		
			if (sender instanceof Player) {
				p = (Player) sender;
			}
			
			if (cmd.getName().equalsIgnoreCase("pk") && p != null) {
				if (args.length == 0) {
					p.sendMessage(Parkour.GOLD + "---------=[ " + Parkour.D_AQUA + "Citi-Build Parkour Commands" + Parkour.GOLD + " ]=---------");

					if (Parkour.permission.has(p, "parkour.mapeditor") || Parkour.permission.has(p, "parkour.admin")) {
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " create <mapName> <previous mapID> <next mapID>" + Parkour.WHITE + " - Create a new map");
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " done" + Parkour.WHITE + " - Confirm and create the map");
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " delete <mapID>" + Parkour.WHITE + " - Delete a map");
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " changeMapName <mapID> <newMapName>" + Parkour.WHITE + " - Change the map name");
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " changePrevious <mapID> <previous mapID>" + Parkour.WHITE + " - Change the previous map");
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " changeNext <mapID> <next mapID>" + Parkour.WHITE + " - Change the next map");
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " setSpawn <mapID>" + Parkour.WHITE + " - Set the map spawn");
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " toggleWater <mapID>" + Parkour.WHITE + " - Toggles Water repsawn on this Map");
						p.sendMessage(APREFIX + Parkour.GREEN + "/" + CommandLabel + " toggleLava <mapID>" + Parkour.WHITE + " - Toggles Lava Respawn on this Map");
					}
					if (Parkour.permission.has(p, "parkour.admin")) {
						p.sendMessage(APREFIX + Parkour.D_GREEN + "/" + CommandLabel + " toggle <mapID>" + Parkour.WHITE + " - toggle ON/OFF a parkour");
						p.sendMessage(APREFIX + Parkour.D_GREEN + "/" + CommandLabel + " setLobby" + Parkour.WHITE + " - Set the lobby spawn");
						p.sendMessage(APREFIX + Parkour.D_GREEN + "/" + CommandLabel + " resetScores <mapID>" + Parkour.WHITE + "- Reset All scores for a map");
						p.sendMessage(APREFIX + Parkour.D_GREEN + "/" + CommandLabel + " pReset <Player> <mapID> | all" + Parkour.WHITE + " - Reset scores for a player");
					}
					p.sendMessage(PREFIX + Parkour.GRAY + "/" + CommandLabel + " join <mapID>" + Parkour.WHITE + " - Join a map");
					p.sendMessage(PREFIX + Parkour.GRAY + "/" + CommandLabel + " leave" + Parkour.WHITE + " - Leave the map");
					p.sendMessage(PREFIX + Parkour.GRAY + "/" + CommandLabel + " lobby" + Parkour.WHITE + " - Return to the lobby");
					p.sendMessage(PREFIX + Parkour.GRAY + "/" + CommandLabel + " cp | checkpoint" + Parkour.WHITE + " - Teleport to your last checkpoint");
					p.sendMessage(PREFIX + Parkour.GRAY + "/" + CommandLabel + " maplist" + Parkour.WHITE + " - Show all the maps");
					p.sendMessage(PREFIX + Parkour.GRAY + "/" + CommandLabel + " best <MapID>" + Parkour.WHITE + " - Show the best score of a map");
				} 
				
				else {
	/*
	 * User Commands | parkour.use
	 * Join, Leave, Lobby, Checkpoint, Maplist, Best
	 */
					if (args[0].equalsIgnoreCase("test")) {
						if(p.getName().equalsIgnoreCase("Isklar")){

						}
					}
					
					if (args[0].equalsIgnoreCase("join")) {
						if (Parkour.permission.has(p, "parkour.use")) {
							if (args.length == 2) {
								if (plugin.isNumber(args[1])) {
									if (plugin.maps.contains(plugin.toInt(args[1]))) {
										int mapID = plugin.toInt(args[1]);

										if (plugin.toggleParkour.get(mapID)) {
											
											if(Parkour.permission.has(p, "parkour.completed.map"+plugin.getMapPrevious(mapID)) || plugin.getMapPrevious(mapID) == 0){
													
													if (plugin.isPlayerInParkour(p)) {
														plugin.ParkourContainer.remove(p.getName());
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
														p.setGameMode(GameMode.ADVENTURE);
													} 
													else {
														p.sendMessage(PREFIX + Parkour.RED + "The spawn for map " +Parkour.GREEN + args[1] +Parkour.RED + " is not set");
													}	
											}
											else{
												p.sendMessage(PREFIX + Parkour.RED + "You have not unlocked this parkour, complete "+Parkour.GREEN + plugin.getMapName(plugin.getMapPrevious(mapID))+Parkour.RED+" to progress");
											}
										}
										else{
											p.sendMessage(PREFIX + "This parkour is" + Parkour.RED + " disabled");
										}
										
									} else {
										p.sendMessage(PREFIX + Parkour.RED + args[1] +" is not a valid map ID");
									}
								} else {
									p.sendMessage(PREFIX + Parkour.RED + args[1] +" is not a valid number");
								}
							} else {
								p.sendMessage(PREFIX + "You must specify the map ID");
							}
						} else{
							Parkour.sendError("noPermission", p, plugin);
						}
					} 
	/* Leave */				
					else if (args[0].equalsIgnoreCase("leave")) {
						if (Parkour.permission.has(p,"parkour.use")) {
							if (plugin.isPlayerInParkour(p)) {
								p.sendMessage(PREFIX + Parkour.AQUA + "You have left the parkour");
								plugin.ParkourContainer.remove(p.getName());
								if (plugin.lobby != null) {
									p.teleport(plugin.lobby);
									p.setGameMode(GameMode.ADVENTURE);
								}
		
							} else {
								p.sendMessage(PREFIX + Parkour.RED + "You are not in a parkour, use /pk lobby to return to the lobby");
							}
						} else{
							Parkour.sendError("noPermission", p, plugin);
						}
					}
					
	/* Lobby */				
					else if (args[0].equalsIgnoreCase("lobby")) {
						if (Parkour.permission.has(p,"parkour.use")) {
							if (plugin.isPlayerInParkour(p)) {
								p.sendMessage(PREFIX + Parkour.RED + "You are in a parkour course, use /pk leave to leave");
							} else {
								if (plugin.lobby != null) {
									p.teleport(plugin.lobby);
									p.setGameMode(GameMode.ADVENTURE);
									p.sendMessage(PREFIX + Parkour.AQUA + "You have returned to the lobby");
								}
							}
						} else{
							Parkour.sendError("noPermission", p, plugin);
						}
					}
	/* Checkpoint */
					else if ((args[0].equalsIgnoreCase("cp")) || (args[0].equalsIgnoreCase("checkpoint"))) {
						if (Parkour.permission.has(p, "parkour.use")) {
							if (plugin.isPlayerInParkour(p)) {
								plugin.teleportLastCheckpoint(p);
							} else {
								p.sendMessage(PREFIX + Parkour.RED + "You are not in a parkour, use /pk lobby to return to the lobby");
							}
						} else{
							Parkour.sendError("noPermission", p, plugin);
						}
					}
	/* time */
					else if(args[0].equalsIgnoreCase("time")){
						if (Parkour.permission.has(p, "parkour.use")) {
							if (plugin.isPlayerInParkour(p)) {
								long totalTime = System.currentTimeMillis()
										- plugin.getPlTime(plugin.ParkourContainer.get(p.getName()));
								p.sendMessage(PREFIX + Parkour.AQUA + "Current time: "+Parkour.GRAY+plugin.convertTime(totalTime));
							} else {
								p.sendMessage(PREFIX + Parkour.RED + "You are not in a parkour, use /pk lobby to return to the lobby");
							}
						} else{
							Parkour.sendError("noPermission", p, plugin);
						}
					}
	/* Maplist */				
					else if (args[0].equalsIgnoreCase("MapList")) {
						if (Parkour.permission.has(p, "parkour.use")) {
							p.sendMessage(Parkour.GOLD + "---------=[ " + Parkour.D_AQUA + "Parkour Map List" + Parkour.GOLD + " ]=---------");
							p.sendMessage(Parkour.GOLD + "-------=[ " + Parkour.AQUA + "Enabled:" + Parkour.GREEN + "■" +Parkour.D_AQUA+ Parkour.GRAY + " | " +Parkour.AQUA+ "Disabled:" + Parkour.GRAY + "■" + Parkour.GOLD + " ]=-------");
							for (int i : plugin.maps) {
								String mapID = "" + i;
								
								if (plugin.maps.contains(plugin.toInt(mapID))) {
									FileConfiguration cfg = plugin.getConfig();
			
									String mode = Parkour.RED + "■";
									boolean isToggled = false;
									if (plugin.toggleParkour.get(i)) {
										mode = Parkour.GREEN + "■";
										isToggled = true;
									}
									String waterActive = Parkour.AQUA + " Water-Respawn:"+ Parkour.GRAY + "■";
									String lavaActive = Parkour.AQUA + " Lava-Respawn:"+ Parkour.GRAY + "■";
									boolean isWaterActive = cfg.getBoolean("Parkour.map" + mapID + ".waterrespawn");
									boolean isLavaActive = cfg.getBoolean("Parkour.map" + mapID + ".lavarespawn");
									if (isWaterActive){
										waterActive = Parkour.AQUA + " Water-Respawn:"+ Parkour.GREEN + "■";
									}
									if (isLavaActive){
										lavaActive = Parkour.AQUA + " Lava-Respawn:"+ Parkour.GREEN + "■";
									}
									
									if (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor")){
										p.sendMessage(Parkour.GRAY + ""+ i + mode + Parkour.GRAY+ " | " + Parkour.AQUA + plugin.getMapName(i) + Parkour.GRAY 
												+ " (" + plugin.getCfgTotalCheckpoints(i) + " CPs)" + waterActive + Parkour.GRAY + " |" + lavaActive);
									}
									else if ( Parkour.permission.has(p, "parkour.use") && isToggled){
									p.sendMessage(Parkour.GRAY + ""+ i +"- " + Parkour.AQUA + plugin.getMapName(i) + Parkour.GRAY + " (" + (plugin.getCfgTotalCheckpoints(i)-2) 
											+ " CPs)" +  waterActive + Parkour.GRAY + " |" + lavaActive);
									}
								}
							}
						} else{
							Parkour.sendError("noPermission", p, plugin);
						}
					}
	/* Best */				
					else if (args[0].equalsIgnoreCase("best")) {
						if (Parkour.permission.has(p, "parkour.use")) {
							if (args.length == 2) {
								if (plugin.isNumber(args[1])) {
									if (plugin.maps.contains(plugin.toInt(args[1]))) {
										plugin.displayHighscores(plugin.toInt(args[1]), p);
									} else {
										p.sendMessage(PREFIX + Parkour.RED + args[1] +" is not a valid map ID");
									}
								} else {
									p.sendMessage(PREFIX + Parkour.RED + args[1] +" is not a valid ID");
								}
							} else {
								p.sendMessage(PREFIX + Parkour.RED + "You didn't specify the map");
							}
						} else{
							Parkour.sendError("noPermission", p, plugin);
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
								if (plugin.isNumber(args[2]) && plugin.isNumber(args[3])) {
									if (!plugin.newMap) {
										ItemStack stick = new ItemStack(Material.STICK, 1);
										p.sendMessage(PREFIX + "MapEditor: " + Parkour.GREEN + "ON " + Parkour.GRAY + "(Use the stick and right click on all checkpoint in order)");
										p.getInventory().addItem(stick);
										plugin.newMapPlayerEditor = p.getName();
										plugin.newMap = true;
										plugin.CheckpointNumber = 1;
										plugin.newMapName = args[1];
										plugin.newMapPrevious = Integer.parseInt(args[2]);
										plugin.newMapNext = Integer.parseInt(args[3]);
										plugin.NewMapNumber = (plugin.maxMapNumber() + 1);
									} else {
										p.sendMessage(APREFIX + Parkour.RED + "A player is already using the MapEditor (" + plugin.newMapPlayerEditor + ")");
									}
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[2] + " or " + args[3] + " is not a valid ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + "Correct usage : /pk create <map name> <previous map> <next mapID>");
							}
						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage : /pk create <map name> <previous mapID> <next mapID>");
						}
					} 
	/* Done */			
					else if (args[0].equalsIgnoreCase("done")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (!plugin.newMap) {
							p.sendMessage(APREFIX + Parkour.RED + "MapEditor is not ON");
						} else {
							if (p.getName().equalsIgnoreCase(plugin.newMapPlayerEditor)) {
								if (plugin.CheckpointNumber >= 2) {
									p.sendMessage(APREFIX + Parkour.AQUA + plugin.newMapName + " (" +Parkour.GREEN+ "map "  + plugin.NewMapNumber +Parkour.AQUA + ") created" + Parkour.GRAY + " | MapEditor: " + Parkour.RED + "OFF");
									p.sendMessage(APREFIX + Parkour.AQUA + "Remember to set a spawn using /pk setspawn <map ID>");
									FileConfiguration cfg = plugin.getConfig();
									cfg.set("Parkour.mapsnumber", (plugin.getConfig().getInt("Parkour.mapsnumber")) + 1);
									cfg.set("Parkour.map" + plugin.NewMapNumber + ".world", p.getWorld().getName());
									cfg.set("Parkour.map" + plugin.NewMapNumber + ".mapName", plugin.newMapName);
									cfg.set("Parkour.map" + plugin.NewMapNumber + ".mapPrevious", plugin.newMapPrevious);
									cfg.set("Parkour.map" + plugin.NewMapNumber + ".mapNext", plugin.newMapNext);
									cfg.set("Parkour.map" + plugin.NewMapNumber + ".numberCp", (plugin.CheckpointNumber - 1));
									cfg.set("Parkour.map" + plugin.NewMapNumber + ".toggle", true);
									cfg.set("Parkour.map" + plugin.NewMapNumber + ".waterrespawn", false);
									cfg.set("Parkour.map" + plugin.NewMapNumber + ".lavarespawn", false);

									plugin.saveConfig();
									plugin.intMaps();
									plugin.loadToggleMap();

									plugin.newMapName = null;
									plugin.newMapPrevious = 0;
									plugin.newMapNext = 0;
									plugin.CheckpointNumber = 0;
									plugin.newMap = false;
									plugin.intCheckpointsLoc();
									plugin.newMapCheckpoints.clear();

									plugin.newMapPlayerEditor = null;
								} else {
									p.sendMessage(APREFIX + Parkour.RED + "A parkour need at least 3 checkpoints" + Parkour.GRAY + " | MapEditor: " + Parkour.RED + "OFF");
									plugin.newMapPlayerEditor = null;
									plugin.newMapName = null;
									plugin.newMapPrevious = 0;
									plugin.newMapNext = 0;
									plugin.newMapCheckpoints.clear();
									plugin.CheckpointNumber = 0;
									plugin.NewMapNumber = 0;
									plugin.newMap = false;
								}

							} else {
								p.sendMessage(APREFIX + Parkour.RED + "A player is already using the Map Editor (" + plugin.newMapPlayerEditor + ") You must wait a bit");
							}
						}
					}
	/* Delete */				
					else if (args[0].equalsIgnoreCase("delete")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									String mapID = args[1];
									plugin.getConfig().getConfigurationSection("Parkour").set("map" + mapID, null);
									plugin.getConfig().set("Parkour.mapsnumber",
											Integer.valueOf(plugin.getConfig().getInt("Parkour.mapsnumber") - 1));
									plugin.saveConfig();
									p.sendMessage(APREFIX +Parkour.AQUA + "map " +Parkour.GREEN + mapID +Parkour.AQUA + " is now deleted");

									for (Iterator<String> it = plugin.Records.keySet().iterator(); it.hasNext();) {
										String key = it.next();
										String[] KeySplit = key.split(":");
										if (KeySplit[0].equals(args[1])) {
											it.remove();
										}
									}
									plugin.saveScore();
									plugin.intCheckpointsLoc();
									plugin.intMaps();
									plugin.loadToggleMap();
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + Parkour.RED + "You must specify the map ID");
						}
					}
	/* ChangeMapName */				
					else if (args[0].equalsIgnoreCase("changeMapName")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 3) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									plugin.getConfig().set("Parkour.map" + args[1] + ".mapName", args[2]);
									plugin.saveConfig();
									p.sendMessage(APREFIX + Parkour.AQUA + "Map name set to '" + Parkour.AQUA + args[2] + "' for map " + Parkour.GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage : /pk changeMapName <map ID> <new map name>");
						}
					}
	/* ChangeMapPrevious */
					else if (args[0].equalsIgnoreCase("changePrevious")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 3) {
							if (plugin.isNumber(args[1]) && plugin.isNumber(args[2])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									plugin.getConfig().set("Parkour.map" + args[1] + ".mapPrevious", Integer.parseInt(args[2]));
									plugin.saveConfig();
									p.sendMessage(APREFIX + Parkour.AQUA + "Previous map set to '" + Parkour.AQUA + args[2] + "' for map " + Parkour.GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " or " + args[2] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk changePrevious <map ID> <previous map>");
						}
					}
	/* ChangeMapNext */
					else if (args[0].equalsIgnoreCase("changeNext")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 3) {
							if (plugin.isNumber(args[1]) && plugin.isNumber(args[2])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									plugin.getConfig().set("Parkour.map" + args[1] + ".mapNext", Integer.parseInt(args[2]));
									plugin.saveConfig();
									p.sendMessage(APREFIX + Parkour.AQUA + "Next map set to '" + Parkour.AQUA + args[2] + "' for map " + Parkour.GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " or " + args[2] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk changePrevious <map ID> <new ID>");
						}
					}
	/* SetSpawn */				
					else if (args[0].equalsIgnoreCase("setspawn")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapID = args[1];
									cfg.set("Parkour.map" + mapID + ".spawn.posX", p.getLocation().getX());
									cfg.set("Parkour.map" + mapID + ".spawn.posY", p.getLocation().getY());
									cfg.set("Parkour.map" + mapID + ".spawn.posZ", p.getLocation().getZ());
									cfg.set("Parkour.map" + mapID + ".spawn.posPitch", p.getLocation().getPitch());
									cfg.set("Parkour.map" + mapID + ".spawn.posYaw", p.getLocation().getYaw());
									plugin.saveConfig();
									p.sendMessage(APREFIX + Parkour.AQUA + "Parkour spawn set for map " + Parkour.GREEN + mapID);
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk setspawn <map ID>");
						}
					} 
	/* MapInfo */			
					else if (args[0].equalsIgnoreCase("mapInfo") && (Parkour.permission.has(p, "parkour.admin"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								int mapID = Integer.parseInt(args[1]);
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									
									String mode = Parkour.RED + "■";
									@SuppressWarnings("unused")
									boolean isToggled = false;
									if (plugin.toggleParkour.get(mapID)) {
										mode = Parkour.GREEN + "■";
										isToggled = true;
									}
							
									String waterActive = Parkour.AQUA + " Water:"+ Parkour.GRAY + "■";
									String lavaActive = Parkour.AQUA + " Lava:"+ Parkour.GRAY + "■";
									boolean isWaterActive = cfg.getBoolean("Parkour.map" + mapID + ".waterrespawn");
									boolean isLavaActive = cfg.getBoolean("Parkour.map" + mapID + ".lavarespawn");
									if (isWaterActive){
										waterActive = Parkour.AQUA + " Water:"+ Parkour.GREEN + "■";
									}
									if (isLavaActive){
										lavaActive = Parkour.AQUA + " Lava:"+ Parkour.GREEN + "■";
									}
									
									p.sendMessage(Parkour.GOLD + "---------=[ " + Parkour.D_AQUA + " Map Info " + Parkour.GOLD + " ]=---------");
									p.sendMessage(PREFIX + Parkour.AQUA + "Map ID: " + Parkour.GRAY +args[1]);
									p.sendMessage(PREFIX + Parkour.AQUA + "Map Name: " + Parkour.GRAY +plugin.getMapName(mapID));
									p.sendMessage(PREFIX + Parkour.AQUA + "Enabled: " + mode);
									p.sendMessage(PREFIX + Parkour.AQUA + "Previous Map: "+ Parkour.GRAY +plugin.getMapPrevious(mapID));
									p.sendMessage(PREFIX + Parkour.AQUA + "Next Map: "+ Parkour.GRAY +plugin.getMapNext(mapID));
									p.sendMessage(PREFIX + Parkour.AQUA + "Checkpoints: " + Parkour.GRAY +(plugin.getCfgTotalCheckpoints(mapID)-2) );
									p.sendMessage(PREFIX + Parkour.AQUA + "Respawns: "+ waterActive + lavaActive);
									Entry<String, Long> topScore = plugin.getHighscore(mapID);
									p.sendMessage(PREFIX + Parkour.AQUA + "Best Time: " +Parkour.GRAY + topScore.getKey() + " | " + plugin.convertTime(topScore.getValue()));
									
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk mapinfo <map ID>");
						}
					}
	/* ToggleWater */				
					else if (args[0].equalsIgnoreCase("toggleWater")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapID = args[1];
									boolean isActive = !cfg.getBoolean("Parkour.map" + mapID + ".waterrespawn");
									cfg.set("Parkour.map" + mapID + ".waterrespawn", isActive);
									plugin.saveConfig();
									if (isActive) p.sendMessage(APREFIX + Parkour.AQUA + "Waterrespawn is now "+Parkour.GREEN +"ON" + Parkour.AQUA + " for map " +Parkour.GREEN + mapID);
									else p.sendMessage(APREFIX + Parkour.AQUA + "Waterrespawn is now "+Parkour.RED +"OFF" +Parkour.AQUA + " for map " +Parkour.GREEN + mapID);
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk toggleWater <map ID>");
						}
					} 
	/* ToggleLava */			
					else if (args[0].equalsIgnoreCase("toggleLava")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapID = args[1];
									boolean isActive = !cfg.getBoolean("Parkour.map" + mapID + ".lavarespawn");
									cfg.set("Parkour.map" + mapID + ".lavarespawn", isActive);
									plugin.saveConfig();
									if (isActive) p.sendMessage(APREFIX + Parkour.AQUA + "Lavarespawn is now "+Parkour.GREEN +"ON" + Parkour.AQUA + " for map " +Parkour.GREEN + mapID);
									else p.sendMessage(APREFIX + Parkour.AQUA + "Lavarespawn is now "+Parkour.RED +"OFF" +Parkour.AQUA + " for map " +Parkour.GREEN + mapID);
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /plugin toggleLava <map ID>");
						}
					}
					
	/*
	 * Admin Commands | parkour.admin
	 * Toggle, SetLobby, ResetScores, PReset
	 */				
					else if (args[0].equalsIgnoreCase("toggle") && Parkour.permission.has(p, "parkour.admin")) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									if (plugin.getConfig().getBoolean("Parkour.map" + args[1] + ".toggle")) {
										p.sendMessage(APREFIX + Parkour.AQUA + "Map "+ args[1] + " toggled " + Parkour.RED + "OFF");
										plugin.getConfig().set("Parkour.map" + args[1] + ".toggle", false);
										plugin.saveConfig();
									} else {
										p.sendMessage(APREFIX + Parkour.AQUA + "Map "+ args[1] + " toggled " + Parkour.GREEN + "ON");
										plugin.getConfig().set("Parkour.map" + args[1] + ".toggle", true);
										plugin.saveConfig();
									}
									plugin.loadToggleMap();
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk toggle <map ID>");
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
						p.sendMessage(APREFIX + Parkour.AQUA + "Lobby set");
						plugin.loadLobby();
					}
	/* PlayerReset */
					else if (args[0].equalsIgnoreCase("pReset") && Parkour.permission.has(p, "parkour.admin")) {
						if (args.length == 3) {
							boolean DeleteOnAllMaps = false;
							if (args[2].equalsIgnoreCase("all")) {
								DeleteOnAllMaps = true;
							}

							if (plugin.isNumber(args[2]) || DeleteOnAllMaps) {
								if ((plugin.isNumber(args[2]) && plugin.maps.contains(plugin.toInt(args[2]))) || DeleteOnAllMaps) {
									boolean PlayerFound = false;
									String playerName = args[1];
									Player targetPlayer = Bukkit.getServer().getPlayerExact(playerName);
									String mapID = args[2];

									Iterator<String> it = plugin.Records.keySet().iterator();
									
									while (it.hasNext()) {
										String key = it.next();
										String[] KeySplit = key.split(":");

										System.out.println("Key: " + key);

										if (KeySplit[1].equalsIgnoreCase(playerName)) {
											if (DeleteOnAllMaps) {
												Parkour.permission.playerRemove(targetPlayer, "parkour.completed.map"+KeySplit[0]);
												it.remove();
												PlayerFound = true;
											} else if (Integer.parseInt(KeySplit[0]) == Integer.parseInt(mapID)) {
												PlayerFound = true;
												it.remove();
												Parkour.permission.playerRemove(targetPlayer, "parkour.completed.map"+mapID);
											}
										}
									}
									plugin.saveScore();

									if (!PlayerFound) {
										p.sendMessage(APREFIX + Parkour.RED + "Player not found in this scoreboard");
										return true;
									}

									if (DeleteOnAllMaps) {
										p.sendMessage(APREFIX + Parkour.AQUA + "Scores and unlocks reset for player " + Parkour.GREEN + playerName +Parkour.AQUA+ " on all maps");
									} else {
										p.sendMessage(APREFIX + Parkour.AQUA + "Scores and unlocks reset for player "+Parkour.GREEN + playerName + Parkour.AQUA + " on map " +Parkour.GREEN+ mapID);
									}

									plugin.loadScore();
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[2] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[2] + " is not a valid number");
								p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk pReset <username> <map ID | all>");
							}
						} else {
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk pReset <username> <map ID | all>");
						}
					}
	/* ResetScores */
					else if (args[0].equalsIgnoreCase("resetScores") && Parkour.permission.has(p, "parkour.admin")) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									int mapID = Integer.parseInt(args[1]);
									p.sendMessage(PREFIX + Parkour.AQUA + "Scores reset for map " +Parkour.GREEN + mapID);

									for (Iterator<String> it = plugin.Records.keySet().iterator(); it.hasNext();) {
										String key = it.next();
										String[] pName = key.split(":");
										int pMap = Integer.parseInt(pName[0]);
										if (pMap == mapID) {
											it.remove();
										}
									}
									plugin.saveScore();
								} else {
									p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid map ID");
								}
							} else {
								p.sendMessage(APREFIX + Parkour.RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(PREFIX + Parkour.RED + "You must specify the map ID");
							p.sendMessage(APREFIX + Parkour.RED + "Correct usage /pk resetScores <map ID>");
						}
					} 
	/* Reload config */
					else if(args[0].equalsIgnoreCase("reload") && Parkour.permission.has(p, "parkour.admin")) {
						plugin.reloadCfg();
						p.sendMessage(APREFIX + "Configuration reloaded");
						
					}

					else {
						p.sendMessage(APREFIX + Parkour.RED + "Unrecognised command, use /pk for help");
					}
				}
			}
			return true;
		}

}
