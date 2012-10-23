package me.azazad.turrets.upgrade;

public class UpgradeTier{
    private final int firingInterval;
    private final double range;
    private final float accuracy;
    
    public UpgradeTier(int firingInterval,double range,float accuracy){
        this.firingInterval = firingInterval;
        this.range = range;
        this.accuracy = accuracy;
    }
    
    public int getFiringInterval(){
        return firingInterval;
    }
    
    public double getRange(){
        return range;
    }
    
    public float getAccuracy(){
        return accuracy;
    }
}