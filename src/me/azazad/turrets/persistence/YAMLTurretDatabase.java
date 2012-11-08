package me.azazad.turrets.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import me.azazad.bukkit.util.BlockLocation;
import me.azazad.turrets.Turret;
import me.azazad.turrets.TurretOwner;
import me.azazad.turrets.TurretsPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class YAMLTurretDatabase implements TurretDatabase{
    private static final String TURRETS_PATH = "turrets";
    private static final String LOCATION_PATH = "location";
    private static final String OWNER_PATH = "owner";
    @SuppressWarnings("unused")
	private static final String RELOAD_SHOOTER_PATH = "shooter";
    private static final String AMMO_BOX_PATH = "ammobox";
    private static final String USES_AMMO_PATH = "usesAmmo";
    private static final String UNLIM_AMMO_TYPE = "unlimammotype";
    
    private final File file;
    private final TurretsPlugin plugin;
    private final YamlConfiguration backing;
    
    public YAMLTurretDatabase(File file,TurretsPlugin plugin){
        this.file = file;
        this.plugin = plugin;
        this.backing = new YamlConfiguration();
    }
    
    @Override
    public Collection<Turret> loadTurrets() throws IOException{
        if(!file.exists()){
        	file.getParentFile().mkdirs();
        }
        
        try{
            backing.load(file);
        }catch(InvalidConfigurationException e){
            throw new IOException("Backing file is corrupt.");
        }
        
        //ConfigurationSection turretSections = backing.createSection(TURRETS_PATH);
        ConfigurationSection turretSections = backing.getConfigurationSection(TURRETS_PATH);
        //CULPRIT RIGHT HUR. was create, now is get
        
        Set<String> turretIDs = turretSections.getKeys(false);
        List<Turret> turrets = new ArrayList<Turret>();
        Server server = plugin.getServer();
        for(String turretID : turretIDs){
            ConfigurationSection turretSection = turretSections.getConfigurationSection(turretID);
            BlockLocation location = BlockLocation.loadFromConfigSection(turretSection,LOCATION_PATH,server);
            String ownerName = turretSection.getString(OWNER_PATH);
            Turret turret = new Turret(location,ownerName,plugin,turretSection.getBoolean(USES_AMMO_PATH));
            turrets.add(turret);
            plugin.getTurretOwner(turret.getOwnerName()).addTurretOwned(turret);
            //if there is an ammobox for this turret
            if (turretSection.getKeys(false).contains(AMMO_BOX_PATH)) {
            	ConfigurationSection chestSections = turretSection.getConfigurationSection(AMMO_BOX_PATH);
            	Set<String> chestIDs = chestSections.getKeys(false);
            	for (String chestID: chestIDs) {
            		ConfigurationSection chestSection = chestSections.getConfigurationSection(chestID);
            		BlockLocation chestLocation = BlockLocation.loadFromConfigSection(chestSection,server);
            		turret.getTurretAmmoBox().addAmmoChest(chestLocation.getLocation().getBlock());
            	}
            }
            if(!turret.getUsesAmmoBox()) turret.setUnlimitedAmmoType(Material.getMaterial(turretSection.getString(UNLIM_AMMO_TYPE)));
        }
        
        return turrets;
    }
    
    @Override
    public void saveTurrets(Collection<Turret> turrets) throws IOException{
        ConfigurationSection turretSections = backing.createSection(TURRETS_PATH);
        int id = 0;
        for(Turret turret : turrets){
        	TurretOwner turretOwner = plugin.getTurretOwner(Bukkit.getPlayer(turret.getOwnerName()));
            if(turretOwner!=null) turretOwner.removeTurretOwned(turret);
            ConfigurationSection turretSection = turretSections.createSection("t"+id);
            turret.getBlockLocation().saveToConfigSection(turretSection,LOCATION_PATH);
            turretSection.set(OWNER_PATH,turret.getOwnerName());
            turretSection.set(USES_AMMO_PATH, turret.getUsesAmmoBox());
	        if (turret.getTurretAmmoBox().getAmmoChestNum() > 0) {
	        	int chestid = 0;
	        	for(Chest chest : turret.getTurretAmmoBox().getMap().values()) {
	            	(BlockLocation.fromLocation(chest.getLocation())).saveToConfigSection(turretSection,AMMO_BOX_PATH + ".ammoChest" + chestid);
	            	chestid++;
	        	}
	        }
	        if(!turret.getUsesAmmoBox()) turretSection.set(UNLIM_AMMO_TYPE, turret.getUnlimitedAmmoType().toString());
            id++;
        }
        
        backing.save(file);
    }
}