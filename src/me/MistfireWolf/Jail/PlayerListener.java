package me.MistfireWolf.Jail;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	
	Main configGetter;

	public PlayerListener(Main plugin)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		configGetter = plugin;
	}
	
	long tStart;
	long tEnd;
	
	
	@SuppressWarnings({ })
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{

		tStart = System.currentTimeMillis();
		
		if (configGetter.JFile.get("JailedPlayers." + event.getPlayer().getName()) != null)
		{
			Log.info("Player has name in config");
			boolean JailedStatus = configGetter.JFile.getBoolean("JailedPlayers." + event.getPlayer().getName() + ".JailedStatus");
			boolean Unjailed = configGetter.JFile.getBoolean("JailedPlayers." + event.getPlayer().getName() + ".Unjailed");
			boolean InJail = configGetter.JFile.getBoolean("JailedPlayers." + event.getPlayer().getName() + ".InJail");
			if (JailedStatus == true)
			{
				Log.info("STAGE 1");
				Log.info("player is jailed");
				configGetter.setTPDest(event.getPlayer().getName());
				
				double x = (double) configGetter.JFile.get("Jail.X");
				double y = (double) configGetter.JFile.get("Jail.Y");
				double z = (double) configGetter.JFile.get("Jail.Z");
				String world = configGetter.JFile.getString("Jail.World");
				World tpWorld = Bukkit.getWorld(world);
				double dpitch = (double) configGetter.JFile.get("Jail.pitch");
				double dyaw = (double) configGetter.JFile.get("Jail.yaw");
				float pitch = (float) dpitch;
				float yaw = (float) dyaw;
				Location JLocation = new Location(tpWorld, x, y, z, yaw, pitch);
				event.getPlayer().teleport(JLocation);
				event.getPlayer().sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You have been jailed!");
				configGetter.JFile.set("JailedPlayers." + event.getPlayer().getName() + ".JailedStatus", false);
				configGetter.JFile.set("JailedPlayers." + event.getPlayer().getName() + ".InJail", true);
				configGetter.saveJRFile();
				Log.info("STAGE 2");
				
				configGetter.checkPlayerStatus(event.getPlayer().getName());
				configGetter.online = true;
				
			}
			else if (Unjailed == true && InJail == true)
			{

				double X = (double) configGetter.JFile.get("JailedPlayers." + event.getPlayer().getName() + ".TPDestX");
				double Y = (double) configGetter.JFile.get("JailedPlayers." + event.getPlayer().getName() + ".TPDestY");
				double Z = (double) configGetter.JFile.get("JailedPlayers." + event.getPlayer().getName() + ".TPDestZ");
				double dyaw = (double) configGetter.JFile.get("JailedPlayers." + event.getPlayer().getName() + ".TPDestYaw");
				double dpitch = (double) configGetter.JFile.get("JailedPlayers." + event.getPlayer().getName() + ".TPDestPitch");
				float yaw = (float) dyaw;
				float pitch = (float) dpitch;
				String world = configGetter.JFile.getString("JailedPlayers." + Bukkit.getPlayer(event.getPlayer().getName()).getName() + ".TPDestW");
				World tpWorld = Bukkit.getServer().getWorld(world);
				Location TPDest = new Location(tpWorld, X, Y, Z, yaw, pitch);
				event.getPlayer().teleport(TPDest);
				
				configGetter.JFile.set("JailedPlayers." + event.getPlayer().getName() + ".InJail", false);
				configGetter.JFile.set("JailedPlayers." + event.getPlayer().getName() + ".RemainingTime", 0);
				configGetter.saveJRFile();
				event.getPlayer().sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " You have been unjailed!");
				configGetter.online = false;
				
			}
			else if (InJail == true)
			{
				configGetter.checkPlayerStatus(event.getPlayer().getName());
				configGetter.online = true;
			}
		}
		else
		{
			configGetter.JFile.set("JailedPlayers." + event.getPlayer().getName() + ".JailedStatus", false);
			configGetter.JFile.set("JailedPlayers." + event.getPlayer().getName() + ".InJail", false);
			configGetter.JFile.set("JailedPlayers." + event.getPlayer().getName() + ".Unjailed", true);
			configGetter.saveJRFile();
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		boolean InJail = configGetter.JFile.getBoolean("JailedPlayers." + event.getPlayer().getName() + ".InJail");
		if (InJail == true)
		{
			String playerName = event.getPlayer().getName();
			
			long RemainingTimeOld = configGetter.JFile.getLong("JailedPlayers." + playerName + ".RemainingTime");
			configGetter.EndTime = System.currentTimeMillis();
			long timeElapsed = configGetter.EndTime - configGetter.JoinTime;
			long RemainingTimeNew = RemainingTimeOld - timeElapsed;
			
			configGetter.JFile.set("JailedPlayers." + playerName + ".RemainingTime", RemainingTimeNew);
			configGetter.saveJRFile();
			configGetter.online = false;
			configGetter.checkPlayerStatus(event.getPlayer().getName());
		}
	}
}
