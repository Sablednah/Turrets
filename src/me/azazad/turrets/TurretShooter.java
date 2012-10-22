package me.azazad.turrets;

import org.bukkit.entity.Player;

public class TurretShooter{
	private boolean shooterClicked = false;
	private Player player = null;
	
	public TurretShooter(Player player) {
		this.player = player;
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
	
}
