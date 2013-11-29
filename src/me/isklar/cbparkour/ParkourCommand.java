package me.isklar.cbparkour;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ParkourCommand implements CommandExecutor{
	
	// Chat colours
	ChatColor BLACK = ChatColor.BLACK;				//\u00A70
	ChatColor D_BLUE = ChatColor.DARK_BLUE;			//\u00A71
	ChatColor D_GREEN = ChatColor.DARK_GREEN;		//\u00A72
	ChatColor D_AQUA = ChatColor.DARK_AQUA;			//\u00A73
	ChatColor D_RED = ChatColor.DARK_RED;			//\u00A74
	ChatColor D_PURPLE = ChatColor. DARK_PURPLE;	//\u00A75
	ChatColor GOLD = ChatColor.GOLD;				//\u00A76
	ChatColor D_GRAY = ChatColor.DARK_GRAY;			//\u00A77
	ChatColor GRAY = ChatColor.GRAY;				//\u00A78
	ChatColor BLUE = ChatColor.BLUE;				//\u00A79
	ChatColor GREEN = ChatColor.GREEN;				//\u00A7a
	ChatColor AQUA = ChatColor.AQUA;				//\u00A7b
	ChatColor RED = ChatColor.RED;					//\u00A7c
	ChatColor LIGHT_PURPLE = ChatColor.LIGHT_PURPLE;//\u00A7d
	ChatColor YELLOW = ChatColor.YELLOW;			//\u00A7e
	ChatColor WHITE = ChatColor.WHITE;				//\u00A7f	
	// Chat effects
	ChatColor BOLD = ChatColor.BOLD;				//\u00A7l
	ChatColor STRIKE = ChatColor.STRIKETHROUGH;		//\u00A7m
	ChatColor ULINE = ChatColor.UNDERLINE;			//\u00A7n
	ChatColor ITALIC = ChatColor.ITALIC;			//\u00A7o
	ChatColor RESET = ChatColor.RESET;				//\u00A7r
		
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
					p.sendMessage(GOLD + "---------=[ " + D_AQUA + "Citi-Build Parkour Commands" + GOLD + " ]=---------");

					if (Parkour.permission.has(p, "parkour.mapeditor") || Parkour.permission.has(p, "parkour.admin")) {
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " create <mapName> <previous mapnum> <next mapnum>" + WHITE + " - Create a new map");
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " done" + WHITE + " - Confirm and create the map");
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " delete <mapNumber>" + WHITE + " - Delete a map");
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " changeMapName <mapNumber> <newMapName>" + WHITE + " - Change the map name");
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " changePrevious <mapNumber> <previous mapnum>" + WHITE + " - Change the previous map");
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " changePrevious <mapNumber> <next mapnum>" + WHITE + " - Change the next map");
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " setSpawn <mapNumber>" + WHITE + " - Set the map spawn");
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " toggleWater <mapNumber>" + WHITE + " - Toggles Water repsawn on this Map");
						p.sendMessage(APREFIX + GREEN + "/" + CommandLabel + " toggleLava <mapNumber>" + WHITE + " - Toggles Lava Respawn on this Map");
					}
					if (Parkour.permission.has(p, "parkour.admin")) {
						p.sendMessage(APREFIX + D_GREEN + "/" + CommandLabel + " toggle <mapNumber>" + WHITE + " - toggle ON/OFF a parkour");
						p.sendMessage(APREFIX + D_GREEN + "/" + CommandLabel + " setLobby" + WHITE + " - Set the lobby spawn");
						p.sendMessage(APREFIX + D_GREEN + "/" + CommandLabel + " resetScores <mapNumber>" + WHITE + "- Reset All scores for a map");
						p.sendMessage(APREFIX + D_GREEN + "/" + CommandLabel + " pReset <Player> [<mapNumber> / all]" + WHITE + " - Reset scores for a player");
					}
					p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " join <mapNumber>" + WHITE + " - Join a map");
					p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " leave" + WHITE + " - Leave the map");
					p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " lobby" + WHITE + " - Return to the lobby");
					p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " cp | checkpoint" + WHITE + " - Teleport to your last checkpoint");
					p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " maplist" + WHITE + " - Show all the maps");
					p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " best <MapNumber>" + WHITE + " - Show the best score of a map");
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
										int mapNumber = plugin.toInt(args[1]);

										if (plugin.toggleParkour.get(mapNumber)) {
											
											if(Parkour.permission.has(p, "parkour.completed.map"+plugin.getMapPrevious(mapNumber)) || plugin.getMapPrevious(mapNumber) == 0){
													
													if (plugin.ParkourContainer.containsKey(p.getName())) {
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
													} 
													else {
														p.sendMessage(PREFIX + RED + "The spawn for map " +GREEN + args[1] +RED + " is not set");
													}	
											}
											else{
												p.sendMessage(PREFIX + RED + "You have not unlocked this parkour, complete "+GREEN + plugin.getMapName(plugin.getMapPrevious(mapNumber))+RED+" to progress");
											}
										}
										else{
											p.sendMessage(PREFIX + "This parkour is" + RED + " disabled");
										}
										
									} else {
										p.sendMessage(PREFIX + RED + args[1] +" is not a valid map number");
									}
								} else {
									p.sendMessage(PREFIX + RED + args[1] +" is not a valid number");
								}
							} else {
								p.sendMessage(PREFIX + "You must specify the map number");
							}
						}
						else {
							p.sendMessage(PREFIX + "You do not have pk.permission to use this command");
						}
					} 
	/* Leave */				
					else if (args[0].equalsIgnoreCase("leave")) {
						if (Parkour.permission.has(p,"parkour.use")) {
							if (plugin.ParkourContainer.containsKey(p.getName())) {
								p.sendMessage(PREFIX + AQUA + "You have left the parkour");
								plugin.ParkourContainer.remove(p.getName());
								if (plugin.lobby != null) {
									p.teleport(plugin.lobby);
								}
		
							} else {
								p.sendMessage(PREFIX + RED + "You are not in a parkour, use /pk lobby to return to the lobby");
							}
						}
					}
					
	/* Lobby */				
					else if (args[0].equalsIgnoreCase("lobby")) {
						if (Parkour.permission.has(p,"parkour.use")) {
							if (plugin.ParkourContainer.containsKey(p.getName())) {
								p.sendMessage(PREFIX + RED + "You are in a parkour course, use /pk leave to leave");
							} else {
								if (plugin.lobby != null) {
									p.teleport(plugin.lobby);
									p.sendMessage(PREFIX + AQUA + "You have returned to the lobby");
								}
							}
						}
					}
	/* Checkpoint */
					else if ((args[0].equalsIgnoreCase("cp")) || (args[0].equalsIgnoreCase("checkpoint"))) {
						if (Parkour.permission.has(p, "parkour.use")) {
							if (plugin.ParkourContainer.containsKey(p.getName())) {
								plugin.teleportLastCheckpoint(p);
							} else {
								p.sendMessage(PREFIX + RED + "You are not in a parkour");
							}
						}
					}
	/* Maplist */				
					else if (args[0].equalsIgnoreCase("MapList")) {
						if (Parkour.permission.has(p, "parkour.use")) {
							p.sendMessage(GOLD + "---------=[ " + D_AQUA + "Parkour Map List" + GOLD + " ]=---------");
							p.sendMessage(GOLD + "-------=[ " + AQUA + "Enabled:" + GREEN + "■" +D_AQUA+ GRAY + " | " +AQUA+ "Disabled:" + GRAY + "■" + GOLD + " ]=-------");
							for (int i : plugin.maps) {
								String mapNumber = "" + i;
								
								if (plugin.maps.contains(plugin.toInt(mapNumber))) {
									FileConfiguration cfg = plugin.getConfig();
			
									String mode = RED + "■";
									boolean isToggled = false;
									if (plugin.toggleParkour.get(i)) {
										mode = GREEN + "■";
										isToggled = true;
									}
									String waterActive = AQUA + " Water-Respawn:"+ GRAY + "■";
									String lavaActive = AQUA + " Lava-Respawn:"+ GRAY + "■";
									boolean isWaterActive = cfg.getBoolean("Parkour.map" + mapNumber + ".waterrespawn");
									boolean isLavaActive = cfg.getBoolean("Parkour.map" + mapNumber + ".lavarespawn");
									if (isWaterActive){
										waterActive = AQUA + " Water-Respawn:"+ GREEN + "■";
									}
									if (isLavaActive){
										lavaActive = AQUA + " Lava-Respawn:"+ GREEN + "■";
									}
									
									if (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor")){
										p.sendMessage(GRAY + ""+ i + mode + GRAY+ " | " + AQUA + plugin.getMapName(i) + GRAY 
												+ " (" + plugin.getCfgTotalCheckpoints(i) + " CPs)" + waterActive + GRAY + " |" + lavaActive);
									}
									else if ( Parkour.permission.has(p, "parkour.use") && isToggled){
									p.sendMessage(GRAY + ""+ i +"- " + AQUA + plugin.getMapName(i) + GRAY + " (" + (plugin.getCfgTotalCheckpoints(i)-2) 
											+ " CPs)" +  waterActive + GRAY + " |" + lavaActive);
									}
								}
							}
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
										p.sendMessage(PREFIX + RED + args[1] +" is not a valid map number");
									}
								} else {
									p.sendMessage(PREFIX + RED + args[1] +" is not a valid number");
								}
							} else {
								p.sendMessage(PREFIX + RED + "You didn't specify the map");
							}
						}
					}		
	/*
	 * Map Commands | parkour.mapeditor
	 * Create, Done, Delete, changeMapName, changeMapPrevious, changeMapNext, setSpawn, toggleWater, toggleLava
	 */
					else if (args[0].equalsIgnoreCase("Create")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 4) {
							if (args[1] != null && args[2] != null && args[3] != null) {
								if (plugin.isNumber(args[2]) && plugin.isNumber(args[3])) {
									if (!plugin.newMap) {
										ItemStack stick = new ItemStack(Material.STICK, 1);
										p.sendMessage(PREFIX + "MapEditor: " + GREEN + "ON " + GRAY + "(Use the stick and right click on all checkpoint in order)");
										p.getInventory().addItem(stick);
										plugin.newMapPlayerEditor = p.getName();
										plugin.newMap = true;
										plugin.CheckpointNumber = 1;
										plugin.newMapName = args[1];
										plugin.newMapPrevious = Integer.parseInt(args[2]);
										plugin.newMapNext = Integer.parseInt(args[3]);
										plugin.NewMapNumber = (plugin.maxMapNumber() + 1);
									} else {
										p.sendMessage(APREFIX + RED + "A player is already using the MapEditor (" + plugin.newMapPlayerEditor + ")");
									}
								} else {
									p.sendMessage(APREFIX + RED + args[2] + " or " + args[3] + " is not a valid number");
								}
							} else {
								p.sendMessage(APREFIX + RED + "Correct usage : /pk create <map name> <previous map> <next mapNum>");
							}
						} else {
							p.sendMessage(APREFIX + RED + "Correct usage : /pk create <map name> <previous mapNum> <next mapNum>");
						}
					}
	/* Done */			
					else if (args[0].equalsIgnoreCase("done")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (!plugin.newMap) {
							p.sendMessage(APREFIX + RED + "MapEditor is not ON");
						} else {
							if (p.getName().equalsIgnoreCase(plugin.newMapPlayerEditor)) {
								if (plugin.CheckpointNumber >= 2) {
									p.sendMessage(APREFIX + AQUA + plugin.newMapName + " (" +GREEN+ "map "  + plugin.NewMapNumber +AQUA + ") created" + GRAY + " | MapEditor: " + RED + "OFF");
									p.sendMessage(APREFIX + AQUA + "Remember to set a spawn using /pk setspawn <map number>");
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
									p.sendMessage(APREFIX + RED + "A parkour need at least 3 checkpoints" + GRAY + " | MapEditor: " + RED + "OFF");
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
								p.sendMessage(APREFIX + RED + "A player is already using the Map Editor (" + plugin.newMapPlayerEditor + ") You must wait a bit");
							}
						}
					}
	/* Delete */				
					else if (args[0].equalsIgnoreCase("delete")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									String mapNumber = args[1];
									plugin.getConfig().getConfigurationSection("Parkour").set("map" + mapNumber, null);
									plugin.getConfig().set("Parkour.mapsnumber",
											Integer.valueOf(plugin.getConfig().getInt("Parkour.mapsnumber") - 1));
									plugin.saveConfig();
									p.sendMessage(APREFIX +AQUA + "map " +GREEN + mapNumber +AQUA + " is now deleted");

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
									p.sendMessage(APREFIX + RED + args[1] + " is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + RED + "You must specify the map number");
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
									p.sendMessage(APREFIX + AQUA + "Map name set to '" + AQUA + args[2] + "' for map " + GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + RED + args[1] + " is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + RED + "Correct usage : /pk changeMapName <map number> <new map name>");
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
									p.sendMessage(APREFIX + AQUA + "Previous map set to '" + AQUA + args[2] + "' for map " + GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + RED + args[1] + " is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " or " + args[2] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + RED + "Correct usage /pk changePrevious <map number> <previous map>");
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
									p.sendMessage(APREFIX + AQUA + "Next map set to '" + AQUA + args[2] + "' for map " + GREEN + args[1]);
								} else {
									p.sendMessage(APREFIX + RED + args[1] + " is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " or " + args[2] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + RED + "Correct usage /pk changePrevious <map number> <next mapnumber>");
						}
					}
	/* SetSpawn */				
					else if (args[0].equalsIgnoreCase("setspawn")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapNumber = args[1];
									cfg.set("Parkour.map" + mapNumber + ".spawn.posX", p.getLocation().getX());
									cfg.set("Parkour.map" + mapNumber + ".spawn.posY", p.getLocation().getY());
									cfg.set("Parkour.map" + mapNumber + ".spawn.posZ", p.getLocation().getZ());
									cfg.set("Parkour.map" + mapNumber + ".spawn.posPitch", p.getLocation().getPitch());
									cfg.set("Parkour.map" + mapNumber + ".spawn.posYaw", p.getLocation().getYaw());
									plugin.saveConfig();
									p.sendMessage(APREFIX + AQUA + "Parkour spawn set for map " + GREEN + mapNumber);
								} else {
									p.sendMessage(APREFIX + RED + "It is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + RED + "Correct usage /pk setspawn <map number>");
						}
					} 
	/* ToggleWater */				
					else if (args[0].equalsIgnoreCase("toggleWater")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapNumber = args[1];
									boolean isActive = !cfg.getBoolean("Parkour.map" + mapNumber + ".waterrespawn");
									cfg.set("Parkour.map" + mapNumber + ".waterrespawn", isActive);
									plugin.saveConfig();
									if (isActive) p.sendMessage(APREFIX + AQUA + "Waterrespawn is now "+GREEN +"ON" + AQUA + " for map " +GREEN + mapNumber);
									else p.sendMessage(APREFIX + AQUA + "Waterrespawn is now "+RED +"OFF" +AQUA + " for map " +GREEN + mapNumber);
								} else {
									p.sendMessage(APREFIX + RED + args[1] + " is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + RED + "Correct usage /pk toggleWater <map number>");
						}
					} 
	/* ToggleLava */			
					else if (args[0].equalsIgnoreCase("toggleLava")
							&& (Parkour.permission.has(p, "parkour.admin") || Parkour.permission.has(p, "parkour.mapeditor"))) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									FileConfiguration cfg = plugin.getConfig();
									String mapNumber = args[1];
									boolean isActive = !cfg.getBoolean("Parkour.map" + mapNumber + ".lavarespawn");
									cfg.set("Parkour.map" + mapNumber + ".lavarespawn", isActive);
									plugin.saveConfig();
									if (isActive) p.sendMessage(APREFIX + AQUA + "Lavarespawn is now "+GREEN +"ON" + AQUA + " for map " +GREEN + mapNumber);
									else p.sendMessage(APREFIX + AQUA + "Lavarespawn is now "+RED +"OFF" +AQUA + " for map " +GREEN + mapNumber);
								} else {
									p.sendMessage(APREFIX + RED + args[1] + " is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " is not a valid number");
							}

						} else {
							p.sendMessage(APREFIX + RED + "Correct usage /plugin toggleLava <map number>");
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
										p.sendMessage(APREFIX + AQUA + "Map "+ args[1] + " toggled " + RED + "OFF");
										plugin.getConfig().set("Parkour.map" + args[1] + ".toggle", false);
										plugin.saveConfig();
									} else {
										p.sendMessage(APREFIX + AQUA + "Map "+ args[1] + " toggled " + GREEN + "ON");
										plugin.getConfig().set("Parkour.map" + args[1] + ".toggle", true);
										plugin.saveConfig();
									}
									plugin.loadToggleMap();
								} else {
									p.sendMessage(APREFIX + RED + args[1] + " is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " is not a valid number");
							}
						} else {
							p.sendMessage(APREFIX + RED + "Correct usage /pk toggle <map number>");
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
						p.sendMessage(APREFIX + AQUA + "Lobby set");
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
									String mapNumber = args[2];

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
											} else if (Integer.parseInt(KeySplit[0]) == Integer.parseInt(mapNumber)) {
												PlayerFound = true;
												it.remove();
												Parkour.permission.playerRemove(targetPlayer, "parkour.completed.map"+mapNumber);
											}
										}
									}
									plugin.saveScore();

									if (!PlayerFound) {
										p.sendMessage(APREFIX + RED + "Player not found in this scoreboard");
										return true;
									}

									if (DeleteOnAllMaps) {
										p.sendMessage(APREFIX + AQUA + "Scores and unlocks reset for player " + GREEN + playerName +AQUA+ " on all maps");
									} else {
										p.sendMessage(APREFIX + AQUA + "Scores and unlocks reset for player "+GREEN + playerName + AQUA + " on map " +GREEN+ mapNumber);
									}

									plugin.loadScore();
								} else {
									p.sendMessage(APREFIX + RED + "It is not a valid map number");
								}
							} else {
								p.sendMessage(APREFIX + RED + "It is not a valid number");
								p.sendMessage(APREFIX + RED + "Correct usage /pk pReset <username> <map number / all>");
							}
						} else {
							p.sendMessage(APREFIX + RED + "Correct usage /pk pReset <username> <map number / all>");
						}
					}
	/* ResetScores */
					else if (args[0].equalsIgnoreCase("resetScores") && Parkour.permission.has(p, "parkour.admin")) {
						if (args.length == 2) {
							if (plugin.isNumber(args[1])) {
								if (plugin.maps.contains(plugin.toInt(args[1]))) {
									int mapNumber = Integer.parseInt(args[1]);
									p.sendMessage(PREFIX + AQUA + "Scores reset for map " +GREEN + mapNumber);

									for (Iterator<String> it = plugin.Records.keySet().iterator(); it.hasNext();) {
										String key = it.next();
										String[] pName = key.split(":");
										int pMap = Integer.parseInt(pName[0]);
										if (pMap == mapNumber) {
											it.remove();
										}
									}
									plugin.saveScore();
								} else {
									p.sendMessage(APREFIX + RED + "It is not a valid map number");
								}
							} else {
								p.sendMessage(PREFIX + RED + "It is not a valid number");
							}
						} else {
							p.sendMessage(PREFIX + RED + "You must specify the map number");
							p.sendMessage(APREFIX + RED + "Correct usage /pk resetScores <map number>");
						}
					}

					else {
						p.sendMessage(APREFIX + RED + "Use /pk for help");
					}
				}
			}
			return true;
		}

}
