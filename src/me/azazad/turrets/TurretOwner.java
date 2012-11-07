package me.azazad.turrets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TurretOwner{
	private String ownerName;
	private Set<String> whitelist;
	private Set<String> blacklist;
	private boolean usingBlacklist;
	private boolean pvpEnabled;
	private int numTurretsOwned;
	private int maxTurretsAllowed;
	private OfflinePlayer offlinePlayer;
	private Player onlinePlayer;
	private List<Turret> turretsOwned;
	
	public TurretOwner(TurretsPlugin plugin, String ownerName, int maxTurretsAllowed, Set<String> whitelist, Set<String> blacklist, boolean usingBlacklist, boolean pvpEnabled) {
		this.ownerName = ownerName;
		this.whitelist = whitelist;
		if(whitelist==null) this.whitelist = new HashSet<String>();
		this.blacklist = blacklist;
		if(blacklist==null) this.blacklist = new HashSet<String>();
		this.usingBlacklist = usingBlacklist;
		this.pvpEnabled = pvpEnabled;
		this.offlinePlayer = Bukkit.getOfflinePlayer(ownerName);
		this.onlinePlayer = offlinePlayer.getPlayer();
		this.maxTurretsAllowed = maxTurretsAllowed;
		this.numTurretsOwned = 0;
		this.turretsOwned = new ArrayList<Turret>();
	}
	
	public void refreshOnlineStatus() {
		this.onlinePlayer = offlinePlayer.getPlayer();
	}
	
	public Player getOnlinePlayer() {
		return this.onlinePlayer;
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return this.offlinePlayer;
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
	
	public String getOwnerName() {
		return this.ownerName;
	}
	
	public boolean isPlayerInWhitelist(String player) {
		return this.whitelist.contains(player.toLowerCase());
	}
	
	public boolean isPlayerInBlacklist(String player) {
		return this.blacklist.contains(player.toLowerCase());
	}
	
	public void addPlayerToBlacklist(String player) {
		if(!this.blacklist.contains(player.toLowerCase())) this.blacklist.add(player.toLowerCase());
	}
	
	public void addPlayerToWhitelist(String player) {
		if(!this.whitelist.contains(player.toLowerCase())) this.whitelist.add(player.toLowerCase());
	}
	
	public void removePlayerFromBlacklist(String player) {
		if(this.blacklist.contains(player.toLowerCase())) this.blacklist.remove(player.toLowerCase());
	}
	
	public void removePlayerFromWhitelist(String player) {
		if(this.whitelist.contains(player.toLowerCase())) this.whitelist.remove(player.toLowerCase());
	}
	
	public Set<String> getWhitelist() {
		return this.whitelist;
	}
	
	public Set<String> getBlacklist() {
		return this.blacklist;
	}
	
	public void copyWhitelist(Set<String> whitelistToCopyFrom) {
		this.whitelist = whitelistToCopyFrom;
	}
	
	public void copyBlacklist(Set<String> blacklistToCopyFrom) {
		this.blacklist = blacklistToCopyFrom;
	}
	
	public void setPvpEnabled(boolean state) {
		this.pvpEnabled = state;
	}
	
	public void setUseBlacklist(boolean use) {
		this.usingBlacklist = use;
	}
	
	public boolean isPvpEnabled() {
		return this.pvpEnabled;
	}
	
	public boolean isUsingBlacklist() {
		return this.usingBlacklist;
	}
}
