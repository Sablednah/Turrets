package me.azazad.turrets;

import java.io.IOException;
import java.util.logging.Level;

import me.azazad.bukkit.util.PlayerCommandSender;
import me.azazad.bukkit.util.Reload;

import org.bukkit.ChatColor;
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
            if(subcommand.equals("setammotype")) {
            	if(sender instanceof Player) {
            		if(((Player)sender).hasPermission("turrets.setammotype")) {
		            	if (args.length==2) {
		            		String subcommand1 = args[1].toLowerCase();
		            		if (subcommand1.equals("unlimited")) {
		            			if(plugin.getPlayerCommander((Player) sender)==null) {
		    	            		PlayerCommandSender pcs = new PlayerCommandSender((Player) sender);
		                			plugin.playerCommanders.add(pcs);
		                			pcs.getPlayer().sendMessage("Click turret to set to unlimited ammo.");
		                			pcs.setUnlimAmmoCommanded(true);
		                			pcs.setTurretAmmoStep(1);
		                			pcs.setLockedState(true);
		    	            	}
		            		} else if (subcommand1.equals("useammobox")) {
		            			if(plugin.getPlayerCommander((Player) sender)==null) {
		    	            		PlayerCommandSender pcs = new PlayerCommandSender((Player) sender);
		                			plugin.playerCommanders.add(pcs);
		                			pcs.getPlayer().sendMessage("Click turret to set to use its ammo box.");
		                			pcs.setUnlimAmmoCommanded(false);
		                			pcs.setTurretAmmoStep(1);
		                			pcs.setLockedState(true);
		    	            	}
		            		} else sender.sendMessage("Correct usage: /turrets setAmmoType unlimited/useAmmoBox");
		            	} else sender.sendMessage("Correct usage: /turrets setAmmoType unlimited/useAmmoBox");
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
	            		try {
							Reload.turretsReload(sender, plugin);
						} catch (IOException e) {
							e.printStackTrace();
						}
            		}
            	}else{
            		try {
						Reload.turretsReload(sender, plugin);
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            	return(true);
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