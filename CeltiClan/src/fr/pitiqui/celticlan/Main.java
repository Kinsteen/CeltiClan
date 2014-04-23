package fr.pitiqui.celticlan;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import fr.pitiqui.celticlan.Config;

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

	Connection connection = null;
	static Statement stat;
	
	HashMap<String, String> chat = new HashMap<String, String>();
	
	public void onEnable()
	{
		dataFolder = this.getDataFolder();
		
		Config.loadConfigFile();
		
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		
		host = (String) Config.loadElement("BDD.clan.host");
		bdd = (String) Config.loadElement("BDD.clan.database");
		port = (int) Config.loadInt("BDD.clan.port");
		user = (String) Config.loadElement("BDD.clan.user");
		pass = (String) Config.loadElement("BDD.clan.pass");
		
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
		for(Player p : Bukkit.getServer().getOnlinePlayers())
		{
			chat.remove(p.getUniqueId());
		}
	}
	
	public static File dataFolder()
	{
		return dataFolder;
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
				p.sendMessage(ChatColor.RED + "CeltiClan v0.1.2");
			}
			else
			{
				if(args[0].equalsIgnoreCase("create") && user.has("clan.create"))
				{
					if(args.length == 1)
					{
						p.sendMessage(prefix + "Vous devez mettre un nom de clan !");
					}
					else
					{
						if(args.length == 2)
						{
							p.sendMessage(prefix + "Vous devez mettre un sigle !");
						}
						else
						{
							try
							{
								stat.executeUpdate("INSERT INTO core_clan (`clan`, `sigle`, `chef`, `invit`) VALUES ('" + args[1] + "', '"+ args[2] +"', '" + p.getUniqueId().toString() + "', '1');");
								stat.executeUpdate("INSERT INTO core_players (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
								p.sendMessage(prefix + ChatColor.GREEN + "Vous avez crée le clan " + args[1] + " !");
							}
							catch (SQLException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
				
				if(args[0].equalsIgnoreCase("join") && user.has("clan.join"))
				{
					if(args.length == 1)
					{
						p.sendMessage(prefix + ChatColor.RED + "Vous devez mettre un nom de clan !");
					}
					else
					{
						boolean findJoin = false;
						boolean canJoin = false;
						ResultSet res = null;
						String clanName = null;
						try
						{
							res  = stat.executeQuery("SELECT * FROM core_clan");
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
								boolean propClan = false;
								boolean invitOff = false;
								res = stat.executeQuery("SELECT * FROM core_players");
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
									res = stat.executeQuery("SELECT * FROM core_clan");
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
										stat.executeUpdate("INSERT INTO core_players (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
										p.sendMessage(prefix + ChatColor.GREEN + "Vous avez rejoint le clan " + args[1] + " !");
									}
									else
									{
										boolean lol = false;
										res = stat.executeQuery("SELECT * FROM core_invit");
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
											stat.executeUpdate("INSERT INTO core_players (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
											stat.executeUpdate("DELETE FROM core_invit WHERE joueur='" + p.getUniqueId().toString() + "';");
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
					}
				}
				
				if(args[0].equalsIgnoreCase("setinvit") && user.has("clan.setinvit"))
				{
					if(args.length == 1)
					{
						p.sendMessage(prefix + ChatColor.RED + "Vous devez précisez si les invitations sont obligatoires ! (true = Invitation obligatoires, false = tout le monde peut rejoindre)");
					}
					else
					{
						boolean findInvit = false;
						ResultSet res = null;
						try
						{
							res = stat.executeQuery("SELECT * FROM core_clan");
							
							while(res.next())
							{
								if(res.getString("chef").equalsIgnoreCase(p.getUniqueId().toString()))
								{
									findInvit = true;
									if(args[1].equalsIgnoreCase("true"))
									{
										stat.executeUpdate("UPDATE core_clan SET invit=0 WHERE `chef`='" + p.getUniqueId().toString() +"'");
										p.sendMessage(prefix + ChatColor.GREEN + "Vous avez défini les invitations de votre clan a privé");
										break;
									}
									else if(args[1].equalsIgnoreCase("false"))
									{
										stat.executeUpdate("UPDATE core_clan SET invit=1 WHERE `chef`='" + p.getUniqueId().toString() +"'");
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
					}
				}
				
				if(args[0].equalsIgnoreCase("leave") && user.has("clan.leave"))
				{
					ResultSet res = null;
					boolean findLeave = false;
					boolean deletePlayer = false;
					String clanName = null;
					try
					{
						res  = stat.executeQuery("SELECT * FROM core_players");
						while(res.next())
						{
							if(res.getString("joueur").equalsIgnoreCase(p.getUniqueId().toString()))
							{
								findLeave = true;
								stat.executeUpdate("DELETE FROM core_players WHERE joueur='" + p.getUniqueId().toString() +"'");
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
							res  = stat.executeQuery("SELECT * FROM core_clan");
							while(res.next())
							{
								if(res.getString("chef").equalsIgnoreCase(p.getUniqueId().toString()))
								{
									clanName = res.getString("clan");
									deletePlayer = true;
									stat.executeUpdate("DELETE FROM core_clan WHERE chef='" + p.getUniqueId().toString() +"'");
									p.sendMessage(prefix + ChatColor.GREEN + "et supprimer votre clan !");
									break;
								}
							}
						}
						
						if(deletePlayer == true)
						{
							stat.executeUpdate("DELETE FROM core_players WHERE clan='" + clanName +"'");
						}
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
				}
				
				if(args[0].equalsIgnoreCase("invit") && user.has("clan.invit"))
				{
					if(args.length == 1)
					{
						p.sendMessage(prefix + ChatColor.RED + "Vous devez mettre un joueur à inviter !");
					}
					else
					{
						if(args.length == 3)
						{
							p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
						}
						else
						{
							ResultSet res = null;
							boolean findInvit = false;
							String clanName = null;
							
							try
							{
								res = stat.executeQuery("SELECT * FROM core_clan");
								
								while(res.next())
								{
									if(res.getString("chef").equalsIgnoreCase(p.getUniqueId().toString()))
									{
										findInvit = true;
										clanName = res.getString("clan");
										break;
									}
								}
							}
							catch (SQLException e1)
							{
								e1.printStackTrace();
							}
								
							if(findInvit == true)
							{
								try {
									stat.executeUpdate("INSERT INTO `core_invit`(`clan`, `joueur`) VALUES ('" + clanName + "','" + Bukkit.getServer().getPlayer(args[1]).getUniqueId().toString() + "')");
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
					}
				}
				
				if(args[0].equalsIgnoreCase("list") && user.has("clan.list"))
				{
					if(args.length == 1)
					{
						ResultSet res = null;
						
						p.sendMessage(prefix + ChatColor.YELLOW + "Liste des clans :");
						
						try
						{
							res = stat.executeQuery("SELECT * FROM core_clan");
							
							while(res.next())
							{
								p.sendMessage(prefix + ChatColor.YELLOW + res.getString("clan"));
							}
						}
						catch(SQLException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						if(args.length == 3)
						{
							p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
						}
						else
						{
							ResultSet res = null;
							try
							{
								p.sendMessage(prefix + ChatColor.YELLOW + "Liste des joueurs du clan " + args[1] + ":");
								res = stat.executeQuery("SELECT * FROM core_players WHERE clan='" + args[1] + "';");
								while(res.next())
								{
									UUID uuid = UUID.fromString(res.getString("joueur"));
									p.sendMessage(prefix + ChatColor.YELLOW + getServer().getPlayer(uuid).getName());
								}
							}
							catch(SQLException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
				
				if(args[0].equalsIgnoreCase("kick") && user.has("clan.kick"))
				{
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
								res = stat.executeQuery("SELECT * FROM core_clan");
								
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
										res = stat.executeQuery("SELECT * FROM core_players");
										
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
											stat.executeUpdate("DELETE FROM core_players WHERE joueur='" + playName + "'");
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
				}
				
				if(args[0].equalsIgnoreCase("admin"))
				{
					if(args[1].equalsIgnoreCase("delete") && user.has("clan.admin.delete"))
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
									
									res = stat.executeQuery("SELECT * FROM core_clan");
									
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
										stat.executeUpdate("DELETE FROM core_clan WHERE clan='" + clanName + "'");
										stat.executeUpdate("DELETE FROM core_players WHERE clan='" + clanName + "'");
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
					
					if(args[1].equalsIgnoreCase("kick") && user.has("clan.admin.kick"))
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
									
									res = stat.executeQuery("SELECT * FROM core_players");
									
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
										stat.executeUpdate("DELETE FROM core_players WHERE joueur='" + playName + "'");
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
				}
				
				if(args[0].equalsIgnoreCase("reload") && user.has("clan.reload"))
				{
					reloadConfig();
					sender.sendMessage(prefix + "Config reloaded !");
				}
			}
		}
		return false;
	}
	
	public String getClan(Player p)
	{
		ResultSet res = null;
		String result = null;
		
		try {
			res = stat.executeQuery("SELECT clan FROM core_players WHERE joueur='" + p.getUniqueId() + "'");
			
			while(res.next())
			{
				result = res.getString("clan");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@SuppressWarnings("deprecation")
	public static ArrayList<Player> getPlayersInClan(String clan)
	{
		ArrayList<Player> arr = new ArrayList<Player>();
		ResultSet res = null;
		
		try {
			res = stat.executeQuery("SELECT * FROM core_players WHERE clan='" + clan + "'");
			
			while(res.next())
			{
				arr.add(Bukkit.getServer().getPlayer(res.getString("joueur")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return arr;
	}
	
	public void reloadConfig()
	{
		Config.loadConfigFile();
		
		host = (String) Config.loadElement("BDD.clan.host");
		bdd = (String) Config.loadElement("BDD.clan.database");
		port = (int) Config.loadInt("BDD.clan.port");
		user = (String) Config.loadElement("BDD.clan.user");
		pass = (String) Config.loadElement("BDD.clan.pass");
	}
}
