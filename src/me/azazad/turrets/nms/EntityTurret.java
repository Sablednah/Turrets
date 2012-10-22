package me.azazad.turrets.nms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.azazad.turrets.Turret;
import me.azazad.turrets.TurretAmmoBox;
import me.azazad.turrets.TurretShooter;
import me.azazad.turrets.targeting.TargetAssessment;
import me.azazad.turrets.targeting.TargetAssessor;
import me.azazad.turrets.upgrade.UpgradeTier;
import me.azazad.util.RandomUtils;
import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityEgg;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityPotion;
import net.minecraft.server.EntitySmallFireball;
import net.minecraft.server.EntitySnowball;
import net.minecraft.server.EntityThrownExpBottle;
import net.minecraft.server.ItemMonsterEgg;
import net.minecraft.server.Vec3D;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.util.NumberConversions;

public class EntityTurret extends net.minecraft.server.EntityMinecart{
    private static final double REBOUND = 0.1;
    private static final double ITEM_SPAWN_DISTANCE = 1.2;
    
    private final Turret turret;
    @SuppressWarnings("unused")
	private final World bukkitWorld;
    private final double pivotX,pivotY,pivotZ;
    private Entity target;
    private int firingCooldown = 0;
    private int targetSearchCooldown = 0;
    
    //private int firingInterval = 40;
    private int targetSearchInterval = 10;
    //private double range = 10.0;
    //private float accuracy = 1.0f;//default 6.0f
    
    private int turretLookMatchShooterCD = 10;
    private boolean playerControl = false;
    private TurretShooter shooter = null;
    
    
    public EntityTurret(Turret turret,World world,double pivotX,double pivotY,double pivotZ){
        super(((CraftWorld)world).getHandle());
        this.turret = turret;
        this.bukkitWorld = world;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
        setPosition(this.pivotX,this.pivotY,this.pivotZ);
    }
    
    public Turret getTurret(){
        return turret;
    }
    
    public boolean damageEntity(DamageSource damageSource,int damage){
        net.minecraft.server.Entity nmsDamager = damageSource.getEntity();
        
        if(nmsDamager != null){
            Entity damager = nmsDamager.getBukkitEntity();
            
            if(damager instanceof LivingEntity){
                return super.damageEntity(damageSource,damage);
            }else{
                return true;
            }
        }else{
            return super.damageEntity(damageSource,damage);
        }
    }
    
    @Override
    public boolean c(EntityHuman entityhuman) {
    	return true;
    }
    
