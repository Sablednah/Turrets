package me.azazad.turrets;

import me.azazad.turrets.nms.EntityTurret;
import me.azazad.turrets.upgrade.UpgradeTier;
import me.azazad.bukkit.util.BlockLocation;
import me.azazad.bukkit.util.PlayerCommandSender;
import net.minecraft.server.EntityMinecart;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.entity.CraftMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

public class TurretsListener implements Listener{
    private final TurretsPlugin plugin;
    
    public TurretsListener(TurretsPlugin plugin){
        this.plugin = plugin;
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event){
        //Turret creation
    	if(plugin.getPlayerCommander(event.getPlayer())!=null) {
    		PlayerCommandSender pcs = plugin.getPlayerCommander(event.getPlayer());
	    	if(pcs.getTurretCreationStep()== 1 && pcs.getLockedState()) {
    			if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
    				Player player = event.getPlayer();
    	    		Block clickedBlock = event.getClickedBlock();
	    			if (TurretsPlugin.POST_MATERIALS.contains(clickedBlock.getType())) {
	    				BlockLocation postLocation = new BlockLocation(clickedBlock.getLocation());
	    				if(!plugin.canBuildTurret(postLocation)) {
	    					Turret turret = plugin.getTurret(postLocation);
	    					if(player.isOp() || turret.getOwnerName().equals(player.getName()) || plugin.allowAllToAddAmmoBox) {
		    					player.sendMessage("Next click a chest to link.");
		    					pcs.setTurretSelected(turret);
		    					pcs.setTurretCreationStep(2);
	    					} else {
	    						player.sendMessage("You can't add ammo to someone else's turret!");
	    					}
	    				}
	    			}
	    		}
	    	} else if (pcs.getTurretCreationStep()== 2 && pcs.getLockedState()) {
	    		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
	    			Player player = event.getPlayer();
    	    		Block clickedBlock = event.getClickedBlock();
	    			if(clickedBlock.getType() == Material.CHEST) {
	    				Turret turret = plugin.getPlayerCommander(player).getTurretSelected();
	    				if (turret.checkIfBlockByTurret(clickedBlock)) {
	    					if(turret.addTurretAmmoBoxChest(clickedBlock)) player.sendMessage("Ammo box linked to turret!");
	    					else player.sendMessage(ChatColor.RED + "Ammo box linkage failed! Already linked?");
	    					plugin.playerCommanders.remove(pcs);
	    				}
	    			}
	    		}
	    	} else if (pcs.getTurretDeletionStep()== 1 && pcs.getLockedState()) {
	    		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	    			Player player = event.getPlayer();
    	    		Block clickedBlock = event.getClickedBlock();
    	    		if (TurretsPlugin.POST_MATERIALS.contains(clickedBlock.getType())) {
	    				BlockLocation postLocation = new BlockLocation(clickedBlock.getLocation());
	    				if(!plugin.canBuildTurret(postLocation)) {
	    					player.sendMessage("Next click a chest to unlink.");
	    					pcs.setTurretSelected(plugin.getTurret(postLocation));
	    					pcs.setTurretDeletionStep(2);
	    				}
    	    		}
	    		}
	    	} else if (pcs.getTurretDeletionStep()== 2 && pcs.getLockedState()) {
	    		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
	    			Player player = event.getPlayer();
    	    		Block clickedBlock = event.getClickedBlock();
	    			if(clickedBlock.getType() == Material.CHEST) {
	    				Turret turret = plugin.getPlayerCommander(player).getTurretSelected();
	    				if (turret.checkIfBlockByTurret(clickedBlock)) {
	    					if(turret.getTurretAmmoBox().removeAmmoChest(clickedBlock)) player.sendMessage("Ammo box link severed!");
	    					else player.sendMessage(ChatColor.RED + "Ammo box severing failed! Ammo box not linked to this turret?");
	    					plugin.playerCommanders.remove(pcs);
	    				}
	    			}
	    		}
	    	} else if (pcs.getTurretAmmoStep() == 1 && pcs.getLockedState()) {
	    		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
	    			Player player = event.getPlayer();
    	    		Block clickedBlock = event.getClickedBlock();
	    			if(TurretsPlugin.POST_MATERIALS.contains(clickedBlock.getType())) {
	    				BlockLocation postLocation = new BlockLocation(clickedBlock.getLocation());
	    				if(!plugin.canBuildTurret(postLocation)) {
	    					player.sendMessage("Turret ammo modified.");
	    					pcs.setTurretSelected(plugin.getTurret(postLocation));
	    					Turret turret = pcs.getTurretSelected();
	    					if(player.isOp() || turret.getOwnerName().equals(player.getName()) || (plugin.allowAllToChangeAmmo)) {
		    					turret.setUsesAmmoBox(!pcs.getUnlimAmmoCommanded());
		    					plugin.playerCommanders.remove(pcs);
	    					}
	    				}
	    			}
	    		}
	    	} else if(pcs.getTurretActivationStep() == 1) {
	    		if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	    			Player player = event.getPlayer();
	    			Block clickedBlock = event.getClickedBlock();
	    			if(TurretsPlugin.POST_MATERIALS.contains(clickedBlock.getType())) {
	    				BlockLocation postLocation = new BlockLocation(clickedBlock.getLocation());
	    				if(!plugin.canBuildTurret(postLocation)) {
	    					player.sendMessage("Turret activated.");
//	    					pcs.setTurretSelected(plugin.getTurret(postLocation));
//	    					Turret turret = pcs.getTurretSelected();
	    					Turret turret = plugin.getTurret(postLocation);
	    					if(player.isOp() || turret.getOwnerName().equals(player.getName()) || (plugin.allowAllToModActivate)) {
	    						turret.getEntity().setIsActive(true);
		    					plugin.playerCommanders.remove(pcs);
	    					}
	    				}
	    			}
	    		}
	    	} else if(pcs.getTurretDeactivationStep() == 1) {
	    		if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	    			Player player = event.getPlayer();
	    			Block clickedBlock = event.getClickedBlock();
	    			if(TurretsPlugin.POST_MATERIALS.contains(clickedBlock.getType())) {
	    				BlockLocation postLocation = new BlockLocation(clickedBlock.getLocation());
	    				if(!plugin.canBuildTurret(postLocation)) {
	    					player.sendMessage("Turret deactivated.");
//	    					pcs.setTurretSelected(plugin.getTurret(postLocation));
//	    					Turret turret = pcs.getTurretSelected();
	    					Turret turret = plugin.getTurret(postLocation);
	    					if(player.isOp() || turret.getOwnerName().equals(player.getName()) || (plugin.allowAllToModActivate)) {
		    					turret.getEntity().setIsActive(false);
		    					plugin.playerCommanders.remove(pcs);
	    					}
	    				}
	    			}
	    		}
	    	}
    	}
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getBlockFace() == BlockFace.UP){
            Block clickedBlock = event.getClickedBlock();
            ItemStack itemInHand = event.getItem();
            Player player = event.getPlayer();
            
            if(TurretsPlugin.POST_MATERIALS.contains(clickedBlock.getType()) && itemInHand.getType() == Material.MINECART){
                if(player.hasPermission("turrets.addturret")) {
	                BlockLocation postLocation = new BlockLocation(clickedBlock.getLocation());
	                if(plugin.canBuildTurret(postLocation)){
	                    Turret turret = new Turret(postLocation,player,plugin);
	                    if(itemInHand.getAmount() == 1) player.setItemInHand(new ItemStack(Material.AIR));
	                    else {
	                    	itemInHand.setAmount(itemInHand.getAmount()-1);
	                    	player.setItemInHand(itemInHand);
	                    }
	                    player.updateInventory();
	                    plugin.addTurret(turret);
	                    plugin.notifyPlayer(player,TurretsMessage.TURRET_CREATED);
	                }else{
	                    plugin.notifyPlayer(player,TurretsMessage.TURRET_CANNOT_BUILD);
	                }
                } else player.sendMessage("You don't have permissions to create a turret!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
    	if (plugin.getTurret(event.getPlayer())!=null) {//if player is shooter of turret
			plugin.getTurret(event.getPlayer()).getEntity().getShooter().setClickedFlag(true);
    	}
    }
    
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
    	if(event.getVehicle().getType().equals(EntityType.MINECART)) {
    		BlockLocation turretLoc = BlockLocation.fromLocation(event.getVehicle().getLocation().add(0,-1,0));
    		if ((event.getEntered() instanceof Player) && !plugin.canBuildTurret(turretLoc)) {
    			Player rider = (Player) event.getEntered();
    			if(rider.hasPermission("turrets.manturret")) {
	    			Turret turret = plugin.getTurret(turretLoc);
	    			if (rider.isOp() || turret.getOwnerName().equals(rider.getName()) || plugin.allowAllToMan) {
		    			TurretShooter shooter = new TurretShooter(rider);
		    			turret.getEntity().attachShooter(shooter);
	    			}
    			}
    		}
    	}
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
    	if(event.getVehicle().getType().equals(EntityType.MINECART)) {
    		BlockLocation turretLoc = BlockLocation.fromLocation(event.getVehicle().getLocation().add(0,-1,0));
    		if ((event.getExited() instanceof Player) && !plugin.canBuildTurret(turretLoc)) {
    			//Player rider = (Player) event.getExited();
    			Turret turret = plugin.getTurret(turretLoc);
    			turret.getEntity().detachShooter();
    		}
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event){
        Entity entity = event.getVehicle();
        
        //Turret destruction
        if(entity instanceof Minecart){
            Minecart minecart = (Minecart)entity;
            EntityMinecart nmsMinecart = ((CraftMinecart)minecart).getHandle();
            
            if(nmsMinecart instanceof EntityTurret){
                EntityTurret nmsTurret = (EntityTurret)nmsMinecart;
                Turret turret = nmsTurret.getTurret();
                
                if(plugin.getTurrets().contains(turret)){
                    Entity attacker = event.getAttacker();
                    
                    if(attacker instanceof Player){
                        Player player = (Player)attacker;
                        
                        if(player.hasPermission("turrets.removeturret")) {
	                        if(player.isOp() || turret.getOwnerName().equals(player.getName()) || plugin.allowAllToDestroy) {
		                        plugin.removeTurret(turret);
		                        plugin.notifyPlayer(player,TurretsMessage.TURRET_DESTROYED);
		                        return;
	                        }
                        }
                        plugin.notifyPlayer(player,TurretsMessage.NO_DESTROY_PERM);
                        event.setCancelled(true);
                        return;
                        
                    }else{
                        plugin.removeTurret(turret);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
    	Entity entity = event.getEntity();
    	if(entity instanceof Minecart) {
    		Minecart minecart = (Minecart)entity;
    		EntityMinecart nmsMinecart = ((CraftMinecart)minecart).getHandle();
    		if(nmsMinecart instanceof EntityTurret) {
    			EntityTurret nmsTurret = (EntityTurret)nmsMinecart;
    			Turret turret = nmsTurret.getTurret();
    			if(plugin.getTurrets().contains(turret)) {
    				event.setCancelled(true);
    			}
    		}
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();
        
        //Turret destruction
        if(TurretsPlugin.POST_MATERIALS.contains(material)){
            BlockLocation postLocation = BlockLocation.fromLocation(block.getLocation());
            Turret turret = plugin.getTurret(postLocation);
            
            if(turret != null){
                if(!player.hasPermission("turrets.destroyturret")) {
                    plugin.notifyPlayer(player,TurretsMessage.NO_DESTROY_PERM);
                    event.setCancelled(true);
                    return;
                }
                
                plugin.removeTurret(turret);
                
                plugin.notifyPlayer(player,TurretsMessage.TURRET_DESTROYED);
            }
        }
        
        //Ammobox destruction
        if(material == Material.CHEST) {
        	for (Turret turret:plugin.getTurrets()) {
        		if (turret.getTurretAmmoBox().isChestAttachedToTurret(block)) {
        			if(block.getRelative(BlockFace.NORTH).getType().equals(Material.CHEST) || block.getRelative(BlockFace.EAST).getType().equals(Material.CHEST) || block.getRelative(BlockFace.SOUTH).getType().equals(Material.CHEST) || block.getRelative(BlockFace.WEST).getType().equals(Material.CHEST)) {
        				Chest chest;
                		if(block.getRelative(BlockFace.NORTH).getType().equals(Material.CHEST)) chest=(Chest)block.getRelative(BlockFace.NORTH).getState();
                		else if(block.getRelative(BlockFace.EAST).getType().equals(Material.CHEST)) chest=(Chest)block.getRelative(BlockFace.EAST).getState();
                		else if(block.getRelative(BlockFace.SOUTH).getType().equals(Material.CHEST)) chest=(Chest)block.getRelative(BlockFace.SOUTH).getState();
                		else chest=(Chest)block.getRelative(BlockFace.WEST).getState();
                		turret.getTurretAmmoBox().removeAmmoChest(block);
            			turret.getTurretAmmoBox().addAmmoChest(chest.getBlock());
        			}
        			else {
        				if (turret.getTurretAmmoBox().removeAmmoChest(block)) player.sendMessage("Chest detached!");
        				else player.sendMessage("Attached chest didn't detach correctly! Something's wrong..");
        			}
        		}
        	}
        } else {
        	BlockLocation postLocation = BlockLocation.fromLocation(block.getLocation().add(0,1,0));
            Turret turret = plugin.getTurret(postLocation);
            if(turret!=null) {
            	if(player.hasPermission("turret.upgrade")) {
            		turret.updateUpgradeTier(Material.AIR);
            	} else {
            		player.sendMessage("You don't have permission to upgrade turrets!");
            		event.setCancelled(true);
            	}
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if(block.getType().equals(Material.CHEST)) {
        	if(block.getRelative(BlockFace.NORTH).getType().equals(Material.CHEST) || block.getRelative(BlockFace.EAST).getType().equals(Material.CHEST) || block.getRelative(BlockFace.SOUTH).getType().equals(Material.CHEST) || block.getRelative(BlockFace.WEST).getType().equals(Material.CHEST)) {
        		Chest chest;
        		if(block.getRelative(BlockFace.NORTH).getType().equals(Material.CHEST)) chest=(Chest)block.getRelative(BlockFace.NORTH).getState();
        		else if(block.getRelative(BlockFace.EAST).getType().equals(Material.CHEST)) chest=(Chest)block.getRelative(BlockFace.EAST).getState();
        		else if(block.getRelative(BlockFace.SOUTH).getType().equals(Material.CHEST)) chest=(Chest)block.getRelative(BlockFace.SOUTH).getState();
        		else chest=(Chest)block.getRelative(BlockFace.WEST).getState();
        		for(Turret turret:plugin.getTurrets()) {
            		if(turret.getTurretAmmoBox().isChestAttachedToTurret(chest.getBlock())) {
		    			turret.getTurretAmmoBox().removeAmmoChest(chest.getBlock());
		    			turret.getTurretAmmoBox().addAmmoChest(block);
		        	}
        		}
        	}
    	}
        
        //Change upgrade tiers
        BlockLocation postLocation = BlockLocation.fromLocation(block.getLocation().add(0,1,0));
        Turret turret = plugin.getTurret(postLocation);
        
        if(turret != null){
        	if(player.hasPermission("turrets.upgrade")) {
	            UpgradeTier prevTier = turret.getUpgradeTier();
	            UpgradeTier newTier = turret.updateUpgradeTier();
	            
	            if(newTier != prevTier){
	                plugin.notifyPlayer(player,TurretsMessage.TURRET_UPGRADED);
	                plugin.notifyPlayer(player,"Firing interval (lower is faster): "+ChatColor.AQUA+newTier.getFiringInterval());
	                plugin.notifyPlayer(player,"Range: "+ChatColor.AQUA+newTier.getRange());
	                plugin.notifyPlayer(player,"Accuracy (lower is more accurate): "+ChatColor.AQUA+newTier.getAccuracy());
	            }
        	} else {
        		player.sendMessage("You don't have permissions to upgrade turrets!");
        		event.setCancelled(true);
        	}
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if(event.getItem().hasMetadata("no_pickup")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
    	if(plugin.getTurret(event.getEntity())!=null) {
    		Turret turret = plugin.getTurret(event.getEntity());
    		turret.getEntity().detachShooter();
    	}
    }
}