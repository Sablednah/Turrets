package me.azazad.turrets;

import java.util.Map;
import java.util.HashMap;

import me.azazad.bukkit.util.BlockLocation;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;

public class TurretAmmoBox {
	private Map<BlockLocation,Chest> chests;
	
	public TurretAmmoBox() {
		this.chests = new HashMap<BlockLocation,Chest>();
	}
	
	public int getAmmoChestNum() {
		return(chests.size());
	}
	
	public Map<BlockLocation,Chest> getMap() {
		return(chests);
	}
	
	public boolean isChestAttachedToTurret(Block chest_block) {
		return(chests.containsKey(BlockLocation.fromLocation(chest_block.getLocation())));
	}
	
	public boolean addAmmoChest(Block chest_block) {
		Chest chest = (Chest)(chest_block.getState());
		if (!isChestAttachedToTurret(chest_block)) {
			this.chests.put(BlockLocation.fromLocation(chest_block.getLocation()),chest);
			return true;
		}
		else return false;
	}
	
	public boolean removeAmmoChest(Block chest_block) {
		Chest chest = (Chest)(chest_block.getState());
		if (this.chests.containsValue(chest)) {
			this.chests.remove(BlockLocation.fromLocation(chest.getLocation()));
			return(true);
		} else return(false);
		
	}
}