    @Override
    public void h_(){
        if(this.j() > 0){
            this.h(this.j() - 1);
        }
        
        if(this.getDamage() > 0){
            this.setDamage(this.getDamage() - 1);
        }
        
        if(this.locY < -64.0D){
            this.C();
        }
        
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        
        //Movement
        this.motX = (this.pivotX - this.locX) * REBOUND;
        this.motY = (this.pivotY - this.locY) * REBOUND;
        this.motZ = (this.pivotZ - this.locZ) * REBOUND;
        this.move(this.motX,this.motY,this.motZ);
        
        //Air resistance
        /*this.motX *= 0.99f;
        this.motY *= 0.99f;
        this.motZ *= 0.99f;*/
        
        UpgradeTier upgradeTier = turret.getUpgradeTier();
        int firingInterval = upgradeTier.getFiringInterval();
        double range = upgradeTier.getRange();
        float accuracy = upgradeTier.getAccuracy();
        boolean lockedOn = false;
        //If not currently targeting an entity, find a suitable one
        if(!getPlayerControl()) {
	        if(target == null){
	            if(targetSearchCooldown == 0){
	                Entity foundTarget = findTarget(range);
	                
	                if(foundTarget != null){
	                    target = foundTarget;
	                }else{
	                    targetSearchCooldown = targetSearchInterval;
	                }
	            }
	        }
	        
	        if(targetSearchCooldown > 0){
	            targetSearchCooldown--;
	        }
	        
	        lockedOn = false;
	        
	        //Track target
	        if(target != null){
	            net.minecraft.server.Entity nmsTarget = ((CraftEntity)target).getHandle();
	            
	            if(canSee(nmsTarget)){
	                net.minecraft.server.World targetWorld = nmsTarget.world;
	                double x = nmsTarget.locX;
	                double y = nmsTarget.locY + nmsTarget.getHeadHeight();
	                double z = nmsTarget.locZ;
	                
	                if(targetWorld == this.world && !target.isDead()){
	                    double dx = x - pivotX;
	                    double dy = y - pivotY;
	                    double dz = z - pivotZ;
	                    double distanceSquared = dx * dx + dy * dy + dz * dz;
	                    
	                    
	                    
	                    if(distanceSquared <= range * range){
	                        lookAt(x,y,z);
	                        lockedOn = true;
	                    }else{
	                        target = null;
	                    }
	                }else{
	                    target = null;
	                }
	            }else{
	                target = null;
	            }
	        }
        }
        else {
        	if(turretLookMatchShooterCD == 0) {
	        	double dist = 4.0;
	        	Location ploc = this.getShooter().getPlayer().getEyeLocation();
	        	float plocPitch = ploc.getPitch();
	        	float plocYaw = ploc.getYaw();
	        	double yawInRad = ((double)plocYaw)*Math.PI/180;
	        	double pitchP90inRad = ((double)plocPitch+90)*Math.PI/180;
	        	double plookX = ploc.getX() + dist*Math.sin((double)(-yawInRad+2*Math.PI))*Math.sin(pitchP90inRad);
	        	double plookY = ploc.getY() + dist*Math.cos(pitchP90inRad);
	        	double plookZ = ploc.getZ() + dist*Math.cos(yawInRad)*Math.sin(pitchP90inRad);
	        	//Bukkit.broadcastMessage("x= " + plookX + ", y=" + plookY + ", z=" + plookZ);
	        	lookAt(plookX,plookY,plookZ);
	        	turretLookMatchShooterCD = 10;
        	} else turretLookMatchShooterCD--;
        }
        
        
        this.b(this.yaw,this.pitch);
        //**************************Firing check*******************************/
	    if(getPlayerControl()) {
	    	//check if shooter tried to shoot since last cooldown
	    	if(shooter.didShooterClick() && firingCooldown == 0) {
	    		fireItemStack(accuracy);
	    		firingCooldown = firingInterval*4/5;
	    		shooter.setClickedFlag(false);
	    	}
	    } else {
	    	//Fire item if locked onto target
        	if(lockedOn && firingCooldown == 0){
	            fireItemStack(accuracy);
	            firingCooldown = firingInterval;
	        }
    	}
        
        if(firingCooldown > 0){
            firingCooldown--;
        }
        
        /*if (this.passenger != null && this.passenger.dead) {
            if (this.passenger.vehicle == this) {
                this.passenger.vehicle = null;
            }

            this.passenger = null;
        }*/
    }
    
    public void lookAt(double x,double y,double z){
        double dx = x - this.locX;
        double dy = y - this.locY;
        double dz = z - this.locZ;
        double dh = Math.sqrt(dx * dx + dz * dz);
        this.yaw = ((float)Math.atan2(dz,dx) * 180F / (float)Math.PI);
        this.pitch = ((float)-Math.atan(dy / dh) * 180F / (float)Math.PI);
    }
    
    @SuppressWarnings("unchecked")
	public Entity findTarget(double range){
        List<net.minecraft.server.Entity> nmsEntities = world.getEntities(this,this.boundingBox.grow(range,range,range));
        List<LivingEntity> targets = new ArrayList<LivingEntity>();
        double rangeSquared = range * range;
        
        for(net.minecraft.server.Entity nmsEntity : nmsEntities){
            if(nmsEntity == this){
                continue;
            }
            
            double dx = nmsEntity.locX - this.locX;
            double dy = nmsEntity.locY - this.locY;
            double dz = nmsEntity.locZ - this.locZ;
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            
            if(distanceSquared <= rangeSquared){
                Entity entity = nmsEntity.getBukkitEntity();
                if(entity instanceof LivingEntity){
                    targets.add((LivingEntity)entity);
                }
            }
        }
        
        if(targets.isEmpty()){
            return null;
        }
        
        filterTargets(targets);
        
        while(!targets.isEmpty()){
            LivingEntity possibleTarget = RandomUtils.randomElement(targets,random);
            net.minecraft.server.Entity nmsPossibleTarget = ((CraftEntity)possibleTarget).getHandle();
            
            if(canSee(nmsPossibleTarget)){
                return possibleTarget;
            }else{
                targets.remove(possibleTarget);
            }
        }
        
        return null;
    }
    
