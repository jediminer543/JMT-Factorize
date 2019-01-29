package org.jmt.factorize.multiblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jmt.factorize.Factorize;
import org.jmt.factorize.multiblock.mbs.MultiBlockDSU;

/**
 * 
 * TODO: DOCUMENT
 * 
 * @author jediminer543
 *
 */
public class MultiblockController {

	static List<IMultiblock> multiblocks = new ArrayList<>();
	static Map<Location, List<IMultiblockInstance>> trackedByLoc = new HashMap<>();
	static Map<IMultiblockInstance, List<Location>> trackedByInst = new HashMap<>();

	public static void registerMultiBlock(IMultiblock im) {
		multiblocks.add(im);
	}

	public static List<Location> getValidMultiInstance(IMultiblock imb, Location coreLoc) {
		List<Location> out;
		if (imb.getPattern() == null) {
			Factorize.instance.getLogger().warning("Invalid Multiblock Array From: " + imb.getClass().getName());
			return null;
		}
		for (int i = 0; i < 4; i++) {
			out = getValidMultiInstanceRot(imb, coreLoc, i);
			if (out != null) {
				return out;
			}
		}
		return null;
	}

	// Pre Defined Rotation matrices
	static final int[] rot_0_mat = new int[] { 1, 0, 0, 0, 1, 0, 0, 0, 1 };
	static final int[] rot_1_mat = new int[] { 0, 0, 1, 0, 1, 0, -1, 0, 0 };
	static final int[] rot_2_mat = new int[] { -1, 0, 0, 0, 1, 0, 0, 0, -1 };
	static final int[] rot_3_mat = new int[] { 0, 0, -1, 0, 1, 0, 1, 0, 0 };
	static final int[][] rot_mats = new int[][] { rot_0_mat, rot_1_mat, rot_2_mat, rot_3_mat };

	/**
	 * Hacky impl ATM; only supports rotations around the Y axis (Think like a
	 * furnace)
	 * 
	 * TODO: Make Rotation less shite
	 * 
	 * @param imb
	 * @param coreLoc
	 * @param rotation
	 * @return
	 */
	private static List<Location> getValidMultiInstanceRot(IMultiblock imb, Location coreLoc, int rotation) {
		List<Location> out = new ArrayList<>();
		int[] rotmat = rot_mats[rotation];
		Factorize.instance.getLogger().info(imb.getPattern().toString());
		for (PBlock pb : imb.getPattern()) {
			int x = coreLoc.getBlockX() + (pb.getX() * rotmat[0] + pb.getZ() * rotmat[2]);
			int z = coreLoc.getBlockZ() + (pb.getX() * rotmat[(2 * 3) + 0] + pb.getZ() * rotmat[(2 * 3) + 2]);
			int y = coreLoc.getBlockY() + pb.getY();
			if (!pb.isMatch(coreLoc.getWorld().getBlockAt(x, y, z))) {
				Factorize.instance.getLogger()
						.log(Level.FINEST,
								String.format(
										"Multiblock missmatch at (%d,%d,%d); "
												+ "In world (%d,%d,%d); Expected %s; Actual %s",
										pb.getX(), pb.getY(), pb.getZ(), x, y, z,
										coreLoc.getWorld().getBlockAt(x, y, z).getType().toString(),
										((PBlockTyped) pb).type.toString()));
				return null;
			} else {
				out.add(new Location(coreLoc.getWorld(), x, y, z));
			}
		}
		return out;
	}

	public static boolean tryCreateMultiInstance(IMultiblock imb, Location coreLoc, Player initer) {
		List<Location> locs = getValidMultiInstance(imb, coreLoc);
		if (locs != null) {
			IMultiblockInstance inst = imb.setupNew(coreLoc, initer);
			locs.forEach(i -> {
				if (!trackedByLoc.containsKey(i))
					trackedByLoc.put(i, new ArrayList<>());
				trackedByLoc.get(i).add(inst);
			});
			trackedByInst.put(inst, locs);
			save(coreLoc, inst);
			return true;
		}
		return false;
	}

