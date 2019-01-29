package org.jmt.factorize;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jmt.factorize.config.DSUConfig;
import org.jmt.factorize.config.MultiBlockStoreConfig;
import org.jmt.factorize.dsu.DSUBag;
import org.jmt.factorize.dsu.DeepStorageUnit;
import org.jmt.factorize.multiblock.MultiblockController;
import org.jmt.factorize.tools.Hammer;
import org.jmt.factorize.util.IconMenu;

/**
 * Welcome to factorize
 * 
 * Factorio for bukkit
 * 
 * @author jediminer543
 *
 */
public class Factorize extends JavaPlugin {
	
	public static Factorize instance = null;
	
	/**
	 * DSU config file
	 */
	public DSUConfig dsuConf = null;
	
	/**
	 * Multiblock config file
	 */
	public MultiBlockStoreConfig mbsc = null;
	
	
	@Override
	public void onEnable() {
		instance = this;
		getLogger().info("Factorize is LOADING [gear icon]");
		//Multiblocks
		mbsc = new MultiBlockStoreConfig(this, "mbsc.yml");
		mbsc.reloadConfig();
		MultiblockController.enable();
		//DSUs
		//Load the DSU config file
		dsuConf = new DSUConfig(this, "dsu.yml");
		dsuConf.reloadConfig();
		//SetUp DSUS
		DeepStorageUnit.enable(this);
		DSUBag.enable(this);
		//Tools
		Hammer.enable(this);
	}
	
	
	@Override
	public void onDisable() {
		getLogger().info("Factorize is SHUTTING DOWN [sad gear icon]");
		//DSUs
		DeepStorageUnit.disable(this);
		DSUBag.disable(this);
		//Tools
		Hammer.disable(this);
		//Multiblocks
		MultiblockController.disable();
		//Save all resources
		this.save();
		instance = null;
	}
	
	/**
	 * Save all current resources
	 */
	public void save() {
		//Saves plugins own config (currently does nothing)
		this.saveConfig();
		//Saves DSU config
		dsuConf.saveConfig();
		//Saves DSU config
		mbsc.saveConfig();
	}
	
	

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		boolean player = false;
		HumanEntity senderp = null;
		//Get player if one exists
		if (sender instanceof HumanEntity)
		{
			player = true;
			senderp = (HumanEntity) sender;
		}
		switch (cmd.getName().toLowerCase())
		{
			case "factorize":
				//Bulk command
				//NYI
				sender.sendMessage("NYI");
				return true;
			case "spawn":
				if(player)
				{
					//Send player to spawn
					senderp.teleport(senderp.getWorld().getSpawnLocation());
				}
				else
				{
					//Cannot send console/command block to spawn
					sender.sendMessage("Only players can execute this command");
				}	
				return true;
			case "wild":
				if (player)
				{
					//Teleport 
					if (senderp.getWorld() == Bukkit.getServer().getWorlds().get(0))
					{
						Random rand = new Random();
						int r = 1000;
						int x = rand.nextInt(r - 100)+100;
						int z = (int) Math.sqrt(r^2-x^2);
						World world = Bukkit.getServer().getWorlds().get(0);
						Chunk c = world.getChunkAt(x/ 16, z/ 16);
						c.load(true);
						int y;
						y = world.getHighestBlockAt(x, z).getY() + 1;
						senderp.teleport(new Location(world, x + senderp.getLocation().getX(), y, z + senderp.getLocation().getZ()));
						
					}
					else
					{
						sender.sendMessage("This Command only works in the overworld");
					}
					return true;
				}
				else
				{
					sender.sendMessage("Only players can execute this command");
				}			
				return false;
			case "dsu":
				if (player)
				{
					int dsu = Integer.valueOf(args[0]);
					if (!DeepStorageUnit.open(senderp, dsu)) {
						sender.sendMessage("Invalid DSU");
					}
					return true;
				}
				else
				{
					sender.sendMessage("Only players can execute this command");
				}			
				return false;
			case "hammertime":
				if (player)
				{
					ItemStack is = Hammer.getHammer(Material.DIAMOND_PICKAXE);
					senderp.getInventory().addItem(is);
					return true;
				}
				else
				{
					sender.sendMessage("Only players can execute this command");
				}			
				return false;
			case "devtest":
				if (player)
				{
					IconMenu menu = new IconMenu("DevTest", 9, (m, p, idx) -> {
						switch (idx) {
						case 3:
							sender.sendMessage("HALLO");
							break;
						case 4:
							sender.sendMessage("TEST");
							break;
						case 5:
							sender.sendMessage("WOOT");
							break;
						}
						return false;
					}, this);
					menu.setOption(3, new ItemStack(Material.APPLE), "HELLO", "???");
					menu.setOption(4, new ItemStack(Material.DIAMOND), "TEST", "???");
					menu.setOption(5, new ItemStack(Material.OAK_LOG), "WOOT", "???");
					menu.open(senderp);
				}
				else
				{
					sender.sendMessage("Only players can execute this command");
				}			
				return false;
			default:
				return false;
		}
	}
	
}