    private void filterTargets(List<LivingEntity> targets){
        Iterator<LivingEntity> it = targets.iterator();
        while(it.hasNext()){
            LivingEntity mob = it.next();
            TargetAssessment assessment = assessTarget(mob);
            
            if(assessment != TargetAssessment.HOSTILE){
                it.remove();
            }
        }
    }
    
    private TargetAssessment assessTarget(LivingEntity mob){
        TargetAssessment overallAssessment = TargetAssessment.MEH;
        
        for(TargetAssessor assessor : turret.getTargetAssessors()){
            TargetAssessment assessment = assessor.assessMob(mob);
            
            if(assessment != TargetAssessment.MEH){
                overallAssessment = assessment;
            }
        }
        
        return overallAssessment;
    }
    
    public void fireItemStack(float accuracy){
    	if (this.getTurret().getUsesAmmoBox()) {
    		TurretAmmoBox ammoBox = this.getTurret().getTurretAmmoBox();
    		if (ammoBox.getAmmoChestNum() > 0) {
    			for (Chest chest : ammoBox.getMap().values()) {
    				if (chest.getInventory().contains(Material.ARROW)) {
    					ItemStack[] chestInv = chest.getInventory().getContents();
    					for (ItemStack item : chestInv) {
    						if (item!=null && item.getType().equals(Material.ARROW)) {
    							if (item.getAmount() > 1) {
    								item.setAmount(item.getAmount()-1);
    							} else {
    								item.setType(Material.AIR);
    							}
    							fireItemStack(new ItemStack(Material.ARROW,1),accuracy);
    							break;
    						}
    					}
    					break;
    				}
    			}
    		}
    	} else {
	        ItemStack itemStack = new ItemStack(Material.ARROW,1);
	        fireItemStack(itemStack,accuracy);
    	}
    }
    
    public void fireItemStack(ItemStack itemStack,float accuracy){
        int blockX = NumberConversions.floor(this.locX);
        int blockY = NumberConversions.floor(this.locY);
        int blockZ = NumberConversions.floor(this.locZ);
        
        if(itemStack != null){
            double rYaw = (yaw - 90.0) * Math.PI / 180.0;
            double rPitch = pitch * Math.PI / 180.0;
            
            double factorX = Math.sin(rYaw) * -Math.cos(rPitch);
            double factorY = -Math.sin(rPitch);
            double factorZ = Math.cos(rYaw) * Math.cos(rPitch);
            
            double itemX = this.locX + ITEM_SPAWN_DISTANCE * factorX;
            double itemY = this.locY + ITEM_SPAWN_DISTANCE * factorY;
            double itemZ = this.locZ + ITEM_SPAWN_DISTANCE * factorZ;
            
            switch(itemStack.getType()){
                case ARROW:
                    EntityArrow entityArrow = new EntityArrow(world,itemX,itemY,itemZ);
                    entityArrow.shoot(factorX,factorY,factorZ,1.1f,accuracy);
                    entityArrow.fromPlayer = 1;
                    world.addEntity(entityArrow);
                    world.triggerEffect(1002,blockX,blockY,blockZ,0);
                    break;
                    
                case EGG:
                    EntityEgg entityEgg = new EntityEgg(world,itemX,itemY,itemZ);
                    entityEgg.c(factorX,factorY,factorZ,1.1f,accuracy);
                    world.addEntity(entityEgg);
                    world.triggerEffect(1002,blockX,blockY,blockZ,0);
                    break;
                    
                case SNOW_BALL:
                    EntitySnowball entitySnowball = new EntitySnowball(world,itemX,itemY,itemZ);
                    entitySnowball.c(factorX,factorY,factorZ,1.1f,accuracy);
                    world.addEntity(entitySnowball);
                    world.triggerEffect(1002,blockX,blockY,blockZ,0);
                    break;
                    
                case POTION:
                    if(Potion.fromItemStack(itemStack).isSplash()){
                        EntityPotion entityPotion = new EntityPotion(world,itemX,itemY,itemZ,itemStack.getDurability());
                        entityPotion.c(factorX,factorY,factorZ,1.375f,accuracy * 0.5f);
                        world.addEntity(entityPotion);
                        world.triggerEffect(1002,blockX,blockY,blockZ,0);
                    }else{
                        tossItemStack(itemStack,accuracy);
                    }
                    break;
                    
                case EXP_BOTTLE:
                    EntityThrownExpBottle entityThrownEXPBottle = new EntityThrownExpBottle(world,itemX,itemY,itemZ);
                    entityThrownEXPBottle.c(factorX,factorY,factorZ,1.375f,accuracy * 0.5f);
                    world.addEntity(entityThrownEXPBottle);
                    world.triggerEffect(1002,blockX,blockY,blockZ,0);
                    break;
                    
                case MONSTER_EGG:
                    ItemMonsterEgg.a(world,itemStack.getDurability(),itemX,itemY,itemZ);
                    world.triggerEffect(1002,blockX,blockY,blockZ,0);
                    break;
                    
                case FIREBALL:
                    EntitySmallFireball entitySmallFireball = new EntitySmallFireball(world,itemX,itemY,itemZ,factorX,factorY,factorZ);
                    world.addEntity(entitySmallFireball);
                    world.triggerEffect(1009,blockX,blockY,blockZ,0);
                    break;
                    
                default:
                    tossItemStack(itemStack,accuracy);
                    break;
            }
            
            world.triggerEffect(2000,blockX,blockY,blockZ,0);
        }else{
            world.triggerEffect(1001,blockX,blockY,blockZ,0);
        }
    }
    
