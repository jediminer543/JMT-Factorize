package org.jmt.factorize.util;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/**
 * 
 * A menu that lets you use Materials (items) as icons
 * to do things
 * 
 * Heavily inspired by https://bukkit.org/threads/icon-menu.108342/
 * 
 * @author jediminer543
 *
 */
public class IconMenu implements Listener {
	
	/**
	 * Function that handles a click on the UI
	 * 
	 * @author jediminer543
	 *
	 */
	@FunctionalInterface
	public static interface IconMenuClickHandler {
		
		/**
		 * Handle an interaction with the icon menu
		 * 
		 * @param p HumanEntity who clicked on the menu item
		 * @param buttonIdx Index of menu that was clicked
		 * @return Should IconMenu close or not
		 */
		public boolean handle(IconMenu ui, HumanEntity p, int buttonIdx);
		
	}
	
	String name;
	int size;
	IconMenuClickHandler handler;
	
	/**
	 * Icon set; names can be localised (they are unused)
	 */
	ItemStack[] icons;
	
	/**
	 * Create a new icon menu; designed to be re-usable
	 * 
	 * @param name Name unique to THIS icon menu
	 * @param size Size of icon menu
	 * @param clickh Click handler for menu
	 * @param p Plugin registering icon menu
	 */
	public IconMenu(String name, int size, IconMenuClickHandler clickh, Plugin p) {
		this.name = name;
		this.size = size;
		this.handler = clickh;
		icons = new ItemStack[size];
		p.getServer().getPluginManager().registerEvents(this, p);
	}
	
	public void open(HumanEntity p) {
		Inventory inv = Bukkit.createInventory(p, size, name);
		for (int i = 0; i < size; i++) {
			if (icons[i] != null) inv.setItem(i, icons[i]);
		}
		p.openInventory(inv);
	}
	
	public void setOption(int idx, ItemStack icon, String buttonLabel, String buttonHint) {
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(buttonLabel);
		meta.setLore(Arrays.asList(buttonHint.split("\n")));
		icon.setItemMeta(meta);
		icons[idx] = icon;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	void onInventoryClick(InventoryClickEvent ecv) {
		if (ecv.getClickedInventory().getName().equals(this.name)) {
			ecv.setCancelled(true);
			if (ecv.getAction() == InventoryAction.PICKUP_ALL) {
				int slot = ecv.getSlot();
				if (slot >= 0 && slot < size && icons[slot] != null) {
					HumanEntity player = ecv.getWhoClicked();
					if (handler.handle(this, player, slot)) {
						player.closeInventory();
					}
				}
			}
			if (ecv.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				ecv.setCancelled(true);
			}
		} else if (ecv.getClickedInventory().getName().equals(this.name)) {
			if (ecv.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				ecv.setCancelled(true);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	void onInventoryClose(InventoryCloseEvent ecv) {
		if (ecv.getInventory().getName().equals(this.name)) {
			HandlerList.unregisterAll(this);
		}
	}
}
