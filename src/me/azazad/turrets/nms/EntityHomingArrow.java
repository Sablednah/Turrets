package me.azazad.turrets.nms;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MathHelper;
import net.minecraft.server.World;

public class EntityHomingArrow extends EntityArrow{
	
	private float fShoot = 1.1F;
    public int fromPlayer = 0;
    public int shake = 0;
    public Entity shooter;
    private EntityLiving target;
	
	public EntityHomingArrow(World world) {
        super(world);
    }

    public EntityHomingArrow(World world, double d0, double d1, double d2) {
        super(world,d0,d1,d2);
    }

    public EntityHomingArrow(World world, EntityLiving entityliving, EntityLiving entityliving1, float f, float f1) {
        super(world,entityliving,entityliving1,f,f1);
    }

    public EntityHomingArrow(World world, EntityLiving entityliving, float f) {
        super(world,entityliving,f);
    }

	private void updateHomingMotion() {
		double entX = target.locX;
        double entY = target.locY + target.getHeadHeight();
        double entZ = target.locZ;
        
        double dx = -(entX - this.locX);
        double dy = entY - this.locY;
        double dz = -(entZ - this.locZ);
        double dh = Math.sqrt(dx * dx + dz * dz);
        double rYaw = (float)Math.atan2(dz,dx)-90F;
        double rPitch = (float)-Math.atan(dy / dh);
        double d0 = Math.sin(rYaw) * -Math.cos(rPitch);
        double d1 = -Math.sin(rPitch);
        double d2 = Math.cos(rYaw) * Math.cos(rPitch);
        
    	
        float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
//        d0 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
//        d1 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
//        d2 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
        d0 *= (double) fShoot;
        d1 *= (double) fShoot;
        d2 *= (double) fShoot;
        this.motX = d0;
        this.motY = d1;
        this.motZ = d2;
	}
	
	public void setTarget(EntityLiving entityliving) {
		this.target = entityliving;
	}
	
    public void shoot(EntityLiving entityliving, float f) {
    	this.target = entityliving;
    	this.fShoot = f;
    	updateHomingMotion();
    	double d0 = this.motX;
    	double d1 = this.motY;
    	double d2 = this.motZ;
        float f3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

        this.lastYaw = this.yaw = (float) (Math.atan2(d0, d2) * 180.0D / 3.1415927410125732D);
        this.lastPitch = this.pitch = (float) (Math.atan2(d1, (double) f3) * 180.0D / 3.1415927410125732D);
    }
	
    @Override
    public void j_() {
        super.j_();
        if(this.target!=null) this.updateHomingMotion();
    }
}