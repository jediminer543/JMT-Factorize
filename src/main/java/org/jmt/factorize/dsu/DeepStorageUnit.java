package org.jmt.factorize.dsu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jmt.factorize.Factorize;

/**
 * 
 * Lets you store many of a single thing
 *  
 * @author jediminer543
 *
 */
public class DeepStorageUnit {
	
	public static class DSUInstance {
		Material type;
		long count;
		
		public DSUInstance() {
			this(null, 0);
		}
		
		public DSUInstance(Material type, long count) {
			this.type = type;
			this.count = count;
		}
		
		public int increment(long delta) {
			long oldCount = count;
			count = Math.min(count+delta, Integer.MAX_VALUE);
			return (int) (delta-(count-oldCount));
		}
		
		public int decrement(long delta) {
			long oldCount = count;
			count = Math.max(count-delta, 0);
			if (count == 0) {
				type = null;
			}
			return (int) (oldCount-count);
		}

		public Material getType() {
			return type;
		}

		public long getCount() {
			return count;
		}
		
	}
	
	static List<DSUInstance> dsus = new ArrayList<>();
	
	static String name = "DSU";
	static int size = 9;	
	/**
	 * Icon set; names can be localised (they are unused)
	 */
	static ItemStack[] icons = new ItemStack[size];
	
	static {
		setOption(0, new ItemStack(Material.PAPER), "Count", "Count the number of item in DSU");
		setOption(7, new ItemStack(Material.HOPPER), "Put all", "Put all matching items into DSU");
		setOption(8, new ItemStack(Material.DROPPER), "Take all", "Take all items out of DSU\nor as many will fit in your inventory");
	}
	
	public static boolean open(HumanEntity p, int dsuidx) {
		if (dsus.size() == dsuidx) {
			dsus.add(new DSUInstance());
		} else if (dsus.size() < dsuidx) {
			return false;
		}
		Inventory inv = Bukkit.createInventory(p, size, name+" "+dsuidx);
		for (int i = 0; i < size; i++) {
			if (icons[i] != null) inv.setItem(i, icons[i]);
		}
		senDSUStack(inv, dsuidx);
		p.openInventory(inv);
		return true;
	}
	
	private static void senDSUStack(Inventory inv, int dsuidx) {
		Material type = dsus.get(dsuidx).type == null ? Material.PAPER : dsus.get(dsuidx).type;
		ItemStack dsuItem = new ItemStack(type);
		ItemMeta meta = dsuItem.getItemMeta();
		meta.setLore(Arrays.asList("DSU Item Stack", "Currently contains " + dsus.get(dsuidx).count + " items"));
		dsuItem.setItemMeta(meta);
		inv.setItem(4, dsuItem);
	}
	
	private static void setOption(int idx, ItemStack icon, String buttonLabel, String buttonHint) {
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(buttonLabel);
		meta.setLore(Arrays.asList(buttonHint.split("\n")));
		icon.setItemMeta(meta);
		icons[idx] = icon;
	}
	
