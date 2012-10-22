package me.azazad.turrets;

import java.util.List;
import me.azazad.turrets.nms.EntityTurret;
import me.azazad.turrets.targeting.TargetAssessor;
import me.azazad.turrets.upgrade.UpgradeTier;
import me.azazad.bukkit.util.BlockLocation;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Turret{
	public boolean TESTING_GITHUB;
	private boolean usesAmmoBox;
    private final BlockLocation location;
    private String ownerName;
    private final TurretsPlugin plugin;
    private EntityTurret entity;
    private UpgradeTier upgradeTier;
    private TurretAmmoBox turretAmmoBox;
    
    public Turret(BlockLocation location,Player owner,TurretsPlugin plugin){
        this(location,owner.getName(),plugin);
    }
    
    public Turret(BlockLocation location,String ownerName,TurretsPlugin plugin){
        this.location = location;
        this.ownerName = ownerName;
        this.plugin = plugin;
        this.turretAmmoBox = new TurretAmmoBox();
        this.usesAmmoBox = true;
        this.entity = new EntityTurret(this,location.getWorld(),location.getX() + 0.5,location.getY() + 1.3,location.getZ() + 0.5);
        initializeUpgradeTier();
    }
    
    public BlockLocation getLocation(){
        return location;
    }
    
    public String getOwnerName(){
        return ownerName;
    }
    
    public EntityTurret getEntity(){
        return entity;
    }
    
    public UpgradeTier getUpgradeTier(){
        return upgradeTier;
    }
    
    public List<TargetAssessor> getTargetAssessors(){
        return plugin.getTargetAssessors();
    }
    
    public void spawn(){
        ((CraftWorld)location.getWorld()).getHandle().addEntity(entity);
    }
    
    public void despawn(){
        ((CraftWorld)location.getWorld()).getHandle().removeEntity(entity);
    }
    
    public void remove(){
        plugin.removeTurret(this);
    }
    
    public void fireItemStack(ItemStack itemStack,float accuracy){
        entity.fireItemStack(itemStack,accuracy);
    }
    
    public UpgradeTier updateUpgradeTier(){
        Block baseBlock = location.getWorld().getBlockAt(location.getX(),location.getY() - 1,location.getZ());
        if(baseBlock != null){
            updateUpgradeTier(baseBlock.getType());
        }
        
        return upgradeTier;
    }
    
    public UpgradeTier updateUpgradeTier(Material material){
        upgradeTier = plugin.getUpgradeLadder().getUpgradeTier(material);
        return upgradeTier;
    }
    
    private void initializeUpgradeTier(){
        updateUpgradeTier();
    }
    
    @Override
    public boolean equals(Object object){
        if(object == null){return false;}
        if(object == this){return true;}
        if(object.getClass() != getClass()){return false;}
        
        Turret turret = (Turret)object;
        return turret.location.equals(this.location);
    }
    
    @Override
    public int hashCode(){
        int hash = 5;
        hash = 61 * hash + (this.location != null ? this.location.hashCode() : 0);
        return hash;
    }
    
    public boolean addTurretAmmoBoxChest(Block chest) {
    	if (this.turretAmmoBox.getAmmoChestNum() < 4 && this.turretAmmoBox.addAmmoChest(chest)) {
    		return(true);
    	} else {
    		return(false);
    	}
    }
    
    public TurretAmmoBox getTurretAmmoBox() {
    	return this.turretAmmoBox;
    }
    
    public boolean getUsesAmmoBox() {
    	return this.usesAmmoBox;
    }
    
    public void setUsesAmmoBox(boolean state) {
    	this.usesAmmoBox = state;
    }

	public boolean checkIfBlockByTurret(Block clickedBlock) {
		int turretBlockX = this.getLocation().getX();
		int turretBlockY = this.getLocation().getY();
		int turretBlockZ = this.getLocation().getZ();
		int clickedBlockX = clickedBlock.getX();
		int clickedBlockY = clickedBlock.getY();
		int clickedBlockZ = clickedBlock.getZ();
		if (clickedBlockY == turretBlockY) {
			if (clickedBlockX >= turretBlockX-1 && clickedBlockX <= turretBlockX+1 && clickedBlockZ >= turretBlockZ-1 && clickedBlockZ <= turretBlockZ+1) {
				if (!(clickedBlockX == turretBlockX && clickedBlockZ == turretBlockZ)) {
					return true;
				}else return false;
			}else return false;
		} else return false;
	}
}