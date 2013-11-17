package me.isklar.cbparkour;

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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import me.isklar.cbparkour.ParkourCheckpointEvent;
import me.isklar.cbparkour.ParkourFinishEvent;
import me.isklar.cbparkour.ParkourStartEvent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

@SuppressWarnings("unused")
public class Parkour extends JavaPlugin implements Listener {

	// Vault initiation
	public static Economy economy = null;
	public static Permission permission = null;

	// Used for parkour creation
	ArrayList<Location> newMapCheckpoints = new ArrayList<Location>();
	boolean newMap = false;
	String newMapPlayerEditor = "";
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

	boolean vault;

	// Used for player parkour management
	Location lobby = null;
	ArrayList<Integer> maps = new ArrayList<Integer>();
	HashMap<Integer, Boolean> toggleParkour = new HashMap<Integer, Boolean>(); // Parkour active or not
	HashMap<Location, String> cLoc = new HashMap<Location, String>(); // HashMap infos> Location : mapNumber_Chekcpoint
	HashMap<String, String> Parkour = new HashMap<String, String>(); // HashMap infos> playerName :
																		// mapNumber_parkourStartTime_Chekcpoint
	HashMap<String, Long> Records = new HashMap<String, Long>(); // Map:Player, Time
	HashMap<String, Long> rewardPlayersCooldown = new HashMap<String, Long>(); // HashMap infos> playerName :
																				// LastRewardTime

	// Used for saving/loading scores
	String path = "plugins" + File.separator + "CBParkour" + File.separator + "PlayersScores.scores";
	File scores = new File(path);

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
	// Prefixes, user and admin
	String PREFIX;
	String APREFIX;
	
/*
 * 	Setup
 */
	private static final Logger log = Logger.getLogger("Minecraft");
	
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

