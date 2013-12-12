package com.citibuild.cbparkour.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.citibuild.cbparkour.Parkour;

public class ParkourFinishEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Player player;
	private final int mapID;
	private final long time;
	private final boolean firstTime;
	
	public ParkourFinishEvent(Parkour plugin, Player player, int mapID, long time, boolean firstTime) {
		this.player = player;
		this.mapID = mapID;
		this.time = time;
		this.firstTime = firstTime;
		
		plugin.pkVars.loadedUsers.get(player.getName()).setMapID(0);
		plugin.pkVars.loadedUsers.get(player.getName()).setTime(time);
		plugin.pkVars.loadedUsers.get(player.getName()).setCheckpoint(0);
		plugin.pkFuncs.savePlayerInfo(player);
		
		plugin.pkUnlockFuncs.addUnlockedLevel(player, String.valueOf(mapID));
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getMapNumber() {
		return mapID;
	}
	
	public long getTime() {
		return time;
	}
	
	public boolean isFirstTime() {
		return firstTime;
	}
	
    @Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}