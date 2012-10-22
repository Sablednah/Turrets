package me.azazad.bukkit.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockFinder {
	 public static Block getBlockXAway(Player p, double distance) {
		 Location ploc = p.getEyeLocation();
		 double yaw = ploc.getYaw();
		 double pitch = ploc.getPitch();
		 p.sendMessage("Pitch: " + Double.toString(pitch));
		 p.sendMessage("Yaw: " + Double.toString(yaw));
		 int xx = (int) (ploc.getBlockX() + distance*Math.sin(Math.toRadians(-yaw+360))*Math.sin(Math.toRadians(pitch+90)));
		 int yy = (int) (ploc.getBlockY() + distance*Math.cos(Math.toRadians(pitch+90)));
		 int zz = (int) (ploc.getBlockZ() + distance*Math.cos(Math.toRadians(yaw))*Math.sin(Math.toRadians(pitch+90)));
		 p.sendMessage("ploc.x - target.x = " + (ploc.getBlockX()-xx));
		 p.sendMessage("ploc.y - target.y = " + (ploc.getBlockY()-yy));
		 p.sendMessage("ploc.z - target.z = " + (ploc.getBlockZ()-zz));
		 Location nloc = new Location(ploc.getWorld(), xx, yy, zz);
		 Block b = nloc.getBlock();
		 return b;
	 }
}
