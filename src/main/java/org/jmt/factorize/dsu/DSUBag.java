package org.jmt.factorize.dsu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jmt.factorize.Factorize;

public class DSUBag {

	public static ItemStack makeDSUBag(int idx) {
		ItemStack is = new ItemStack(Material.FLOWER_POT, 1);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName("DSU Bukkit");
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		meta.setLore(Arrays.asList(("DSUBAG\nGives access to a DSU\nDSUID:\n" + idx).split("\n")));
		is.setItemMeta(meta);
		return is;
	}
	
	private static ItemStack makeDSUBagTemplate() {
		ItemStack is = new ItemStack(Material.FLOWER_POT, 1);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName("DSU Bukkit");
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		meta.setLore(Arrays.asList(("DSUBAGTMP\nGives access to a DSU\nDSUID:\n" + "NYA").split("\n")));
		is.setItemMeta(meta);
		return is;
	}
	
	public static boolean isDSUBag(ItemStack is) {
		if (is != null && is.hasItemMeta() && is.getItemMeta().hasLore()
				&& is.getItemMeta().getLore().size() == 4) {
			if (is.getItemMeta().getLore().get(0).equals("DSUBAG")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDSUBagTemplate(ItemStack is) {
		if (is != null && is.hasItemMeta() && is.getItemMeta().hasLore()
				&& is.getItemMeta().getLore().size() == 4) {
			if (is.getItemMeta().getLore().get(0).equals("DSUBAGTMP")) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean templateToBag(Player p, ItemStack is, int idx) {
		if (isDSUBagTemplate(is)) {
			is.setAmount(Math.max(0, is.getAmount()-1));
			p.getInventory().addItem(makeDSUBag(idx));
			return true;
		}
		return false;
	}


		
	static class BagListener implements Listener {
		@EventHandler
		void onPlayerInteract(PlayerInteractEvent pie) {
			if (pie.getAction() == Action.RIGHT_CLICK_AIR || pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
				ItemStack is = pie.getPlayer().getInventory().getItemInMainHand();
				if (is != null && is.hasItemMeta() && is.getItemMeta().hasLore()
						&& is.getItemMeta().getLore().size() == 4) {
					if (is.getItemMeta().getLore().get(0).equals("DSUBAG")) {
						pie.setCancelled(true);
						int idx = Integer.parseInt(is.getItemMeta().getLore().get(3));
						DeepStorageUnit.open(pie.getPlayer(), idx);
					} else if (is.getItemMeta().getLore().get(0).equals("DSUBAGTMP") && !pie.isCancelled()) {
						pie.setCancelled(true);
						pie.getPlayer().sendMessage("This bag has not been activated");
						pie.getPlayer().sendMessage("Use it on a DSU multiblock to bind it");
					}
				}  
			}
		}
	};
	
	static final BagListener listen = new BagListener();
	
	public static List<Recipe> ourRecipies = new ArrayList<>();

	private static Plugin p;
	
	public static void setupOurRecipes() {
		//Recipe setup
		//DSUBukkit
		ItemStack db = makeDSUBagTemplate();
		NamespacedKey dnk = new NamespacedKey(p, "dsu_bukkit_tmp");
		ShapedRecipe drp = new ShapedRecipe(dnk, db);
		drp.shape("-+-", "~#~", "-+-");
		drp.setIngredient('#', Material.ENDER_CHEST);
		drp.setIngredient('+', Material.END_STONE);
		drp.setIngredient('-', Material.OBSIDIAN);
		drp.setIngredient('~', Material.BLAZE_ROD);
		//Add recipies
		//add them to lookup so we can purge them later
		ourRecipies.add(drp);
		//Add them to server
		p.getServer().addRecipe(drp);
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
		DSUBag.p = p;
		Bukkit.getServer().getPluginManager().registerEvents(listen, p);
		setupOurRecipes();
	}
	
	public static void disable(Factorize p) {
		cleanupOurRecipies();
	}
}
