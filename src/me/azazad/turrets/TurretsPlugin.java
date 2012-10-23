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
//import net.milkbowl.vault.permission.Permission;
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
    //public static final Material POST_MATERIAL = Material.FENCE;
    public static final List<Material> POST_MATERIALS = new ArrayList<Material>();
    public List<PlayerCommandSender> playerCommanders = new ArrayList<PlayerCommandSender>();
    
    public static final String PERM_TURRET_CREATE = "turrets.create";
    public static final String PERM_TURRET_DESTROY = "turrets.destroy";
    public static final String PERM_ADMIN = "turrets.admin";
    public static Logger globalLogger;
    public boolean activeOnCreate = true;
    public boolean allowAllToMan = false;
    public boolean allowAllToChangeAmmo = false;
    public boolean allowAllToAddAmmoBox = false;
    public boolean allowAllToDestroy = false;
    public boolean allowAllToModActivate = false;
    
    public PluginDescriptionFile pdf;
    private final UpgradeLadder upgradeLadder = new UpgradeLadder();
    private TurretDatabase turretDatabase;
    private final List<TargetAssessor> targetAssessors = new ArrayList<TargetAssessor>();
    private final Map<BlockLocation,Turret> turrets = new HashMap<BlockLocation,Turret>();
    private final Collection<Turret> unmodifiableTurrets = Collections.unmodifiableCollection(turrets.values());
    
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
        logger.info("Upgrade tiers loaded.");
        
        //load providers
//        ServicesManager servicesManager = server.getServicesManager();
//        
//        RegisteredServiceProvider<Permission> permissionsRSP = servicesManager.getRegistration(Permission.class);
//        if(permissionsRSP != null){
//            permissionsProvider = permissionsRSP.getProvider();
//        }
//        
//        if(permissionsProvider == null){
//            logger.severe("Failed to integrate with Vault. Are you sure it is installed correctly? Disabling this plugin.");
//            pluginManager.disablePlugin(this);
//            return;
//        }
        
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
    
    private void loadConfigOptions(Configuration config, Logger logger) {
		if (config.getConfigurationSection("activeOnCreate")!=null) this.activeOnCreate = config.getBoolean("activeOnCreate");
		else config.set("activeOnCreate",true);
		if (config.getConfigurationSection("allowAllToMan")!=null) this.allowAllToMan = config.getBoolean("allowAllToMan");
		else config.set("allowAllToMan",false);
		if (config.getConfigurationSection("allowAllToChangeAmmo")!=null) this.allowAllToChangeAmmo = config.getBoolean("allowAllToChangeAmmo");
		else config.set("allowAllToChangeAmmo",false);
		if (config.getConfigurationSection("allowAllToAddAmmoBox")!=null) this.allowAllToAddAmmoBox = config.getBoolean("allowAllToAddAmmoBox");
		else config.set("allowAllToAddAmmoBox",false);
		if (config.getConfigurationSection("allowAllToDestroy")!=null) this.allowAllToDestroy = config.getBoolean("allowAllToDestroy");
		else config.set("allowAllToDestroy",false);
		if (config.getConfigurationSection("allowAllToModActivate")!=null) this.allowAllToModActivate = config.getBoolean("allowAllToModActivate");
		else config.set("allowAllToModActivate",false);
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
    		if (cur_turr.getEntity().getShooter()!=null) {
	    		if (cur_turr.getEntity().getShooter().getPlayer().equals(p)) {
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
        Collection<Turret> dbTurrets = turretDatabase.loadTurrets();
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
}