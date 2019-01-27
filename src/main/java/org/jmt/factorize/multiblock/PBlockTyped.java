package org.jmt.factorize.multiblock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jmt.factorize.Factorize;

public class PBlockTyped extends PBlock {

	Material type;
	
	public PBlockTyped(int x, int y, int z, Material type) {
		super(x, y, z);
		this.type = type;
	};
	
	/**
	 * Old style; isntead use {@link #PBlockTyped(int, int, int, Material)}
	 * @param type
	 * @param x
	 * @param y
	 * @param z
	 */
	@Deprecated
	public PBlockTyped(Material type, int x, int y, int z) {
		super(x, y, z);
		this.type = type;
	};
	
	@Override
	public boolean isMatch(Block b) {
		Factorize.instance.getLogger().info(String.format("Real: %s, This: %s, Equality: %s", b.getType().toString(), this.toString(), b.getType() == type));
		return b.getType() == type;
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d,%d) of %s", x, y, z, type.toString());
	}

}
