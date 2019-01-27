package org.jmt.factorize.multiblock.mbs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jmt.factorize.dsu.DeepStorageUnit;
import org.jmt.factorize.multiblock.IMultiblock;
import org.jmt.factorize.multiblock.IMultiblockInstance;
import org.jmt.factorize.multiblock.PBlock;
import org.jmt.factorize.multiblock.PBlockTyped;

/**
 * 
 * TODO: DOCUMENT
 * 
 * @author jediminer543
 *
 */
public class MultiBlockDSU implements IMultiblock {
	
	int nextDsuID = 0;
	
	class MultiBlockInstDSU implements IMultiblockInstance {

		int dsuId = 0;
		
		public MultiBlockInstDSU() {
			this.dsuId = nextDsuID++;
		}

		@Override
		public void handleInteract(PlayerInteractEvent pie) {
			if (pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
				pie.setCancelled(true);
				DeepStorageUnit.open(pie.getPlayer(), dsuId);
			} 
		}
		
	}
	
	public static final List<PBlock> pattern;
	static {
		ArrayList<PBlock> temp = new ArrayList<>();
		temp.add(new PBlockTyped(0, 0, 0, Material.END_STONE));
		temp.add(new PBlockTyped(1, 0, 1, Material.END_STONE));
		temp.add(new PBlockTyped(1, 0,-1, Material.END_STONE));
		temp.add(new PBlockTyped(2, 0, 0, Material.END_STONE));
		temp.add(new PBlockTyped(1, 1, 0, Material.END_STONE));
		temp.add(new PBlockTyped(1,-1, 0, Material.END_STONE));
		temp.add(new PBlockTyped(1, 0, 0, Material.ENDER_CHEST));
		temp.add(new PBlockTyped(0, 1, 1, Material.OBSIDIAN));
		temp.add(new PBlockTyped(0,-1, 1, Material.OBSIDIAN));
		temp.add(new PBlockTyped(0, 1,-1, Material.OBSIDIAN));
		temp.add(new PBlockTyped(0,-1,-1, Material.OBSIDIAN));
		temp.add(new PBlockTyped(2, 1, 1, Material.OBSIDIAN));
		temp.add(new PBlockTyped(2,-1, 1, Material.OBSIDIAN));
		temp.add(new PBlockTyped(2, 1,-1, Material.OBSIDIAN));
		temp.add(new PBlockTyped(2,-1,-1, Material.OBSIDIAN));
		temp.add(new PBlockTyped(0, 1, 0, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(0,-1, 0, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(0, 0, 1, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(0, 0,-1, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(1, 1, 1, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(1,-1, 1, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(1, 1,-1, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(1,-1,-1, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(2, 1, 0, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(2,-1, 0, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(2, 0, 1, Material.LAPIS_BLOCK));
		temp.add(new PBlockTyped(2, 0,-1, Material.LAPIS_BLOCK));
		pattern = Collections.unmodifiableList(temp);
	}
	
	
	
	@Override
	public List<PBlock> getPattern() {
		return pattern;
	}

	@Override
	public IMultiblockInstance setupNew(Location coreLoc, Player _) {
		return new MultiBlockInstDSU();
	}

}
