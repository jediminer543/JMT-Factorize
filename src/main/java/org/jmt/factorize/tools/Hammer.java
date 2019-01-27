package org.jmt.factorize.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jmt.factorize.Factorize;

/**
 * Breaks in a 3x3 around target block
 * 
 * See: Tinkers Construct
 * 
 * @author jediminer543
 *
 */
public class Hammer {

	private static final int HAMMER_BASE_DUR_MULT = 6;
	
	public static ItemStack getHammer(Material mat) {
		ItemStack is = new ItemStack(mat, 1);
		ItemMeta meta = is.getItemMeta();
		String name = mat.name().split("_")[0].toLowerCase();
		meta.setDisplayName(name.substring(0, 1).toUpperCase() + name.substring(1) + " Hammer");
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		meta.setLore(Arrays.asList(("HAMMATIME\nBreaks in a 3x3 pattern\nDurability:\n" + HAMMER_BASE_DUR_MULT*is.getType().getMaxDurability()).split("\n")));
		is.setItemMeta(meta);
		return is;
	}
	
	private static Plugin p;
	
	static class HammerListener implements Listener {
		Random r = new Random();
		
		@EventHandler
		void onPlayerInteract(PlayerInteractEvent pie) {
			if (pie.getAction() == Action.LEFT_CLICK_BLOCK) {
				pie.getPlayer().setMetadata("factorize_hammer_break_dir",
						new FixedMetadataValue(p, pie.getBlockFace()));
			}
		}

		@EventHandler
		void onBlockBreak(BlockBreakEvent bbe) {
			ItemStack hammer = bbe.getPlayer().getInventory().getItemInMainHand();
			if (hammer != null && hammer.getItemMeta() != null &&
					hammer.getItemMeta().getLore() != null && hammer.getItemMeta().getLore().size() > 1
					&& hammer.getItemMeta().getLore().get(0) != null
					&& hammer.getItemMeta().getLore().get(0).equals("HAMMATIME")) {
				Block b = bbe.getBlock();
				BlockFace hitFace = null;
				try {
					hitFace = (BlockFace) bbe.getPlayer().getMetadata("factorize_hammer_break_dir").get(0).value();
				} catch (Exception e) {
					System.err.println("Failed to get breakdir");
					e.printStackTrace();
					return;
				}
				if (hitFace == null) {
					return;
				}
				int xdev = (1-Math.abs(hitFace.getModX()));
				int ydev = (1-Math.abs(hitFace.getModY()));
				int zdev = (1-Math.abs(hitFace.getModZ()));
				for (int x = -xdev; x <= xdev; x++) {
					for (int y = -ydev; y <= ydev; y++) {
						for (int z = -zdev; z <= zdev; z++) {
							boolean broke = false;
							if (bbe.getPlayer().getGameMode() == GameMode.CREATIVE) {
								//Ensure no drops
								b.getWorld().getBlockAt(b.getX() + x, b.getY() + y, b.getZ() + z)
									.setType(Material.AIR);
							} else {
								broke = b.getWorld().getBlockAt(b.getX() + x, b.getY() + y, b.getZ() + z)
										.breakNaturally(hammer);
							}
							if (broke) {
								if (hammer.getItemMeta().hasEnchant(Enchantment.DURABILITY)) {
									if (r.nextFloat() <= 0.15*hammer.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)) {
										continue;
									}
								}
								int durability = Integer.parseInt(hammer.getItemMeta().getLore().get(3))-1;
								ItemMeta im = hammer.getItemMeta();
								List<String> out = new ArrayList<>(im.getLore());
								out.set(3, ""+durability);
								im.setLore(out);
								hammer.setItemMeta(im);
								if (durability <= 0) {
									hammer.setAmount(0);
									return;
								}
							}
						}
					}
				}
			}
		}
	};
	
	static final HammerListener listen = new HammerListener();
	
//	public static List<Recipe> modifiedRecipies = new ArrayList<>();
//	
//	public static void setupOldRecipes() {
//		
//	}
//	
//	public static void resetOldRecipes() {
//		
//	}
	
	public static List<Recipe> ourRecipies = new ArrayList<>();

	public static void setupOurRecipes() {
		//Recipe setup
		//Diamond
		ItemStack dh = getHammer(Material.DIAMOND_PICKAXE);
		NamespacedKey dnk = new NamespacedKey(p, "diamond_hammer");
		ShapedRecipe drp = new ShapedRecipe(dnk, dh);
		drp.shape("###", " * ", " * ");
		drp.setIngredient('#', Material.DIAMOND_BLOCK);
		drp.setIngredient('*', Material.STICK);
		//Gold
		ItemStack gh = getHammer(Material.GOLDEN_PICKAXE);
		NamespacedKey gnk = new NamespacedKey(p, "hold_hammer");
		ShapedRecipe grp = new ShapedRecipe(gnk, gh);
		grp.shape("###", " * ", " * ");
		grp.setIngredient('#', Material.GOLD_BLOCK);
		grp.setIngredient('*', Material.STICK);
		//Iron
		ItemStack ih = getHammer(Material.IRON_PICKAXE);
		NamespacedKey ink = new NamespacedKey(p, "iron_hammer");
		ShapedRecipe irp = new ShapedRecipe(ink, ih);
		irp.shape("###", " * ", " * ");
		irp.setIngredient('#', Material.IRON_BLOCK);
		irp.setIngredient('*', Material.STICK);
		//Add recipies
		//add them to lookup so we can purge them later
		ourRecipies.add(drp);
		ourRecipies.add(grp);
		ourRecipies.add(irp);
		//Add them to server
		p.getServer().addRecipe(drp);
		p.getServer().addRecipe(grp);
		p.getServer().addRecipe(irp);
	}
	
	public static void cleanupOurRecipies() {
		Iterator<Recipe> it = p.getServer().recipeIterator();
		while (it.hasNext()) {
			Recipe r = it.next();
			if (ourRecipies.contains(r)) {
				it.remove();
			}
		}
	}
	
	public static void enable(Factorize p) {
		Hammer.p = p;
		Bukkit.getServer().getPluginManager().registerEvents(listen, p);
		setupOurRecipes();
	}
	
	public static void disable(Factorize p) {
		cleanupOurRecipies();
	}
}
