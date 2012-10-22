package me.azazad.bukkit.util;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class BlockLocation{
    private final World world;
    private final int x,y,z;
    
    public BlockLocation(Location location){
        this(location.getWorld(),location.getBlockX(),location.getBlockY(),location.getBlockZ());
    }
    
    public BlockLocation(World world,int x,int y,int z){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public World getWorld(){
        return world;
    }
    
    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }
    
    public int getZ(){
        return z;
    }
    
    public Location getLocation(){
        return new Location(world,x,y,z);
    }
    
    public static BlockLocation loadFromConfigSection(ConfigurationSection section,String path,Server server){
        World world = server.getWorld(section.getString(path+".world"));
        int x = section.getInt(path+".x");
        int y = section.getInt(path+".y");
        int z = section.getInt(path+".z");
        return new BlockLocation(world,x,y,z);
    }
    
    public void saveToConfigSection(ConfigurationSection section,String path){
        section.set(path+".world",world.getName());
        section.set(path+".x",x);
        section.set(path+".y",y);
        section.set(path+".z",z);
    }
    
    public static BlockLocation fromLocation(Location location){
        return new BlockLocation(location);
    }
    
    @Override
    public boolean equals(Object object){
        if(object == null){return false;}
        if(object == this){return true;}
        if(object.getClass() != getClass()){return false;}
        
        BlockLocation bl = (BlockLocation)object;
        return (bl.world != null ? bl.world.equals(this.world) : bl.world == this.world) && bl.x == this.x && bl.y == this.y && bl.z == this.z;
    }
    
    @Override
    public int hashCode(){
        int hash = 7;
        hash = 41 * hash + (this.world != null ? this.world.hashCode() : 0);
        hash = 41 * hash + this.x;
        hash = 41 * hash + this.y;
        hash = 41 * hash + this.z;
        return hash;
    }

	public static BlockLocation loadFromConfigSection(ConfigurationSection section, Server server) {
		World world = server.getWorld(section.getString(".world"));
        int x = section.getInt(".x");
        int y = section.getInt(".y");
        int z = section.getInt(".z");
        return new BlockLocation(world,x,y,z);
	}
}