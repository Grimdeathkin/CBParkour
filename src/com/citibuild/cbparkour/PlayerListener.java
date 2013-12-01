package com.citibuild.cbparkour;

import java.util.Map;

import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerListener implements Listener{

	private final Parkour plugin;
	public PlayerListener(Parkour plugin) {
		this.plugin = plugin;
	}
	
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		Bukkit.broadcastMessage("EVENT");
		if (plugin.isPlayerInParkour(e.getPlayer())) {
			plugin.ParkourContainer.remove(e.getPlayer().getName());
		}
		if (plugin.rewardPlayersCooldown.containsKey(e.getPlayer().getName())) {
			plugin.rewardPlayersCooldown.remove(e.getPlayer().getName());
		}
		if (e.getPlayer().getName().equals(plugin.newMapPlayerEditor)) {
			plugin.newMapPlayerEditor = null;
			plugin.newMapName = null;
			plugin.newMapPrevious = 0;
			plugin.newMapNext = 0;
			plugin.newMapCheckpoints.clear();
			plugin.CheckpointNumber = 0;
			plugin.NewMapNumber = 0;
			plugin.newMap = false;
		}
	}
	
	
	@EventHandler
	public void onPlayerDmg(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (plugin.isPlayerInParkour(p) && plugin.InvincibleWhileParkour) {
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
						int mapNumber = plugin.getMapNumber(ChatColor.stripColor(s.getLine(2)));
						if (mapNumber != 0) {
							if (plugin.maps.contains(mapNumber)) {
								Player p = e.getPlayer();
								if (!Parkour.permission.has(p, "parkour.use")) {
									Parkour.sendError("noParkourPermission", p, plugin);
									return;
								}

								if (!plugin.toggleParkour.get(mapNumber)) {
									Parkour.sendError("parkourDisabled", p, plugin);
									return;
								}
								
								if(plugin.getMapPrevious(mapNumber) != 0){
									if(!Parkour.permission.has(p, "parkour.completed.map"+plugin.getMapPrevious(mapNumber))){
										Parkour.sendInfo("notUnlocked", p, mapNumber, plugin);
										return;
									}
								}
								
	
								if (plugin.isPlayerInParkour(p)) {
									plugin.ParkourContainer.remove(p.getName());
								}
	
								FileConfiguration cfg = plugin.getConfig();
	
								if (cfg.contains("Parkour.map" + mapNumber + ".spawn")) {
									Location loc = new Location(plugin.getServer().getWorld(
											plugin.getConfig().getString("Parkour.map" + mapNumber + ".world")),
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
									p.sendMessage(plugin.PREFIX+ Parkour.AQUA+"Welcome to "+ Parkour.GREEN +plugin.getMapName(mapNumber));
								} else {
									p.sendMessage(plugin.PREFIX + Parkour.RED + "Map spawn is not set");
								}
							} else {
								e.getPlayer().sendMessage(Parkour.RED + "This map no longer exists");
							}
						}
					}	
					
					if (s.getLine(1).equalsIgnoreCase("leave")) {
						if (plugin.isPlayerInParkour(e.getPlayer())) {
							e.getPlayer().sendMessage(Parkour.AQUA + "You have left the parkour");
							plugin.ParkourContainer.remove(e.getPlayer().getName());
	
						}
						if (plugin.lobby != null) {
							e.getPlayer().teleport(plugin.lobby);
							e.getPlayer().setGameMode(GameMode.SURVIVAL);
						}
					}
					
					if (s.getLine(1).equalsIgnoreCase("Best Times")) {
						int mapNumber = plugin.getMapNumber(ChatColor.stripColor(s.getLine(2)));
						if (mapNumber != 0) {
							if (plugin.maps.contains(mapNumber)) {
								plugin.displayHighscores(mapNumber, e.getPlayer());
							} else {
								e.getPlayer().sendMessage(Parkour.RED + "This map no longer exists");
							}
						}
					}
                    e.setCancelled(true);
				}
			}
		}
		/* Map Creation */
		if (plugin.newMap)
		{
			if (e.getPlayer().getName().equals(plugin.newMapPlayerEditor) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Player p = e.getPlayer();
				ItemStack stick = new ItemStack(Material.STICK, 1);
				if (p.getItemInHand().getTypeId() == 280 && e.getClickedBlock().getTypeId() == 70) {
					if (!plugin.cLoc.containsKey(e.getClickedBlock().getLocation())) {
						Location bLoc = e.getClickedBlock().getLocation();

						if (plugin.newMapCheckpoints.contains(bLoc)) {
							p.sendMessage(plugin.APREFIX + Parkour.RED + "This checkpoint is alredy used for this map");
						} else {
							FileConfiguration cfg = plugin.getConfig();

							p.sendMessage(plugin.APREFIX + Parkour.AQUA + "Checkpoint " + Parkour.GREEN + plugin.CheckpointNumber + Parkour.AQUA + " set on new map " +Parkour.GREEN+ plugin.NewMapNumber);

							cfg.set("Parkour.map" + plugin.NewMapNumber + ".cp." + plugin.CheckpointNumber + ".posX", bLoc.getX());
							cfg.set("Parkour.map" + plugin.NewMapNumber + ".cp." + plugin.CheckpointNumber + ".posY", bLoc.getY());
							cfg.set("Parkour.map" + plugin.NewMapNumber + ".cp." + plugin.CheckpointNumber + ".posZ", bLoc.getZ());

							plugin.saveConfig();
							plugin.newMapCheckpoints.add(bLoc);
							plugin.CheckpointNumber++;

						}
					} else {
						p.sendMessage(plugin.APREFIX + Parkour.RED + "This checkpoint is alredy used for another map");
					}
				} else {
					p.sendMessage(plugin.APREFIX + Parkour.RED + "Use a stick to place checkpoints (Right click on stone pressure plate)");
					p.getInventory().addItem(stick);
				}
			}
		}
		// Teleport item here
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

				if (plugin.cLoc.containsKey(bLoc)) {

					int Checkpoint = plugin.getCheckpoint(plugin.cLoc.get(bLoc));
					int mapNumber = plugin.getCpMapNumber(plugin.cLoc.get(bLoc));
					
					if (!Parkour.permission.has(p, "parkour.use")) {
						Parkour.sendError("noParkourPermission", p, plugin);
						if (plugin.lobby != null) {
							p.teleport(plugin.lobby);
							p.setGameMode(GameMode.SURVIVAL);
							p.sendMessage(plugin.PREFIX + Parkour.AQUA + "You have been returned to the lobby");
						}
						return;
					}

					if (!plugin.toggleParkour.get(mapNumber)) {
						Parkour.sendError("parkourDisabled", p, plugin);
						if (plugin.lobby != null) {
							p.teleport(plugin.lobby);
							p.setGameMode(GameMode.SURVIVAL);
							p.sendMessage(plugin.PREFIX + Parkour.AQUA + "You have been returned to the lobby");
						}
						return;
					}
					
					if(plugin.getMapPrevious(mapNumber) != 0){
						if(!Parkour.permission.has(p, "parkour.completed.map"+plugin.getMapPrevious(mapNumber))){
							Parkour.sendInfo("notUnlocked", p, mapNumber, plugin);
							if (plugin.lobby != null) {
								p.teleport(plugin.lobby);
								p.setGameMode(GameMode.SURVIVAL);
								p.sendMessage(plugin.PREFIX + Parkour.AQUA + "You have been returned to the lobby");
							}
							return;
						}
					}
					
					// Player starts course
					if (!plugin.isPlayerInParkour(p)) {

						if (Checkpoint == 1) {
							int Map = plugin.getCpMapNumber(plugin.cLoc.get(bLoc));
							p.setGameMode(GameMode.ADVENTURE);
							plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(p, Map, false));
							
							plugin.ParkourContainer.put(
									p.getName(),
									(plugin.getCpMapNumber(plugin.cLoc.get(bLoc)) + "_"
											+ System.currentTimeMillis() + "_1"));
							p.sendMessage(plugin.PREFIX +Parkour.AQUA + "You have started your timer for " + Parkour.GREEN + plugin.getMapName(Map));
							
							if (plugin.CheckpointEffect) {
								p.playEffect(bLoc, Effect.POTION_BREAK, 2);
							}
							if (plugin.removePotionsEffectsOnParkour) {
								for (PotionEffect effect : p.getActivePotionEffects()) {
									p.removePotionEffect(effect.getType());
								}
							}
							if (plugin.FullHunger) {
								p.setFoodLevel(20);
							}
						} else {
							p.sendMessage(plugin.PREFIX + Parkour.RED + "You must start at the checkpoint 1");
						}
					} 
					// Player is in a parkour and hits a checkpoint
					else {
						p.setGameMode(GameMode.ADVENTURE);
						int PlCheckpoint = plugin.getPlCheckpoint(plugin.ParkourContainer.get(p.getName()));
						int CpMap = plugin.getCpMapNumber(plugin.cLoc.get(bLoc));
						int Map = plugin.getPlMapNumber(plugin.ParkourContainer.get(p.getName()));
						int TotalCheckpoints = plugin.getCfgTotalCheckpoints(Map);
						// Start new course
						if (CpMap != Map) {
							if (Checkpoint == 1) {
								plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(p, Map, false));						
								p.sendMessage(plugin.PREFIX + Parkour.AQUA + "You have started your timer for " + Parkour.GREEN + plugin.getMapName(CpMap));
								plugin.ParkourContainer.put(
										p.getName(),
										(plugin.getCpMapNumber(plugin.cLoc.get(bLoc)) + "_"
												+ System.currentTimeMillis() + "_1"));
								
								if (plugin.CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (plugin.removePotionsEffectsOnParkour) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (plugin.FullHunger) {
									p.setFoodLevel(20);
								}

							} else {
								p.sendMessage(plugin.PREFIX + Parkour.RED + "You are not in this parkour");

							}
						} 
						/* Player restarts course */
						else {

							if (Checkpoint == 1) {
								p.setGameMode(GameMode.ADVENTURE);
								if (plugin.CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (plugin.removePotionsEffectsOnParkour) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (plugin.FullHunger) {
									p.setFoodLevel(20);
								}
								plugin.getServer().getPluginManager().callEvent(new ParkourStartEvent(p, Map, true));
								p.sendMessage(plugin.PREFIX + Parkour.AQUA + "You have restarted your time for " +Parkour.GREEN+ plugin.getMapName(Map));
								plugin.setPlTime(p.getName(), System.currentTimeMillis());
								plugin.setPlCheckpoint(p.getName(), 1);

							} 
							/* Player completes course */
							else if ((Checkpoint == TotalCheckpoints) && (PlCheckpoint == (Checkpoint - 1))) {
								if (plugin.CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								
								// Unlock next course
								if(!Parkour.permission.has(p, "parkour.completed.map"+ Map)){
									Parkour.permission.playerAdd(p, "parkour.completed.map"+ Map);
									
									FileConfiguration cfg = plugin.getConfig();
									if(cfg.getInt("Parkour.map"+Map+".mapNext") != 0){
										Parkour.sendInfo("mapUnlock", p, Map, plugin);
										final Player player = p;
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                            @Override
											public void run() {
                                            	plugin.playJingle(player);
											}
										}, 5L);
									}
								}
								
								long totalTime = System.currentTimeMillis()
										- plugin.getPlTime(plugin.ParkourContainer.get(p.getName()));
								plugin.ParkourContainer.remove(p.getName());
								
								
								if (!plugin.Records.containsKey(Map + ":" + p.getName())) {
								
									plugin.getServer().getPluginManager().callEvent(new ParkourFinishEvent(p, Map, totalTime, true));
									p.sendMessage(plugin.PREFIX + Parkour.AQUA + "You finished for the first time in " +
											Parkour.GRAY + plugin.convertTime(totalTime));
									plugin.Records.put(Map + ":" + p.getName(), totalTime);
									plugin.saveScore();
									
									Map<String, Long> records = plugin.getRecords(Map);
									Map.Entry<String, Long> entry = records.entrySet().iterator().next();
									Long topTime = entry.getValue();
									String topName = entry.getKey();
									if(!topName.equalsIgnoreCase(p.getName())){
									p.sendMessage(plugin.PREFIX + Parkour.AQUA + "Global record: "+Parkour.GRAY + topName + Parkour.AQUA + " | " + Parkour.GRAY + plugin.convertTime(topTime));
									}
									//if user time > first record
									if (totalTime <= topTime){
										if (plugin.BroadcastMessage) {
											plugin.getServer().broadcastMessage(plugin.PREFIX+
													ChatColor.translateAlternateColorCodes('&', plugin.BroadcastMsg)
															.replaceAll("PLAYER", p.getName())
															.replaceAll("TIME", plugin.convertTime(totalTime))
															.replaceAll("MAPNAME", plugin.getMapName(Map)));
										}
									}
									plugin.giveReward(p);

								} else {
									Map<String, Long> records = plugin.getRecords(Map);
									Map.Entry<String, Long> entry = records.entrySet().iterator().next();
									Long topTime = entry.getValue();
									String topName = entry.getKey();
									
									// Player beat old score
									if (plugin.Records.get(Map + ":" + p.getName()) >= totalTime) {
										p.sendMessage(plugin.PREFIX + Parkour.GREEN + "You beat your old time of " + Parkour.GRAY + plugin.convertTime(plugin.Records.get(Map + ":" + p.getName())));
										p.sendMessage(plugin.PREFIX + Parkour.AQUA + "You finished this parkour in " + Parkour.GRAY + plugin.convertTime(totalTime));
										
										plugin.Records.put(Map + ":" + p.getName(), totalTime);
										plugin.saveScore();
										if(!topName.equalsIgnoreCase(p.getName())){
										p.sendMessage(plugin.PREFIX + Parkour.AQUA + "Global record: "+ Parkour.GRAY + topName + Parkour.AQUA + " | " + Parkour.GRAY + plugin.convertTime(topTime));
										}
										
										// Player beat best global time
										if (totalTime <= topTime){
											if (plugin.BroadcastMessage) {
												plugin.getServer().broadcastMessage(plugin.PREFIX+
														ChatColor.translateAlternateColorCodes('&', plugin.BroadcastMsg)
																.replaceAll("PLAYER", p.getName())
																.replaceAll("TIME", plugin.convertTime(totalTime))
																.replaceAll("MAPNAME", plugin.getMapName(Map)));
											}
										}
												
										plugin.giveReward(p);

									} else {
										String username;
										p.sendMessage(plugin.PREFIX + Parkour.RED + "You didn't beat your old time "+ Parkour.GRAY + plugin.convertTime(plugin.Records.get(Map + ":" + p.getName())));
										p.sendMessage(plugin.PREFIX + Parkour.AQUA + "You finished this parkour in " +Parkour.GRAY+ plugin.convertTime(totalTime));

										if(topName.equalsIgnoreCase(p.getName())){
											username = "You";
										}
										else{
											username = topName;
										}
										p.sendMessage(plugin.PREFIX + Parkour.AQUA + "Global record: " +Parkour.GRAY+ username +Parkour.AQUA+ " | " +Parkour.GRAY+ plugin.convertTime(topTime));
										if (!plugin.rewardIfBetterScore) {
											plugin.giveReward(p);
										}
									}
									
									plugin.getServer().getPluginManager().callEvent(new ParkourFinishEvent(p, Map, totalTime, false));

								}

								// Adds delay before lobby TP
								final String pl = p.getName();
								if (plugin.lobby != null) {
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
										public void run() {
                                          plugin.getServer().getPlayer(pl).teleport(plugin.lobby);
										}
									}, 5L);
								}
							} else if (PlCheckpoint == (Checkpoint - 1)) {

								if (plugin.CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								plugin.setPlCheckpoint(p.getName(), Checkpoint);
								p.sendMessage(plugin.PREFIX + Parkour.AQUA + "Checkpoint " + (Checkpoint - 1) + "/" + (TotalCheckpoints - 2));
								plugin.getServer().getPluginManager().callEvent(
										new ParkourCheckpointEvent(p, Map, (Checkpoint-1), System.currentTimeMillis() 
												- plugin.getPlTime(plugin.ParkourContainer.get(p.getName()))));

							} else if (Checkpoint <= PlCheckpoint) {
								p.sendMessage(plugin.PREFIX + Parkour.RED + "You already reached this checkpoint!");

							} else if (Checkpoint > PlCheckpoint) {
								p.sendMessage(plugin.PREFIX + Parkour.RED + "You forgot to pass the last checkpoint!");

							}
						}
					}
				}
			}
			if (plugin.isPlayerInParkour(p)) {
				int Map = plugin.getPlMapNumber(plugin.ParkourContainer.get(p.getName()));
				if ((e.getTo().getBlock().getType() == Material.WATER || e.getTo().getBlock().getType() == Material.STATIONARY_WATER)) {
					if (plugin.getConfig().getBoolean("Parkour.map" + Map + ".waterrespawn"))
						plugin.teleportLastCheckpoint(e.getPlayer());
				}
				if ((e.getTo().getBlock().getType() == Material.LAVA || e.getTo().getBlock().getType() == Material.STATIONARY_LAVA)) {
					if (plugin.getConfig().getBoolean("Parkour.map" + Map + ".lavarespawn"))
						plugin.teleportLastCheckpoint(e.getPlayer());
				}
			}
		}
	}
}
