package me.azazad.turrets;

import java.util.HashSet;
import java.util.Set;

public class OwnerWBlists {
	private String ownerName;
	private Set<String> whitelist;
	private Set<String> blacklist;
	private boolean usingBlacklist;
	private boolean pvpEnabled;
	
	public OwnerWBlists(String ownerName, Set<String> whitelist, Set<String> blacklist, boolean usingBlacklist, boolean pvpEnabled) {
		this.ownerName = ownerName;
		this.whitelist = whitelist;
		if(whitelist==null) this.whitelist = new HashSet<String>();
		this.blacklist = blacklist;
		if(blacklist==null) this.blacklist = new HashSet<String>();
		this.usingBlacklist = usingBlacklist;
		this.pvpEnabled = pvpEnabled;
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
