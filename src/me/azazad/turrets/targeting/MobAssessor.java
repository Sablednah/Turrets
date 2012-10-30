package me.azazad.turrets.targeting;

import me.azazad.bukkit.util.MobAlignment;
import org.bukkit.entity.LivingEntity;

public class MobAssessor implements TargetAssessor{
    @Override
    public TargetAssessment assessMob(LivingEntity mob){
        if(MobAlignment.isHostile(mob)){
            return TargetAssessment.HOSTILE;
        }else if(MobAlignment.isEither(mob)){
        	return TargetAssessment.EITHER;
        }else{
            return TargetAssessment.MEH;
        }
    }
}