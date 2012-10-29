//TODO: auto-save turrets every 5 minutes or so, make turrets freezable
//TODO: handle removal of minecart entity more cleanly

package me.azazad.turrets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.bukkit.Server;
import org.bukkit.block.Chest;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
//import org.bukkit.plugin.RegisteredServiceProvider;
//import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TurretsPlugin extends JavaPlugin{
    private static final String TURRET_DB_FILENAME = "turrets.yml";
    private static final String OWNER_DB_FILENAME = "ownerWhitelists.yml";
    private static File ownerWhitelistsFile;
    public static final List<Material> POST_MATERIALS = new ArrayList<Material>();
    public List<PlayerCommandSender> playerCommanders = new ArrayList<PlayerCommandSender>();
    
    public static Logger globalLogger;
    private Map<String,Boolean> configMap = new HashMap<String,Boolean>();
    
    public PluginDescriptionFile pdf;
    private final UpgradeLadder upgradeLadder = new UpgradeLadder();
    private TurretDatabase turretDatabase;
    private List<Material> boxAmmoTypes = new ArrayList<Material>();
    private List<Material> unlimitedAmmoTypes = new ArrayList<Material>();
    private final List<TargetAssessor> targetAssessors = new ArrayList<TargetAssessor>();
    private final Map<BlockLocation,Turret> turrets = new HashMap<BlockLocation,Turret>();
    private final Map<Player,TurretOwner> turretOwners = new HashMap<Player,TurretOwner>();
    private final Collection<Turret> unmodifiableTurrets = Collections.unmodifiableCollection(turrets.values());
    private int maxTurretsPerPlayer;
    
//    private Permission permissionsProvider;
    
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
        
        //register listeners
        pluginManager.registerEvents(new TurretsListener(this),this);
        
        //register commands
        getCommand("turrets").setExecutor(new TurretsCommand(this));
        
        //initialize post_material types
        POST_MATERIALS.add(Material.FENCE);
        POST_MATERIALS.add(Material.IRON_FENCE);
        POST_MATERIALS.add(Material.NETHER_FENCE);
        
        
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
        this.saveConfig();
    }
	
	private void loadConfigOptions(Configuration config, Logger logger) {
		configMap.put("activeOnCreate", true);
		configMap.put("allowAllToMan", false);
		configMap.put("allowAllToChangeAmmo", false);
		configMap.put("allowAllToAddAmmoBox", false);
		configMap.put("allowAllToDestroy", false);
		configMap.put("allowAllToModActivate", false);
		configMap.put("pickupUnlimArrows", false);
		configMap.put("pickupAmmoArrows", true);
		String configMapKey;
		for(int i=0; i< configMap.size(); i++) {
			configMapKey = configMap.keySet().toArray()[i].toString();
			if(config.get(configMapKey,null)!=null){
				configMap.put(configMapKey, config.getBoolean(configMapKey));
			}
		}
		if(config.get("maxTurretsPerPlayer",null)!=null) this.setMaxTurretsPerPlayer(config.getInt("maxTurretsPerPlayer"));
		else{
			config.set("maxTurretsPerPlayer", 12);
			logger.warning("Couldn't find maxTurretsPerPlayer. Setting to default: 12");
		}
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
            if(!turrets.containsKey(turret.getLocation())){
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
		return this.configMap;
	}

	public int getMaxTurretsPerPlayer() {
		return maxTurretsPerPlayer;
	}

	public void setMaxTurretsPerPlayer(int maxTurretsPerPlayer) {
		this.maxTurretsPerPlayer = maxTurretsPerPlayer;
	}
	
	public Map<Player,TurretOwner> getTurretOwners() {
		return this.turretOwners;
	}
	
	public void reloadPlugin() {
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
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Reloaded "+ ChatColor.GRAY + getDescription().getFullName());
	}
}