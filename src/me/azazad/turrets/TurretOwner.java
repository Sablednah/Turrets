package me.azazad.turrets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class TurretOwner {
	private int numTurretsOwned;
	private int maxTurretsAllowed;
	private Player player;
	private List<Turret> turretsOwned;
	
	public TurretOwner(Player player, TurretsPlugin plugin) {
		this.player = player;
		this.maxTurretsAllowed = plugin.getMaxTurretsPerPlayer();
		this.numTurretsOwned = 0;
		this.turretsOwned = new ArrayList<Turret>();
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public int getNumTurretsOwned() {
		return this.numTurretsOwned;
	}
	
	public void addTurretOwned(Turret turret) {
		this.numTurretsOwned++;
		this.turretsOwned.add(turret);
	}
	
	public void removeTurretOwned(Turret turret) {
		this.numTurretsOwned--;
		this.turretsOwned.remove(turret);
	}
	
	public int getMaxTurretsAllowed() {
		return this.maxTurretsAllowed;
	}
	
	public void setMaxTurretsAllowed(int num) {
		this.maxTurretsAllowed = num;
	}
	
	public List<Turret> getTurretsOwned() {
		return this.turretsOwned;
	}
}
