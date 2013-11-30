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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

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
 * 	Events
 */
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		if (isPlayerInParkour(e.getPlayer())) {
			ParkourContainer.remove(e.getPlayer().getName());
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
			if (isPlayerInParkour(p) && InvincibleWhileParkour) {
				e.setCancelled(true);
				p.setFireTicks(0);
			}

		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		Player player = e.getPlayer();
		if (e.getLine(0).equalsIgnoreCase("[pk]") && !player.hasPermission("parkour.mapeditor")) {
			e.setCancelled(true);
			e.getBlock().setType(Material.AIR);
			sendError("noPermission", player, this);
		}

		if (e.getPlayer().hasPermission("parkour.mapeditor")) {
			// 15 char max per lines (on sign)

			if (e.getLine(0).equalsIgnoreCase("[pk]")) {
				if (e.getLine(1).equalsIgnoreCase("leave")) {
					e.setLine(0, "[Parkour]");
					e.setLine(1, "Leave");
				} else if (e.getLine(1).equalsIgnoreCase("join")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[Parkour]");
							e.setLine(1, "Join");
							e.setLine(2, AQUA + getMapName(MapNumber));
						} else {
							e.setCancelled(true);
							e.getBlock().setType(Material.AIR);
							sendError("badmap", player, this);
						}
					} else {
						e.setCancelled(true);
						e.getBlock().setType(Material.AIR);
						sendError("2notnumber", player, this);
					}
				} else if (e.getLine(1).equalsIgnoreCase("info")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "Parkour #" + MapNumber);
							e.setLine(1, "---------------");
							e.setLine(2, AQUA + getMapName(MapNumber));
						} else {
							e.setCancelled(true);
							e.getBlock().setType(Material.AIR);
							sendError("badmap", player, this);
						}
					} else {
						e.setCancelled(true);
						e.getBlock().setType(Material.AIR);
						sendError("2notnumber", player, this);
					}
				} else if (e.getLine(1).equalsIgnoreCase("best")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[Parkour]");
							e.setLine(1, "Best Times");
							e.setLine(2, AQUA + getMapName(MapNumber));
							e.setLine(3, "Click Me!");

						} else {
							e.setCancelled(true);
							e.getBlock().setType(Material.AIR);
							sendError("badmap", player, this);
						}
					} else {
						e.setCancelled(true);
						e.getBlock().setType(Material.AIR);
						sendError("2notnumber", player, this);
					}
				} else {
					e.setCancelled(true);
					e.getBlock().breakNaturally();
					sendError("badsign", player, this);
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
								if (!permission.has(p, "parkour.use")) {
									sendError("noParkourPermission", p, this);
									return;
								}

								if (!toggleParkour.get(mapNumber)) {
									sendError("parkourDisabled", p, this);
									return;
								}
								
								if(getMapPrevious(mapNumber) != 0){
									if(!permission.has(p, "parkour.completed.map"+getMapPrevious(mapNumber))){
										sendInfo("notUnlocked", p, mapNumber);
										return;
									}
								}
								
	
								if (isPlayerInParkour(p)) {
									ParkourContainer.remove(p.getName());
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
									p.setGameMode(GameMode.ADVENTURE);
									p.sendMessage(PREFIX+ AQUA+"Welcome to "+ GREEN +getMapName(mapNumber));
								} else {
									p.sendMessage(PREFIX + RED + "Map spawn is not set");
								}
							} else {
								e.getPlayer().sendMessage(RED + "This map no longer exists");
							}
						}
					}	
					
					if (s.getLine(1).equalsIgnoreCase("leave")) {
						if (isPlayerInParkour(e.getPlayer())) {
							e.getPlayer().sendMessage(AQUA + "You have left the parkour");
							ParkourContainer.remove(e.getPlayer().getName());
	
						}
						if (lobby != null) {
							e.getPlayer().teleport(lobby);
							e.getPlayer().setGameMode(GameMode.ADVENTURE);
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
                    e.setCancelled(true);
				}
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
		/* Player hits a registered checkpoint */
		if (((int) e.getFrom().getX() != (int) e.getTo().getX())
				|| ((int) e.getFrom().getY() != (int) e.getTo().getY())
				|| ((int) e.getFrom().getZ() != (int) e.getTo().getZ())) {
			if (e.getTo().getBlock().getType() == Material.STONE_PLATE) {
				int x = e.getTo().getBlock().getX();
				int y = e.getTo().getBlock().getY();
				int z = e.getTo().getBlock().getZ();
				Location bLoc = new Location(e.getTo().getWorld(), x, y, z);

				if (cLoc.containsKey(bLoc)) {

					int Checkpoint = getCheckpoint(cLoc.get(bLoc));
					int mapNumber = getCpMapNumber(cLoc.get(bLoc));
					
					if (!permission.has(p, "parkour.use")) {
						sendError("noParkourPermission", p, this);
						if (lobby != null) {
							p.teleport(lobby);
							p.setGameMode(GameMode.ADVENTURE);
							p.sendMessage(PREFIX + AQUA + "You have been returned to the lobby");
						}
						return;
					}

					if (!toggleParkour.get(mapNumber)) {
						sendError("parkourDisabled", p, this);
						if (lobby != null) {
							p.teleport(lobby);
							p.setGameMode(GameMode.ADVENTURE);
							p.sendMessage(PREFIX + AQUA + "You have been returned to the lobby");
						}
						return;
					}
					
					if(getMapPrevious(mapNumber) != 0){
						if(!permission.has(p, "parkour.completed.map"+getMapPrevious(mapNumber))){
							sendInfo("notUnlocked", p, mapNumber);
							if (lobby != null) {
								p.teleport(lobby);
								p.setGameMode(GameMode.ADVENTURE);
								p.sendMessage(PREFIX + AQUA + "You have been returned to the lobby");
							}
							return;
						}
					}
					
					// Player starts course
					if (!isPlayerInParkour(p)) {

						if (Checkpoint == 1) {
							int Map = getCpMapNumber(cLoc.get(bLoc));
							p.setGameMode(GameMode.ADVENTURE);
							getServer().getPluginManager().callEvent(new ParkourStartEvent(p, Map, false));
							
							ParkourContainer.put(
									p.getName(),
									(getCpMapNumber(cLoc.get(bLoc)) + "_"
											+ System.currentTimeMillis() + "_1"));
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
					} 
					// Player is in a parkour and hits a checkpoint
					else {
						int PlCheckpoint = getPlCheckpoint(ParkourContainer.get(p.getName()));
						int CpMap = getCpMapNumber(cLoc.get(bLoc));
						int Map = getPlMapNumber(ParkourContainer.get(p.getName()));
						int TotalCheckpoints = getCfgTotalCheckpoints(Map);
						// Start new course
						if (CpMap != Map) {
							if (Checkpoint == 1) {
								getServer().getPluginManager().callEvent(new ParkourStartEvent(p, Map, false));						
								p.sendMessage(PREFIX + AQUA + "You have started your timer for " + GREEN + getMapName(CpMap));
								ParkourContainer.put(
										p.getName(),
										(getCpMapNumber(cLoc.get(bLoc)) + "_"
												+ System.currentTimeMillis() + "_1"));
								
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
								p.setGameMode(GameMode.ADVENTURE);
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
								setPlTime(p.getName(), System.currentTimeMillis());
								setPlCheckpoint(p.getName(), 1);

							} 
							/* Player completes course */
							else if ((Checkpoint == TotalCheckpoints) && (PlCheckpoint == (Checkpoint - 1))) {
								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								
								// Unlock next course
								if(!permission.has(p, "parkour.completed.map"+ Map)){
									permission.playerAdd(p, "parkour.completed.map"+ Map);
									
									FileConfiguration cfg = getConfig();
									if(cfg.getInt("Parkour.map"+Map+".mapNext") != 0){
										sendInfo("mapUnlock", p, Map);
										final Player player = p;
										getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                            @Override
											public void run() {
												playJingle(player);
											}
										}, 5L);
									}
								}
								
								long totalTime = System.currentTimeMillis()
										- getPlTime(ParkourContainer.get(p.getName()));
								ParkourContainer.remove(p.getName());
								
								
								if (!Records.containsKey(Map + ":" + p.getName())) {
								
									getServer().getPluginManager().callEvent(new ParkourFinishEvent(p, Map, totalTime, true));
									p.sendMessage(PREFIX + AQUA + "You finished for the first time in " +
											GRAY + convertTime(totalTime));
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
									giveReward(p);

								} else {
									Map<String, Long> records = getRecords(Map);
									Map.Entry<String, Long> entry = records.entrySet().iterator().next();
									Long topTime = entry.getValue();
									String topName = entry.getKey();
									
									// Player beat old score
									if (Records.get(Map + ":" + p.getName()) >= totalTime) {
										p.sendMessage(PREFIX + GREEN + "You beat your old time of " + GRAY + convertTime(Records.get(Map + ":" + p.getName())));
										p.sendMessage(PREFIX + AQUA + "You finished this parkour in " + GRAY + convertTime(totalTime));
										
										Records.put(Map + ":" + p.getName(), totalTime);
										saveScore();
										if(!topName.equalsIgnoreCase(p.getName())){
										p.sendMessage(PREFIX + AQUA + "Global record: "+ GRAY + topName + AQUA + " | " + GRAY + convertTime(topTime));
										}
										
										// Player beat best global time
										if (totalTime <= topTime){
											if (BroadcastMessage) {
												getServer().broadcastMessage(PREFIX+
														ChatColor.translateAlternateColorCodes('&', BroadcastMsg)
																.replaceAll("PLAYER", p.getName())
																.replaceAll("TIME", convertTime(totalTime))
																.replaceAll("MAPNAME", getMapName(Map)));
											}
										}
												
										giveReward(p);

									} else {
										String username;
										p.sendMessage(PREFIX + RED + "You didn't beat your old time "+ GRAY + convertTime(Records.get(Map + ":" + p.getName())));
										p.sendMessage(PREFIX + AQUA + "You finished this parkour in " +GRAY+ convertTime(totalTime));

										if(topName.equalsIgnoreCase(p.getName())){
											username = "You";
										}
										else{
											username = topName;
										}
										p.sendMessage(PREFIX + AQUA + "Global record: " +GRAY+ username +AQUA+ " | " +GRAY+ convertTime(topTime));
										if (!rewardIfBetterScore) {
											giveReward(p);
										}
									}
									
									getServer().getPluginManager().callEvent(new ParkourFinishEvent(p, Map, totalTime, false));

								}

								// Adds delay before lobby TP
								final String pl = p.getName();
								if (lobby != null) {
									getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                                                                @Override
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
								getServer().getPluginManager().callEvent(new ParkourCheckpointEvent(p, Map, (Checkpoint-1), System.currentTimeMillis() - getPlTime(ParkourContainer.get(p.getName()))));

							} else if (Checkpoint <= PlCheckpoint) {
								p.sendMessage(PREFIX + RED + "You already reached this checkpoint!");

							} else if (Checkpoint > PlCheckpoint) {
								p.sendMessage(PREFIX + RED + "You forgot to pass the last checkpoint!");

							}
						}
					}
				}
			}
			if (isPlayerInParkour(p)) {
				int Map = getPlMapNumber(ParkourContainer.get(p.getName()));
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

	private void setPlCheckpoint(String p, int Cp) {
		String HashTableSrc = ParkourContainer.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String CpFinal = Splitter[0] + "_" + Splitter[1] + "_" + Cp;
		ParkourContainer.put(p, CpFinal);
	}

	private void setPlTime(String p, Long Time) {
		String HashTableSrc = ParkourContainer.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String TimeFinal = Splitter[0] + "_" + Time + "_" + Splitter[2];
		ParkourContainer.put(p, TimeFinal);
	}

	private Long getPlTime(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Long.valueOf(Splitter[1]);
	}

	private int getPlCheckpoint(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Integer.parseInt(Splitter[2]);
	}

	private int getPlMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Integer.parseInt(Splitter[0]);
	}

	private int getCpMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
        return Integer.parseInt(Splitter[0]);
	}

	private int getCheckpoint(String HashTable) {
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
	public void sendInfo(String info, Player player, int mapNumber){
		if(info.equalsIgnoreCase("notUnlocked")) {
			player.sendMessage(PREFIX + RED + "You have not unlocked this parkour, complete "+GREEN + getMapName(getMapPrevious(mapNumber))+RED+" to progress");

		} else if(info.equalsIgnoreCase("mapUnlock")) {
			String nextMapName = getMapName(getMapNext(mapNumber));
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
	private void playJingle(final Player player){
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

	private void giveReward(Player p) {
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
	 * 
	 * @param map
     *
	 * @return
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

	/**
	 * Displays the high-scores of a map to a player
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

	public Entry<String, Long>  getHighscore(int mapNumber){
		Map<String, Long> records = getRecords(mapNumber);
		Map.Entry<String, Long> entry = records.entrySet().iterator().next();
		return entry;
	}
	public String getMapName(int mapNumber) {
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapName")) {
			return getConfig().getString("Parkour.map" + mapNumber + ".mapName");
		} else {
			return "unknownMapName";
		}
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

	public int getMapNumber(String mapName) {
		for (int i : maps) {
			if (getConfig().getString("Parkour.map" + i + ".mapName").equals(mapName)) { 
				return i; }
		}
		return 0;
	}
	
	public String getPrefix(){
		return PREFIX;
	}

	public String getAPrefix(){
		return APREFIX;
	}
}