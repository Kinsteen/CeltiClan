package fr.pitiqui.celticlan;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin
{
	static File dataFolder;
	
	String prefix = ChatColor.RED + "[CeltiClan] " + ChatColor.RESET;
	
	String host;
	String bdd;
	int port;
	String user;
	String pass;
	String url;
	String table_players;
	String table_clan;
	String table_invit;
	
	Config config = new Config();
	ConfigHome configHome = new ConfigHome();

	Connection connection = null;
	static Statement stat;
	
	Location loc = null;
	
	HashMap<String, String> chat = new HashMap<String, String>();
	
	public void onEnable()
	{
		config.setDataFolder(getDataFolder());
		configHome.setDataFolder(getDataFolder());
		
		config.initConfig("BDD.clan.host", "localhost", "BDD.clan.port", "3306", "BDD.clan.database", "CeltiClan", "BDD.clan.user", "root", "BDD.clan.pass", "", "BDD.clan.table_invit", "clan_invit", "BDD.clan.table_players", "clan_players", "BDD.clan.table_clan", "clan_clan");
		configHome.initConfig("", "");
		
		config.loadConfigFile();
		configHome.loadConfigFile();
		
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		
		host = config.loadString("BDD.clan.host");
		bdd = config.loadString("BDD.clan.database");
		port = config.loadInt("BDD.clan.port");
		user = config.loadString("BDD.clan.user");
		pass = config.loadString("BDD.clan.pass");
		table_players = config.loadString("BDD.clan.table_players");
		table_clan = config.loadString("BDD.clan.table_clan");
		table_invit = config.loadString("BDD.clan.table_invit");
				
		try {
			getLogger().info("Loading driver...");
		    Class.forName("com.mysql.jdbc.Driver");
		    getLogger().info("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
		try {
			getLogger().info("Connecting database...");
		    connection = DriverManager.getConnection("jdbc:mysql://"+ host +":"+ port +"/"+ bdd, user, pass);
		    getLogger().info("Database connected!");
		} catch (SQLException e)
		{
		    throw new RuntimeException("Cannot connect the database!", e);
		}
		
		try {
			stat = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void onDisable()
	{
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player p = null;
		PermissionUser user = null;
		if(sender instanceof Player)
		{
			p = (Player) sender;
			user = PermissionsEx.getUser(p);
		}
		
		if(cmd.getName().equalsIgnoreCase("clan"))
		{
			if(args.length == 0)
			{
				p.sendMessage(ChatColor.RED + "CeltiClan v0.1.6");
			}
			else
			{
				switch (args[0])
				{
					case "create":
						if(user.has("clan.create"))
						{
							switch (args.length)
							{
								case 1:
									p.sendMessage(prefix + "Vous devez mettre un nom de clan !");
									break;

								case 2:
									p.sendMessage(prefix + "Vous devez mettre un sigle !");
									break;
									
								case 3:
									try {
										ResultSet res = stat.executeQuery("SELECT clan FROM " + table_clan);
										boolean find = false;
										
										while(res.next())
										{
											if(res.getString("clan").equalsIgnoreCase(args[1]))
											{
												find = true;
												break;
											}
										}
										
										if(find == true)
										{
											p.sendMessage(prefix + ChatColor.RED + "Un clan sous le nom de " + args[1] + " a deja été créer.");
										}
										else
										{
											stat.executeUpdate("INSERT INTO " + table_clan + " (`clan`, `sigle`, `chef`, `invit`) VALUES ('" + args[1] + "', '"+ args[2] +"', '" + p.getUniqueId().toString() + "', '1');");
											stat.executeUpdate("INSERT INTO " + table_players + " (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
											p.sendMessage(prefix + ChatColor.GREEN + "Vous avez crée le clan " + args[1] + " !");
										}
									} catch (SQLException e1) {
										e1.printStackTrace();
									}
									break;
									
								default:
									p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'avez pas la permission de créer un clan !");
						}
						break;
						
					case "join":
						if(user.has("clan.create"))
						{
							switch (args.length)
							{
								case 1:
									p.sendMessage(prefix + "Vous devez mettre un nom de clan !");
									break;
									
								case 2:
									boolean findJoin = false;
									boolean canJoin = false;
									boolean propClan = false;
									boolean invitOff = false;
									ResultSet res = null;
									String clanName = null;
									try
									{
										res  = stat.executeQuery("SELECT * FROM " + table_clan);
										while(res.next())
										{
											if(res.getString("clan").equalsIgnoreCase(args[1]))
											{
												clanName = res.getString("clan");
												findJoin = true;
												canJoin = true;
												break;
											}
										}
										if(canJoin == true)
										{
											res = stat.executeQuery("SELECT * FROM " + table_players);
											while(res.next())
											{
												if(res.getString("joueur").equalsIgnoreCase(p.getUniqueId().toString()))
												{
													propClan = true;
													break;
												}
												else
												{
													propClan = false;
												}
											}
											
											if(propClan != true)
											{
												res = stat.executeQuery("SELECT * FROM " + table_clan);
												while(res.next())
												{
													if(res.getInt("invit") == 1)
													{
														invitOff = true;
														break;
													}
													else
													{
														invitOff = false;
													}
												}
												
												if(invitOff == true)
												{
													stat.executeUpdate("INSERT INTO " + table_players + " (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
													p.sendMessage(prefix + ChatColor.GREEN + "Vous avez rejoint le clan " + args[1] + " !");
												}
												else
												{
													boolean lol = false;
													res = stat.executeQuery("SELECT * FROM " + table_invit);
													while(res.next())
													{
														if(res.getString("joueur").equals(p.getUniqueId().toString()) && res.getString("clan").equalsIgnoreCase(clanName))
														{
															lol = true;
															break;
														}
													}
													if(lol == true)
													{
														stat.executeUpdate("INSERT INTO " + table_players + " (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
														stat.executeUpdate("DELETE FROM " + table_invit + " WHERE joueur='" + p.getUniqueId().toString() + "';");
														p.sendMessage(prefix + ChatColor.GREEN + "Vous avez rejoint le clan " + args[1] + " !");
													}
													else
													{
														p.sendMessage(prefix + ChatColor.RED + "Vous n'avez pas été invité !");
													}
												}
											}
											else
											{
												p.sendMessage(prefix + ChatColor.RED + "Vous ne pouvez pas rejoindre votre clan !");
											}
										}
										
										if(findJoin == false)
										{
											p.sendMessage(prefix + ChatColor.RED + "Le clan n'a pas été trouvé.");
										}
									}
									catch (SQLException e)
									{
										e.printStackTrace();
									}
									break;
									
								default:
									p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage("Vous n'avez pas la permission de fonder un clan !");
						}
						break;
						
					case "setinvit":
						if(user.has("clan.setinvit"))
						{
							switch (args.length)
							{
								case 1:
									p.sendMessage(prefix + ChatColor.RED + "Vous devez précisez si les invitations sont obligatoires ! (true = Invitation obligatoires, false = tout le monde peut rejoindre)");
									break;
									
								case 2:
									boolean findInvit = false;
									ResultSet res = null;
									try
									{
										res = stat.executeQuery("SELECT * FROM " + table_clan);
										
										while(res.next())
										{
											if(res.getString("chef").equalsIgnoreCase(p.getUniqueId().toString()))
											{
												findInvit = true;
												if(args[1].equalsIgnoreCase("true"))
												{
													stat.executeUpdate("UPDATE " + table_clan + " SET invit=0 WHERE `chef`='" + p.getUniqueId().toString() +"'");
													p.sendMessage(prefix + ChatColor.GREEN + "Vous avez défini les invitations de votre clan a privé");
													break;
												}
												else if(args[1].equalsIgnoreCase("false"))
												{
													stat.executeUpdate("UPDATE " + table_clan + " SET invit=1 WHERE `chef`='" + p.getUniqueId().toString() +"'");
													p.sendMessage(prefix + ChatColor.GREEN + "Vous avez défini les invitations de votre clan a libre");
													break;
												}
												else
												{
													p.sendMessage(prefix + ChatColor.RED + "true = Invitation obligatoires, false = tout le monde peut rejoindre");
												}
											}
										}
										
										if(findInvit == false)
										{
											p.sendMessage(prefix + ChatColor.RED + "Vous n'etes pas proprietaires d'un clan !");
											findInvit = false;
										}
									}
									catch (SQLException e)
									{
										e.printStackTrace();
									}
									break;
	
								default:
									p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'avez pas la permission de régler les invitations d'un clan");
						}
						break;
					
					case "leave":
						if(user.has("clan.leave"))
						{
							ResultSet res = null;
							boolean findLeave = false;
							boolean deletePlayer = false;
							String clanName = null;
							try
							{
								res  = stat.executeQuery("SELECT * FROM " + table_players);
								while(res.next())
								{
									if(res.getString("joueur").equalsIgnoreCase(p.getUniqueId().toString()))
									{
										findLeave = true;
										stat.executeUpdate("DELETE FROM " + table_players + " WHERE joueur='" + p.getUniqueId().toString() +"'");
										p.sendMessage(prefix + ChatColor.GREEN + "Vous avez quittez votre clan.");
										break;
									}
								}
								
								if(findLeave == false)
								{
									p.sendMessage(prefix + ChatColor.RED + "Vous n'etes pas dans un clan");
								}
								if(findLeave == true)
								{
									res  = stat.executeQuery("SELECT * FROM " + table_clan);
									while(res.next())
									{
										if(res.getString("chef").equalsIgnoreCase(p.getUniqueId().toString()))
										{
											clanName = res.getString("clan");
											deletePlayer = true;
											stat.executeUpdate("DELETE FROM " + table_clan + " WHERE chef='" + p.getUniqueId().toString() +"'");
											p.sendMessage(prefix + ChatColor.GREEN + "et supprimer votre clan !");
											break;
										}
									}
								}
								
								if(deletePlayer == true)
								{
									stat.executeUpdate("DELETE FROM " + table_players + " WHERE clan='" + clanName +"'");
								}
							}
							catch(SQLException e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'avez pas la permission de quitter un clan !");
						}
						break;
						
					case "invit":
						if(user.has("clan.invit"))
						{
							switch (args.length)
							{
								case 1:
									p.sendMessage(prefix + ChatColor.RED + "Vous devez mettre un joueur à inviter !");
									break;
									
								case 2:
									ResultSet res = null;
									boolean findInvit = false;
									String clanName = null;
									
									try
									{
										res = stat.executeQuery("SELECT * FROM " + table_clan);
										
										while(res.next())
										{
											if(res.getString("chef").equalsIgnoreCase(p.getUniqueId().toString()))
											{
												findInvit = true;
												clanName = res.getString("clan");
												break;
											}
										}
										
										if(findInvit == true)
										{
											try {
												stat.executeUpdate("INSERT INTO `" + table_invit + "`(`clan`, `joueur`) VALUES ('" + clanName + "','" + Bukkit.getServer().getPlayer(args[1]).getUniqueId().toString() + "')");
											} catch (SQLException e) {
												e.printStackTrace();
											}
											p.sendMessage(prefix + ChatColor.GREEN + "Vous avez invité " + args[1]);
										}
										else
										{
											p.sendMessage(prefix + ChatColor.RED + "Vous n'etes pas proprietaire d'un clan");
										}
									}
									catch (SQLException e1)
									{
										e1.printStackTrace();
									}
									break;
	
								default:
									p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'avez pas la permission d'inviter un joueur !");
						}
						break;
						
					case "list":
						if(user.has("clan.list"))
						{
							switch (args.length)
							{
								case 1:
									ResultSet res = null;
									
									p.sendMessage(prefix + ChatColor.YELLOW + "Liste des clans :");
									
									try
									{
										res = stat.executeQuery("SELECT * FROM " + table_clan);
										
										while(res.next())
										{
											p.sendMessage(prefix + ChatColor.YELLOW + res.getString("clan"));
										}
									}
									catch(SQLException e)
									{
										e.printStackTrace();
									}
									break;
									
								case 2:
									ResultSet res1 = null;
									try
									{
										p.sendMessage(prefix + ChatColor.YELLOW + "Liste des joueurs du clan " + args[1] + ":");
										res1 = stat.executeQuery("SELECT * FROM " + table_players + " WHERE clan='" + args[1] + "';");
										while(res1.next())
										{
											UUID uuid = UUID.fromString(res1.getString("joueur"));
											p.sendMessage(prefix + ChatColor.YELLOW + getServer().getPlayer(uuid).getName());
										}
									}
									catch(SQLException e)
									{
										e.printStackTrace();
									}
									break;
	
								default:
									p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'avez pas la permission de lister les clans / les joueurs d'un clan !");
						}
						break;
						
					case "kick":
							switch(args.length)
							{
								case 1:
									p.sendMessage(prefix + ChatColor.RED + "Mauvaise usage de la commande ! Ex: /clan delete <nom du clan>");
									break;
									
								case 2:
									ResultSet res = null;
									boolean findInvit = false;
									boolean ok = false;
									String playName = null;
									
									try
									{
										res = stat.executeQuery("SELECT * FROM " + table_clan);
										
										while(res.next())
										{
											if(res.getString("chef").equalsIgnoreCase(p.getUniqueId().toString()))
											{
												try
												{
													playName = getServer().getPlayer(args[1]).getUniqueId().toString();
												}
												catch(NullPointerException e)
												{
													p.sendMessage(prefix + ChatColor.RED + "Le joueur n'existe pas !");
													break;
												}
												findInvit = true;
												ok = true;
												break;
											}
										}
										
										if(ok == true)
										{
											if(findInvit == true)
											{
												boolean youMad = false;
												res = stat.executeQuery("SELECT * FROM " + table_players);
												
												while(res.next())
												{
													if(res.getString("joueur").equalsIgnoreCase(getServer().getPlayer(args[1]).getUniqueId().toString()))
													{
														youMad = true;
														break;
													}
												}
												
												if(youMad == true)
												{
													stat.executeUpdate("DELETE FROM " + table_players + " WHERE joueur='" + playName + "'");
													p.sendMessage(prefix + ChatColor.GREEN + "Le joueur " + args[1] + " a été kické !");
													for(Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
													{
														if(onlinePlayer.getName().equalsIgnoreCase(args[1]))
														{
															onlinePlayer.sendMessage(prefix + ChatColor.YELLOW + "Vous avez été expulsé de votre clan par " + p.getName() + " !");
														}
													}
												}
												else
												{
													p.sendMessage(prefix + ChatColor.RED + "Le joueur n'est pas dans votre clan !");
												}
											}
											else
											{
												p.sendMessage(prefix + ChatColor.RED + "Vous n'etes pas proprietaire d'un clan !");
											}
										}
									}
									catch(SQLException e)
									{
										e.printStackTrace();
									}
									break;
									
								default:
									break;
							}
						break;
						
					case "admin":
						switch (args[1])
						{
							case "delete":
								if(user.has("clan.admin.delete"))
								{
									switch(args.length)
									{
										case 1:
											p.sendMessage(prefix + ChatColor.GREEN + "Administration du plugin !");
											break;
											
										case 2:
											p.sendMessage(prefix + ChatColor.RED + "Mauvaise usage de la commande ! Ex: /clan delete <nom du clan>");
											break;
											
										case 3:
											try
											{
												boolean find = false;
												ResultSet res = null;
												String clanName = null;
												
												res = stat.executeQuery("SELECT * FROM " + table_clan);
												
												while(res.next())
												{
													if(args[2].equalsIgnoreCase(res.getString("clan")))
													{
														clanName = res.getString("clan");
														find = true;
														break;
													}
												}
												
												if(find == true)
												{
													stat.executeUpdate("DELETE FROM " + table_clan + " WHERE clan='" + clanName + "'");
													stat.executeUpdate("DELETE FROM " + table_players + " WHERE clan='" + clanName + "'");
													p.sendMessage(prefix + ChatColor.GREEN + "Le clan " + clanName + " a été supprimé !");
												}
												else
												{
													p.sendMessage(prefix + ChatColor.RED + "Le clan n'a pas été trouvé !");
												}
											}
											catch (SQLException e)
											{
												e.printStackTrace();
											}
											break;
					
										default:
											p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
											break;
									}
								}
								else
								{
									p.sendMessage(prefix + ChatColor.RED + "Vous n'avez pas la permission de supprimer un clan !");
								}
								break;
								
							case "kick":
								if(user.has("clan.admin.kick"))
								{
									switch(args.length)
									{
										case 1:
											p.sendMessage(prefix + ChatColor.GREEN + "Administration du plugin !");
											break;
											
										case 2:
											p.sendMessage(prefix + ChatColor.RED + "Mauvaise usage de la commande ! Ex: /clan kick <nom du joueur>");
											break;
											
										case 3:
											try
											{
												boolean find = false;
												ResultSet res = null;
												String playName = null;
												
												res = stat.executeQuery("SELECT * FROM " + table_clan);
												
												while(res.next())
												{
													if(getServer().getPlayer(args[2]).getUniqueId().toString().equalsIgnoreCase(res.getString("joueur")))
													{
														playName = getServer().getPlayer(args[2]).getUniqueId().toString();
														find = true;
														break;
													}
												}
												
												if(find == true)
												{
													stat.executeUpdate("DELETE FROM " + table_players + " WHERE joueur='" + playName + "'");
													p.sendMessage(prefix + ChatColor.GREEN + "Le joueur " + args[2] + " a été kické !");
													for(Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
													{
														if(onlinePlayer.getName().equalsIgnoreCase(args[2]))
														{
															onlinePlayer.sendMessage(prefix + ChatColor.YELLOW + "Vous avez été expulsé de votre clan par " + p.getName() + " !");
														}
													}
												}
												else
												{
													p.sendMessage(prefix + ChatColor.RED + "Le joueur n'appartient pas a un clan !");
												}
											}
											catch (SQLException e)
											{
												e.printStackTrace();
											}
											break;
					
										default:
											p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
											break;
									}
								}
								else
								{
									p.sendMessage(prefix + ChatColor.RED + "Vous n'avez pas la permission d'expulser un joueur !");
								}
								break;
	
							default:
								break;
						}
						break;
						
					case "sethome":
						if(isChef(p))
						{
							configHome.setElement("home." + getClan(p) + ".world", p.getLocation().getWorld().getName());
							configHome.setElement("home." + getClan(p) + ".x", p.getLocation().getBlockX());
							configHome.setElement("home." + getClan(p) + ".y", p.getLocation().getBlockY());
							configHome.setElement("home." + getClan(p) + ".z", p.getLocation().getBlockZ());
							configHome.setElement("home." + getClan(p) + ".pitch", p.getLocation().getPitch());
							configHome.setElement("home." + getClan(p) + ".yaw", p.getLocation().getYaw());
							p.sendMessage(prefix + ChatColor.GREEN + "Le home de votre clan a été placé !");
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'etes pas proprietaire du clan !");
						}
						break;
						
					case "home":
						if(isInClan(p))
						{
							p.sendMessage(prefix + ChatColor.GREEN + "Téléportation dans 5 secondes...");
							final Player player = p;
							loc = null;
							try
							{
								loc = new Location(Bukkit.getServer().getWorld(configHome.loadString("home." + getClan(p) + ".world")), configHome.loadInt("home." + getClan(p) + ".x"), configHome.loadInt("home." + getClan(p) + ".y"), configHome.loadInt("home." + getClan(p) + ".z"), (float) configHome.loadDouble("home." + getClan(p) + ".yaw"), (float) configHome.loadDouble("home." + getClan(p) + ".pitch"));
								Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
								{
									@Override
									public void run()
									{
										player.teleport(loc);
									}
								}, 60L);
							}
							catch(IllegalArgumentException e)
							{
								p.sendMessage(prefix + ChatColor.RED + "Le home de votre clan a été placé !");
							}
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'etes pas dans un clan");
						}
						break;
					
					default:
						p.sendMessage(prefix + ChatColor.RED + "Cette commande n'a pas été trouvé !");
						break;
				}
			}
		}
		return false;
	}
	
	public String getClan(Player p)
	{
		ResultSet res = null;
		String result = null;
		
		try
		{
			res = stat.executeQuery("SELECT clan FROM " + table_players + " WHERE joueur='" + p.getUniqueId() + "'");
			
			while(res.next())
			{
				result = res.getString("clan");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public boolean isInClan(Player player)
	{
		ResultSet res = null;
		boolean find = false;
		
		try
		{
			res = stat.executeQuery("SELECT * FROM " + table_players + " WHERE joueur='" + player.getUniqueId().toString() + "'");
			
			while(res.next())
			{
				if(res.getString("joueur").equalsIgnoreCase(player.getUniqueId().toString()))
				{
					find = true;
					break;
				}
			}
			
			if(find == true)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isChef(Player player)
	{
		ResultSet res = null;
		boolean find = false;
		try
		{
			res = stat.executeQuery("SELECT chef FROM " + table_clan + " WHERE chef='" + player.getUniqueId().toString() + "'");
			
			while(res.next())
			{
				if(res.getString("chef").equalsIgnoreCase(player.getUniqueId().toString()))
				{
					find = true;
				}
			}
			
			if(find == true)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public void reloadconfig()
	{
		config.loadConfigFile();
		
		config.initConfig("BDD.clan.host", "localhost", "BDD.clan.port", "3306", "BDD.clan.database", "CeltiClan", "BDD.clan.user", "root", "BDD.clan.pass", "", "BDD.clan.table_invit", "clan_invit", "BDD.clan.table_players", "clan_players", "BDD.clan.table_clan", "clan_clan");
		
		host = config.loadString("BDD.clan.host");
		bdd = config.loadString("BDD.clan.database");
		port = config.loadInt("BDD.clan.port");
		user = config.loadString("BDD.clan.user");
		pass = config.loadString("BDD.clan.pass");
		
		try {
			getLogger().info("Connecting database...");
		    connection = DriverManager.getConnection("jdbc:mysql://"+ host +":"+ port +"/"+ bdd, user, pass);
		    getLogger().info("Database connected!");
		} catch (SQLException e)
		{
		    throw new RuntimeException("Cannot connect the database!", e);
		}
		
		try {
			stat = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
