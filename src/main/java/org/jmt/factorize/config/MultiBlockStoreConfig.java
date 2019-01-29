package org.jmt.factorize.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jmt.factorize.Factorize;
import org.jmt.factorize.multiblock.IMultiblock;
import org.jmt.factorize.multiblock.IMultiblockInstance;

public class MultiBlockStoreConfig extends CustomConfig {

	public MultiBlockStoreConfig(JavaPlugin plugin, String fileName) {
		super(plugin, fileName);
		if (!this.getConfig().isSet("addr.next")) {
			this.getConfig().set("addr.next", 0);
		}
	}
	
	public void setMultiBlock(Location coreLoc, IMultiblockInstance imb) {
		int addr = 0;
		if (!this.getConfig().isSet("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY())) { 
			addr = this.getConfig().getInt("addr.next", 0);
			this.getConfig().set("addr.next", addr+1);
			this.getConfig().set("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY(), addr);
		} else {
			addr = this.getConfig().getInt("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY());
		}
		if (!this.getConfig().isSet("mb."+addr)) {
			this.getConfig().set("mb."+addr+".loc", coreLoc);
		}
		this.getConfig().set("mb."+addr+"."+imb.getID()+".type", imb.getType().getName());
		if (!this.getConfig().isSet("mb."+addr+"."+imb.getID()+".data")) {
			this.getConfig().createSection("mb."+addr+"."+imb.getID()+".data");
		}
		imb.save(this.getConfig().getConfigurationSection("mb."+addr+"."+imb.getID()+".data"));
	}
	
	public void unsetMultiBlock(Location coreLoc, IMultiblockInstance imb) {
		int addr = 0;
		if (!this.getConfig().isSet("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY())) { 
			addr = this.getConfig().getInt("addr.next", 0);
			this.getConfig().set("addr.next", addr+1);
			this.getConfig().set("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY(), addr);
		} else {
			addr = this.getConfig().getInt("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY());
		}
		if (!this.getConfig().isSet("mb."+addr)) {
			this.getConfig().set("mb."+addr+".loc", coreLoc);
		}
		this.getConfig().set("mb."+addr+"."+imb.getID(), null);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Location, Map<UUID, Class<? extends IMultiblock>>> getAllMultiBlocks() {
		int addrMax = this.getConfig().getInt("addr.next", 0);
		Map<Location, Map<UUID, Class<? extends IMultiblock>>> out = new HashMap<Location, Map<UUID, Class<? extends IMultiblock>>>();
		for (int i = 0; i < addrMax; i++) {
			Location loc = this.getConfig().getSerializable("mb."+i+".loc", Location.class);
			out.put(loc, new HashMap<>());
			if (!this.getConfig().isSet("mb."+i)) {
				Factorize.instance.getLogger().warning("Multiblock config index " + i + " not found");
				continue;
			}
			List<UUID> potentialMblocks = this.getConfig().getConfigurationSection("mb."+i).getKeys(false).stream().filter(l -> !l.equals("loc")).map(UUID::fromString).collect(Collectors.toList());
			for (UUID u : potentialMblocks) {
				try {
					String mbtypeClass = this.getConfig().getString("mb."+i+"."+u+".type");
					Class<?> imb = Class.forName(mbtypeClass);
					if (imb instanceof Class) {
						out.get(loc).put(u, (Class<? extends IMultiblock>) imb);
					} else {
						throw new ClassNotFoundException("Attempted to load class " + mbtypeClass + " as IMultiblock when it is not");
					}
				} catch (ClassNotFoundException e) {
					Factorize.instance.getLogger().log(Level.WARNING, "Attempted to load multiblock not existing in classpath; version error?", e);
				}
			}
		}
		return out;
	}
	
	public ConfigurationSection getMultiBlockData(Location coreLoc, UUID id) {
		int addr = 0;
		if (!this.getConfig().isSet("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY())) { 
			addr = this.getConfig().getInt("addr.next", 0);
			this.getConfig().set("addr.next", addr+1);
			this.getConfig().set("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY(), addr);
		} else {
			addr = this.getConfig().getInt("addr."+coreLoc.getWorld().getUID()+"."+coreLoc.getBlockX()+"."+coreLoc.getBlockY()+"."+coreLoc.getBlockY());
		}
		if (!this.getConfig().isSet("mb."+addr)) {
			throw new RuntimeException("The multiblock addressing section " + addr + " was not found");
		}
		return this.getConfig().getConfigurationSection("mb."+addr+"."+id+".data");
	}
}
