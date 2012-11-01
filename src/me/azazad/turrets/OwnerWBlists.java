package me.azazad.turrets;

import java.util.Set;

public class OwnerWBlists {
	private String ownerName;
	private Set<String> whitelist;
	private Set<String> blacklist;
	
	public OwnerWBlists(String ownerName, Set<String> whitelist, Set<String> blacklist) {
		this.ownerName = ownerName;
		this.whitelist = whitelist;
		this.blacklist = blacklist;
	}
	
	public String getOwnerName() {
		return this.ownerName;
	}
	
	public boolean isPlayerInWhitelist(String player) {
		return this.whitelist.contains(player);
	}
	
	public boolean isPlayerInBlacklist(String player) {
		return this.blacklist.contains(player);
	}
	
	public void addPlayerToBlacklist(String player) {
		if(!this.blacklist.contains(player)) this.blacklist.add(player);
	}
	
	public void addPlayerToWhitelist(String player) {
		if(!this.whitelist.contains(player)) this.whitelist.add(player);
	}
	
	public void removePlayerFromBlacklist(String player) {
		if(this.blacklist.contains(player)) this.blacklist.remove(player);
	}
	
	public void removePlayerFromWhitelist(String player) {
		if(this.whitelist.contains(player)) this.whitelist.remove(player);
	}
	
	public Set<String> getWhitelist() {
		return this.whitelist;
	}
	
	public Set<String> getBlacklist() {
		return this.blacklist;
	}
}
