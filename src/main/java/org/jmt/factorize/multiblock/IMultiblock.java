package org.jmt.factorize.multiblock;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * 
 * TODO: DOCUMENT
 * 
 * @author jediminer543
 *
 */
public interface IMultiblock {

	/**
	 * Checks if a block is a compatible as a core for this multiblock
	 * @param b
	 * @return
	 */
	public default boolean isValidCoreBlock(Block b) {
		return getPattern().stream().filter(i -> i.x == 0 && i.y == 0 && i.z == 0).anyMatch(i -> i.isMatch(b));
	}
	
	/**
	 * Get the multiblock pattern; Should be aligned around (0,0,0), 
	 * which should be the ROOT part (i.e. sign, etc.) 
	 * 
	 * @return List of blocks that make up the structure
	 */
	public List<PBlock> getPattern();
	
	/**
	 * 
	 */
	public IMultiblockInstance setupNew(Location coreLoc, Player p);
}
