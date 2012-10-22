package me.azazad.turrets.targeting;

import org.bukkit.entity.LivingEntity;

public interface TargetAssessor{
    public TargetAssessment assessMob(LivingEntity mob);
}