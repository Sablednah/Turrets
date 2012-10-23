package me.azazad.bukkit.util;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public enum MobAlignment{
    PASSIVE,NEUTRAL,HOSTILE;
    
    private static final Map<EntityType,MobAlignment> alignments;
    
    public static MobAlignment getAlignment(Entity entity){
        if(entity != null){
            return getAlignment(entity.getType());
        }else{
            return null;
        }
    }
    
    public static MobAlignment getAlignment(EntityType type){
        return alignments.get(type);
    }
    
    public static boolean isHostile(Entity entity){
        if(entity != null){
            return isHostile(entity.getType());
        }else{
            return false;
        }
    }
    
    public static boolean isHostile(EntityType type){
        return getAlignment(type) == HOSTILE;
    }
    
    static{
        alignments = new EnumMap<EntityType,MobAlignment>(EntityType.class);
        alignments.put(EntityType.BLAZE,HOSTILE);
        alignments.put(EntityType.CAVE_SPIDER,HOSTILE);
        alignments.put(EntityType.CHICKEN,PASSIVE);
        alignments.put(EntityType.COW,PASSIVE);
        alignments.put(EntityType.CREEPER,HOSTILE);
        alignments.put(EntityType.ENDERMAN,NEUTRAL);
        alignments.put(EntityType.ENDER_DRAGON,HOSTILE);
        alignments.put(EntityType.GHAST,HOSTILE);
        alignments.put(EntityType.GIANT,HOSTILE);
        alignments.put(EntityType.IRON_GOLEM,NEUTRAL);
        alignments.put(EntityType.MAGMA_CUBE,HOSTILE);
        alignments.put(EntityType.MUSHROOM_COW,PASSIVE);
        alignments.put(EntityType.OCELOT,NEUTRAL);
        alignments.put(EntityType.PIG,PASSIVE);
        alignments.put(EntityType.PIG_ZOMBIE,NEUTRAL);
        alignments.put(EntityType.PLAYER,NEUTRAL);
        alignments.put(EntityType.SHEEP,PASSIVE);
        alignments.put(EntityType.SILVERFISH,HOSTILE);
        alignments.put(EntityType.SKELETON,HOSTILE);
        alignments.put(EntityType.SLIME,HOSTILE);
        alignments.put(EntityType.SNOWMAN,NEUTRAL);
        alignments.put(EntityType.SPIDER,HOSTILE);
        alignments.put(EntityType.SQUID,PASSIVE);
        alignments.put(EntityType.VILLAGER,PASSIVE);
        alignments.put(EntityType.WOLF,NEUTRAL);
        alignments.put(EntityType.ZOMBIE,HOSTILE);
    }
}