	static final Listener listen = new Listener() {
		@SuppressWarnings("deprecation")
		@EventHandler
		void onInventoryClick(InventoryClickEvent ecv) {
			Inventory dsu = ecv.getInventory();
			//TODO Fix all deprecation
			if (!dsu.getName().startsWith(name)) {
				//NOT INV WE CARE ABOUT
				return;
			}
			if (ecv.getClickedInventory() == null) {
				return;
			}
			Inventory playerinv = ecv.getWhoClicked().getInventory();
			HumanEntity player = ecv.getWhoClicked();
			int idx = Integer.valueOf(dsu.getName().split(" ")[1]);
			if (ecv.getClickedInventory().getName().equals(dsu.getName())) {
				//FROM DSU TO INV/CURSOR OR TO DSU FROM CURSOR
				if (ecv.getAction() == InventoryAction.PICKUP_ALL) {
					ecv.setCancelled(true);
					int slot = ecv.getSlot();
					if (slot >= 0 && slot < size && (icons[slot] != null || slot == 5)) {
						switch (slot) {
						case 0:
							if (dsus.get(idx).type != null) {
								player.sendMessage("Contains " + dsus.get(idx).count + " of type " + dsus.get(idx).type);
							} else {
								player.sendMessage("DSU is empty");
							}
							senDSUStack(dsu, idx);
							break;
						case 7:
							if (dsus.get(idx).type != null) {
								List<ItemStack> iss = Arrays.stream(playerinv.getContents()).filter(is -> is != null && is.getType().equals(dsus.get(idx).type)).collect(Collectors.toList());
								iss.stream().forEach((is) -> {
									int toSet = dsus.get(idx).increment(is.getAmount());
									is.setAmount(toSet);
									senDSUStack(dsu, idx);
								});
							} else {
								player.sendMessage("DSU is empty");
							}
							senDSUStack(dsu, idx);
							break;
						case 8:
							if (dsus.get(idx).type != null) {
								boolean rejected = false;
								while (!rejected && dsus.get(idx).count > 0) {
									ItemStack stack = new ItemStack(dsus.get(idx).type, dsus.get(idx).decrement(64));
									if (playerinv.addItem(stack).size() != 0) {
										dsus.get(idx).increment(stack.getAmount());
										if (dsus.get(idx).type == null) {
											dsus.get(idx).type = stack.getType();
										}
										rejected = true;
									}
								}
							} else {
								player.sendMessage("DSU is empty");
							}
							senDSUStack(dsu, idx);
							break;
						default:
							break;
						}
					}
				} else if (ecv.getAction() == InventoryAction.PLACE_ALL) {
					ItemStack cursor = ecv.getCursor();
					if (dsus.get(idx).type == null) {
						dsus.get(idx).type = cursor.getType();
					}
					if (dsus.get(idx).type.equals(cursor.getType())) {
						int toSet = dsus.get(idx).increment(cursor.getAmount());
						cursor.setAmount(toSet);
						senDSUStack(dsu, idx);
					}
					ecv.setCancelled(true);
				} else if (ecv.getAction() == InventoryAction.PLACE_ONE) {
					ItemStack cursor = ecv.getCursor();
					if (dsus.get(idx).type == null) {
						dsus.get(idx).type = cursor.getType();
					}
					if (dsus.get(idx).type.equals(cursor.getType())) {
						int toSet = dsus.get(idx).increment(1);
						cursor.setAmount(cursor.getAmount()-1+toSet);
						senDSUStack(dsu, idx);
					}
					ecv.setCancelled(true);
				} else if (ecv.getAction() == InventoryAction.PLACE_SOME) {
					ItemStack cursor = ecv.getCursor();
					if (dsus.get(idx).type == null) {
						dsus.get(idx).type = cursor.getType();
					}
					if (dsus.get(idx).type.equals(cursor.getType())) {
						int toSet = dsus.get(idx).increment(cursor.getAmount());
						cursor.setAmount(toSet);
						senDSUStack(dsu, idx);
					}
					ecv.setCancelled(true);
				} else if (ecv.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					ecv.setCancelled(true);
					int slot = ecv.getSlot();
					if (slot == 4) {
						ItemStack stack = new ItemStack(dsus.get(idx).type, dsus.get(idx).decrement(64));
						if (playerinv.addItem(stack).size() != 0) {
							dsus.get(idx).increment(stack.getAmount());
							if (dsus.get(idx).type == null) {
								dsus.get(idx).type = stack.getType();
							}
						} else {
						}
						senDSUStack(dsu, idx);
					}
				} else {
					ecv.setCancelled(true);
				}
			} else {
				//CLICKING FROM INV TO DSU
				if (ecv.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					ecv.setCancelled(true);
					ItemStack cursor = ecv.getCurrentItem();
					if (dsus.get(idx).type == null) {
						dsus.get(idx).type = cursor.getType();
					}
					if (dsus.get(idx).type.equals(cursor.getType())) {
						int toSet = dsus.get(idx).increment(cursor.getAmount());
						cursor.setAmount(toSet);
						senDSUStack(dsu, idx);
					}
				}
			}
		}
	};
	
	public static void enable(Factorize p) {
		dsus = new ArrayList<>();
		int idx = 0;
		DSUInstance dsuinst = p.dsuConf.loadDSU(idx);
		while (dsuinst != null) {
			dsus.add(dsuinst);
			idx++;
			dsuinst = p.dsuConf.loadDSU(idx);
		}
		Bukkit.getServer().getPluginManager().registerEvents(listen, p);
	}
	
	public static void disable(Factorize p) {
		for (int i = 0; i<dsus.size(); i++) {
			p.dsuConf.saveDSU(dsus.get(i), i);
		}
	}

	public static int getNextID() {
		return dsus.size();
	}
}