	public static void removeMultiblocksAt(Location toRemove) {
		List<IMultiblockInstance> tokill = trackedByLoc.get(toRemove);
		List<Location> tgts = tokill.stream().map(i -> trackedByInst.get(i)).flatMap(List::stream).distinct()
				.collect(Collectors.toList());
		tgts.forEach(trackedByLoc::remove);
		tokill.forEach(trackedByInst::remove);
		tokill.forEach(i -> delete(toRemove, i));
	}

	public static void tryCreateAnyMultiInstance(Location coreLoc, Player p) {
		List<IMultiblock> imbs = multiblocks.stream().filter(i -> i.isValidCoreBlock(coreLoc.getBlock()))
				.collect(Collectors.toList());
		if (!imbs.isEmpty()) {
			if (imbs.stream().map(
					i -> tryCreateMultiInstance(i, coreLoc, p))
					.anyMatch(i -> i)) {
				p.sendMessage("Multiblock Created");
			}
		}
	}
	
	static final Listener listen = new Listener() {
		@EventHandler
		void onPlayerInteract(PlayerInteractEvent pie) {
			if (pie.getClickedBlock() != null) {
				if (trackedByLoc.containsKey(pie.getClickedBlock().getLocation())) {
					trackedByLoc.get(pie.getClickedBlock().getLocation()).forEach(i -> i.handleInteract(pie));
				}
				if (pie.getAction() == Action.RIGHT_CLICK_BLOCK
						&& !trackedByLoc.containsKey(pie.getClickedBlock().getLocation())) {
					tryCreateAnyMultiInstance(pie.getClickedBlock().getLocation(), pie.getPlayer());
				}
			}
		}

		@EventHandler
		void onBlockBreak(BlockBreakEvent bbe) {
			if (trackedByLoc.containsKey(bbe.getBlock().getLocation())) {
				removeMultiblocksAt(bbe.getBlock().getLocation());
				bbe.getPlayer().sendMessage("Removed Multiblock");
			}
		}

		@EventHandler
		void onBlockPlace(BlockPlaceEvent bpe) {
			if (!bpe.isCancelled() && bpe.canBuild()) {
				tryCreateAnyMultiInstance(bpe.getBlock().getLocation(), bpe.getPlayer());
			}
		}
	};
	
	public static void loadAll() {
		Map<Location, Map<UUID, Class<? extends IMultiblock>>> savedMbs = Factorize.instance.mbsc.getAllMultiBlocks();
		for (Entry<Location, Map<UUID, Class<? extends IMultiblock>>> pos : savedMbs.entrySet()) {
			for (Entry<UUID, Class<? extends IMultiblock>> mbs : pos.getValue().entrySet()) {
				boolean done = false;
				for (IMultiblock imb : multiblocks) {
					if (mbs.getValue().isInstance(imb)) {
						List<Location> locs = getValidMultiInstance(imb, pos.getKey());
						if (locs != null) {
							IMultiblockInstance inst = imb.setupNew(pos.getKey(), mbs.getKey(), 
									Factorize.instance.mbsc.getMultiBlockData(pos.getKey(), mbs.getKey()));
							locs.forEach(i -> {
								if (!trackedByLoc.containsKey(i))
									trackedByLoc.put(i, new ArrayList<>());
								trackedByLoc.get(i).add(inst);
							});
							trackedByInst.put(inst, locs);						} else {
							Factorize.instance.getLogger().warning("Failed to load multiblock " + mbs.getKey());
						}
						done = true;
						break;
					}
				}
				if (!done) {
					Factorize.instance.getLogger().warning("Failed to load multiblock " + mbs.getKey() 
						+ " as multiblock type " + mbs.getValue() + " wasn't found");
				}
			}
		}
	}
	
	public static void save(Location coreLoc, IMultiblockInstance mbs) {
		Factorize.instance.mbsc.setMultiBlock(coreLoc, mbs);
	}
	
	public static void delete(Location coreLoc, IMultiblockInstance mbs) {
		Factorize.instance.mbsc.unsetMultiBlock(coreLoc, mbs);
	}


	public static void enable() {
		registerMultiBlock(new MultiBlockDSU());
		Bukkit.getServer().getPluginManager().registerEvents(listen, Factorize.instance);
		loadAll();
	}
	
	public static void disable() {
	}

}
