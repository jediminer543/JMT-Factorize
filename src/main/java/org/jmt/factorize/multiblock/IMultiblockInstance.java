package org.jmt.factorize.multiblock;

import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * TODO
 * 
 * @author jediminer543
 *
 */
public interface IMultiblockInstance {
	
	public UUID getID();
	
	/**
	 * Passes along any 
	 */
	public void handleInteract(PlayerInteractEvent pie);
	
	//TODO ADD seralisation
	
	/**
	 * 
	 * @param savPoint
	 */
	public void save(ConfigurationSection savPoint);
	
	public Class<? extends IMultiblock> getType();
}
