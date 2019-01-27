package org.jmt.factorize.multiblock;

import org.bukkit.block.Block;

/**
 * A fake block like construct that allows for defining
 * patterns 
 * 
 * @author jediminer543
 *
 */
public abstract class PBlock {
	int x, y, z;
	
	public PBlock(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getX() { return x; }
	
	public int getY() { return y; }
	
	public int getZ() { return z; }
	
	public abstract boolean isMatch(Block b);
	
	@Override
	public String toString() {
		return String.format("(%d,%d,%d)", x, y, z);
	}
	
}
