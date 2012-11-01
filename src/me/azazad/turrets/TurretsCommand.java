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
import org.bukkit.command.ConsoleCommandSender;
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
        if(args.length > 0) {
            String subcommand = args[0].toLowerCase();
            //note: this command gets hung. doesn't work
            if(subcommand.equals("setammousage")) {
            	if(sender instanceof Player) {
            		Player player = (Player) sender;
            		if((player).hasPermission("turrets.setammousage")) {
		            	if (args.length==2) {
		            		String subcommand1 = args[1].toLowerCase();
		            		if (subcommand1.equals("unlimited")) {
		            			if(plugin.getPlayerCommander((Player) sender)==null) {
		    	            		PlayerCommandSender pcs = new PlayerCommandSender(player);
		                			plugin.playerCommanders.add(pcs);
		                			pcs.getPlayer().sendMessage("Click turret to set to unlimited ammo.");
		                			pcs.setUnlimAmmoCommanded(true);
		                			pcs.setTurretAmmoUsageStep(1);
		                			pcs.setLockedState(true);
		    	            	}
		            		} else if (subcommand1.equals("useammobox")) {
		            			if(plugin.getPlayerCommander(player)==null) {
		    	            		PlayerCommandSender pcs = new PlayerCommandSender(player);
		                			plugin.playerCommanders.add(pcs);
		                			pcs.getPlayer().sendMessage("Click turret to set to use its ammo box.");
		                			pcs.setUnlimAmmoCommanded(false);
		                			pcs.setTurretAmmoUsageStep(1);
		                			pcs.setLockedState(true);
		    	            	}
		            		} else sender.sendMessage("Correct usage: /turrets setAmmoUsage <unlimited/useAmmoBox> [all]");
		            	} else if(args.length==3) {
		            		String subcommand1 = args[1].toLowerCase();
		            		String subcommand2 = args[2].toLowerCase();
		            		if (subcommand1.equals("unlimited") && subcommand2.equals("all")) {
		            			if(plugin.getTurretOwners().get(player)!=null) {
		            				TurretOwner turretOwner = plugin.getTurretOwners().get(player);
		            				for(Turret turret : turretOwner.getTurretsOwned()) {
		            					turret.setUsesAmmoBox(false);
		            				}
		            				player.sendMessage("All turrets set to unlimited ammo.");
		            			} else player.sendMessage("You have no turrets!");
		            		} else if (subcommand1.equals("useammobox") && subcommand2.equals("all")) {
		            			if(plugin.getTurretOwners().get(player)!=null) {
		            				TurretOwner turretOwner = plugin.getTurretOwners().get(player);
		            				for(Turret turret : turretOwner.getTurretsOwned()) {
		            					turret.setUsesAmmoBox(true);
		            				}
		            				player.sendMessage("All turrets set to use ammo.");
		            			} else player.sendMessage("You have no turrets!");
		            		} else sender.sendMessage("Correct usage: /turrets setAmmoUsage <unlimited/useAmmoBox> [all]");
		            	} else sender.sendMessage("Correct usage: /turrets setAmmoUsage <unlimited/useAmmoBox> [all]");
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
            		}else sender.sendMessage(ChatColor.RED + "You don't have permission to reload Turrets!");
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
        					if(ammoType.equals("arrow") || ammoType.equals("arrows")) matToUse = Material.ARROW;
        					else if(ammoType.equals("snowball") || ammoType.equals("snow_ball") || ammoType.equals("snowballs") || ammoType.equals("snow_balls")) matToUse = Material.SNOW_BALL;
        					else if(ammoType.equals("expbottle") || ammoType.equals("exp_bottle") || ammoType.equals("expbottles") || ammoType.equals("exp_bottles")) matToUse = Material.EXP_BOTTLE;
        					else if(ammoType.equals("monsteregg") || ammoType.equals("monster_egg") || ammoType.equals("monstereggs") || ammoType.equals("monster_eggs")) matToUse = Material.MONSTER_EGG;
        					else if(ammoType.equals("egg") || ammoType.equals("eggs")) matToUse = Material.EGG;
        					else if(ammoType.equals("potion") || ammoType.equals("potions")) matToUse = Material.POTION;
        					else if(ammoType.equals("fireball") || ammoType.equals("fire_ball") || ammoType.equals("fireballs") || ammoType.equals("fire_balls")) matToUse = Material.FIREBALL;
        					
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
        			if (sender.hasPermission("turrets.config") || sender instanceof ConsoleCommandSender) {
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
        			}
        		}else sender.sendMessage(ChatColor.RED + "That configuration command doesn't exist!");
        		return true;
        	}else if(subcommand.equals("whitelist")) {
        		if(args.length==3) {
        			if(sender instanceof Player) {
        				//player adding or removing from their whitelist
        				if(sender.hasPermission("turrets.modwblists")) {
	        				OwnerWBlists ownerList = plugin.getOwnerWBlists(sender.toString());
		        			if(args[1].equalsIgnoreCase("add")) {
		        				if(ownerList.isPlayerInBlacklist(args[2])) {
		        					ownerList.removePlayerFromBlacklist(args[2]);
		        					sender.sendMessage(args[2] + " removed from your blacklist.");
		        				}
		        				if(!ownerList.isPlayerInWhitelist(args[2])) {
		        					ownerList.addPlayerToWhitelist(args[2]);
		        					sender.sendMessage(args[2] + " added to your whitelist.");
		        				}
		        				else sender.sendMessage(ChatColor.RED + args[2] + " is already on your whitelist!");
		        			}else if(args[1].equalsIgnoreCase("remove")) {
		        				if(ownerList.isPlayerInWhitelist(args[2])) {
		        					ownerList.removePlayerFromWhitelist(args[2]);
		        					sender.sendMessage(args[2] + " removed from your whitelist.");
		        				} else sender.sendMessage(ChatColor.RED + args[2] + " not on your whitelist!");
		        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur whitelist add <USERNAME>");
        				}else sender.sendMessage(ChatColor.RED + "You don't have permission to modify your white or blacklist!");
        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur whitelist <userWithWhitelist> add <useToAdd>");
        		}else if(args.length==4) {
        			//commander adding or removing from another player's whitelist
        			if((sender instanceof Player && sender.hasPermission("turrets.modotherwblists")) || sender instanceof ConsoleCommandSender) {
        				String playerWLowner = args[1];
        				OwnerWBlists ownerList = plugin.getOwnerWBlists(playerWLowner);
	        			if(args[2].equalsIgnoreCase("add")) {
	        				if(ownerList.isPlayerInBlacklist(args[3])) {
	        					ownerList.removePlayerFromBlacklist(args[3]);
	        					sender.sendMessage(args[3] + " removed from " + playerWLowner + "'s blacklist.");
	        				}
	        				if(!ownerList.isPlayerInWhitelist(args[3])) {
	        					ownerList.addPlayerToWhitelist(args[3]);
	        					sender.sendMessage(args[3] + " added to " + playerWLowner + "'s whitelist.");
	        				}
	        				else sender.sendMessage(ChatColor.RED + args[3] + " is already on " + playerWLowner + "'s whitelist!");
	        			}else if(args[2].equalsIgnoreCase("remove")) {
	        				if(ownerList.isPlayerInWhitelist(args[3])) {
	        					ownerList.removePlayerFromWhitelist(args[3]);
	        					sender.sendMessage(args[3] + " removed from your whitelist.");
	        				} else sender.sendMessage(ChatColor.RED + args[3] + " not on " + playerWLowner + "'s whitelist!");
	        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur whitelist <userWithWhitelist> add <useToAdd>");
	        			
        			}else sender.sendMessage(ChatColor.RED + "You don't have permission to modify others' white or blacklists!");
        		}else sender.sendMessage("Incorrect whitelist command.");
        		return true;
        	}else if(subcommand.equals("blacklist")) {
        		if(args.length==3) {
        			if(sender instanceof Player) {
	        			//player adding or removing from their blacklist
	        			OwnerWBlists ownerList = plugin.getOwnerWBlists(sender.toString());
	        			if(args[1].equalsIgnoreCase("add")) {
	        				if(ownerList.isPlayerInWhitelist(args[2])) {
	        					ownerList.removePlayerFromWhitelist(args[2]);
	        					sender.sendMessage(args[2] + " removed from your whitelist.");
	        				}
	        				if(!ownerList.isPlayerInBlacklist(args[2])) {
	        					ownerList.addPlayerToBlacklist(args[2]);
	        					sender.sendMessage(args[2] + " added to your blacklist.");
	        				}
	        				else sender.sendMessage(ChatColor.RED + args[2] + " is already on your blacklist!");
	        			}else if(args[1].equalsIgnoreCase("remove")) {
	        				if(ownerList.isPlayerInBlacklist(args[2])) {
	        					ownerList.removePlayerFromBlacklist(args[2]);
	        					sender.sendMessage(args[2] + " removed from your blacklist.");
	        				} else sender.sendMessage(ChatColor.RED + args[2] + " not on your blacklist!");
	        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur blacklist add <USERNAME>");
        			}
        		}else if(args.length==4) {
        			//commander adding or removing from another player's blacklist
        			if((sender instanceof Player && sender.hasPermission("turrets.modotherwblists")) || sender instanceof ConsoleCommandSender) {
        				String playerBLowner = args[1];
        				OwnerWBlists ownerList = plugin.getOwnerWBlists(playerBLowner);
	        			if(args[2].equalsIgnoreCase("add")) {
	        				if(ownerList.isPlayerInWhitelist(args[3])) {
	        					ownerList.removePlayerFromWhitelist(args[3]);
	        					sender.sendMessage(args[3] + " removed from " + playerBLowner + "'s whitelist.");
	        				}
	        				if(!ownerList.isPlayerInBlacklist(args[3])) {
	        					ownerList.addPlayerToBlacklist(args[3]);
	        					sender.sendMessage(args[3] + " added to " + playerBLowner + "'s blacklist.");
	        				}
	        				else sender.sendMessage(ChatColor.RED + args[3] + " is already on " + playerBLowner + "'s blacklist!");
	        			}else if(args[2].equalsIgnoreCase("remove")) {
	        				if(ownerList.isPlayerInBlacklist(args[3])) {
	        					ownerList.removePlayerFromBlacklist(args[3]);
	        					sender.sendMessage(args[3] + " removed from your blacklist.");
	        				} else sender.sendMessage(ChatColor.RED + args[3] + " not on " + playerBLowner + "'s blacklist!");
	        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur blacklist <userWithBlacklist> add <useToAdd>");
        			}else sender.sendMessage(ChatColor.RED + "You don't have permission to modify others' white or blacklists!");
        		}else sender.sendMessage("Incorrect blacklist command.");
        		return true;
        	}else if(subcommand.equals("gwhitelist")) {
        		if(args.length==3) {
        			//player adding or removing from global whitelist
        			if((sender instanceof Player && sender.hasPermission("turrets.modgloballists")) || sender instanceof ConsoleCommandSender) {
        				OwnerWBlists globalList = plugin.getOwnerWBlists("global");
	        			if(args[1].equalsIgnoreCase("add")) {
	        				if(globalList.isPlayerInBlacklist(args[2])) {
	        					globalList.removePlayerFromBlacklist(args[2]);
	        					sender.sendMessage(args[2] + " removed from global blacklist.");
	        				}
	        				if(!globalList.isPlayerInWhitelist(args[2])) {
	        					globalList.addPlayerToWhitelist(args[2]);
	        					sender.sendMessage(args[2] + " added to global whitelist.");
	        				}
	        				else sender.sendMessage(ChatColor.RED + args[2] + " is already on the global whitelist!");
	        			}else if(args[1].equalsIgnoreCase("remove")) {
	        				if(globalList.isPlayerInWhitelist(args[3])) {
	        					globalList.removePlayerFromWhitelist(args[3]);
	        					sender.sendMessage(args[3] + " removed from global whitelist.");
	        				} else sender.sendMessage(ChatColor.RED + args[3] + " not on global whitelist!");
	        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur gwhitelist add <USERNAME>");
        			}else sender.sendMessage(ChatColor.RED + "You don't have permission to modify the global white and blacklists!");
        		}else sender.sendMessage(ChatColor.RED + "Ex. /tur gwhitelist add <USERNAME>");
        		return true;
        	}else if(subcommand.equals("gblacklist")) {
        		if(args.length==3) {
        			//player adding or removing from global blacklist
        			if((sender instanceof Player && sender.hasPermission("turrets.modgloballists")) || sender instanceof ConsoleCommandSender) {
        				OwnerWBlists globalList = plugin.getOwnerWBlists("global");
	        			if(args[1].equalsIgnoreCase("add")) {
	        				if(globalList.isPlayerInWhitelist(args[2])) {
	        					globalList.removePlayerFromWhitelist(args[2]);
	        					sender.sendMessage(args[2] + " removed from global whitelist.");
	        				}
	        				if(!globalList.isPlayerInBlacklist(args[2])) {
	        					globalList.addPlayerToBlacklist(args[2]);
	        					sender.sendMessage(args[2] + " added to global blacklist.");
	        				}
	        				else sender.sendMessage(ChatColor.RED + args[2] + " is already on the global blacklist!");
	        			}else if(args[1].equalsIgnoreCase("remove")) {
	        				if(globalList.isPlayerInBlacklist(args[3])) {
	        					globalList.removePlayerFromBlacklist(args[3]);
	        					sender.sendMessage(args[3] + " removed from global blacklist.");
	        				} else sender.sendMessage(ChatColor.RED + args[3] + " not on global blacklist!");
	        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur gblacklist add <USERNAME>");
        			}else sender.sendMessage(ChatColor.RED + "You don't have permission to modify the global white and blacklists!");
        		}else sender.sendMessage(ChatColor.RED + "Ex. /tur gblacklist add <USERNAME>");
        		return true;
        	}else if(subcommand.equals("wblists")) {
        		if((sender instanceof Player && sender.hasPermission("turrets.modotherwblists")) || sender instanceof ConsoleCommandSender) {
	        		if(args.length==3) {
	        			if(args[1].equalsIgnoreCase("delete")) {
	        				//TODO:write deletion code (checks for existence etc)
	        				String userName = args[2];
	        				if(plugin.getOwnerWBlists(userName)!=null) {
	        					boolean hasTurret = false;
	        					for(Turret turret : plugin.getTurrets()) {
	        						if(turret.getOwnerName().equalsIgnoreCase(userName)) {
	        							hasTurret = true;
	        						}
	        					}
	        					if(hasTurret) {
	        						plugin.removeFromOwnerWBlists(userName);
	        						plugin.addToOwnerWBlists(userName);
	        						sender.sendMessage(userName + " has existing turrets. White/blacklists still exist, but are now empty.");
	        					} else {
	        						plugin.removeFromOwnerWBlists(userName);
	        						sender.sendMessage(userName + "'s white and blacklists removed.");
	        					}
	        				}
	        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur wblists delete <userName>");
	        		}else if(args.length==4) {
	        			if(args[1].equalsIgnoreCase("copy")) {
	        				//TODO:write copy code (checks for existence etc)
	        				String userFrom = args[2];	
	        				String userTo = args[3];
	        				if(plugin.getOwnerWBlists(userFrom)!=null) {
	        					if(plugin.getOwnerWBlists(userTo)!=null) plugin.removeFromOwnerWBlists(userTo);
	        					OwnerWBlists ownerFromLists = plugin.getOwnerWBlists(userFrom);
	        					plugin.addToOwnerWBlists(userTo, ownerFromLists);
	        					sender.sendMessage(userFrom + "'s white and blacklists copied to " + userTo);
	        				}else sender.sendMessage(ChatColor.RED + userFrom + " does not have a list entry to copy!");
	        			}else sender.sendMessage(ChatColor.RED + "Ex. /tur wblists copy <userFrom> <userTo>");
	        		}else sender.sendMessage(ChatColor.RED + "Ex. /tur wblists copy <userFrom> <userTo>");
        		}else sender.sendMessage(ChatColor.RED + "You don't have permission to copy and delete lists!");
        		return true;
        	}
        	else{
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