package com.citibuild.cbparkour.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.citibuild.cbparkour.Parkour;

public class ParkourStartEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Player player;
	private final int mapID;
	private final boolean restart;
	
	public ParkourStartEvent(Parkour plugin, Player player, int mapID, boolean restart) {
		this.player = player;
		this.mapID = mapID;
		this.restart = restart;
		
		plugin.pkVars.loadedUsers.get(player.getName()).setMapID(mapID);
		plugin.pkVars.loadedUsers.get(player.getName()).setTime(0L);
		plugin.pkVars.loadedUsers.get(player.getName()).setCheckpoint(1);
		plugin.pkFuncs.savePlayerInfo(player);
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getMapNumber() {
		return mapID;
	}
	
	public boolean isRestarting() {
		return restart;
	}
	
    @Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}