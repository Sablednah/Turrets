package me.azazad.turrets;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TurretShooter{
	private boolean shooterClicked = false;
	private Player player = null;
	private Location startLoc;
	
	public TurretShooter(Player player) {
		this.player = player;
		this.startLoc = player.getLocation();
	}
	
	public Player getPlayer() {
		return(this.player);
	}
	
	public void setClickedFlag(boolean state) {
		this.shooterClicked = state;
	}
	
	public boolean didShooterClick() {
		return(shooterClicked);
	}
	
	public Location getStartLoc() {
		return this.startLoc;
	}
	
	public double getStartX() {
		return this.startLoc.getX();
	}
	
	public double getStartY() {
		return this.startLoc.getY();
	}
	
	public double getStartZ() {
		return this.startLoc.getZ();
	}
}
