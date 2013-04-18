package me.azazad.turrets;

import java.util.List;
import me.azazad.turrets.nms.EntityRotatingTurret;
import me.azazad.turrets.targeting.TargetAssessor;
import me.azazad.turrets.upgrade.UpgradeTier;
import me.azazad.bukkit.util.BlockLocation;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Turret{
	private boolean usesAmmoBox;
    private final BlockLocation location;
    private String ownerName;
    private final TurretsPlugin plugin;
    private EntityRotatingTurret entity;
    private UpgradeTier upgradeTier;
    private TurretAmmoBox turretAmmoBox;
    private boolean playerControl = false;
    private TurretShooter shooter = null;
    private boolean isActive;
	private Material unlimitedAmmoType = Material.ARROW;
	private TurretOwner turretOwner;
    
    public Turret(BlockLocation location,Player owner,TurretsPlugin plugin, boolean useAmmoBox){
        this(location,owner.getName(),plugin,useAmmoBox);
    }
    
    public Turret(BlockLocation location,String ownerName,TurretsPlugin plugin,boolean useAmmoBox){
        this.location = location;
        this.ownerName = ownerName;
        this.turretOwner = plugin.getTurretOwner(ownerName);
        this.plugin = plugin;
        this.turretAmmoBox = new TurretAmmoBox();
        this.usesAmmoBox = useAmmoBox;
        this.entity = new EntityRotatingTurret(this,location.getWorld(),location.getX() + 0.5,location.getY() + 1.3,location.getZ() + 0.5);
        initializeUpgradeTier();
    }
    
    public BlockLocation getBlockLocation(){
        return location;
    }
    
    public String getOwnerName(){
        return ownerName;
    }
    
    public EntityRotatingTurret getEntity() {
        return entity;
    }
    
    public UpgradeTier getUpgradeTier(){
        return upgradeTier;
    }
    
    public List<TargetAssessor> getTargetAssessors(){
        return getPlugin().getTargetAssessors();
    }
    
    public void spawn(){
        ((CraftWorld)location.getWorld()).getHandle().addEntity(entity);
    }
    
    public void despawn(){
        ((CraftWorld)location.getWorld()).getHandle().removeEntity(entity);
    }
    
    public void remove(){
        getPlugin().removeTurret(this);
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
        upgradeTier = getPlugin().getUpgradeLadder().getUpgradeTier(material);
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
		int turretBlockX = this.getBlockLocation().getX();
		int turretBlockY = this.getBlockLocation().getY();
		int turretBlockZ = this.getBlockLocation().getZ();
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

	public TurretsPlugin getPlugin() {
		return plugin;
	}
	
	private void setPlayerControl(boolean state) {
    	this.playerControl = state;
    }
    
    public boolean getPlayerControl() {
    	return(this.playerControl);
    }
    
    public void attachShooter(TurretShooter shooter) {
    	this.shooter = shooter;
    	setPlayerControl(true);
    }
    
    public void detachShooter() {
    	this.shooter = null;
    	setPlayerControl(false);
    }
    
    public TurretShooter getShooter() {
    	return(this.shooter);
    }
    
    public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setUnlimitedAmmoType(Material material) {
		this.unlimitedAmmoType  = material;
	}
	
	public Material getUnlimitedAmmoType() {
		return this.unlimitedAmmoType;
	}
	
	public TurretOwner getTurretOwner() {
		return this.turretOwner;
	}
}