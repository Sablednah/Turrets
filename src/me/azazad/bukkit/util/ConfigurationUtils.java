package me.azazad.bukkit.util;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public final class ConfigurationUtils{
    private ConfigurationUtils(){}
    
    public static Location getLocation(String string,ConfigurationSection section,Server server){
        World world = server.getWorld(section.getString(string+".world"));
        double x = section.getDouble(string+".x");
        double y = section.getDouble(string+".y");
        double z = section.getDouble(string+".z");
        return new Location(world,x,y,z);
    }
    
    public static void setLocation(String string,Location location,ConfigurationSection section){
        section.set(string+".world",location.getWorld().getName());
        section.set(string+".x",location.getX());
        section.set(string+".y",location.getY());
        section.set(string+".z",location.getZ());
    }
}