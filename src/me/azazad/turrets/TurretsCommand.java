package me.azazad.turrets;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

import me.azazad.bukkit.util.PlayerCommandSender;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TurretsCommand implements CommandExecutor{
    @SuppressWarnings("unused")
	private static final String MSG_CMD_INGAME_ONLY = "This command can only be used in-game.";
    
    private final TurretsPlugin plugin;
    
    public TurretsCommand(TurretsPlugin plugin){
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender,Command command,String name,String[] args){
        if(args.length > 0){
            String subcommand = args[0].toLowerCase();
            //note: this command gets hung. doesn't work
            if(subcommand.equals("setammousage")) {
            	if(sender instanceof Player) {
            		if(((Player)sender).hasPermission("turrets.setammousage")) {
		            	if (args.length==2) {
		            		String subcommand1 = args[1].toLowerCase();
		            		if (subcommand1.equals("unlimited")) {
		            			if(plugin.getPlayerCommander((Player) sender)==null) {
		    	            		PlayerCommandSender pcs = new PlayerCommandSender((Player) sender);
		                			plugin.playerCommanders.add(pcs);
		                			pcs.getPlayer().sendMessage("Click turret to set to unlimited ammo.");
		                			pcs.setUnlimAmmoCommanded(true);
		                			pcs.setTurretAmmoUsageStep(1);
		                			pcs.setLockedState(true);
		    	            	}
		            		} else if (subcommand1.equals("useammobox")) {
		            			if(plugin.getPlayerCommander((Player) sender)==null) {
		    	            		PlayerCommandSender pcs = new PlayerCommandSender((Player) sender);
		                			plugin.playerCommanders.add(pcs);
		                			pcs.getPlayer().sendMessage("Click turret to set to use its ammo box.");
		                			pcs.setUnlimAmmoCommanded(false);
		                			pcs.setTurretAmmoUsageStep(1);
		                			pcs.setLockedState(true);
		    	            	}
		            		} else sender.sendMessage("Correct usage: /turrets setAmmoUsage unlimited/useAmmoBox");
		            	} else sender.sendMessage("Correct usage: /turrets setAmmoUsage unlimited/useAmmoBox");
            		} else sender.sendMessage("You don't have permission to do that!");
            	} else sender.sendMessage("Only players can set turret ammo type of turret.");
            	return true;
            }
            else if(subcommand.equals("addammobox")) {
	            if (sender instanceof Player) {
	            	if(plugin.getPlayerCommander((Player) sender)==null) {
	            		PlayerCommandSender pcs = new PlayerCommandSender((Player) sender);
            			plugin.playerCommanders.add(pcs);
            			pcs.setTurretCreationStep(1);
	            	}
            		if(plugin.getPlayerCommander((Player) sender).getTurretCreationStep()==1 && !plugin.getPlayerCommander((Player) sender).getLockedState()) {
	            		sender.sendMessage("Select turret to add ammo box to.");
	            		plugin.getPlayerCommander((Player)sender).setLockedState(true);
	            	} else sender.sendMessage(ChatColor.RED + "You are executing another command!");
	            } else sender.sendMessage(ChatColor.RED + "Only a player can add ammo boxes to turrets!");
            	return true;
            }
            else if(subcommand.equals("removeammobox")) {
            	if (sender instanceof Player) {
            		if(plugin.getPlayerCommander((Player) sender)==null) {
            			PlayerCommandSender pcs = new PlayerCommandSender((Player) sender);
            			plugin.playerCommanders.add(pcs);
            			pcs.setTurretDeletionStep(1);
	            	}
            		if(plugin.getPlayerCommander((Player) sender).getTurretDeletionStep()==1 && !plugin.getPlayerCommander((Player) sender).getLockedState()) {
            			sender.sendMessage("Select turret to remove ammo box from.");
	            		plugin.getPlayerCommander((Player)sender).setLockedState(true);
            		} else sender.sendMessage(ChatColor.RED + "You are executing another command!");
            	}  else sender.sendMessage(ChatColor.RED + "Only a player can remove ammo boxes from turrets!");
            	return true;
            }
            else if(subcommand.equals("cancel")) {
            	if (sender instanceof Player) {
            		Player player = (Player) sender;
            		if (plugin.getPlayerCommander(player)!= null && plugin.getPlayerCommander(player).getTurretCreationStep() != 1) {
            			plugin.playerCommanders.remove(plugin.getPlayerCommander(player));
            			player.sendMessage("Ammo box command cancelled!");
	            	} else sender.sendMessage(ChatColor.BLUE + "[Turrets] " + ChatColor.WHITE + "Nothing to cancel!");
            	} else sender.sendMessage("Only a player can use this command!");
            	return true;
            }
            else if(subcommand.equals("save")){
            	if (sender instanceof Player) {
            		if(sender.hasPermission("turrets.loadsave.save")) {
		                try{
		                    plugin.saveTurrets();
		                    sender.sendMessage("Turrets saved to database.");
		                }catch(IOException e){
		                    plugin.getLogger().log(Level.WARNING,"Failed to save turrets",e);
		                    sender.sendMessage("Error saving turrets.");
		                }
            		} else sender.sendMessage("You don't have permissions to save Turrets!");
            	} else {
            		try{
	                    plugin.saveTurrets();
	                    sender.sendMessage("Turrets saved to database.");
	                }catch(IOException e){
	                    plugin.getLogger().log(Level.WARNING,"Failed to save turrets",e);
	                    sender.sendMessage("Error saving turrets.");
	                }
            	}
                return true;
            }else if (subcommand.equals("reload")) {
            	if (sender instanceof Player) {
            		if (sender.hasPermission("turrets.loadsave.reload")){
            		// TODO: check for permissions
						plugin.reloadPlugin();
            		}
            	}else{
            		plugin.reloadPlugin();
            	}
            	return(true);
        	}else if(subcommand.equals("activate")){
        		if (sender instanceof Player) {
        			Player player = (Player)sender;
            		if(plugin.getPlayerCommander(player)==null) {
            			if (player.hasPermission("turrets.activate")) {
            				PlayerCommandSender pcs = new PlayerCommandSender(player);
                			plugin.playerCommanders.add(pcs);
                			pcs.setTurretActivateStep(1);
                			sender.sendMessage("Select turret to activate.");
            			} else sender.sendMessage(ChatColor.RED + "You don't have permission to activate turrets!");
            		} else sender.sendMessage(ChatColor.RED + "You are executing another command!");
            	}  else sender.sendMessage(ChatColor.RED + "Only a player can activate turrets!");
            	return true;
        	}else if(subcommand.equals("deactivate")){
        		if (sender instanceof Player) {
        			Player player = (Player)sender;
            		if(plugin.getPlayerCommander(player)==null) {
            			if (player.hasPermission("turrets.deactivate")) {
            				PlayerCommandSender pcs = new PlayerCommandSender(player);
                			plugin.playerCommanders.add(pcs);
                			pcs.setTurretDeactivationStep(1);
                			sender.sendMessage("Select turret to deactivate.");
            			} else sender.sendMessage(ChatColor.RED + "You don't have permission to activate turrets!");
            		} else sender.sendMessage(ChatColor.RED + "You are executing another command!");
            	}  else sender.sendMessage(ChatColor.RED + "Only a player can activate turrets!");
            	return true;
        	} else if(subcommand.equals("setammotype")) {
        		if(sender instanceof Player) {
        			Player player = (Player)sender;
        			if(player.hasPermission("turrets.setammotype")) {
        				if(args.length==2) {
        					String ammoType = args[1];
        					Material matToUse = null;
        					if(ammoType.equals("arrow")) matToUse = Material.ARROW;
        					else if(ammoType.equals("snowball") || ammoType.equals("snow_ball")) matToUse = Material.SNOW_BALL;
        					else if(ammoType.equals("expbottle") || ammoType.equals("exp_bottle")) matToUse = Material.EXP_BOTTLE;
        					else if(ammoType.equals("monsteregg") || ammoType.equals("monster egg") || ammoType.equals("monster_egg")) matToUse = Material.MONSTER_EGG;
        					else if(ammoType.equals("egg")) matToUse = Material.EGG;
        					else if(ammoType.equals("potion")) matToUse = Material.POTION;
        					else if(ammoType.equals("fireball") || ammoType.equals("fire_ball")) matToUse = Material.FIREBALL;
        					
        					if(matToUse!=null && plugin.getUnlimitedAmmoTypes().contains(matToUse)) {
        						if(plugin.getPlayerCommander(player) == null) {
        							PlayerCommandSender pcs = new PlayerCommandSender(player);
        							plugin.playerCommanders.add(pcs);
        							pcs.setTurretAmmoTypeStep(1);
        							pcs.setAmmoChangeAmmoTypeVal(matToUse);
        							player.sendMessage("Now click which turret you'd like to change the ammo type of.");
        						} else player.sendMessage(ChatColor.RED + "You are executing another command!");
        					} else player.sendMessage(ChatColor.RED + args[1] + " is not a supported ammo type!");
        				} else player.sendMessage("Example usage: /turrets setAmmoType fireball");
        			} else player.sendMessage(ChatColor.RED + "You don't have permission to change turret's ammo type!");
        		} else sender.sendMessage(ChatColor.RED + "Only a player can change a turret's ammo type!");
        		return true;
        	} else if(subcommand.equals("config")) {
        		if(args.length==3) {
        			String subcmd1 = args[1].toLowerCase();
        			if(subcmd1.equals("addunlimammotype")) {
        				
        			}else if(subcmd1.equals("removeunlimammotype")) {
        				
        			}else if(subcmd1.equals("addboxammotype")) {
        				
        			}else if(subcmd1.equals("removeboxammotype")) {
        				
        			} else {
	        			String cmdConfigKey = args[1];
	        			Set<String> configKeys = plugin.getConfigMap().keySet();
	        			boolean foundConfigKey = false;
	        			for(String configKey : configKeys) {
	        				if(cmdConfigKey.equalsIgnoreCase(configKey)) {
	        					if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")) {
		        					boolean cmdConfigVal = Boolean.parseBoolean(args[2]);
		        					plugin.getConfigMap().put(configKey, cmdConfigVal);
		        					plugin.getConfig().set(configKey, cmdConfigVal);
		        					sender.sendMessage("Set config " + configKey + " to " + cmdConfigVal);
		        					foundConfigKey = true;
	        					} else sender.sendMessage("Must use true or false for the config parameter value.");
	        					break;
	        				}
	        			}
	        			if(!foundConfigKey) {
	        				sender.sendMessage("Could not find config parameter " + args[1]);
	        				sender.sendMessage(ChatColor.RED + "Example usage: /turrets config allowAllToDestroy true");
	        			}
        			}
        		}else sender.sendMessage(ChatColor.RED + "That configuration command doesn't exist!");
        		return true;
        	}else{
                sender.sendMessage(subcommand+" is not a Turrets command.");
                return true;
            }
        }else{
            sender.sendMessage(plugin.pdf.getName()+" version "+plugin.pdf.getVersion());
            sender.sendMessage("Total number of turrets: "+plugin.getTurrets().size());
            return false;
        }
    }
}