//TODO: auto-save turrets every 5 minutes or so
//TODO: handle removal of minecart entity more cleanly

package me.azazad.turrets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.azazad.bukkit.util.BlockLocation;
import me.azazad.bukkit.util.PlayerCommandSender;
import me.azazad.turrets.persistence.TurretDatabase;
import me.azazad.turrets.persistence.YAMLTurretDatabase;
import me.azazad.turrets.targeting.MobAssessor;
import me.azazad.turrets.targeting.TargetAssessor;
import me.azazad.turrets.upgrade.UpgradeLadder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.block.Chest;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
//import org.bukkit.plugin.RegisteredServiceProvider;
//import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TurretsPlugin extends JavaPlugin{
    private static final String TURRET_DB_FILENAME = "turrets.yml";
    private static final String OWNER_DB_FILENAME = "turretOwners.yml";
    private static final String OLD_OWNER_DB_FILENAME = "ownerWBlists.yml";
    private static FileConfiguration turretOwnersFC;
    private static File turretOwnersFile = null;
    private static File oldTurretOwnersFile = null;
    public static final List<Material> POST_MATERIALS = new ArrayList<Material>();
    public List<PlayerCommandSender> playerCommanders = new ArrayList<PlayerCommandSender>();
    
    public Set<String> globalWhitelist;
    
    public static Logger globalLogger;
    private Map<String,Boolean> booleanConfigMap = new HashMap<String,Boolean>();
    
    public PluginDescriptionFile pdf;
    private final UpgradeLadder upgradeLadder = new UpgradeLadder();
    private TurretDatabase turretDatabase;
    private List<Material> boxAmmoTypes = new ArrayList<Material>();
    private List<Material> unlimitedAmmoTypes = new ArrayList<Material>();
    private final List<TargetAssessor> targetAssessors = new ArrayList<TargetAssessor>();
    private final Map<BlockLocation,Turret> turrets = new HashMap<BlockLocation,Turret>();
    private final Map<String,TurretOwner> turretOwners = new HashMap<String,TurretOwner>();
    private final Collection<Turret> unmodifiableTurrets = Collections.unmodifiableCollection(turrets.values());
    private int maxTurretsPerPlayer;
    
    
    public TurretsPlugin(){
        targetAssessors.add(new MobAssessor());
    }
    
    @Override
    public void onLoad(){
        //TODO: check to make sure that the server is CraftBukkit
    	
        pdf = getDescription();
        turretDatabase = new YAMLTurretDatabase(new File(getDataFolder(),TURRET_DB_FILENAME),this);
        
    }
    
    @Override
    public void onEnable(){
    	globalLogger = getLogger();
        Logger logger = getLogger();
        Server server = getServer();
        PluginManager pluginManager = server.getPluginManager();
        
        //load configuration
        saveDefaultConfig();
        Configuration config = getConfig();
        upgradeLadder.loadUpgradeTiers(config,logger);
        loadConfigOptions(config,logger);
        loadAmmoTypes(config,logger);
        logger.info("Config file loaded.");
        
        //load owner whitelists
        turretOwnersFile = new File(getDataFolder(), OWNER_DB_FILENAME);
        oldTurretOwnersFile = new File(getDataFolder(), OLD_OWNER_DB_FILENAME);
		firstRun();
		turretOwnersFC = new YamlConfiguration();
		loadYamls();
		loadTurretOwners();
        
        //register listeners
        pluginManager.registerEvents(new TurretsListener(this),this);
        
        //register commands
        getCommand("turrets").setExecutor(new TurretsCommand(this));
        
        //initialize post_material types
        POST_MATERIALS.add(Material.FENCE);
        POST_MATERIALS.add(Material.IRON_FENCE);
        POST_MATERIALS.add(Material.NETHER_FENCE);
        POST_MATERIALS.add(Material.COBBLE_WALL);
        
        
        //load turrets
        try{
            loadAndSpawnTurrets();
            logger.info("Turrets loaded and spawned.");
        }catch(IOException e){
            logger.log(Level.SEVERE,"Failed to load turrets",e);
        }
        
        logger.info("Total number of turrets: "+turrets.size());
        
    }

	@Override
    public void onDisable(){
        Logger logger = getLogger();
        
        try{
            despawnAndSaveTurrets();
            logger.info("Despawned and saved turrets.");
        }catch(IOException e){
            logger.log(Level.SEVERE,"Failed to save turrets",e);
        }
        saveTurretOwners();
        saveYamls();
        this.saveConfig();
    }

	private void loadAmmoTypes(Configuration config, Logger logger) {
		if(config.get("boxAmmoTypes",null)==null) {
			config.set("boxAmmoTypes", "arrow,snow_ball");
		}
		else {
			String ammoTypesString = config.getString("boxAmmoTypes");
			ammoTypesString = ammoTypesString.toLowerCase().replaceAll("\\s", "");
			String[] ammoTypesArr = ammoTypesString.split(",");
			for(String ammoType : ammoTypesArr) {
				if(ammoType.equals("arrow")) this.getBoxAmmoTypes().add(Material.ARROW);
				else if(ammoType.equals("snowball") || ammoType.equals("snow_ball")) this.getBoxAmmoTypes().add(Material.SNOW_BALL);
				else if(ammoType.equals("expbottle") || ammoType.equals("exp_bottle")) this.getBoxAmmoTypes().add(Material.EXP_BOTTLE);
				else if(ammoType.equals("monsteregg") || ammoType.equals("monster egg") || ammoType.equals("monster_egg")) this.getBoxAmmoTypes().add(Material.MONSTER_EGG);
				else if(ammoType.equals("egg")) this.getBoxAmmoTypes().add(Material.EGG);
				else if(ammoType.equals("potion")) this.getBoxAmmoTypes().add(Material.POTION);
				else if(ammoType.equals("fireball") || ammoType.equals("fire_ball")) this.getBoxAmmoTypes().add(Material.FIREBALL);
			}
		}
		if(config.get("unlimitedAmmoTypes",null)==null) {
			config.set("unlimitedAmmoTypes", "arrow,snow_ball");
		}
		else {
			String ammoTypesString = config.getString("unlimitedAmmoTypes");
			ammoTypesString = ammoTypesString.toLowerCase().replaceAll("\\s", "");
			String[] ammoTypesArr = ammoTypesString.split(",");
			for(String ammoType : ammoTypesArr) {
				if(ammoType.equals("arrow")) this.getUnlimitedAmmoTypes().add(Material.ARROW);
				else if(ammoType.equals("snowball") || ammoType.equals("snow_ball")) this.getUnlimitedAmmoTypes().add(Material.SNOW_BALL);
				else if(ammoType.equals("expbottle") || ammoType.equals("exp_bottle")) this.getUnlimitedAmmoTypes().add(Material.EXP_BOTTLE);
				else if(ammoType.equals("monsteregg") || ammoType.equals("monster egg") || ammoType.equals("monster_egg")) this.getUnlimitedAmmoTypes().add(Material.MONSTER_EGG);
				else if(ammoType.equals("egg")) this.getUnlimitedAmmoTypes().add(Material.EGG);
				else if(ammoType.equals("potion")) this.getUnlimitedAmmoTypes().add(Material.POTION);
				else if(ammoType.equals("fireball") || ammoType.equals("fire_ball")) this.getUnlimitedAmmoTypes().add(Material.FIREBALL);
			}
		}
	}

	public List<Material> getUnlimitedAmmoTypes() {
		return this.unlimitedAmmoTypes;
	}
    
    public UpgradeLadder getUpgradeLadder(){
        return upgradeLadder;
    }
    
    public TurretDatabase getTurretDatabase(){
        return turretDatabase;
    }
    
    public List<TargetAssessor> getTargetAssessors(){
        return targetAssessors;
    }
    
    public Collection<Turret> getTurrets(){
        return unmodifiableTurrets;
    }
    
    public Map<BlockLocation, Turret> getTurretMap() {
    	return(turrets);
    }
    
    public Turret getTurret(BlockLocation postLocation){
        return turrets.get(postLocation);
    }
    
    public Turret getTurret(Player p) {
    	Turret turret = null;
    	for (Turret cur_turr : turrets.values()) {
    		if (cur_turr.getShooter()!=null) {
	    		if (cur_turr.getShooter().getPlayer().equals(p)) {
	    			turret = cur_turr;
	    			break;
	    		}
    		}
    	}
    	return turret;
    }
    
    public PlayerCommandSender getPlayerCommander(Player player) {
    	PlayerCommandSender pcsInCommand = null;
	    if (this.playerCommanders.size()>0) {
	    	for(PlayerCommandSender pcs : this.playerCommanders) {
	    		if (pcs.getPlayer().equals(player)) {
	    			pcsInCommand = pcs;
	    		}
	    	}
    	}
    	return pcsInCommand;
    }
    
    public boolean isPlayerAShooter(Player p) {
    	return(getTurret(p)!=null);
    }
    
    public void addTurret(Turret turret){
        BlockLocation location = turret.getLocation();
        
        if(!turrets.containsKey(location)){
            turrets.put(location,turret);
            turret.spawn();
        }
    }
    
    public void removeTurret(Turret turret){
        turret.despawn();
        turrets.remove(turret.getLocation());
    }
    
    public boolean canBuildTurret(BlockLocation location){
        return !turrets.containsKey(location);
    }
    
    public void saveTurrets() throws IOException{
        turretDatabase.saveTurrets(turrets.values());
    }
    
    public void loadAndSpawnTurrets() throws IOException{
    	Collection<Turret> dbTurrets=null;
    	try{dbTurrets = turretDatabase.loadTurrets();}
        catch(IOException e){};
        if(dbTurrets == null){
            return;
        }
        
        for(Turret turret : dbTurrets){
            if(!turrets.containsKey(turret.getLocation())) {
                turrets.put(turret.getLocation(),turret);
                turret.spawn();
            }
        }
    }
    
    public void despawnAndSaveTurrets() throws IOException{
        Iterator<Map.Entry<BlockLocation,Turret>> it = turrets.entrySet().iterator();
        turretDatabase.saveTurrets(turrets.values());
        while(it.hasNext()){
            Map.Entry<BlockLocation,Turret> entry = it.next();
            Turret turret = entry.getValue();
            turret.despawn();
            it.remove();
        }
    }
    
