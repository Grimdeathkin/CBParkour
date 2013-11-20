package me.isklar.cbparkour;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourStartEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Player player;
	private final int MapNumber;
	private final boolean restart;
	
	public ParkourStartEvent(Player player, int MapNumber, boolean restart) {
		this.player = player;
		this.MapNumber = MapNumber;
		this.restart = restart;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getMapNumber() {
		return MapNumber;
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