		if (!scores.getAbsoluteFile().exists()) {
			try {
				scores.createNewFile();
				saveScore();
			} catch (IOException e) {
				e.printStackTrace();
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
		Parkour.clear();
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
 * 	Commands
 */
	
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}

		if (cmd.getName().equalsIgnoreCase("parkour") && p != null) {
			if (args.length == 0) {
				p.sendMessage(GOLD + "---------=[ " + D_AQUA + "Citi-Build Parkour Commands" + GOLD + " ]=---------");

				if (permission.has(p, "parkour.mapeditor") || permission.has(p, "parkour.admin")) {
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
				if (permission.has(p, "parkour.admin")) {
					p.sendMessage(APREFIX + D_GREEN + "/" + CommandLabel + " toggle <mapNumber>" + WHITE + " - toggle ON/OFF a parkour");
					p.sendMessage(APREFIX + D_GREEN + "/" + CommandLabel + " setLobby" + WHITE + " - Set the lobby spawn");
					p.sendMessage(APREFIX + D_GREEN + "/" + CommandLabel + " resetScores <mapNumber>" + WHITE + "- Reset All scores for a map");
					p.sendMessage(APREFIX + D_GREEN + "/" + CommandLabel + " pReset <Player> [<mapNumber> / all]" + WHITE + " - Reset scores for a player");
				}
				p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " join <mapNumber>" + WHITE + " - Join a map");
				p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " leave" + WHITE + " - Leave the map");
				p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " cp | checkpoint" + WHITE + " - Teleport to your last checkpoint");
				p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " maplist" + WHITE + " - Show all the maps");
				p.sendMessage(PREFIX + GRAY + "/" + CommandLabel + " best <MapNumber>" + WHITE + " - Show the best score of a map");
			} 
			
			else {
/*
 * User Commands | parkour.use
 * Join, Leave, Checkpoint, Maplist, Best
 */
				if (args[0].equalsIgnoreCase("test")) {
					if(p.getName().equalsIgnoreCase("Isklar")){	
						
					}
				}
				if (args[0].equalsIgnoreCase("join")) {
					if (permission.has(p, "parkour.use")) {
						if (args.length == 2) {
							if (isNumber(args[1])) {
								if (maps.contains(toInt(args[1]))) {
	
									if (Parkour.containsKey(p.getName())) {
										Parkour.remove(p.getName());
									}
	
									FileConfiguration cfg = getConfig();
	
									if (cfg.contains("Parkour.map" + args[1] + ".spawn")) {
										Location loc = new Location(getServer().getWorld(
												getConfig().get("Parkour.map" + args[1] + ".world").toString()),
												cfg.getDouble("Parkour.map" + args[1] + ".spawn.posX"),
												cfg.getDouble("Parkour.map" + args[1] + ".spawn.posY"),
												cfg.getDouble("Parkour.map" + args[1] + ".spawn.posZ"));
										loc.setPitch((float) cfg.getDouble("Parkour.map" + args[1] + ".spawn.posPitch"));
										loc.setYaw((float) cfg.getDouble("Parkour.map" + args[1] + ".spawn.posYaw"));
	
										p.teleport(loc);
									} else {
										p.sendMessage(PREFIX + RED + "The spawn for map " +GREEN + args[1] +RED + " is not set");
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
						p.sendMessage(PREFIX + "You do not have permission to use this command");
					}
				} 
/* Leave */				
				else if (args[0].equalsIgnoreCase("leave")) {
					if (permission.has(p,"parkour.use")) {
						if (Parkour.containsKey(p.getName())) {
							p.sendMessage(PREFIX + AQUA + "You have left the parkour");
							Parkour.remove(p.getName());
							if (lobby != null) {
								p.teleport(lobby);
							}
	
						} else {
							p.sendMessage(PREFIX + RED + "You are not in a parkour");
						}
					}
				}
/* Checkpoint */
				else if ((args[0].equalsIgnoreCase("cp")) || (args[0].equalsIgnoreCase("checkpoint"))) {
					if (permission.has(p, "parkour.use")) {
						if (Parkour.containsKey(p.getName())) {
							teleportLastCheckpoint(p);
						} else {
							p.sendMessage(PREFIX + RED + "You are not in a parkour");
						}
					}
				}
/* Maplist */				
				else if (args[0].equalsIgnoreCase("MapList")) {
					if (permission.has(p, "parkour.use")) {
						p.sendMessage(GOLD + "---------=[ " + D_AQUA + "Parkour Map List" + GOLD + " ]=---------");
						p.sendMessage(GOLD + "-------=[ " + D_AQUA + "Enabled:" + GREEN + "■" +D_AQUA+ GRAY + " | " +D_AQUA+ "Disabled:" + RED + "■" + GOLD + " ]=-------");
						boolean isToggled = false;
						for (int i : maps) {
							String mapNumber = "" + i;
							
							if (maps.contains(toInt(mapNumber))) {
								FileConfiguration cfg = getConfig();
		
								String mode = RED + "■";
								isToggled = false;
								if (toggleParkour.get(i)) {
									mode = GREEN + "■";
									isToggled = true;
								}
								String waterActive = AQUA + " Water: "+ GREEN + "■";
								String lavaActive = AQUA + " Lava: "+ GREEN + "■";
								boolean isWaterActive = !cfg.getBoolean("Parkour.map" + mapNumber + ".waterrespawn");
								boolean isLavaActive = !cfg.getBoolean("Parkour.map" + mapNumber + ".lavarespawn");
								if (isWaterActive){
									waterActive = AQUA + " Water-Respawn:"+ RED + "■";
								}
								if (isLavaActive){
									lavaActive = AQUA + " Lava-Respawn:"+ RED + "■";
								}
								
								if (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor")){
									p.sendMessage(mode + GRAY + " | " + AQUA + getMapName(i) + GRAY + " (map" + i
											+ ") (" + getCfgTotalCheckpoints(i) + " CPs)" + waterActive + GRAY + " |" + lavaActive);
								}
								else if ( permission.has(p, "parkour.use") && isToggled){
								p.sendMessage(GRAY + "- " + AQUA + getMapName(i) + GRAY + " (" + getCfgTotalCheckpoints(i) 
										+ " Checkpoints)" +  waterActive + GRAY + " |" + lavaActive);
								}
							}
						}
					}
				}
/* Best */				
				else if (args[0].equalsIgnoreCase("best")) {
					if (permission.has(p, "parkour.use")) {
						if (args.length == 2) {
							if (isNumber(args[1])) {
								if (maps.contains(toInt(args[1]))) {
									displayHighscores(toInt(args[1]), p);
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
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (args.length == 4) {
						if (args[1] != null && args[2] != null && args[3] != null) {
							if (isNumber(args[2]) && isNumber(args[3])) {
								if (!newMap) {
									ItemStack stick = new ItemStack(Material.STICK, 1);
									p.sendMessage(PREFIX + "MapEditor: " + GREEN + "ON " + GRAY + "(Use the stick and right click on all checkpoint in order)");
									p.getInventory().addItem(stick);
									newMapPlayerEditor = p.getName();
									newMap = true;
									CheckpointNumber = 1;
									newMapName = args[1];
									newMapPrevious = Integer.parseInt(args[2]);
									newMapNext = Integer.parseInt(args[3]);
									NewMapNumber = (maxMapNumber() + 1);
								} else {
									p.sendMessage(APREFIX + RED + "A player is already using the MapEditor (" + newMapPlayerEditor + ")");
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
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (!newMap) {
						p.sendMessage(APREFIX + RED + "MapEditor is not ON");
					} else {
						if (p.getName().equalsIgnoreCase(newMapPlayerEditor)) {
							if (CheckpointNumber >= 3) {
								p.sendMessage(APREFIX + AQUA + newMapName + " (" +GREEN+ "map "  + NewMapNumber +AQUA + ") created" + GRAY + " | MapEditor: " + RED + "OFF");
								p.sendMessage(APREFIX + AQUA + "Remember to set a spawn using /pk setspawn <map number>");
								FileConfiguration cfg = getConfig();
								cfg.set("Parkour.mapsnumber", (getConfig().getInt("Parkour.mapsnumber")) + 1);
								cfg.set("Parkour.map" + NewMapNumber + ".world", p.getWorld().getName());
								cfg.set("Parkour.map" + NewMapNumber + ".mapName", newMapName);
								cfg.set("Parkour.map" + NewMapNumber + ".mapPrevious", newMapPrevious);
								cfg.set("Parkour.map" + NewMapNumber + ".mapNext", newMapNext);
								cfg.set("Parkour.map" + NewMapNumber + ".numberCp", (CheckpointNumber - 1));
								cfg.set("Parkour.map" + NewMapNumber + ".toggle", true);
								cfg.set("Parkour.map" + NewMapNumber + ".waterrespawn", false);
								cfg.set("Parkour.map" + NewMapNumber + ".lavarespawn", false);

								saveConfig();
								intMaps();
								loadToggleMap();

								newMapName = null;
								newMapPrevious = 0;
								newMapNext = 0;
								CheckpointNumber = 0;
								newMap = false;
								intCheckpointsLoc();
								newMapCheckpoints.clear();

								newMapPlayerEditor = null;
							} else {
								p.sendMessage(APREFIX + RED + "A parkour need at least 3 checkpoints" + GRAY + " | MapEditor: " + RED + "OFF");
								newMapPlayerEditor = null;
								newMapName = null;
								newMapPrevious = 0;
								newMapNext = 0;
								newMapCheckpoints.clear();
								CheckpointNumber = 0;
								NewMapNumber = 0;
								newMap = false;
							}

						} else {
							p.sendMessage(APREFIX + RED + "A player is already using the Map Editor (" + newMapPlayerEditor + ") You must wait a bit");
						}
					}
				}
/* Delete */				
				else if (args[0].equalsIgnoreCase("delete")
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								String mapNumber = args[1].toString();
								getConfig().getConfigurationSection("Parkour").set("map" + mapNumber, null);
								getConfig().set("Parkour.mapsnumber",
										Integer.valueOf(getConfig().getInt("Parkour.mapsnumber") - 1));
								saveConfig();
								p.sendMessage(APREFIX +AQUA + "map " +GREEN + mapNumber +AQUA + " is now deleted");

								for (Iterator<String> it = Records.keySet().iterator(); it.hasNext();) {
									String key = it.next();
									String[] KeySplit = key.split(":");
									if (KeySplit[0].equals(args[1])) {
										it.remove();
									}
								}
								saveScore();
								intCheckpointsLoc();
								intMaps();
								loadToggleMap();
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
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (args.length == 3) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								getConfig().set("Parkour.map" + args[1] + ".mapName", args[2]);
								saveConfig();
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
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (args.length == 3) {
						if (isNumber(args[1]) && isNumber(args[2])) {
							if (maps.contains(toInt(args[1]))) {
								getConfig().set("Parkour.map" + args[1] + ".mapPrevious", Integer.parseInt(args[2]));
								saveConfig();
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
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (args.length == 3) {
						if (isNumber(args[1]) && isNumber(args[2])) {
							if (maps.contains(toInt(args[1]))) {
								getConfig().set("Parkour.map" + args[1] + ".mapNext", Integer.parseInt(args[2]));
								saveConfig();
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
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								FileConfiguration cfg = getConfig();
								String mapNumber = args[1].toString();
								cfg.set("Parkour.map" + mapNumber + ".spawn.posX", p.getLocation().getX());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posY", p.getLocation().getY());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posZ", p.getLocation().getZ());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posPitch", p.getLocation().getPitch());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posYaw", p.getLocation().getYaw());
								saveConfig();
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
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								FileConfiguration cfg = getConfig();
								String mapNumber = args[1].toString();
								boolean isActive = !cfg.getBoolean("Parkour.map" + mapNumber + ".waterrespawn");
								cfg.set("Parkour.map" + mapNumber + ".waterrespawn", isActive);
								saveConfig();
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
						&& (permission.has(p, "parkour.admin") || permission.has(p, "parkour.mapeditor"))) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								FileConfiguration cfg = getConfig();
								String mapNumber = args[1].toString();
								boolean isActive = !cfg.getBoolean("Parkour.map" + mapNumber + ".lavarespawn");
								cfg.set("Parkour.map" + mapNumber + ".lavarespawn", isActive);
								saveConfig();
								if (isActive) p.sendMessage(APREFIX + AQUA + "Lavarespawn is now "+GREEN +"ON" + AQUA + " for map " +GREEN + mapNumber);
								else p.sendMessage(APREFIX + AQUA + "Lavarespawn is now "+RED +"OFF" +AQUA + " for map " +GREEN + mapNumber);
							} else {
								p.sendMessage(APREFIX + RED + args[1] + " is not a valid map number");
							}
						} else {
							p.sendMessage(APREFIX + RED + args[1] + " is not a valid number");
						}

					} else {
						p.sendMessage(APREFIX + RED + "Correct usage /pk toggleLava <map number>");
					}
				}
				
/*
 * Admin Commands | parkour.admin
 * Toggle, SetLobby, ResetScores, PReset
 */				
				else if (args[0].equalsIgnoreCase("toggle") && permission.has(p, "parkour.admin")) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								if (getConfig().getBoolean("Parkour.map" + args[1] + ".toggle")) {
									p.sendMessage(APREFIX + AQUA + "Map "+ args[1] + " toggled " + RED + "OFF");
									getConfig().set("Parkour.map" + args[1] + ".toggle", false);
									saveConfig();
								} else {
									p.sendMessage(APREFIX + AQUA + "Map "+ args[1] + " toggled " + GREEN + "ON");
									getConfig().set("Parkour.map" + args[1] + ".toggle", true);
									saveConfig();
								}
								loadToggleMap();
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
				else if (args[0].equalsIgnoreCase("setLobby") && permission.has(p, "parkour.admin")) {
					FileConfiguration cfg = getConfig();
					cfg.set("Lobby.world", p.getWorld().getName());
					cfg.set("Lobby.posX", p.getLocation().getX());
					cfg.set("Lobby.posY", p.getLocation().getY());
					cfg.set("Lobby.posZ", p.getLocation().getZ());
					cfg.set("Lobby.posPitch", p.getLocation().getPitch());
					cfg.set("Lobby.posYaw", p.getLocation().getYaw());
					saveConfig();
					p.sendMessage(APREFIX + AQUA + "Lobby set");
					loadLobby();
				}
/* PlayerReset */
				else if (args[0].equalsIgnoreCase("pReset") && permission.has(p, "parkour.admin")) {
					if (args.length == 3) {
						boolean DeleteOnAllMaps = false;
						if (args[2].equalsIgnoreCase("all")) {
							DeleteOnAllMaps = true;
						}

						if (isNumber(args[2]) || DeleteOnAllMaps) {
							if ((isNumber(args[2]) && maps.contains(toInt(args[2]))) || DeleteOnAllMaps) {
								boolean PlayerFound = false;
								String player = args[1];
								String mapNumber = args[2];

								Iterator<String> it = Records.keySet().iterator();
								
								while (it.hasNext()) {
									String key = it.next();
									String[] KeySplit = key.split(":");

									System.out.println("Key: " + key);

									if (KeySplit[1].equalsIgnoreCase(player)) {
										if (DeleteOnAllMaps) {
											it.remove();
											PlayerFound = true;
										} else if (Integer.parseInt(KeySplit[0]) == Integer.parseInt(mapNumber)) {
											PlayerFound = true;
											it.remove();
										}
									}
								}
								saveScore();

								if (!PlayerFound) {
									p.sendMessage(APREFIX + RED + "Player not found in this scoreboard");
									return true;
								}

								if (DeleteOnAllMaps) {
									p.sendMessage(APREFIX + AQUA + "Scores reset for player " + player + " on all maps");
								} else {
									p.sendMessage(APREFIX + AQUA + "Scores reset for player " + player + " on map " + mapNumber);
								}

								loadScore();
							} else {
								p.sendMessage(APREFIX + RED + "It is not a valid map number");
							}
						} else {
							p.sendMessage(APREFIX + RED + "It is not a valid number");
							p.sendMessage(APREFIX + RED + "Correct usage /pk pReset <username> <map number>");
						}
					} else {
						p.sendMessage(APREFIX + RED + "Correct usage /pk pReset <username> <map number>");
					}
				}
/* ResetScores */
				else if (args[0].equalsIgnoreCase("resetScores") && permission.has(p, "parkour.admin")) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								int mapNumber = Integer.parseInt(args[1]);
								p.sendMessage(PREFIX + AQUA + "Scores reset for map " +GREEN + mapNumber);

								for (Iterator<String> it = Records.keySet().iterator(); it.hasNext();) {
									String key = it.next();
									String[] pName = key.split(":");
									int pMap = Integer.parseInt(pName[0]);
									if (pMap == mapNumber) {
										it.remove();
									}
								}
								saveScore();
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

/*
 * 	Events
 */
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		if (Parkour.containsKey(e.getPlayer().getName())) {
			Parkour.remove(e.getPlayer().getName());
		}
		if (rewardPlayersCooldown.containsKey(e.getPlayer().getName())) {
			rewardPlayersCooldown.remove(e.getPlayer().getName());
		}
		if (e.getPlayer().getName().equals(newMapPlayerEditor)) {
			newMapPlayerEditor = null;
			newMapName = null;
			newMapPrevious = 0;
			newMapNext = 0;
			newMapCheckpoints.clear();
			CheckpointNumber = 0;
			NewMapNumber = 0;
			newMap = false;
		}
	}

	@EventHandler
	public void onPlayerDmg(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (Parkour.containsKey(p.getName()) && InvincibleWhileParkour) {
				e.setCancelled(true);
				p.setFireTicks(0);
			}

		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (e.getLine(0).equalsIgnoreCase("[cbpk]") && !e.getPlayer().hasPermission("parkour.mapeditor")) {
			e.setCancelled(true);
		}

		if (e.getPlayer().hasPermission("parkour.mapeditor")) {
			// 15 char max par lines (on sign)

			if (e.getLine(0).equalsIgnoreCase("[cbpk]")) {
				if (e.getLine(1).equalsIgnoreCase("leave")) {
					e.setLine(0, "[Parkour]");
					e.setLine(1, "Leave");
				}
				if (e.getLine(1).equalsIgnoreCase("join")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[Parkour]");
							e.setLine(1, "Join");
							e.setLine(2, AQUA + getMapName(MapNumber));
						} else {
							e.setCancelled(true);
						}
					} else {
						e.setCancelled(true);
					}
				}
				if (e.getLine(1).equalsIgnoreCase("info")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "Parkour #" + MapNumber);
							e.setLine(1, "---------------");
							e.setLine(2, AQUA + getMapName(MapNumber));
						} else {
							e.setCancelled(true);
						}
					} else {
						e.setCancelled(true);
					}
				}
				if (e.getLine(1).equalsIgnoreCase("best")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[Parkour]");
							e.setLine(1, "Best Times");
							e.setLine(2, AQUA + getMapName(MapNumber));
							e.setLine(3, "Click Me!");

						} else {
							e.setCancelled(true);
						}
					} else {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onIntaract(PlayerInteractEvent e) {
		/* Sign Interaction */
		if(e.getClickedBlock() != null){
			if (e.getClickedBlock().getState() instanceof Sign) {
				
				Sign s = (Sign) e.getClickedBlock().getState();
	
				if (s.getLine(0).equals("[Parkour]")) {
					if (s.getLine(1).equalsIgnoreCase("join")) {
						int mapNumber = getMapNumber(ChatColor.stripColor(s.getLine(2)));
						if (mapNumber != 0) {
							if (maps.contains(mapNumber)) {
								Player p = e.getPlayer();
	
								if (Parkour.containsKey(p.getName())) {
									Parkour.remove(p.getName());
								}
	
								FileConfiguration cfg = getConfig();
	
								if (cfg.contains("Parkour.map" + mapNumber + ".spawn")) {
									Location loc = new Location(getServer().getWorld(
											getConfig().getString("Parkour.map" + mapNumber + ".world")),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posX"),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posY"),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posZ"));
	
									loc.setPitch((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posPitch"));
									loc.setYaw((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posYaw"));
	
									if (!loc.getChunk().isLoaded()) {
										loc.getChunk().load(true);
									}
	
									p.teleport(loc);
								} else {
									p.sendMessage(PREFIX + RED + "Map spawn is not set");
								}
							} else {
								e.getPlayer().sendMessage(RED + "This map no longer exists");
							}
						}
					}	
					
					if (s.getLine(1).equalsIgnoreCase("leave")) {
						if (Parkour.containsKey(e.getPlayer().getName())) {
							e.getPlayer().sendMessage(AQUA + "You have left the parkour");
							Parkour.remove(e.getPlayer().getName());
	
						}
						if (lobby != null) {
							e.getPlayer().teleport(lobby);
						}
					}
					
					if (s.getLine(1).equalsIgnoreCase("Best Times")) {
						int mapNumber = getMapNumber(ChatColor.stripColor(s.getLine(2)));
						if (mapNumber != 0) {
							if (maps.contains(mapNumber)) {
								displayHighscores(mapNumber, e.getPlayer());
							} else {
								e.getPlayer().sendMessage(RED + "This map no longer exists");
							}
						}
					}
				}
				e.setCancelled(true);
			}
		}
		/* Map Creation */
		if (newMap)
		{
			if (e.getPlayer().getName().equals(newMapPlayerEditor) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Player p = e.getPlayer();
				ItemStack stick = new ItemStack(Material.STICK, 1);
				if (p.getItemInHand().getTypeId() == 280 && e.getClickedBlock().getTypeId() == 70) {
					if (!cLoc.containsKey(e.getClickedBlock().getLocation())) {
						Location bLoc = e.getClickedBlock().getLocation();

						if (newMapCheckpoints.contains(bLoc)) {
							p.sendMessage(APREFIX + RED + "This checkpoint is alredy used for this map");
						} else {
							FileConfiguration cfg = getConfig();

							p.sendMessage(APREFIX + AQUA + "Checkpoint " + GREEN + CheckpointNumber + AQUA + " set on new map " +GREEN+ NewMapNumber);

							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posX", bLoc.getX());
							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posY", bLoc.getY());
							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posZ", bLoc.getZ());

							saveConfig();
							newMapCheckpoints.add(bLoc);
							CheckpointNumber++;

						}
					} else {
						p.sendMessage(APREFIX + RED + "This checkpoint is alredy used for another map");
					}
				} else {
					p.sendMessage(APREFIX + RED + "Use a stick to place checkpoints (Right click on stone pressure plate)");
					p.getInventory().addItem(stick);
				}
			}
		}
		/* Teleport to last Checkpoint 
		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getPlayer().getItemInHand().getTypeId() == CheckpointItem){
			if (Parkour.containsKey(e.getPlayer().getName())) teleportLastCheckpoint(e.getPlayer());
			e.setCancelled(true);
		}*/
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {

		Player p = e.getPlayer();
		/* Player starts course */
		if (((int) e.getFrom().getX() != (int) e.getTo().getX())
				|| ((int) e.getFrom().getY() != (int) e.getTo().getY())
				|| ((int) e.getFrom().getZ() != (int) e.getTo().getZ())) {
			if (e.getTo().getBlock().getType() == Material.STONE_PLATE) {
				int x = (int) e.getTo().getBlock().getX();
				int y = (int) e.getTo().getBlock().getY();
				int z = (int) e.getTo().getBlock().getZ();
				Location bLoc = new Location(e.getTo().getWorld(), x, y, z);

				if (cLoc.containsKey(bLoc)) {

					int Checkpoint = getCheckpoint(cLoc.get(bLoc).toString());

					if (!permission.has(p, "parkour.use")) {
						p.sendMessage(PREFIX + RED + "You don't have permission to do this parkour");
						p.teleport(lobby);
						return;
					}

					if (!toggleParkour.get(getCpMapNumber(cLoc.get(bLoc).toString()))) {
						p.sendMessage(PREFIX + "This parkour is" + RED + " disabled");
						return;
					}

					if (!Parkour.containsKey(p.getName())) {

						if (Checkpoint == 1) {
							int Map = getCpMapNumber(cLoc.get(bLoc).toString());
							
							getServer().getPluginManager().callEvent(new ParkourStartEvent(p, Map, false));
							
							Parkour.put(
									p.getName(),
									(getCpMapNumber(cLoc.get(bLoc).toString()) + "_"
											+ Long.valueOf(System.currentTimeMillis()) + "_1"));
							p.sendMessage(PREFIX + AQUA + "You have started your timer for " + GREEN + getMapName(Map));
							
							if (CheckpointEffect) {
								p.playEffect(bLoc, Effect.POTION_BREAK, 2);
							}
							if (removePotionsEffectsOnParkour) {
								for (PotionEffect effect : p.getActivePotionEffects()) {
									p.removePotionEffect(effect.getType());
								}
							}
							if (FullHunger) {
								p.setFoodLevel(20);
							}
						} else {
							p.sendMessage(PREFIX + RED + "You must start at the checkpoint 1");
						}
					} else {
						int PlCheckpoint = getPlCheckpoint(Parkour.get(p.getName()).toString());
						int CpMap = getCpMapNumber(cLoc.get(bLoc).toString());
						int Map = getPlMapNumber(Parkour.get(p.getName()).toString());
						int TotalCheckpoints = getCfgTotalCheckpoints(Map);

						if (CpMap != Map) {
							if (Checkpoint == 1) {
								getServer().getPluginManager().callEvent(new ParkourStartEvent(p, Map, false));						
								p.sendMessage(PREFIX + AQUA + "You have started your timer for " + GREEN + getMapName(CpMap));
								Parkour.put(
										p.getName(),
										(getCpMapNumber(cLoc.get(bLoc).toString()) + "_"
												+ Long.valueOf(System.currentTimeMillis()) + "_1"));
								
								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (removePotionsEffectsOnParkour) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (FullHunger) {
									p.setFoodLevel(20);
								}

							} else {
								p.sendMessage(PREFIX + RED + "You are not in this parkour");

							}
						} 
						/* Player restarts course */
						else {

							if (Checkpoint == 1) {
								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (removePotionsEffectsOnParkour) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (FullHunger) {
									p.setFoodLevel(20);
								}
								getServer().getPluginManager().callEvent(new ParkourStartEvent(p, Map, true));
								p.sendMessage(PREFIX + AQUA + "You have restarted your time for " +GREEN+ getMapName(Map));
								setPlTime(p.getName(), Long.valueOf(System.currentTimeMillis()));
								setPlCheckpoint(p.getName(), 1);

							} 
							/* Player completes course */
							else if ((Checkpoint == TotalCheckpoints) && (PlCheckpoint == (Checkpoint - 1))) {
								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								long totalTime = System.currentTimeMillis()
										- Long.valueOf(getPlTime(Parkour.get(p.getName())));
								Parkour.remove(p.getName());
								
								
								if (!Records.containsKey(Map + ":" + p.getName())) {
								
									getServer().getPluginManager().callEvent(new ParkourFinishEvent(p, Map, totalTime, true));
									p.sendMessage(PREFIX + AQUA + "You finished for the first time in "
											+ convertTime(totalTime));
									Records.put(Map + ":" + p.getName(), totalTime);
									saveScore();
									
									Map<String, Long> records = getRecords(Map);
									Map.Entry<String, Long> entry = records.entrySet().iterator().next();
									Long topTime = entry.getValue();
									String topName = entry.getKey();
									if(!topName.equalsIgnoreCase(p.getName())){
									p.sendMessage(PREFIX + AQUA + "Global record: "+GRAY + topName + AQUA + " | " + GRAY + convertTime(topTime));
									}
									//if user time > first record
									if (totalTime <= topTime){
										if (BroadcastMessage) {
											getServer().broadcastMessage(PREFIX+
													ChatColor.translateAlternateColorCodes('&', BroadcastMsg)
															.replaceAll("PLAYER", p.getName())
															.replaceAll("TIME", convertTime(totalTime))
															.replaceAll("MAPNAME", getMapName(Map)));
										}
									}
									giveReward(p, Map);

								} else {
									Map<String, Long> records = getRecords(Map);
									Map.Entry<String, Long> entry = records.entrySet().iterator().next();
									Long topTime = entry.getValue();
									String topName = entry.getKey();
									
									// Player beat old score
									if (Records.get(Map + ":" + p.getName()) >= totalTime) {
										p.sendMessage(PREFIX + GREEN + "You beat your old time of " + GRAY + convertTime(Records.get(Map + ":" + p.getName())));
										p.sendMessage(PREFIX + AQUA + "You finished in " + GRAY + convertTime(totalTime));
										
										Records.put(Map + ":" + p.getName(), totalTime);
										saveScore();
										if(!topName.equalsIgnoreCase(p.getName())){
										p.sendMessage(PREFIX + AQUA + "Global record: "+ GRAY + topName + AQUA + " | " + GRAY + convertTime(topTime));
										}
										
										// Player beat best global time
										if (totalTime <= topTime){
											if (BroadcastMessage) {
												getServer().broadcastMessage(
														ChatColor.translateAlternateColorCodes('&', BroadcastMsg)
																.replaceAll("PLAYER", p.getName())
																.replaceAll("TIME", convertTime(totalTime))
																.replaceAll("MAPNAME", getMapName(Map)));
											}
										}
												
										giveReward(p, Map);

									} else {
										String username;
										p.sendMessage(PREFIX + RED + "You didn't beat your old time of "+ GRAY + convertTime(Records.get(Map + ":" + p.getName())));
										p.sendMessage(PREFIX + AQUA + "You finished this parkour in " + convertTime(totalTime));

										if(topName.equalsIgnoreCase(p.getName())){
											username = "You";
										}
										else{
											username = topName;
										}
										p.sendMessage(PREFIX + AQUA + "Global record: " +GRAY+ username +AQUA+ " | " +GRAY+ convertTime(topTime));
										if (!rewardIfBetterScore) {
											giveReward(p, Map);
										}
									}
									
									getServer().getPluginManager().callEvent(new ParkourFinishEvent(p, Map, totalTime, false));

								}

								// Adds delay before lobby TP
								final String pl = p.getName();
								if (lobby != null) {
									getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
										public void run() {
											getServer().getPlayer(pl).teleport(lobby);
										}
									}, 5L);
								}
							} else if (PlCheckpoint == (Checkpoint - 1)) {

								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								setPlCheckpoint(p.getName(), Checkpoint);
								p.sendMessage(PREFIX + AQUA + "Checkpoint " + (Checkpoint - 1) + "/" + (TotalCheckpoints - 2));
								getServer().getPluginManager().callEvent(new ParkourCheckpointEvent(p, Map, (Checkpoint-1), System.currentTimeMillis() - getPlTime(Parkour.get(p.getName()))));

							} else if (Checkpoint <= PlCheckpoint) {
								p.sendMessage(PREFIX + RED + "You already reached this checkpoint!");

							} else if (Checkpoint > PlCheckpoint) {
								p.sendMessage(PREFIX + RED + "You forgot to pass the last checkpoint!");

							}
						}
					}
				}
			}
			if (Parkour.containsKey(p.getName())) {
				int Map = getPlMapNumber(Parkour.get(p.getName()).toString());
				if ((e.getTo().getBlock().getType() == Material.WATER || e.getTo().getBlock().getType() == Material.STATIONARY_WATER)) {
					if (getConfig().getBoolean("Parkour.map" + Map + ".waterrespawn"))
						teleportLastCheckpoint(e.getPlayer());
				}
				if ((e.getTo().getBlock().getType() == Material.LAVA || e.getTo().getBlock().getType() == Material.STATIONARY_LAVA)) {
					if (getConfig().getBoolean("Parkour.map" + Map + ".lavarespawn"))
						teleportLastCheckpoint(e.getPlayer());
				}
			}
		}
	}


/*
 * 	Functions
 */
	
	private void teleportLastCheckpoint(Player p) {
		FileConfiguration cfg = getConfig();
		Location lastCheckpoint = null;

		int MapNumber = getPlMapNumber(Parkour.get(p.getName()));
		int PlCheckpoint = getPlCheckpoint(Parkour.get(p.getName()));

		if (PlCheckpoint == 1 || !LastCheckpointTeleport) // Teleport to map spawn
		{
			if (cfg.contains("Parkour.map" + MapNumber + ".spawn")) {
				lastCheckpoint = new Location(
						getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
						cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posX"), cfg.getDouble("Parkour.map"
								+ MapNumber + ".spawn.posY"), cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posZ"));

				lastCheckpoint.setPitch((float) cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posPitch"));
				lastCheckpoint.setYaw((float) cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posYaw"));

				p.teleport(lastCheckpoint);
			} else {
				lastCheckpoint = new Location(
						getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
						cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posX") + 0.5, cfg.getDouble("Parkour.map"
								+ MapNumber + ".cp.1.posY"),
						cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posZ") + 0.5);

				lastCheckpoint.setPitch(p.getLocation().getPitch());
				lastCheckpoint.setYaw(p.getLocation().getYaw());
				p.teleport(lastCheckpoint);
			}
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

	private void setPlCheckpoint(String p, int Cp) {
		String HashTableSrc = Parkour.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String CpFinal = Splitter[0] + "_" + Splitter[1] + "_" + Cp;
		Parkour.put(p, CpFinal);
	}

	private void setPlTime(String p, Long Time) {
		String HashTableSrc = Parkour.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String TimeFinal = Splitter[0] + "_" + Time + "_" + Splitter[2];
		Parkour.put(p, TimeFinal);
	}

	private Long getPlTime(String HashTable) {
		String[] Splitter = HashTable.split("_");
		Long Time = Long.valueOf(Splitter[1]);
		return Time;
	}

	private int getPlCheckpoint(String HashTable) {
		String[] Splitter = HashTable.split("_");
		int Cp = Integer.parseInt(Splitter[2]);
		return Cp;
	}

	private int getPlMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
		int mapNumber = Integer.parseInt(Splitter[0]);
		return mapNumber;
	}

	private int getCpMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
		int CpMap = Integer.parseInt(Splitter[0]);
		return CpMap;
	}

	private int getCheckpoint(String HashTable) {
		String[] Splitter = HashTable.split("_");
		int CpMap = Integer.parseInt(Splitter[1]);
		return CpMap;
	}

	private int getCfgTotalCheckpoints(int mapNumber) {
		return getConfig().getInt("Parkour.map" + mapNumber + ".numberCp");
	}

	private boolean mapExist(String MapNumber) {
		if (getConfig().getInt("Parkour.map" + MapNumber + ".numberCp") != 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isNumber(String number) {
		try {
			Integer.parseInt(number);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void intCheckpointsLoc() {
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

	private void intMaps() {
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

	private void loadToggleMap() {
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

	private void loadLobby() {
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

	private int maxMapNumber() {
		return getConfig().getInt("Parkour.mapsnumber");
	}

	private void saveScore() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream((path))));
			oos.writeObject((Object) Records);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadScore() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)));
			Records.clear();
			@SuppressWarnings("unchecked")
			HashMap<String, Long> scoreMap = (HashMap<String, Long>) ois.readObject();
			Records = scoreMap;
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int toInt(String msg) {
		return Integer.parseInt(msg);
	}

	private void debug(String msg) {
		System.out.println("[ CBParkourDebug ] " + msg);
	}

	private void giveReward(Player p, int mapNumber) {
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
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
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
	 * 
	 * @param map
	 * @return
	 */
	public Map<String, Long> getRecords(int map) {
		Map<String, Long> records = new HashMap<String, Long>();
		for (String m : Records.keySet()) {
			String[] s = m.split(":");
			if (toInt(s[0]) == map) {
				records.put(s[1], Records.get(m));
			}
		}
		return sortByValue(records);
	}


	
	/**
	 * Converts a time in ms into a good read readable format
	 * 
	 * @param ms
	 * @return
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
	public String convertTimeShort(long ms) {
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
		/*if (mins < 10) {
			minsS = "0" + minsS;
		}*/
		/*if (hours < 10) {
			hoursS = hoursS;
		}*/

		return hoursS + "h:" + minsS + "m:" + secsS + "s:" + ms2 + "ms";
	}

	/**
	 * Displays the highscores of a map to a player
	 * 
	 * @param map
	 * @param player
	 */
	public void displayHighscores(int map, Player player) {
		Map<String, Long> records = getRecords(map);
		player.sendMessage(GOLD + "---------=[ " + D_AQUA + "Best Times for " + getMapName(map) + GOLD + " ]=---------");
		boolean inTopTen = false;
		int counter = 1;
		for (String p : records.keySet()) {
			if (p.equals(player.getName())) inTopTen = true;
			player.sendMessage(WHITE + "#" +AQUA + counter + " " + p + " - " + convertTime(records.get(p).longValue()));
			counter++;
			if (counter == 11) break;
		}
		if (!inTopTen && records.containsKey(player.getName())) {
			player.sendMessage(GREEN + "-- Your time --");

			player.sendMessage(WHITE + "#" +AQUA + "x " + player.getName() + " - "
					+ convertTime(records.get(player.getName()).longValue()));
		}
	}

	public String getMapName(int mapNumber) {
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapName")) {
			return getConfig().getString("Parkour.map" + mapNumber + ".mapName");
		} else {
			return "unknownMapName";
		}
	}
	public int getMapNumber(String mapName){
		int mapsnumber = getConfig().getInt("Parkour.mapsnumber");

		for(int x = 1; x<=mapsnumber; x++){
			String configMapName = getConfig().getString("Parkour.map" + x + ".mapName");
			
			if (mapName.equalsIgnoreCase(configMapName)){
				return x;
			}
		}
		return 0;
	}
	public int getMapPrevious(int mapNumber) {
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapPrevious")) {
			return getConfig().getInt("Parkour.map" + mapNumber + ".mapPrevious");

		} else {
			return 0;
		}
	}
	public int getMapNext(int mapNumber) {
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapNext")) {
			return getConfig().getInt("Parkour.map" + mapNumber + ".mapNext");

		} else {
			return 0;
		}
	}

	public int getMapId(String mapName) {
		for (int i : maps) {
			if (getConfig().getString("Parkour.map" + i + ".mapName").equals(mapName)) { return i; }
		}
		return -1;
	}
}