    public void tossItemStack(ItemStack itemStack,float accuracy){
        double rYaw = (yaw - 90.0) * Math.PI / 180.0;
        double rPitch = pitch * Math.PI / 180.0;
        
        double factorX = Math.sin(rYaw) * -Math.cos(rPitch);
        double factorY = -Math.sin(rPitch);
        double factorZ = Math.cos(rYaw) * Math.cos(rPitch);
        
        double itemX = this.locX + ITEM_SPAWN_DISTANCE * factorX;
        double itemY = this.locY + ITEM_SPAWN_DISTANCE * factorY;
        double itemZ = this.locZ + ITEM_SPAWN_DISTANCE * factorZ;
        
        double itemLaunchPower = random.nextDouble() * 0.1 + 0.2;
        double itemMotX = itemLaunchPower * factorX + random.nextGaussian() * 0.007499999832361937 * accuracy;
        double itemMotY = itemLaunchPower * factorY + random.nextGaussian() * 0.007499999832361937 * accuracy;
        double itemMotZ = itemLaunchPower * factorZ + random.nextGaussian() * 0.007499999832361937 * accuracy;
        
        int blockX = NumberConversions.floor(this.locX);
        int blockY = NumberConversions.floor(this.locY);
        int blockZ = NumberConversions.floor(this.locZ);
        
        EntityItem entityItem = new EntityItem(world,itemX,itemY,itemZ,CraftItemStack.createNMSItemStack(itemStack));
        entityItem.motX = itemMotX;
        entityItem.motY = itemMotY;
        entityItem.motZ = itemMotZ;
        world.addEntity(entityItem);
        world.triggerEffect(1000,blockX,blockY,blockZ,0);
    }
    
    //TODO: cache results of this method
    private boolean canSee(net.minecraft.server.Entity nmsEntity){
        return this.world.rayTrace(Vec3D.a().create(this.locX,this.locY + this.getHeadHeight(),this.locZ),Vec3D.a().create(nmsEntity.locX,nmsEntity.locY + nmsEntity.getHeadHeight(),nmsEntity.locZ),false,false) == null;
    }
    
    private void setPlayerControl(boolean state) {
    	this.playerControl = state;
    }
    
    public boolean getPlayerControl() {
    	return(this.playerControl);
    }
    
    public void attachShooter(TurretShooter shooter) {
    	this.shooter = shooter;
    	shooter.getPlayer().getLocation().setX(this.pivotX);
    	shooter.getPlayer().getLocation().setY(this.pivotY + 1);
    	shooter.getPlayer().getLocation().setZ(this.pivotZ);
    	this.getTurret().getPlugin().shooterList.put(shooter.getPlayer(), shooter);
    	setPlayerControl(true);
    }
    
    public void detachShooter() {
    	this.getTurret().getPlugin().shooterList.remove(shooter.getPlayer());
    	this.shooter = null;
    	setPlayerControl(false);
    }
    
    public TurretShooter getShooter() {
    	return(this.shooter);
    }
    
    public float getPitch() {
    	return(this.pitch);
    }
    
    public float getYaw() {
    	return(this.yaw);
    }
    
    public void setPitch(float pitch) {
    	this.pitch = pitch;
    }
    
    public void setYaw(float yaw) {
    	this.yaw = yaw;
    }
}