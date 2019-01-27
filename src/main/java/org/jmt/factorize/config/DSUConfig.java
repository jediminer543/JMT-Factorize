package org.jmt.factorize.config;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.jmt.factorize.dsu.DeepStorageUnit.DSUInstance;

public class DSUConfig extends CustomConfig {

	public DSUConfig(JavaPlugin plugin, String fileName) {
		super(plugin, fileName);
	}
	
	public void saveDSU(DSUInstance ins, int idx) {
		this.getConfig().set("instance."+idx+".material", ins.getType() == null ? "null" : ins.getType().toString());
		this.getConfig().set("instance."+idx+".count", ins.getCount());
	}
	
	public DSUInstance loadDSU(int idx) {
		if (!this.getConfig().isSet("instance."+idx)) {
			return null;
		} else {
			String mat = this.getConfig().getString("instance."+idx+".material");
			return new DSUInstance(mat.equals("null") ? null : Material.getMaterial(mat), 
					this.getConfig().getInt("instance."+idx+".count"));
		}
	}

}
