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
	private UnlockFuncs unlockFuncs;
	
	public PlayerListener(Parkour plugin) {
		this.plugin = plugin;
		unlockFuncs = plugin.pkUnlockFuncs;
	}


	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		int mapID = plugin.pkFuncs.getPlMapNumber(plugin.pkVars.ParkourContainer.get(player.getName()));
		long time = System.currentTimeMillis()                                        
				- plugin.pkFuncs.getPlTime(plugin.pkVars.ParkourContainer.get(player.getName()));
		
		plugin.pkVars.loadedUsers.get(player.getName()).setMapID(mapID);
		plugin.pkVars.loadedUsers.get(player.getName()).setTime(time);
		
		plugin.pkFuncs.savePlayerInfo(player);
		plugin.pkUnlockFuncs.savePlayer(player);
		
		if (plugin.pkFuncs.isPlayerInParkour(e.getPlayer())) {
			plugin.pkVars.ParkourContainer.remove(e.getPlayer().getName());
		}
		if (plugin.pkVars.rewardPlayersCooldown.containsKey(e.getPlayer().getName())) {
			plugin.pkVars.rewardPlayersCooldown.remove(e.getPlayer().getName());
		}
		if (e.getPlayer().getName().equals(plugin.pkVars.newMapPlayerEditor)) {
			plugin.pkVars.newMapPlayerEditor = null;
			plugin.pkVars.setNewMapName(null);
			plugin.pkVars.newMapPrevious = 0;
			plugin.pkVars.setNewMapNext(0);
			plugin.pkVars.getNewMapCheckpoints().clear();
			plugin.pkVars.setCheckpointNumber(0);
			plugin.pkVars.setNewMapNumber(0);
			plugin.pkVars.setNewMap(false);
		}
	}


	@EventHandler
	public void onPlayerDmg(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (plugin.pkFuncs.isPlayerInParkour(p) && plugin.pkVars.isInvincibleWhileParkour()) {
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
									//if(!Parkour.permission.has(p, "parkour.completed.map"+plugin.getMapPrevious(mapID))){
									if(!unlockFuncs.levelUnlocked(p, mapID)) {
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
									p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "Welcome to "+ plugin.pkStrings.highlightTwo + plugin.getMapName(mapID));
								} else {
									p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultError + "Map spawn is not set");
								}
							} else {
								e.getPlayer().sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultError + "This map no longer exists");
							}
						}
					}	

					if (s.getLine(1).equalsIgnoreCase("leave")) {
						if (plugin.pkFuncs.isPlayerInParkour(e.getPlayer())) {
							e.getPlayer().sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You have left the parkour");
							plugin.pkVars.ParkourContainer.remove(e.getPlayer().getName());

						}
						if (plugin.pkVars.getLobby() != null) {
							e.getPlayer().teleport(plugin.pkVars.getLobby());
							e.getPlayer().setGameMode(plugin.pkVars.loadedUsers.get(e.getPlayer().getName()).getPrevGM());
						}
					}

					if (s.getLine(1).equalsIgnoreCase("Best Times")) {
						int mapID = plugin.getMapNumber(ChatColor.stripColor(s.getLine(2)));
						if (mapID != 0) {
							if (plugin.pkVars.maps.contains(mapID)) {
								plugin.displayHighscores(mapID, e.getPlayer());
							} else {
								e.getPlayer().sendMessage(plugin.pkStrings.defaultError + "This map no longer exists");
							}
						}
					}
					e.setCancelled(true);
				}
			}
			/* Map Creation */
			if (plugin.pkVars.isNewMap())
			{
				if (e.getPlayer().getName().equals(plugin.pkVars.newMapPlayerEditor) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Player p = e.getPlayer();
					ItemStack stick = new ItemStack(Material.STICK, 1);
					if (p.getItemInHand().getTypeId() == 280 && e.getClickedBlock().getTypeId() == 70) {
						if (!plugin.pkVars.getcLoc().containsKey(e.getClickedBlock().getLocation())) {
							Location bLoc = e.getClickedBlock().getLocation();

							if (plugin.pkVars.getNewMapCheckpoints().contains(bLoc)) {
								p.sendMessage(plugin.getAPrefix() + ChatColor.RED + "This checkpoint is alredy used for this map");
							} else {
								FileConfiguration cfg = plugin.getConfig();

								p.sendMessage(plugin.getAPrefix() + ChatColor.AQUA + "Checkpoint " + ChatColor.GREEN + plugin.pkVars.getCheckpointNumber() + ChatColor.AQUA + " set on new map " + ChatColor.GREEN + plugin.pkVars.getNewMapNumber());

								cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".cp." + plugin.pkVars.getCheckpointNumber() + ".posX", bLoc.getX());
								cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".cp." + plugin.pkVars.getCheckpointNumber() + ".posY", bLoc.getY());
								cfg.set("Parkour.map" + plugin.pkVars.getNewMapNumber() + ".cp." + plugin.pkVars.getCheckpointNumber() + ".posZ", bLoc.getZ());

								plugin.saveConfig();
								plugin.pkVars.getNewMapCheckpoints().add(bLoc);
								plugin.pkVars
										.setCheckpointNumber(plugin.pkVars
												.getCheckpointNumber() + 1);

							}
						} else {
							p.sendMessage(plugin.getAPrefix() + ChatColor.RED + "This checkpoint is alredy used for another map");
						}
					} else {
						p.sendMessage(plugin.getAPrefix() + ChatColor.RED + "Use a stick to place checkpoints (Right click on stone pressure plate)");
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
		plugin.pkFuncs.loadPlayerInfo(player);
		plugin.pkUnlockFuncs.loadPlayer(player);
		
		int mapID = plugin.pkVars.loadedUsers.get(username).getMapID();
		if(mapID == 0) {
			if(!Parkour.permission.has(player, "parkour.admin")) {
				player.teleport(plugin.pkVars.getLobby());
			}
		} else {
			if(plugin.pkFuncs.mapExist("" + mapID)) {
				PlayerInfo userPInfo = plugin.pkVars.loadedUsers.get(username);
				plugin.pkVars.ParkourContainer.put(username, mapID + "_" + (System.currentTimeMillis() - userPInfo.getTime()) + "_" + userPInfo.getCheckpoint());
			} else {
				if(!Parkour.permission.has(player, "parkour.admin")) {
					player.teleport(plugin.pkVars.getLobby());
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

				if (plugin.pkVars.getcLoc().containsKey(bLoc)) {

					int Checkpoint = plugin.pkFuncs.getCheckpoint(plugin.pkVars.getcLoc().get(bLoc));
					int mapID = plugin.pkFuncs.getCpMapNumber(plugin.pkVars.getcLoc().get(bLoc));

					if (!Parkour.permission.has(p, "parkour.use")) {
						plugin.pkFuncs.sendError("noParkourPermission", p, plugin);
						if (plugin.pkVars.getLobby() != null) {
							p.teleport(plugin.pkVars.getLobby());
							p.setGameMode(plugin.pkVars.loadedUsers.get(e.getPlayer().getName()).getPrevGM());
							plugin.pkVars.loadedUsers.get(p).setPrevGMSet(false);
							plugin.pkVars.loadedUsers.get(p.getName()).setMapID(0);
							p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You have been returned to the lobby");
						}
						return;
					}

					if (!plugin.pkVars.toggleParkour.get(mapID)) {
						plugin.pkFuncs.sendError("parkourDisabled", p, plugin);
						if (plugin.pkVars.getLobby() != null) {
							p.teleport(plugin.pkVars.getLobby());
							p.setGameMode(plugin.pkVars.loadedUsers.get(e.getPlayer().getName()).getPrevGM());
							plugin.pkVars.loadedUsers.get(p.getName()).setPrevGMSet(false);
							plugin.pkVars.loadedUsers.get(p.getName()).setMapID(0);
							p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You have been returned to the lobby");
						}
						return;
					}

					if(plugin.getMapPrevious(mapID) != 0){
						//if(!Parkour.permission.has(p, "parkour.completed.map"+plugin.getMapPrevious(mapID))){
						if(!unlockFuncs.levelUnlocked(p, mapID)) {
							plugin.pkFuncs.sendInfo("notUnlocked", p, mapID, plugin);
							if (plugin.pkVars.getLobby() != null) {
								p.teleport(plugin.pkVars.getLobby());
								p.setGameMode(plugin.pkVars.loadedUsers.get(e.getPlayer().getName()).getPrevGM());
								plugin.pkVars.loadedUsers.get(p.getName()).setPrevGMSet(false);
								plugin.pkVars.loadedUsers.get(p.getName()).setMapID(0);
								p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You have been returned to the lobby");
							}
							return;
						}
					}

					// Player starts course
					if (!plugin.pkFuncs.isPlayerInParkour(p)) {

						if (Checkpoint == 1) {
							int Map = plugin.pkFuncs.getCpMapNumber(plugin.pkVars.getcLoc().get(bLoc));
							plugin.pkFuncs.loadPlayerInfo(p);
							plugin.pkVars.loadedUsers.get(p.getName()).setPrevGM(p.getGameMode());
							plugin.pkVars.loadedUsers.get(p.getName()).setPrevGMSet(true);
							
							p.setGameMode(GameMode.ADVENTURE);
							plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(plugin, p, Map, false));

							plugin.pkVars.ParkourContainer.put(
									p.getName(),
									(plugin.pkFuncs.getCpMapNumber(plugin.pkVars.getcLoc().get(bLoc)) + "_"
											+ System.currentTimeMillis() + "_1"));
							p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You have started your timer for " + plugin.pkStrings.highlightTwo + plugin.getMapName(Map));

							if (plugin.pkVars.isCheckpointEffect()) {
								p.playEffect(bLoc, Effect.POTION_BREAK, 2);
							}
							if (plugin.pkVars.isRemovePotionsEffectsOnParkour()) {
								for (PotionEffect effect : p.getActivePotionEffects()) {
									p.removePotionEffect(effect.getType());
								}
							}
							if (plugin.pkVars.isFullHunger()) {
								p.setFoodLevel(20);
							}
						} else {
							p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultError + "You must start at the checkpoint 1");
						}
					} 
					// Player is in a parkour and hits a checkpoint
					else {
//						p.setGameMode(GameMode.ADVENTURE);
						int PlCheckpoint = plugin.pkFuncs.getPlCheckpoint(plugin.pkVars.ParkourContainer.get(p.getName()));
						int CpMap = plugin.pkFuncs.getCpMapNumber(plugin.pkVars.getcLoc().get(bLoc));
						int Map = plugin.pkFuncs.getPlMapNumber(plugin.pkVars.ParkourContainer.get(p.getName()));
						int TotalCheckpoints = plugin.pkFuncs.getCfgTotalCheckpoints(Map);
						// Start new course
						if (CpMap != Map) {
							if (Checkpoint == 1) {
								plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(plugin, p, Map, false));						
								p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You have started your timer for " + plugin.pkStrings.highlightTwo + plugin.getMapName(CpMap));
								plugin.pkVars.ParkourContainer.put(
										p.getName(),
										(plugin.pkFuncs.getCpMapNumber(plugin.pkVars.getcLoc().get(bLoc)) + "_"
												+ System.currentTimeMillis() + "_1"));

								if (plugin.pkVars.isCheckpointEffect()) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (plugin.pkVars.isRemovePotionsEffectsOnParkour()) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (plugin.pkVars.isFullHunger()) {
									p.setFoodLevel(20);
								}

							} else {
								p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultError + "You are not in this parkour");

							}
						} 
						/* Player restarts course */
						else {

							if (Checkpoint == 1) {
								
								p.setGameMode(GameMode.ADVENTURE);
								if (plugin.pkVars.isCheckpointEffect()) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (plugin.pkVars.isRemovePotionsEffectsOnParkour()) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (plugin.pkVars.isFullHunger()) {
									p.setFoodLevel(20);
								}
								plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(plugin, p, Map, true));
								p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You have restarted your time for " + plugin.pkStrings.highlightTwo + plugin.getMapName(Map));
								plugin.pkFuncs.setPlTime(p.getName(), System.currentTimeMillis());
								plugin.pkFuncs.setPlCheckpoint(p.getName(), 1);

							} 
							/* Player completes course */
							else if ((Checkpoint == TotalCheckpoints) && (PlCheckpoint == (Checkpoint - 1))) {
								if (plugin.pkVars.isCheckpointEffect()) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								// Unlock next course
								//if(!Parkour.permission.has(p, "parkour.completed.map"+ Map)){
								if(!unlockFuncs.getPlayerUnlocks(p).getUnlocks().contains(String.valueOf(mapID))) {
									//Parkour.permission.playerAdd(p, "parkour.completed.map"+ Map);
									unlockFuncs.addUnlockedLevel(p, String.valueOf(mapID));

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


								if (!plugin.pkVars.getRecords().containsKey(Map + ":" + p.getName())) {

									plugin.getServer().getPluginManager().callEvent(new ParkourFinishEvent(plugin, p, Map, totalTime, true));
									p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You finished " + plugin.pkStrings.highlightTwo + plugin.getMapName(Map) + plugin.pkStrings.defaultColor + " for the first time in " +
											plugin.pkStrings.highlightOne + plugin.convertTime(totalTime));
									plugin.pkVars.getRecords().put(Map + ":" + p.getName(), totalTime);
									plugin.pkFuncs.saveScore();

									Map<String, Long> records = plugin.getRecords(Map);
									Map.Entry<String, Long> entry = records.entrySet().iterator().next();
									Long topTime = entry.getValue();
									String topName = entry.getKey();
									if(!topName.equalsIgnoreCase(p.getName())){
										p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "Global record: "+ plugin.pkStrings.highlightOne + topName + ChatColor.GRAY + " | " + plugin.pkStrings.highlightOne + plugin.convertTime(topTime));
									}
									//if user time > first record
									if (totalTime <= topTime){
										if (plugin.pkVars.isBroadcastMessage()) {
											plugin.getServer().broadcastMessage(plugin.getPrefix()+
													ChatColor.translateAlternateColorCodes('&', plugin.pkVars.getBroadcastMsg())
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
									if (plugin.pkVars.getRecords().get(Map + ":" + p.getName()) >= totalTime) {
										p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You beat your old time of " + plugin.pkStrings.highlightOne + plugin.convertTime(plugin.pkVars.getRecords().get(Map + ":" + p.getName())));
										p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You finished " + plugin.pkStrings.highlightTwo + plugin.getMapName(Map) + plugin.pkStrings.defaultColor + " in " + plugin.pkStrings.highlightOne + plugin.convertTime(totalTime));

										plugin.pkVars.getRecords().put(Map + ":" + p.getName(), totalTime);
										plugin.pkFuncs.saveScore();
										if(!topName.equalsIgnoreCase(p.getName())){
											p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "Global record: "+ plugin.pkStrings.highlightOne + topName + ChatColor.GRAY + " | " + plugin.pkStrings.highlightOne + plugin.convertTime(topTime));
										}

										// Player beat best global time
										if (totalTime <= topTime){
											if (plugin.pkVars.isBroadcastMessage()) {
												plugin.getServer().broadcastMessage(plugin.getPrefix()+
														ChatColor.translateAlternateColorCodes('&', plugin.pkVars.getBroadcastMsg())
														.replaceAll("PLAYER", p.getName())
														.replaceAll("TIME", plugin.convertTime(totalTime))
														.replaceAll("MAPNAME", plugin.getMapName(Map)));
											}
										}

										plugin.pkFuncs.giveReward(p);

									} else {
										String username;
										p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultError + "You didn't beat your old time " + plugin.pkStrings.highlightOne + plugin.convertTime(plugin.pkVars.getRecords().get(Map + ":" + p.getName())));
										p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "You finished " + plugin.pkStrings.highlightTwo + plugin.getMapName(Map) + plugin.pkStrings.defaultColor + " in " + plugin.pkStrings.highlightOne + plugin.convertTime(totalTime));

										if(topName.equalsIgnoreCase(p.getName())){
											username = "You";
										}
										else{
											username = topName;
										}
										p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "Global record: " + plugin.pkStrings.highlightOne + username + ChatColor.GRAY + " | " + plugin.pkStrings.highlightOne + plugin.convertTime(topTime));
										if (!plugin.pkVars.isRewardIfBetterScore()) {
											plugin.pkFuncs.giveReward(p);
										}
									}

									plugin.getServer().getPluginManager().callEvent(new ParkourFinishEvent(plugin, p, Map, totalTime, false));

								}

								// Adds delay before lobby TP
								final String pl = p.getName();
								if (plugin.pkVars.getLobby() != null) {
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
										@Override
										public void run() {
											plugin.getServer().getPlayer(pl).teleport(plugin.pkVars.getLobby());
											plugin.getServer().getPlayer(pl).setGameMode(plugin.pkVars.loadedUsers.get(pl).getPrevGM());
										}
									}, 5L);
								}
							} else if (PlCheckpoint == (Checkpoint - 1)) {

								long totalTime = System.currentTimeMillis()                                        
										- plugin.pkFuncs.getPlTime(plugin.pkVars.ParkourContainer.get(p.getName()));

								if (plugin.pkVars.isCheckpointEffect()) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								plugin.pkFuncs.setPlCheckpoint(p.getName(), Checkpoint);
								p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultColor + "Checkpoint " + (Checkpoint - 1) + "/" + (TotalCheckpoints - 2) + ChatColor.GRAY + " | " + plugin.pkStrings.highlightOne + plugin.convertTime(totalTime));

								plugin.getServer().getPluginManager().callEvent(
										new ParkourCheckpointEvent(plugin, p, Map, (Checkpoint-1), totalTime));

							} else if (Checkpoint <= PlCheckpoint) {
								p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultError + "You already reached this checkpoint!");

							} else if (Checkpoint > PlCheckpoint) {
								p.sendMessage(plugin.getPrefix() + plugin.pkStrings.defaultError + "You forgot to pass the last checkpoint!");

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
		PlayerInfo pInfo = plugin.pkVars.loadedUsers.get(p.getName());
		if(plugin.pkFuncs.isPlayerInParkour(p) && !pInfo.isPrevGMSet()) {
			plugin.pkFuncs.sendError("gmChange", p, plugin);
			e.setCancelled(true);
		}

	}
}
