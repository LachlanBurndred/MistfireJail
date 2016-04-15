package me.MistfireWolf.Jail;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class JailedPlayerCommandListener implements Listener 
{

	Main configGetter;

	public JailedPlayerCommandListener(Main plugin)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		configGetter = plugin;
	}
	
	boolean denied;
	
	@EventHandler
	public void PlayerCommandPrerocessEvent(PlayerCommandPreprocessEvent preCommand)
	{
		Player player = preCommand.getPlayer();
		boolean InJail = configGetter.JFile.getBoolean("JailedPlayers." + player.getName() + ".InJail");
		if (InJail == true)
		{
			List<String> commandList = configGetter.JConfig.getStringList("Jailed.Allowed.Commands");
			for (String command : commandList)
			{
				
				if (preCommand.getMessage().equalsIgnoreCase("/" + command.toLowerCase()))
				{
					return;
				}
				else
				{
					denied = true;
				}
			}
			
			if (denied == true)
			{
				preCommand.setCancelled(true);
				player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You cannot use that command in jail!");
			}
			else
			{
				return;
			}
		}
	}
	
}
