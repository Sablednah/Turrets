package me.azazad.bukkit.util;

import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.azazad.turrets.TurretsPlugin;
import me.azazad.turrets.persistence.TurretDatabase;
import me.azazad.turrets.persistence.YAMLTurretDatabase;

public class Reload {
	public static void turretsReload(CommandSender sender, TurretsPlugin plugin) throws IOException {
		TurretDatabase tempTurretDatabase = new YAMLTurretDatabase(new File(plugin.getDataFolder(),"turretsTemp.yml"),plugin);
		tempTurretDatabase.saveTurretsForReload(plugin.getTurretMap().values());
		plugin.getPluginLoader().disablePlugin(plugin);
    	plugin.getPluginLoader().enablePlugin(plugin);
    	tempTurretDatabase.reloadTurrets();
    	sender.sendMessage(ChatColor.YELLOW + "Reloaded "+ ChatColor.GRAY + plugin.getDescription().getFullName());
	}
}
