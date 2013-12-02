package com.citibuild.cbparkour;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

public class PlayerListener implements Listener{

	private final Parkour plugin;
	public PlayerListener(Parkour plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {		
		plugin.pkFuncs.savePlayerInfo(e.getPlayer());
		
		if (plugin.pkFuncs.isPlayerInParkour(e.getPlayer())) {
			plugin.pkVars.ParkourContainer.remove(e.getPlayer().getName());
		}
		if (plugin.pkVars.rewardPlayersCooldown.containsKey(e.getPlayer().getName())) {
			plugin.pkVars.rewardPlayersCooldown.remove(e.getPlayer().getName());
		}
		if (e.getPlayer().getName().equals(plugin.pkVars.newMapPlayerEditor)) {
			plugin.pkVars.newMapPlayerEditor = null;
			plugin.pkVars.newMapName = null;
			plugin.pkVars.newMapPrevious = 0;
			plugin.pkVars.newMapNext = 0;
			plugin.pkVars.newMapCheckpoints.clear();
			plugin.pkVars.CheckpointNumber = 0;
			plugin.pkVars.NewMapNumber = 0;
			plugin.pkVars.newMap = false;
		}
	}


	@EventHandler
	public void onPlayerDmg(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (plugin.pkFuncs.isPlayerInParkour(p) && plugin.pkVars.InvincibleWhileParkour) {
				e.setCancelled(true);
				p.setFireTicks(0);
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
						int mapID = plugin.getMapNumber(ChatColor.stripColor(s.getLine(2)));
						if (mapID != 0) {
							if (plugin.pkVars.maps.contains(mapID)) {
								Player p = e.getPlayer();
								if (!Parkour.permission.has(p, "parkour.use")) {
									plugin.pkFuncs.sendError("noParkourPermission", p, plugin);
									return;
								}

								if (!plugin.pkVars.toggleParkour.get(mapID)) {
									plugin.pkFuncs.sendError("parkourDisabled", p, plugin);
									return;
								}

								if(plugin.getMapPrevious(mapID) != 0){
									if(!Parkour.permission.has(p, "parkour.completed.map"+plugin.getMapPrevious(mapID))){
										plugin.pkFuncs.sendInfo("notUnlocked", p, mapID, plugin);
										return;
									}
								}


								if (plugin.pkFuncs.isPlayerInParkour(p)) {
									plugin.pkVars.ParkourContainer.remove(p.getName());
								}

								FileConfiguration cfg = plugin.getConfig();

								if (cfg.contains("Parkour.map" + mapID + ".spawn")) {
									Location loc = new Location(plugin.getServer().getWorld(
											plugin.getConfig().getString("Parkour.map" + mapID + ".world")),
											cfg.getDouble("Parkour.map" + mapID + ".spawn.posX"),
											cfg.getDouble("Parkour.map" + mapID + ".spawn.posY"),
											cfg.getDouble("Parkour.map" + mapID + ".spawn.posZ"));

									loc.setPitch((float) cfg.getDouble("Parkour.map" + mapID + ".spawn.posPitch"));
									loc.setYaw((float) cfg.getDouble("Parkour.map" + mapID + ".spawn.posYaw"));

									if (!loc.getChunk().isLoaded()) {
										loc.getChunk().load(true);
									}

									p.teleport(loc);
									p.sendMessage(plugin.pkVars.PREFIX+ plugin.pkVars.AQUA+"Welcome to "+ plugin.pkVars.GREEN +plugin.getMapName(mapID));
								} else {
									p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.RED + "Map spawn is not set");
								}
							} else {
								e.getPlayer().sendMessage(plugin.pkVars.RED + "This map no longer exists");
							}
						}
					}	

					if (s.getLine(1).equalsIgnoreCase("leave")) {
						if (plugin.pkFuncs.isPlayerInParkour(e.getPlayer())) {
							e.getPlayer().sendMessage(plugin.pkVars.AQUA + "You have left the parkour");
							plugin.pkVars.ParkourContainer.remove(e.getPlayer().getName());

						}
						if (plugin.pkVars.lobby != null) {
							e.getPlayer().teleport(plugin.pkVars.lobby);
							e.getPlayer().setGameMode(plugin.pkVars.loadedUsers.get(e.getPlayer().getName()).getPrevGM());
						}
					}

					if (s.getLine(1).equalsIgnoreCase("Best Times")) {
						int mapID = plugin.getMapNumber(ChatColor.stripColor(s.getLine(2)));
						if (mapID != 0) {
							if (plugin.pkVars.maps.contains(mapID)) {
								plugin.displayHighscores(mapID, e.getPlayer());
							} else {
								e.getPlayer().sendMessage(plugin.pkVars.RED + "This map no longer exists");
							}
						}
					}
					e.setCancelled(true);
				}
			}
			/* Map Creation */
			if (plugin.pkVars.newMap)
			{
				if (e.getPlayer().getName().equals(plugin.pkVars.newMapPlayerEditor) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Player p = e.getPlayer();
					ItemStack stick = new ItemStack(Material.STICK, 1);
					if (p.getItemInHand().getTypeId() == 280 && e.getClickedBlock().getTypeId() == 70) {
						if (!plugin.pkVars.cLoc.containsKey(e.getClickedBlock().getLocation())) {
							Location bLoc = e.getClickedBlock().getLocation();

							if (plugin.pkVars.newMapCheckpoints.contains(bLoc)) {
								p.sendMessage(plugin.pkVars.APREFIX + plugin.pkVars.RED + "This checkpoint is alredy used for this map");
							} else {
								FileConfiguration cfg = plugin.getConfig();

								p.sendMessage(plugin.pkVars.APREFIX + plugin.pkVars.AQUA + "Checkpoint " + plugin.pkVars.GREEN + plugin.pkVars.CheckpointNumber + plugin.pkVars.AQUA + " set on new map " + plugin.pkVars.GREEN + plugin.pkVars.NewMapNumber);

								cfg.set("Parkour.map" + plugin.pkVars.NewMapNumber + ".cp." + plugin.pkVars.CheckpointNumber + ".posX", bLoc.getX());
								cfg.set("Parkour.map" + plugin.pkVars.NewMapNumber + ".cp." + plugin.pkVars.CheckpointNumber + ".posY", bLoc.getY());
								cfg.set("Parkour.map" + plugin.pkVars.NewMapNumber + ".cp." + plugin.pkVars.CheckpointNumber + ".posZ", bLoc.getZ());

								plugin.saveConfig();
								plugin.pkVars.newMapCheckpoints.add(bLoc);
								plugin.pkVars.CheckpointNumber++;

							}
						} else {
							p.sendMessage(plugin.pkVars.APREFIX + plugin.pkVars.RED + "This checkpoint is alredy used for another map");
						}
					} else {
						p.sendMessage(plugin.pkVars.APREFIX + plugin.pkVars.RED + "Use a stick to place checkpoints (Right click on stone pressure plate)");
						p.getInventory().addItem(stick);
					}
				}
			}
		}
		// Parkour items here
		if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || 
				e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

			if(e.getPlayer().getItemInHand().getType() == Material.SLIME_BALL && 
					ChatColor.stripColor(e.getPlayer().getItemInHand().getItemMeta().getDisplayName()).equalsIgnoreCase("checkpoint")) {

				plugin.getServer().dispatchCommand(e.getPlayer(), plugin.pkItems.slime_cmd);
				plugin.pkItems.giveSlime(e.getPlayer());
				e.setCancelled(true);

			} else if(e.getPlayer().getItemInHand().getType() == Material.RECORD_6 && 
					ChatColor.stripColor(e.getPlayer().getItemInHand().getItemMeta().getDisplayName()).equalsIgnoreCase("current time")) {

				plugin.getServer().dispatchCommand(e.getPlayer(), plugin.pkItems.musicdisk_cmd);
				plugin.pkItems.giveMusicDisk(e.getPlayer());
				e.setCancelled(true);

			}
		}
	}

	/*
	 * Stop dropping of ParkourItems
	 */
	@EventHandler
	public void onDropEvent(PlayerDropItemEvent event) {
		ItemMeta eMeta = event.getItemDrop().getItemStack().getItemMeta();
		if(eMeta.hasDisplayName()) {
			if(plugin.pkItems.itemsList.contains(ChatColor.stripColor(eMeta.getDisplayName().toLowerCase()))) {
				event.setCancelled(true);
			}
		}
	}

	/*
	 * Things to do on PlayerJoin
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String username = player.getName();
		plugin.pkFuncs.loadPlayerInfo(event.getPlayer());
		
		int mapID = plugin.pkVars.loadedUsers.get(username).getMapID();
		if(mapID == 0) {
			if(!Parkour.permission.has(player, "parkour.admin")) {
				player.teleport(plugin.pkVars.lobby);
			}
		} else {
			if(plugin.pkFuncs.mapExist("" + mapID)) {
				PlayerInfo userPInfo = plugin.pkVars.loadedUsers.get(username);
				plugin.pkVars.ParkourContainer.put(username, mapID + "_" + (System.currentTimeMillis() - userPInfo.getTime()) + "_" + userPInfo.getCheckpoint());
			} else {
				if(!Parkour.permission.has(player, "parkour.admin")) {
					player.teleport(plugin.pkVars.lobby);
				}
			}
		}
		
		plugin.pkItems.giveItems(event.getPlayer());
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

				if (plugin.pkVars.cLoc.containsKey(bLoc)) {

					int Checkpoint = plugin.pkFuncs.getCheckpoint(plugin.pkVars.cLoc.get(bLoc));
					int mapID = plugin.pkFuncs.getCpMapNumber(plugin.pkVars.cLoc.get(bLoc));

					if (!Parkour.permission.has(p, "parkour.use")) {
						plugin.pkFuncs.sendError("noParkourPermission", p, plugin);
						if (plugin.pkVars.lobby != null) {
							p.teleport(plugin.pkVars.lobby);
							p.setGameMode(plugin.pkVars.loadedUsers.get(e.getPlayer().getName()).getPrevGM());
							plugin.pkVars.loadedUsers.get(p.getName()).setMapID(0);
							p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You have been returned to the lobby");
						}
						return;
					}

					if (!plugin.pkVars.toggleParkour.get(mapID)) {
						plugin.pkFuncs.sendError("parkourDisabled", p, plugin);
						if (plugin.pkVars.lobby != null) {
							p.teleport(plugin.pkVars.lobby);
							p.setGameMode(plugin.pkVars.loadedUsers.get(e.getPlayer().getName()).getPrevGM());
							plugin.pkVars.loadedUsers.get(p.getName()).setMapID(0);
							p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You have been returned to the lobby");
						}
						return;
					}

					if(plugin.getMapPrevious(mapID) != 0){
						if(!Parkour.permission.has(p, "parkour.completed.map"+plugin.getMapPrevious(mapID))){
							plugin.pkFuncs.sendInfo("notUnlocked", p, mapID, plugin);
							if (plugin.pkVars.lobby != null) {
								p.teleport(plugin.pkVars.lobby);
								p.setGameMode(plugin.pkVars.loadedUsers.get(e.getPlayer().getName()).getPrevGM());
								plugin.pkVars.loadedUsers.get(p.getName()).setMapID(0);
								p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You have been returned to the lobby");
							}
							return;
						}
					}

					// Player starts course
					if (!plugin.pkFuncs.isPlayerInParkour(p)) {

						if (Checkpoint == 1) {
							int Map = plugin.pkFuncs.getCpMapNumber(plugin.pkVars.cLoc.get(bLoc));
							plugin.pkFuncs.loadPlayerInfo(p);
							plugin.pkVars.loadedUsers.get(p.getName()).setPrevGM(p.getGameMode());
							
							p.setGameMode(GameMode.SURVIVAL);
							plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(plugin, p, Map, false));

							plugin.pkVars.ParkourContainer.put(
									p.getName(),
									(plugin.pkFuncs.getCpMapNumber(plugin.pkVars.cLoc.get(bLoc)) + "_"
											+ System.currentTimeMillis() + "_1"));
							p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You have started your timer for " + plugin.pkVars.GREEN + plugin.getMapName(Map));

							if (plugin.pkVars.CheckpointEffect) {
								p.playEffect(bLoc, Effect.POTION_BREAK, 2);
							}
							if (plugin.pkVars.removePotionsEffectsOnParkour) {
								for (PotionEffect effect : p.getActivePotionEffects()) {
									p.removePotionEffect(effect.getType());
								}
							}
							if (plugin.pkVars.FullHunger) {
								p.setFoodLevel(20);
							}
						} else {
							p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.RED + "You must start at the checkpoint 1");
						}
					} 
					// Player is in a parkour and hits a checkpoint
					else {
						p.setGameMode(GameMode.SURVIVAL);
						int PlCheckpoint = plugin.pkFuncs.getPlCheckpoint(plugin.pkVars.ParkourContainer.get(p.getName()));
						int CpMap = plugin.pkFuncs.getCpMapNumber(plugin.pkVars.cLoc.get(bLoc));
						int Map = plugin.pkFuncs.getPlMapNumber(plugin.pkVars.ParkourContainer.get(p.getName()));
						int TotalCheckpoints = plugin.pkFuncs.getCfgTotalCheckpoints(Map);
						// Start new course
						if (CpMap != Map) {
							if (Checkpoint == 1) {
								plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(plugin, p, Map, false));						
								p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You have started your timer for " + plugin.pkVars.GREEN + plugin.getMapName(CpMap));
								plugin.pkVars.ParkourContainer.put(
										p.getName(),
										(plugin.pkFuncs.getCpMapNumber(plugin.pkVars.cLoc.get(bLoc)) + "_"
												+ System.currentTimeMillis() + "_1"));

								if (plugin.pkVars.CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (plugin.pkVars.removePotionsEffectsOnParkour) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (plugin.pkVars.FullHunger) {
									p.setFoodLevel(20);
								}

							} else {
								p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.RED + "You are not in this parkour");

							}
						} 
						/* Player restarts course */
						else {

							if (Checkpoint == 1) {
								
								p.setGameMode(GameMode.SURVIVAL);
								if (plugin.pkVars.CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (plugin.pkVars.removePotionsEffectsOnParkour) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (plugin.pkVars.FullHunger) {
									p.setFoodLevel(20);
								}
								plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(plugin, p, Map, true));
								p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You have restarted your time for " + plugin.pkVars.GREEN+ plugin.getMapName(Map));
								plugin.pkFuncs.setPlTime(p.getName(), System.currentTimeMillis());
								plugin.pkFuncs.setPlCheckpoint(p.getName(), 1);

							} 
							/* Player completes course */
							else if ((Checkpoint == TotalCheckpoints) && (PlCheckpoint == (Checkpoint - 1))) {
								if (plugin.pkVars.CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								// Unlock next course
								if(!Parkour.permission.has(p, "parkour.completed.map"+ Map)){
									Parkour.permission.playerAdd(p, "parkour.completed.map"+ Map);

									FileConfiguration cfg = plugin.getConfig();
									if(cfg.getInt("Parkour.map"+Map+".mapNext") != 0){
										plugin.pkFuncs.sendInfo("mapUnlock", p, Map, plugin);
										final Player player = p;
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
											@Override
											public void run() {
												plugin.pkFuncs.playJingle(player);
											}
										}, 5L);
									}
								}

								long totalTime = System.currentTimeMillis()
										- plugin.pkFuncs.getPlTime(plugin.pkVars.ParkourContainer.get(p.getName()));
								plugin.pkVars.ParkourContainer.remove(p.getName());


								if (!plugin.pkVars.Records.containsKey(Map + ":" + p.getName())) {

									plugin.getServer().getPluginManager().callEvent(new ParkourFinishEvent(plugin, p, Map, totalTime, true));
									p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You finished "+plugin.pkVars.GREEN + plugin.getMapName(Map)+plugin.pkVars.AQUA+ " for the first time in " +
											plugin.pkVars.GRAY + plugin.convertTime(totalTime));
									plugin.pkVars.Records.put(Map + ":" + p.getName(), totalTime);
									plugin.pkFuncs.saveScore();

									Map<String, Long> records = plugin.getRecords(Map);
									Map.Entry<String, Long> entry = records.entrySet().iterator().next();
									Long topTime = entry.getValue();
									String topName = entry.getKey();
									if(!topName.equalsIgnoreCase(p.getName())){
										p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "Global record: "+plugin.pkVars.GRAY + topName + plugin.pkVars.AQUA + " | " + plugin.pkVars.GRAY + plugin.convertTime(topTime));
									}
									//if user time > first record
									if (totalTime <= topTime){
										if (plugin.pkVars.BroadcastMessage) {
											plugin.getServer().broadcastMessage(plugin.pkVars.PREFIX+
													ChatColor.translateAlternateColorCodes('&', plugin.pkVars.BroadcastMsg)
													.replaceAll("PLAYER", p.getName())
													.replaceAll("TIME", plugin.convertTime(totalTime))
													.replaceAll("MAPNAME", plugin.getMapName(Map)));
										}
									}
									plugin.pkFuncs.giveReward(p);

								} else {
									Map<String, Long> records = plugin.getRecords(Map);
									Map.Entry<String, Long> entry = records.entrySet().iterator().next();
									Long topTime = entry.getValue();
									String topName = entry.getKey();

									// Player beat old score
									if (plugin.pkVars.Records.get(Map + ":" + p.getName()) >= totalTime) {
										p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.GREEN + "You beat your old time of " + plugin.pkVars.GRAY + plugin.convertTime(plugin.pkVars.Records.get(Map + ":" + p.getName())));
										p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You finished "+plugin.pkVars.GREEN + plugin.getMapName(Map)+plugin.pkVars.AQUA+ " in " + plugin.pkVars.GRAY + plugin.convertTime(totalTime));

										plugin.pkVars.Records.put(Map + ":" + p.getName(), totalTime);
										plugin.pkFuncs.saveScore();
										if(!topName.equalsIgnoreCase(p.getName())){
											p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "Global record: "+ plugin.pkVars.GRAY + topName + plugin.pkVars.AQUA + " | " + plugin.pkVars.GRAY + plugin.convertTime(topTime));
										}

										// Player beat best global time
										if (totalTime <= topTime){
											if (plugin.pkVars.BroadcastMessage) {
												plugin.getServer().broadcastMessage(plugin.pkVars.PREFIX+
														ChatColor.translateAlternateColorCodes('&', plugin.pkVars.BroadcastMsg)
														.replaceAll("PLAYER", p.getName())
														.replaceAll("TIME", plugin.convertTime(totalTime))
														.replaceAll("MAPNAME", plugin.getMapName(Map)));
											}
										}

										plugin.pkFuncs.giveReward(p);

									} else {
										String username;
										p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.RED + "You didn't beat your old time "+ plugin.pkVars.GRAY + plugin.convertTime(plugin.pkVars.Records.get(Map + ":" + p.getName())));
										p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "You finished "+plugin.pkVars.GREEN + plugin.getMapName(Map)+plugin.pkVars.AQUA+ " in " + plugin.pkVars.GRAY + plugin.convertTime(totalTime));

										if(topName.equalsIgnoreCase(p.getName())){
											username = "You";
										}
										else{
											username = topName;
										}
										p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "Global record: " +plugin.pkVars.GRAY+ username +plugin.pkVars.AQUA+ " | " +plugin.pkVars.GRAY+ plugin.convertTime(topTime));
										if (!plugin.pkVars.rewardIfBetterScore) {
											plugin.pkFuncs.giveReward(p);
										}
									}

									plugin.getServer().getPluginManager().callEvent(new ParkourFinishEvent(plugin, p, Map, totalTime, false));

								}

								// Adds delay before lobby TP
								final String pl = p.getName();
								if (plugin.pkVars.lobby != null) {
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
										@Override
										public void run() {
											plugin.getServer().getPlayer(pl).teleport(plugin.pkVars.lobby);
											plugin.getServer().getPlayer(pl).setGameMode(plugin.pkVars.loadedUsers.get(pl).getPrevGM());
										}
									}, 5L);
								}
							} else if (PlCheckpoint == (Checkpoint - 1)) {

								long totalTime = System.currentTimeMillis()                                        
										- plugin.pkFuncs.getPlTime(plugin.pkVars.ParkourContainer.get(p.getName()));

								if (plugin.pkVars.CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								plugin.pkFuncs.setPlCheckpoint(p.getName(), Checkpoint);
								p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.AQUA + "Checkpoint " + (Checkpoint - 1) + "/" + (TotalCheckpoints - 2) +plugin.pkVars.GRAY+ " | "+ plugin.convertTime(totalTime));

								plugin.getServer().getPluginManager().callEvent(
										new ParkourCheckpointEvent(plugin, p, Map, (Checkpoint-1), totalTime));

							} else if (Checkpoint <= PlCheckpoint) {
								p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.RED + "You already reached this checkpoint!");

							} else if (Checkpoint > PlCheckpoint) {
								p.sendMessage(plugin.pkVars.PREFIX + plugin.pkVars.RED + "You forgot to pass the last checkpoint!");

							}
						}
					}
				}
			}
			if (plugin.pkFuncs.isPlayerInParkour(p)) {
				int Map = plugin.pkFuncs.getPlMapNumber(plugin.pkVars.ParkourContainer.get(p.getName()));
				if ((e.getTo().getBlock().getType() == Material.WATER || e.getTo().getBlock().getType() == Material.STATIONARY_WATER)) {
					if (plugin.getConfig().getBoolean("Parkour.map" + Map + ".waterrespawn"))
						plugin.pkFuncs.teleportLastCheckpoint(e.getPlayer());
				}
				if ((e.getTo().getBlock().getType() == Material.LAVA || e.getTo().getBlock().getType() == Material.STATIONARY_LAVA)) {
					if (plugin.getConfig().getBoolean("Parkour.map" + Map + ".lavarespawn"))
						plugin.pkFuncs.teleportLastCheckpoint(e.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void onGamemodeChange(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		if(plugin.pkFuncs.isPlayerInParkour(p)) {
			plugin.pkFuncs.sendError("gmChange", p, plugin);
			p.setGameMode(GameMode.SURVIVAL);
			e.setCancelled(true);
		}

	}
}
