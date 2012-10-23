package me.azazad.turrets.upgrade;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class UpgradeLadder{
    private static final String TIERS_PATH = "tiers";
    private static final String DEFAULT_TIER_NAME = "default";
    private static final String FIRING_INTERVAL_PATH = "firingInterval";
    private static final String RANGE_PATH = "range";
    private static final String ACCURACY_PATH = "accuracy";
    
    
    private final Map<Material,UpgradeTier> upgradeTiers = new EnumMap<Material,UpgradeTier>(Material.class);
    private UpgradeTier defaultUpgradeTier;
    
    public UpgradeLadder(){
        
    }
    
    public UpgradeTier getUpgradeTier(Material material){
        if(upgradeTiers.containsKey(material)){
            return upgradeTiers.get(material);
        }else{
            return defaultUpgradeTier;
        }
    }
    
    public void loadUpgradeTiers(Configuration config,Logger logger){
        upgradeTiers.clear();
        defaultUpgradeTier = null;
        
        ConfigurationSection tierNodes = config.getConfigurationSection(TIERS_PATH);
        Set<String> tierKeys = tierNodes.getKeys(false);
        
        for(String tierKey : tierKeys){
            ConfigurationSection tierNode = tierNodes.getConfigurationSection(tierKey);
            
            Material material;
            if(tierKey.equalsIgnoreCase(DEFAULT_TIER_NAME)){
                material = null;
            }else{
                material = Material.matchMaterial(tierKey);
                
                if(material == null || !material.isBlock()){
                    logger.warning("Invalid tier \""+tierKey+"\", must be the name of a block. Skipping.");
                    continue;
                }
            }
            
            int firingInterval = tierNode.getInt(FIRING_INTERVAL_PATH);
            if(firingInterval < 1){
                logger.warning("Invalid firing interval \""+firingInterval+"\", must be at least 1. Using default value.");
                firingInterval = 20;
            }
            
            double range = tierNode.getDouble(RANGE_PATH);
            if(range <= 0){
                logger.warning("Invalid range \""+range+"\", must be positive. Using default value.");
                range = 10.0;
            }
            //TODO: disallow outrageous values for range
            
            float accuracy = (float)tierNode.getDouble(ACCURACY_PATH);
            if(accuracy < 0){
                logger.warning("Invalid accuracy \""+accuracy+"\", must be at least 0. Using default value.");
                accuracy = 1.0f;
            }
            
            UpgradeTier upgradeTier = new UpgradeTier(firingInterval,range,accuracy);
            
            if(material != null){
                upgradeTiers.put(material,upgradeTier);
            }else{
                defaultUpgradeTier = upgradeTier;
            }
        }
        
        if(defaultUpgradeTier == null){
            logger.warning("No default upgrade tier, creating one.");
            defaultUpgradeTier = new UpgradeTier(40,10.0,3.0f);//TODO: make this come from the default config.yml
        }
    }
}