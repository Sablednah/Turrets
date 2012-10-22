package me.azazad.turrets.nms;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import net.minecraft.server.ItemInWorldManager;
import me.azazad.turrets.nms.EntityTurret;


public class EntityShooter extends net.minecraft.server.EntityPlayer{



private final double ELocX,ELocY,ELocZ;
private final EntityTurret entityTurret;
	
//	public EntityShooter(MinecraftServer minecraftserver, net.minecraft.server.World world, String s, ItemInWorldManager iteminworldmanager, EntityTurret entityTurret, double ELocX, double ELocY, double ELocZ) {
//		super(minecraftserver, world, s, iteminworldmanager);
//		this.entityTurret = entityTurret;
//		this.ELocX = ELocX;
//		this.ELocY = ELocY;
//		this.ELocZ = ELocZ;
//	}

	public EntityShooter(Server server, World world, String s, ItemInWorldManager iteminworldmanager, EntityTurret entityTurret, double ELocX, double ELocY, double ELocZ) {
		super(((CraftServer)server).getServer(), ((CraftWorld)world).getHandle(), s, iteminworldmanager);
		this.entityTurret = entityTurret;
		this.ELocX = ELocX;
		this.ELocY = ELocY;
		this.ELocZ = ELocZ;
	}
	

	@Override
	public void U() {
		this.aa();
	}
}
