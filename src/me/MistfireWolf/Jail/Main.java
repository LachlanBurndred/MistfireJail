package me.MistfireWolf.Jail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin 
{

	Logger log = Bukkit.getLogger();
	
	// Creating accessible instance of jailRecord.yml
	public File jailRecordFile = new File(getDataFolder(), "jailRecord.yml");
	public FileConfiguration JFile = YamlConfiguration.loadConfiguration(jailRecordFile);
	
	// Creating accessible instance of jailConfig.yml
	public File jailConfig = new File(getDataFolder(), "jailConfig.yml");
	public FileConfiguration JConfig = YamlConfiguration.loadConfiguration(jailConfig);
	
	// Controls player jail time if the server is reloaded.
	private void ReloadEnableCheck()
	{
		for (Player player : Bukkit.getServer().getOnlinePlayers())
		{
			String playerName = player.getName();
			if (JFile.getBoolean("JailedPlayers." + playerName + ".InJail") == true)
			{
				checkPlayerStatus(playerName);
			}
			else
			{
				return;
			}
		}
	}
	
	// Saves all jail info on players before plugin is disabled.
	private void ReloadDisableCheck()
	{
		EndTime = System.currentTimeMillis();
		for (Player player : Bukkit.getOnlinePlayers())
		{
			String playerName = player.getName();
			if (JFile.getBoolean("JailedPlayers." + playerName + ".InJail") == true)
			{
				long Delta = EndTime - JoinTime;
				long tR = JFile.getLong("JailedPlayers." + playerName + ".RemainingTime");
				JFile.set("JailedPlayers." + playerName + ".RemainingTime", tR - Delta);
			}
		}
	}
	
	// Actions when the plugin is enabled on the server.
	@Override
	public void onEnable()
	{
		log.info("MistfireJail has started!");
		loadJRFile();
		loadJConfig();
		new PlayerListener(this);
		new JailedPlayerCommandListener(this);
		ReloadEnableCheck();
	}
	
	// Actions when the plugin is disabled on the server.
	@Override
	public void onDisable()
	{
		ReloadDisableCheck();
		log.info("MistfireJail has disabled!");
		saveJConfig();
		saveJRFile();
	}
	
	// Saves jailRecord.yml
	public void saveJRFile()
	{
		try
		{
			JFile.save(jailRecordFile);
		}
		catch(Exception e)
		{
			return;
		}
	}
	
	// Loads jailRecord.yml
	public void loadJRFile()
	{
		if(jailRecordFile.exists())
		{
			try
			{
				JFile.load(jailRecordFile);
			}
			catch (Exception e)
			{
				return;
			}
		}
		else
		{
			try
			{
				JFile.set("Jail.X", null);
				JFile.set("Jail.Y", null);
				JFile.set("Jail.Z", null);
				JFile.set("Jail.World", "");
				JFile.set("Jail.yaw", null);
				JFile.set("Jail.pitch", null);
				JFile.save(jailRecordFile);
			}
			catch (Exception e)
			{
				return;
			}
		}
	}
	
	// Saves jailConfig.yml
	public void saveJConfig()
	{
		try
		{
			JConfig.save(jailConfig);
		}
		catch (Exception e)
		{
			log.info("Error saving config");
		}
	}
	
	// Loads jailConfig.yml
	public void loadJConfig()
	{
		if (jailConfig.exists())
		{
			try
			{
				JConfig.load(jailConfig);
			}
			catch (Exception e)
			{
				log.info("Error loading config");
			}
		}
		else
		{
			try
			{
				List <String> exampleCommandList = new ArrayList <String>();
				exampleCommandList.add("listwarns");
				exampleCommandList.add("rules");
				exampleCommandList.add("jailtime");
				JConfig.set("Jailed.Comments", "All commands need to be listed WITHOUT the '/' (forward slash)");
				JConfig.set("Jailed.Allowed.Commands", exampleCommandList);
				saveJConfig();
			}
			catch (Exception e)
			{
				log.info("Error creating config");
			}
		}
	}
	
	// Accessible function that sets the location the player will be TPd to once unjailed.
	public void setTPDest(String DestPlayer)
	{
		JFile.set("JailedPlayers." + Bukkit.getPlayer(DestPlayer).getName() + ".TPDestW", Bukkit.getPlayer(DestPlayer).getWorld().getName());
		JFile.set("JailedPlayers." + Bukkit.getPlayer(DestPlayer).getName() + ".TPDestX", Bukkit.getPlayer(DestPlayer).getLocation().getX());
		JFile.set("JailedPlayers." + Bukkit.getPlayer(DestPlayer).getName() + ".TPDestY", Bukkit.getPlayer(DestPlayer).getLocation().getY());
		JFile.set("JailedPlayers." + Bukkit.getPlayer(DestPlayer).getName() + ".TPDestZ", Bukkit.getPlayer(DestPlayer).getLocation().getZ());
		JFile.set("JailedPlayers." + Bukkit.getPlayer(DestPlayer).getName() + ".TPDestYaw", Bukkit.getPlayer(DestPlayer).getLocation().getYaw());
		JFile.set("JailedPlayers." + Bukkit.getPlayer(DestPlayer).getName() + ".TPDestPitch", Bukkit.getPlayer(DestPlayer).getLocation().getPitch());
		saveJRFile();
	}
	
	// Boolean used to check if a player is online or not.
	boolean online = false;
	
	// Time variables to calculate and format time remaining.
	long timeInMillSec;
	long timeInSec;
	long timeInMin;
	long timeInHours;
	long JoinTime;
	long EndTime;
	
	// Checks if a player is jailed or unjailed.
	@SuppressWarnings("deprecation")
	public void checkPlayerStatus(String name)
	{
		online = true;
		JoinTime = System.currentTimeMillis();
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
		{
			public void run() 
			{
				if (online == true)
				{
					EndTime = System.currentTimeMillis();
					long Delta = 0;
					Delta += EndTime - JoinTime;
					long JailTime = JFile.getLong("JailedPlayers." + name + ".RemainingTime");
					long Remainder = JailTime - Delta;
					timeInMillSec = Remainder;
					timeInSec = (timeInMillSec / 1000) % 60;
					timeInMin = (timeInMillSec / (1000 * 60)) % 60;
					if (Delta >= JailTime)
					{
						try
						{
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unjail " + Bukkit.getPlayer(name).getName());
							online = false;
							return;
						}
						catch (Exception e)
						{
							return;
						}
					}
				}
			}
				
		}, 0L, 20L);
		
	}
	
	// Main
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[] )
	{
		
		if (commandLabel.equalsIgnoreCase("tpjail"))
		{
			TeleportToJail(sender);
		}
		
		if (commandLabel.equalsIgnoreCase("setjail"))
		{
			SetJailLocation(sender);
		}
		
		if (commandLabel.equalsIgnoreCase("jail"))
		{
			JailPlayer(sender, args);
		}
		
		if (commandLabel.equalsIgnoreCase("unjail"))
		{
			UnjailPlayer(sender, args);
		}
		
		if (commandLabel.equals("jailtime"))
		{
			DisplayRemainingJailTime(sender, args);
		}
		
		return false;
		
	}
	// END Main
	
	// Teleports the sender to jail if all checks pass.
	private void TeleportToJail(CommandSender _sender)
	{
		if (_sender.hasPermission("mistifre.tpjail"))
		{
			if (_sender instanceof Player)
			{
				
				Player player = (Player) _sender;
				
				double x = (double) JFile.get("Jail.X");
				double y = (double) JFile.get("Jail.Y");
				double z = (double) JFile.get("Jail.Z");
				String world = JFile.getString("Jail.World");
				World tpWorld = Bukkit.getWorld(world);
				double dpitch = (double) JFile.get("Jail.pitch");
				double dyaw = (double) JFile.get("Jail.yaw");
				float yaw = (float) dyaw;
				float pitch = (float) dpitch;
				
				Location JLocation = new Location(tpWorld, x, y, z, yaw, pitch);
				player.teleport(JLocation);
				_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.GOLD + " Teleported to jail!");
			
			}
			else
			{
				_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " Console cannot teleport.");
			}
		}
		else
		{
			_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You do not have permission to perform this command.");
		}	
	}

	// Sets the location (coords) of the jail.
	private void SetJailLocation(CommandSender _sender)
	{
		if (_sender.hasPermission("mistfire.setjail"))
		{
			if (_sender instanceof Player)
			{
				
				Player player = (Player) _sender;
				
				Player playerObject = player.getPlayer();
				World JWorld = playerObject.getWorld();
				double LocX = (double) playerObject.getLocation().getX();
				double LocY = (double) playerObject.getLocation().getY();
				double LocZ = (double) playerObject.getLocation().getZ();
				double JPitch = playerObject.getLocation().getPitch();
				double JYaw = playerObject.getLocation().getYaw();
				JFile.set("Jail.X", LocX);
				JFile.set("Jail.Y", LocY);
				JFile.set("Jail.Z", LocZ);
				JFile.set("Jail.World", JWorld.getName());
				JFile.set("Jail.yaw", JYaw);
				JFile.set("Jail.pitch", JPitch);
				saveJRFile();
				_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " Jail Set!");
			}
			else
			{
				_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " Console cannot set jail.");
			}
		}
		else
		{
			_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You do not have permission to perform this command.");
		}
	}
	
	// Jails the player specified in the command.
	@SuppressWarnings({ "deprecation", "unused" })
	private void JailPlayer(CommandSender _sender, String args[])
	{
		if (_sender.hasPermission("mistfire.jail"))
		{
			if (args.length > 0)
			{
				if (args.length > 1)
				{
					if (args.length >= 2)
					{
						if (Bukkit.getPlayerExact(args[0]) != null)
						{
							String playerName = Bukkit.getPlayer(args[0]).getName();
							boolean InJail = JFile.getBoolean("JailedPlayers." + playerName + ".InJail");
							if (InJail == true)
							{
								log.info("Player is already Jailed");
								_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + "That player is already jailed!");
							}
							else
							{	
								try
								{
									try
									{
										setTPDest(args[0]);
										
										int TimeInSeconds = Integer.parseInt(args[1]) * 60;
										int TimeInMillSeconds = TimeInSeconds * 1000;
										
										double x = (double) JFile.get("Jail.X");
										double y = (double) JFile.get("Jail.Y");
										double z = (double) JFile.get("Jail.Z");
										String world = JFile.getString("Jail.World");
										World tpWorld = Bukkit.getWorld(world);
										double dpitch = (double) JFile.get("Jail.pitch");
										double dyaw = (double) JFile.get("Jail.yaw");
										float pitch = (float) dpitch;
										float yaw = (float) dyaw;
										
										Location JLocation = new Location(tpWorld, x, y, z, yaw, pitch);
										
										if (JLocation != null)
										{
											Bukkit.getPlayer(playerName).teleport(JLocation);
											Bukkit.getPlayer(playerName).sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You have been jailed!");
											_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You have jailed " + Bukkit.getPlayer(args[0]).getName());
											JFile.set("JailedPlayers." + playerName + ".InJail", true);
											JFile.set("JailedPlayers." + playerName + ".Unjailed", false);
											JFile.set("JailedPlayers." + playerName + ".RemainingTime", TimeInMillSeconds);
											saveJRFile();
											
											checkPlayerStatus(playerName);
										}
										else
										{
											_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + "Jail is not set!");
										}
									}
									catch (Exception f)
									{
										_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + "Jail is not set!");
									}
								}
								catch (Exception e)
								{
									try
									{
										setTPDest(args[0]);
										
										int TimeInSeconds = Integer.parseInt(args[1]) * 60;
										int TimeInMillSeconds = TimeInSeconds * 1000;
										
										double x = (double) JFile.get("Jail.X");
										double y = (double) JFile.get("Jail.Y");
										double z = (double) JFile.get("Jail.Z");
										String world = JFile.getString("Jail.World");
										World tpWorld = Bukkit.getWorld(world);
										float dpitch = (float) JFile.get("Jail.pitch");
										float dyaw = (float) JFile.get("Jail.yaw");
										float pitch = (float) dpitch;
										float yaw = (float) dyaw;
										
										Location JLocation = new Location(tpWorld, x, y, z, yaw, pitch);
										
										if (JLocation != null)
										{
											Bukkit.getPlayer(playerName).teleport(JLocation);
											Bukkit.getPlayer(playerName).sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You have been jailed!");
											_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You have jailed " + Bukkit.getPlayer(args[0]).getName());
											JFile.set("JailedPlayers." + playerName + ".InJail", true);
											JFile.set("JailedPlayers." + playerName + ".Unjailed", false);
											JFile.set("JailedPlayers." + playerName + ".RemainingTime", TimeInMillSeconds);
											saveJRFile();
											
											checkPlayerStatus(playerName);
										}
										else
										{
											_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + "Jail is not set!");
										}
									}
									catch (Exception f)
									{
										_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + "Jail is not set!");
									}
								}
							}
						}
						else if (Bukkit.getOfflinePlayer(args[0]) != null)
						{
							JFile.set("JailedPlayers." + Bukkit.getOfflinePlayer(args[0]).getName() + ".JailedStatus", true);
							JFile.set("JailedPlayers." + Bukkit.getOfflinePlayer(args[0]).getName() + ".InJail", true);
							JFile.set("JailedPlayers." + Bukkit.getOfflinePlayer(args[0]).getName() + ".Unjailed", false);

							String playerName = Bukkit.getOfflinePlayer(args[0]).getName();
							int TimeInSeconds = Integer.parseInt(args[1]) * 60;
							int TimeInMillSeconds = TimeInSeconds * 1000;
							JFile.set("JailedPlayers." + playerName + ".RemainingTime", TimeInMillSeconds);
							saveJRFile();
						}
						else
						{
							_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " Player does not exist.");
						}
					}
				}
				else
				{
					_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_AQUA + " /jail [player] [time in minutes]");
				}
			}
			else
			{
				_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_AQUA + " /jail [player] [time in minutes]");
			}
		}
		else
		{
			_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You do not have permission to perform this command.");
		}
	}
	
	// Unjails the player specified in the command.
	@SuppressWarnings("deprecation")
	private void UnjailPlayer(CommandSender _sender, String args[])
	{
		if (_sender.hasPermission("mistfire.unjail"))
		{
			if (args.length > 0)
			{
				if (args.length == 1)
				{
					if (Bukkit.getPlayerExact(args[0]) != null)
					{
						boolean InJail = JFile.getBoolean("JailedPlayers." + Bukkit.getPlayer(args[0]).getName() + ".InJail");
						if (InJail == true)
						{
							try
							{
								String DestPlayer = Bukkit.getPlayer(args[0]).getName();
								
								double X = (double) JFile.get("JailedPlayers." + DestPlayer + ".TPDestX");
								double Y = (double) JFile.get("JailedPlayers." + DestPlayer + ".TPDestY");
								double Z = (double) JFile.get("JailedPlayers." + DestPlayer + ".TPDestZ");
								double dyaw = (double) JFile.get("JailedPlayers." + DestPlayer + ".TPDestYaw");
								double dpitch = (double) JFile.get("JailedPlayers." + DestPlayer + ".TPDestPitch");
								
								float yaw = (float) dyaw;
								float pitch = (float) dpitch;
								
								String world = JFile.getString("JailedPlayers." + Bukkit.getPlayer(DestPlayer).getName() + ".TPDestW");
								World tpWorld = Bukkit.getServer().getWorld(world);
								Location TPDest = new Location(tpWorld, X, Y, Z, yaw, pitch);
								Bukkit.getPlayer(DestPlayer).teleport(TPDest);
								
								JFile.set("JailedPlayers." + Bukkit.getPlayer(args[0]).getName()+ ".JailedStatus", false);
								JFile.set("JailedPlayers." + Bukkit.getPlayer(args[0]).getName() + ".InJail", false);
								JFile.set("JailedPlayers." + Bukkit.getPlayer(args[0]).getName() + ".Unjailed", true);
								JFile.set("JailedPlayers." + Bukkit.getPlayer(args[0]).getName() + ".RemainingTime", 0);
								saveJRFile();
								online = false;
								Bukkit.getPlayer(args[0]).sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " You have been unjailed!");
								_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " You have unjailed " + Bukkit.getPlayer(args[0]).getName());
							}
							catch (Exception e)
							{
								String DestPlayer = Bukkit.getPlayer(args[0]).getName();
								
								double X = (double) JFile.get("JailedPlayers." + DestPlayer + ".TPDestX");
								double Y = (double) JFile.get("JailedPlayers." + DestPlayer + ".TPDestY");
								double Z = (double) JFile.get("JailedPlayers." + DestPlayer + ".TPDestZ");
								float dyaw = (float) JFile.get("JailedPlayers." + DestPlayer + ".TPDestYaw");
								float dpitch = (float) JFile.get("JailedPlayers." + DestPlayer + ".TPDestPitch");
								
								float yaw = (float) dyaw;
								float pitch = (float) dpitch;
								
								String world = JFile.getString("JailedPlayers." + Bukkit.getPlayer(DestPlayer).getName() + ".TPDestW");
								World tpWorld = Bukkit.getServer().getWorld(world);
								Location TPDest = new Location(tpWorld, X, Y, Z, yaw, pitch);
								Bukkit.getPlayer(DestPlayer).teleport(TPDest);
								
								JFile.set("JailedPlayers." + Bukkit.getPlayer(args[0]).getName()+ ".JailedStatus", false);
								JFile.set("JailedPlayers." + Bukkit.getPlayer(args[0]).getName() + ".InJail", false);
								JFile.set("JailedPlayers." + Bukkit.getPlayer(args[0]).getName() + ".Unjailed", true);
								JFile.set("JailedPlayers." + Bukkit.getPlayer(args[0]).getName() + ".RemainingTime", 0);
								saveJRFile();
								online = false;
								Bukkit.getPlayer(args[0]).sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " You have been unjailed!");
								_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " You have unjailed " + Bukkit.getPlayer(args[0]).getName());
							}
						}
						else
						{
							_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + " That player is not jailed!");
						}
					}
					else
					{
						boolean InJail = JFile.getBoolean("JailedPlayers." + Bukkit.getOfflinePlayer(args[0]).getName() + ".InJail");
						if (InJail == true)
						{
							JFile.set("JailedPlayers." + Bukkit.getOfflinePlayer(args[0]).getName() + ".JailedStatus", false);
							JFile.set("JailedPlayers." + Bukkit.getOfflinePlayer(args[0]).getName() + ".InJail", true);
							JFile.set("JailedPlayers." + Bukkit.getOfflinePlayer(args[0]).getName() + ".Unjailed", true);
							JFile.set("JailedPlayers." + Bukkit.getOfflinePlayer(args[0]).getName() + ".RemainingTime", 0);
							saveJRFile();
							_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " You have unjailed " + Bukkit.getOfflinePlayer(args[0]).getName());
						}
						else
						{
							_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + " That player is not jailed!");
						}
					}
				}
				else if (args.length > 1)
				{
					_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + " invalid parameters."); 
					_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_AQUA + " /unjail [player]");
				}
			}
			else
			{
				_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_AQUA + " /unjail [player]");
			}
		}
		else
		{
			_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " You do not have permission to perform this command.");
		}
	}
	
	// Displays how much time the player has remaining until they are automatically unjailed.
	private void DisplayRemainingJailTime(CommandSender _sender, String args[])
	{
		if (args.length == 0)
		{
			if (_sender instanceof Player)
			{
				Player player = (Player) _sender;
				boolean inJail = JFile.getBoolean("JailedPlayers." + player.getName() + ".InJail");
				if (inJail == true)
				{
					String time = String.format("%02d minutes : %02d seconds", timeInMin, timeInSec);
					_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + ChatColor.DARK_AQUA + " You have " + ChatColor.AQUA +  time + ChatColor.DARK_AQUA + " jail time remaining.");
				}
				else
				{
					_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + " You are not in jail!");
				}
			}
			else
			{
				_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + " Console does not have jail time.");
				_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + " Try: /jailtime [player]");
			}
		}
		else if (args.length >= 1)
		{
			if (_sender.hasPermission("mistfire.jailtime.player"))
			{
				try
				{
					boolean playerJailed = JFile.getBoolean("JailedPlayers." + Bukkit.getPlayer(args[0]).getName() + ".InJail");
					if (playerJailed == true)
					{
						String time = String.format("%02d minutes : %02d seconds", timeInMin, timeInSec);
						_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]"+ ChatColor.DARK_AQUA + " " + Bukkit.getPlayer(args[0]).getName() +" has " + ChatColor.AQUA +  time + ChatColor.DARK_AQUA + " jail time remaining.");
					}
					else
					{
						_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + " Player is not jailed or is offline!");
					}
				}
				catch (Exception e)
				{
					_sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_AQUA + "Jail" + ChatColor.DARK_RED + "]" + " Invalid Player.");
				}
			}
		}
	}
	
}