//    public Permission getPermissionsProvider(){
//        return permissionsProvider;
//    }
    
    public void respawnTurret(BlockLocation bloc) {
    	Turret turret = turrets.get(bloc);
    	turret.despawn();
    	turret.spawn();
    }
    
    
    public void notifyPlayer(Player player,TurretsMessage messageType){
        notifyPlayer(player,getMessage(messageType));
    }
    
    public void notifyPlayer(Player player,String message){
        if(message == null){
            return;
        }
        
        player.sendMessage("["+pdf.getName()+"] "+message);
    }
    
    public static String getMessage(TurretsMessage messageType){
        switch(messageType){
            case TURRET_CREATED: return "Turret created!";
            case TURRET_DESTROYED: return "Turret destroyed!";
            case TURRET_UPGRADED: return "Turret upgraded!";
            case TURRET_CANNOT_BUILD: return "Cannot build a turret here!";
            case NO_CREATE_PERM: return "You do not have permission to create turrets.";
            case NO_DESTROY_PERM: return "You do not have permission to destroy turrets.";
            default: return null;
        }
    }

	public Turret turretLinkedToChest(Chest chest) {
		Turret turret = null;
		Turret iterTurret;
		Iterator<Turret> iter = this.turrets.values().iterator();
		while(iter.hasNext()) {
			iterTurret = iter.next();
			if(iterTurret.getTurretAmmoBox().getMap().keySet().contains(BlockLocation.fromLocation(chest.getLocation()))) {
				turret = iterTurret;
			}
		}
		return turret;
	}

	public List<Material> getBoxAmmoTypes() {
		return boxAmmoTypes;
	}
	
	public Map<String, Boolean> getConfigMap() {
		return this.booleanConfigMap;
	}

	public int getMaxTurretsPerPlayer() {
		return maxTurretsPerPlayer;
	}

	public void setMaxTurretsPerPlayer(int maxTurretsPerPlayer) {
		this.maxTurretsPerPlayer = maxTurretsPerPlayer;
	}
	
	public Map<String,TurretOwner> getTurretOwners() {
		return this.turretOwners;
	}
	
	public TurretOwner getTurretOwner(String ownerName) {
		TurretOwner ownerReturn = null;
		for(TurretOwner turretOwner : this.turretOwners.values()) {
			if(turretOwner.getOwnerName().equalsIgnoreCase(ownerName)) ownerReturn = turretOwner;
		}
		return ownerReturn;
	}
	
	public TurretOwner getTurretOwner(Player player) {
		TurretOwner ownerReturn = null;
		for(TurretOwner turretOwner : this.turretOwners.values()) {
			if(turretOwner.getOfflinePlayer().getPlayer().equals(player)) ownerReturn = turretOwner;
		}
		return ownerReturn;
	}
	
	public TurretOwner getTurretOwner(OfflinePlayer offlinePlayer) {
		TurretOwner ownerReturn = null;
		for(TurretOwner turretOwner : this.turretOwners.values()) {
			if(turretOwner.getOfflinePlayer().equals(offlinePlayer)) ownerReturn = turretOwner;
		}
		return ownerReturn;
	}
	
	
	
	public void reloadPlugin(int verbose) {
		globalLogger = getLogger();
        Logger logger = getLogger();
		try{
            despawnAndSaveTurrets();
            logger.info("Despawned and saved turrets.");
        }catch(IOException e){
            logger.log(Level.SEVERE,"Failed to save turrets",e);
        }
        this.saveConfig();
        
        //load configuration
        saveDefaultConfig();
        Configuration config = getConfig();
        upgradeLadder.loadUpgradeTiers(config,logger);
        loadConfigOptions(config,logger);
        loadAmmoTypes(config,logger);
        logger.info("Config file loaded.");
        
        //load turrets
        try{
            loadAndSpawnTurrets();
            logger.info("Turrets loaded and spawned.");
        }catch(IOException e){
            logger.log(Level.SEVERE,"Failed to load turrets",e);
        }
        
        logger.info("Total number of turrets: "+turrets.size());
        if(verbose==1) Bukkit.broadcastMessage(ChatColor.YELLOW + "Reloaded "+ ChatColor.GRAY + getDescription().getFullName());
	}
	
	private void firstRun() {
		if(!turretOwnersFile.exists()){
			if(oldTurretOwnersFile.exists()) {
				turretOwnersFile.getParentFile().mkdirs();
				oldTurretOwnersFile.renameTo(turretOwnersFile);
			}
			else {
				turretOwnersFile.getParentFile().mkdirs();
				copyStreamToFile(getResource(OWNER_DB_FILENAME), turretOwnersFile);
			}
 	    }
	}
	
	private void copyStreamToFile(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void saveYamls() {
	    try {
	    	turretOwnersFC.save(turretOwnersFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadYamls() {
	    try {
	    	turretOwnersFC.load(turretOwnersFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void loadTurretOwners() {
		//TODO: make this loadTurretOwners(), which calls loadTurretOwner(String ownerName) or w/e, for every TurretOwner
		//You need to load all turret owners (offline and online) at server start, as the whitelist info etc is stored there.
		Set<String> ownerList = turretOwnersFC.getKeys(false);
		boolean pvpEnabled;
		boolean defaultUseBlacklist;
		for(String owner : ownerList) {
			ConfigurationSection ownerConfig = turretOwnersFC.getConfigurationSection(owner);
			Set<String> whitelistUserSet = new HashSet<String>();
			Set<String> blacklistUserSet = new HashSet<String>();
			String[] listUsers;
			String bigUserString = ownerConfig.getString("whitelist.users");
			if(bigUserString!=null) {
				listUsers = bigUserString.toLowerCase().replaceAll("\\s", "").split(",");
				for(String whitelistUser : listUsers) whitelistUserSet.add(whitelistUser);
			}
			
			bigUserString = ownerConfig.getString("blacklist.users");
			if(bigUserString!=null) {
				listUsers = bigUserString.toLowerCase().replaceAll("\\s","").split(",");
				for(String blacklistUser : listUsers){
					if(!whitelistUserSet.contains(blacklistUser)) blacklistUserSet.add(blacklistUser);
					else {
						this.getLogger().warning(blacklistUser + " found in both white and blacklist. Removing from both!");
						whitelistUserSet.remove(blacklistUser);
					}
				}
			}
			pvpEnabled = ownerConfig.getBoolean("pvp",this.getConfigMap().get("defaultPvpOn"));
			defaultUseBlacklist = ownerConfig.getBoolean("usingBlacklist", this.getConfigMap().get("defaultUseBlacklist"));
			int maxTurretsAllowed = ownerConfig.getInt("maxTurretsAllowed", maxTurretsPerPlayer);
			if(owner.equals("global")) {
				globalWhitelist = whitelistUserSet;
				maxTurretsAllowed = 1000;	
			}
			TurretOwner turretOwner = new TurretOwner(this, owner, maxTurretsAllowed, whitelistUserSet, blacklistUserSet, defaultUseBlacklist, pvpEnabled);
			turretOwners.put(owner, turretOwner);
		}
	}
	
	private void saveTurretOwners() {	
		//TODO: same thing as loadWBlists().
		Set<String> ownerList = turretOwners.keySet();
		Set<String> whitelistUserSet;
		Set<String> blacklistUserSet;
		for(String owner : ownerList) {
			ConfigurationSection ownerConfig = turretOwnersFC.createSection(owner);
			whitelistUserSet = turretOwners.get(owner).getWhitelist();
			blacklistUserSet = turretOwners.get(owner).getBlacklist();
			String whitelistUserString = "";
			String blacklistUserString = "";
			if(whitelistUserSet.size() > 0) {
				for(String whitelistedUser : whitelistUserSet) {
					whitelistUserString = whitelistUserString.concat(whitelistedUser + ", ");
				}
				whitelistUserString = whitelistUserString.substring(0, whitelistUserString.lastIndexOf(", ")).toLowerCase();
				ownerConfig.set("whitelist.users", whitelistUserString);
			}else ownerConfig.set("whitelist.users", null);
			
			if(blacklistUserSet.size() > 0) {
				for(String blacklistedUser : blacklistUserSet) {
					blacklistUserString = blacklistUserString.concat(blacklistedUser + ", ");
				}
				blacklistUserString = blacklistUserString.substring(0, blacklistUserString.lastIndexOf(", ")).toLowerCase();
				ownerConfig.set("blacklist.users", blacklistUserString);
			}else ownerConfig.set("blacklist.users", null);
			ownerConfig.set("pvp", turretOwners.get(owner).isPvpEnabled());
			ownerConfig.set("usingBlacklist", turretOwners.get(owner).isUsingBlacklist());
			ownerConfig.set("maxTurretsAllowed", turretOwners.get(owner).getMaxTurretsAllowed());
		}
	}
	
	private void loadConfigOptions(Configuration config, Logger logger) {
		booleanConfigMap.put("activeOnCreate", true);
		booleanConfigMap.put("allowAllToMan", false);
		booleanConfigMap.put("allowAllToChangeAmmo", false);
		booleanConfigMap.put("allowAllToAddAmmoBox", false);
		booleanConfigMap.put("allowAllToDestroy", false);
		booleanConfigMap.put("allowAllToModActivate", false);
		booleanConfigMap.put("pickupUnlimArrows", false);
		booleanConfigMap.put("pickupAmmoArrows", true);
		booleanConfigMap.put("defaultPvpOn", false);
		booleanConfigMap.put("defaultUseBlacklist", true);
		booleanConfigMap.put("defaultUseAmmoBox", true);
		String configMapKey;	
		for(int i=0; i< booleanConfigMap.size(); i++) {
			configMapKey = booleanConfigMap.keySet().toArray()[i].toString();
			if(config.get(configMapKey,null)!=null){
				booleanConfigMap.put(configMapKey, config.getBoolean(configMapKey));
			}else config.set(configMapKey, booleanConfigMap.get(configMapKey));
		}
		if(config.get("maxTurretsPerPlayer",null)!=null) this.setMaxTurretsPerPlayer(config.getInt("maxTurretsPerPlayer"));
		else{
			config.set("maxTurretsPerPlayer", 12);
			logger.warning("Couldn't find maxTurretsPerPlayer. Setting to default: 12");
		}
    }
	
//	public OwnerWBlists getOwnerWBlists(String ownerKey) {
//		return ownerWBlistsMap.get(ownerKey.toLowerCase());
//	}
//	
//	public void addToOwnerWBlists(String ownerKey) {
//		this.ownerWBlistsMap.put(ownerKey.toLowerCase(), new OwnerWBlists(ownerKey, null, null, this.getConfigMap().get("defaultUseBlacklist"), this.getConfigMap().get("defaultPvpOn")));
//	}
//	
//	public void addToOwnerWBlists(String ownerKey, OwnerWBlists ownerWBlists) {
//		this.ownerWBlistsMap.put(ownerKey.toLowerCase(), ownerWBlists);
//	}
//	
//	public boolean removeFromOwnerWBlists(String ownerKey) {
//		if(ownerWBlistsMap.containsKey(ownerKey.toLowerCase())) {
//			this.ownerWBlistsMap.remove(ownerKey.toLowerCase());
//			return true;
//		}
//		else return false;
//	}
}