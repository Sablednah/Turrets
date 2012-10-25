package me.azazad.bukkit.util;

import me.azazad.turrets.Turret;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerCommandSender {
	private Player player = null;
	private int turretCreationStep = 0;
	private Turret turretSelected = null;
	private int turretDeletionStep = 0;
	private boolean senderLocked = false;
	private boolean unlimitedAmmoCommanded;
	private int turretAmmoStep = 0;
	private int turretActivationStep = 0;
	private int turretDeactivationStep = 0;
	private int turretAmmoTypeStep = 0;
	private Material ammoChangeAmmoTypeVal = null;
	
	
	public PlayerCommandSender(Player sender) {
		this.player = sender;
	}
	
	public boolean getUnlimAmmoCommanded() {
		return this.unlimitedAmmoCommanded;
	}
	
	public void setUnlimAmmoCommanded(boolean state) {
		this.unlimitedAmmoCommanded = state;
	}
	
	public boolean getLockedState() {
		return this.senderLocked;
	}
	
	public void setLockedState(boolean state) {
		this.senderLocked = state;
	}
	
	public int getTurretCreationStep() {
		return(this.turretCreationStep);
	}
	
	public void setTurretCreationStep(int i) {
		this.turretCreationStep = i;
	}
	
	public int getTurretAmmoUsageStep() {
		return(this.turretAmmoStep);
	}
	
	public void setTurretAmmoUsageStep(int i) {
		this.turretAmmoStep = i;
	}
	
	public int getTurretDeletionStep() {
		return(this.turretDeletionStep);
	}
	
	public void setTurretDeletionStep(int i) {
		this.turretDeletionStep = i;
	}
	
	public Player getPlayer() {
		return(this.player);
	}
	
	public Turret getTurretSelected() {
		return this.turretSelected;
	}
	
	public void setTurretSelected(Turret turret) {
		this.turretSelected = turret;
	}

	public void setTurretActivateStep(int i) {
		this.turretActivationStep  = i;
	}
	
	public int getTurretActivationStep() {
		return this.turretActivationStep;
	}

	public int getTurretDeactivationStep() {
		return this.turretDeactivationStep;
	}
	
	public void setTurretDeactivationStep(int i) {
		this.turretDeactivationStep  = i;
	}

	public void setTurretAmmoTypeStep(int i) {
		this.turretAmmoTypeStep  = i;
	}
	
	public int getTurretAmmoTypeStep() {
		return this.turretAmmoTypeStep;
	}

	public Material getAmmoChangeAmmoTypeVal() {
		return ammoChangeAmmoTypeVal;
	}

	public void setAmmoChangeAmmoTypeVal(Material material) {
		this.ammoChangeAmmoTypeVal = material;
	}
}
