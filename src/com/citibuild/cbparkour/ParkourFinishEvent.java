package com.citibuild.cbparkour;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourFinishEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Player player;
	private final int MapNumber;
	private final long time;
	private final boolean firstTime;
	
	public ParkourFinishEvent(Player player, int MapNumber, long time, boolean firstTime) {
		this.player = player;
		this.MapNumber = MapNumber;
		this.time = time;
		this.firstTime = firstTime;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getMapNumber() {
		return MapNumber;
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