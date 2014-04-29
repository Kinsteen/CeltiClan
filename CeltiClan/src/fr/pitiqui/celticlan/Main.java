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
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin
{
	static File dataFolder;
	
	static String prefix = ChatColor.RED + "[CeltiClan] " + ChatColor.RESET;
	
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
	
	static HashMap<String, String> invit = new HashMap<String, String>();
	
	public void onEnable()
	{
		config.setDataFolder(getDataFolder());
		configHome.setDataFolder(getDataFolder());
		
		config.initConfig("BDD.clan.host", "localhost");
		config.initConfig("BDD.clan.port", "3306");
		config.initConfig("BDD.clan.database", "CeltiClan");
		config.initConfig("BDD.clan.user", "root");
		config.initConfig("BDD.clan.pass", "");
		config.initConfig("BDD.clan.table_invit", "clan_invit");
		config.initConfig("BDD.clan.table_players", "clan_players");
		config.initConfig("BDD.clan.table_clan", "clan_clan");
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
		    getPluginLoader().disablePlugin(this);
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
				p.sendMessage(ChatColor.RED + "CeltiClan v0.1.11");
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
									try
									{
										if(isChef(p))
										{
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
										else
										{
											p.sendMessage(prefix + ChatColor.RED + "Vous n'êtes pas proprietaires d'un clan !");
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
							String clanName = null;
							if(isInClan(p))
							{
								try
								{
									p.sendMessage(prefix + ChatColor.GREEN + "Vous avez quittez votre clan.");
									if(isChef(p))
									{
										stat.executeUpdate("DELETE FROM " + table_clan + " WHERE chef='" + p.getUniqueId().toString() +"'");
										stat.executeUpdate("DELETE FROM " + table_players + " WHERE clan='" + clanName +"'");
										p.sendMessage(prefix + ChatColor.GREEN + "et supprimez votre clan.");
									}
									stat.executeUpdate("DELETE FROM " + table_players + " WHERE joueur='" + p.getUniqueId().toString() +"'");
								}
								catch (SQLException e)
								{
									e.printStackTrace();
								}
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
									
									if(isChef(p))
									{
										try {
											stat.executeUpdate("INSERT INTO `" + table_invit + "`(`clan`, `joueur`) VALUES ('" + getClan(p.getName()) + "','" + this.getPlayer(args[1]).getUniqueId().toString() + "')");
										} catch (SQLException e) { e.printStackTrace(); }
										
										for(Player p1 : Bukkit.getOnlinePlayers())
										{
											if(this.getPlayer(args[1]).getUniqueId().toString().equalsIgnoreCase(p1.getUniqueId().toString()))
											{
												p1.sendMessage(prefix + ChatColor.YELLOW + p.getName() + " vous a invité dans le clan " + getClan(p.getName()));
												p1.sendMessage(prefix + ChatColor.YELLOW + "Pour rejoindre le clan, faites /clan join " + getClan(p.getName()));
											}
											else
											{
												invit.put(p1.getUniqueId().toString(), getClan(p.getName()));
											}
										}
										p.sendMessage(prefix + ChatColor.GREEN + "Vous avez invité " + args[1]);
									}
									else
									{
										p.sendMessage(prefix + ChatColor.RED + "Vous n'êtes pas proprietaire d'un clan");
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
									sender.sendMessage(prefix + ChatColor.YELLOW + "La liste des clans:");
									for(String clanName : getAllClan())
									{
										sender.sendMessage(ChatColor.YELLOW + clanName);
									}
									break;
									
								case 2:
									sender.sendMessage(prefix + ChatColor.YELLOW + "Liste des joueurs du clan " + args[1] + ":");
									/*for(Player player : getOnlinePlayersFromClan(args[1]))
									{
										sender.sendMessage(ChatColor.YELLOW + player.getName());
									}*/
									for(OfflinePlayer player : getOfflinePlayersFromClan(args[1]))
									{
										sender.sendMessage(ChatColor.YELLOW + player.getName());
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
									try
									{
										if(isChef(p))
										{									
											if(getClan(args[1]).equalsIgnoreCase(getClan(p.getName())))
											{
												OfflinePlayer offPlayer = this.getOfflinePlayer(args[1]);
												Player onPlayer = this.getPlayer(args[1]);
												UUID offUUID = null;
												UUID onUUID = null;
												boolean isPlayerOnline = false;
												if(onPlayer != null)
												{
													onUUID = onPlayer.getUniqueId();
													isPlayerOnline = true;
												}
												else
												{
													offUUID = offPlayer.getUniqueId();
												}
												
												if(isPlayerOnline)
												{
													stat.executeUpdate("DELETE FROM " + table_players + " WHERE joueur='" + onUUID.toString() + "'");
													p.sendMessage(prefix + ChatColor.GREEN + "Le joueur " + getServer().getPlayer(onUUID).getName() + " a été kické !");
													getServer().getPlayer(onUUID).sendMessage(prefix + ChatColor.YELLOW + "Vous avez été expulsé de votre clan par " + p.getName() + " !");
												}
												else
												{
													stat.executeUpdate("DELETE FROM " + table_players + " WHERE joueur='" + offUUID.toString() + "'");
													p.sendMessage(prefix + ChatColor.GREEN + "Le joueur " + getServer().getOfflinePlayer(offUUID).getName() + " a été kické !");
												}
											}
											else
											{
												p.sendMessage(prefix + ChatColor.RED + "Le joueur n'est pas dans votre clan !");
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
													if(this.getPlayer(args[2]).getUniqueId().toString().equalsIgnoreCase(res.getString("joueur")))
													{
														playName = this.getPlayer(args[2]).getUniqueId().toString();
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
													p.sendMessage(prefix + ChatColor.RED + "Le joueur n'appartient pas à un clan !");
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
							configHome.setElement("home." + getClan(p.getName()) + ".world", p.getLocation().getWorld().getName());
							configHome.setElement("home." + getClan(p.getName()) + ".x", p.getLocation().getBlockX());
							configHome.setElement("home." + getClan(p.getName()) + ".y", p.getLocation().getBlockY());
							configHome.setElement("home." + getClan(p.getName()) + ".z", p.getLocation().getBlockZ());
							configHome.setElement("home." + getClan(p.getName()) + ".pitch", p.getLocation().getPitch());
							configHome.setElement("home." + getClan(p.getName()) + ".yaw", p.getLocation().getYaw());
							p.sendMessage(prefix + ChatColor.GREEN + "Le home de votre clan a été placé !");
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'êtes pas proprietaire du clan !");
						}
						break;
					
					case "home":
						if(isInClan(p))
						{
							String world = configHome.loadString("home." + getClan(p.getName()) + ".world");
							int x = configHome.loadInt("home." + getClan(p.getName()) + ".x");
							int y = configHome.loadInt("home." + getClan(p.getName()) + ".y");
							int z = configHome.loadInt("home." + getClan(p.getName()) + ".z");
							float yaw = (float) configHome.loadDouble("home." + getClan(p.getName()) + ".yaw");
							float pitch = (float) configHome.loadDouble("home." + getClan(p.getName()) + ".pitch");
							final Player player = p;
							if(y != 0.0)
							{
								p.sendMessage(prefix + ChatColor.GREEN + "Téléportation dans 5 secondes...");
								loc = new Location(Bukkit.getServer().getWorld(world), x, y, z, yaw, pitch);
								Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
								{
									@Override
									public void run()
									{
										player.teleport(loc);
									}
								}, 60L);
							}
							else
							{
								p.sendMessage(prefix + ChatColor.RED + "Le home de votre clan n'a pas été placé !");
							}
						}
						else
						{
							p.sendMessage(prefix + ChatColor.RED + "Vous n'êtes pas dans un clan");
						}
						break;
					
					case "database":
						try {
							stat.executeUpdate("CREATE TABLE clan_players(id INT KEY AUTO_INCREMENT, joueur VARCHAR(255), clan VARCHAR(255))");
							stat.executeUpdate("CREATE TABLE clan_clan(id INT KEY AUTO_INCREMENT, clan VARCHAR(255), sigle VARCHAR(255), chef VARCHAR(255), invit INT)");
							stat.executeUpdate("CREATE TABLE clan_invit(id INT KEY AUTO_INCREMENT, clan VARCHAR(255), joueur VARCHAR(255))");
							sender.sendMessage("Database has been created !");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						break;
						
					case "reload":
						reloadconfig();
						sender.sendMessage("CeltiClan reloaded.");
						break;
						
					case "help":
						sender.sendMessage(ChatColor.YELLOW + "/clan create <nom du clan> <sigle du clan> : Créer un clan avec les paramètres nom du clan et un sigle (Acronyme)");
						sender.sendMessage(ChatColor.YELLOW + "/clan setinvit <true | false> : " + ChatColor.BOLD + ChatColor.RED + "Uniquement pour les chefs" + ChatColor.YELLOW +" : Permet de définir si les invitations sont requises (true = invit requises, false = entrée libre)");
						sender.sendMessage(ChatColor.YELLOW + "/clan invit <nom du joueur> : " + ChatColor.BOLD + ChatColor.RED + "Uniquement pour les chefs" + ChatColor.YELLOW +" : Inviter un joueur");
						sender.sendMessage(ChatColor.YELLOW + "/clan join <nom du clan> : Rejoindre un clan");
						sender.sendMessage(ChatColor.YELLOW + "/clan leave : Permet de quitter un clan (si vous êtes le chef, cela va supprimer le clan)");
						sender.sendMessage(ChatColor.YELLOW + "/clan list : Permet de voir tout les clans du serveur");
						sender.sendMessage(ChatColor.YELLOW + "/clan list <nom du clan> : Permet de voir les joueurs d'un clan");
						sender.sendMessage(ChatColor.YELLOW + "/clan kick <nom du joueur> : " + ChatColor.BOLD + ChatColor.RED + "Uniquement pour les chefs" + ChatColor.YELLOW +" : Permet d'expulser un joueur du clan");
						sender.sendMessage(ChatColor.YELLOW + "/clan sethome : " + ChatColor.BOLD + ChatColor.RED + "Uniquement pour les chefs" + ChatColor.YELLOW +" : Déterminer le home du clan");
						sender.sendMessage(ChatColor.YELLOW + "/clan home : Téléportation après 5 secondes au home du clan.");
						sender.sendMessage(ChatColor.YELLOW + "/clan info <nom du joueur> : Permet de savoir dans quel clan est le joueur.");
						break;
						
					case "info":
						if(args.length == 1)
						{
							sender.sendMessage(prefix + ChatColor.RED + "Pas assez d'arguments !");
						}
						else
						{
							String clan = getClan(args[1]);
							if(clan != null)
							{
								sender.sendMessage(prefix + ChatColor.YELLOW + "Le joueur " + args[1] + " appartient au clan " + clan + " !");
							}
							else
							{
								sender.sendMessage(prefix + ChatColor.YELLOW +  "Le joueur n'est dans aucun clan.");
							}
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
	
	public String getClan(String p)
	{
		ResultSet res = null;
		String result = null;
		
		try
		{
			OfflinePlayer offPlayer = this.getOfflinePlayer(p);
			Player onPlayer = this.getPlayer(p);
			UUID offUUID = null;
			UUID onUUID = null;
			boolean isPlayerOnline = false;
			if(onPlayer != null)
			{
				onUUID = onPlayer.getUniqueId();
				isPlayerOnline = true;
			}
			else
			{
				offUUID = offPlayer.getUniqueId();
			}

			res = stat.executeQuery("SELECT * FROM " + table_players);
			
			while(res.next())
			{
				if(isPlayerOnline)
				{
					if(res.getString("joueur").equalsIgnoreCase(onUUID.toString()))
					{
						result = res.getString("clan");
						break;
					}
				}
				else
				{
					if(res.getString("joueur").equalsIgnoreCase(offUUID.toString()))
					{
						result = res.getString("clan");
						break;
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@SuppressWarnings("deprecation")
	public Player getPlayer(String name)
	{
		return getServer().getPlayer(name);
	}
	
	@SuppressWarnings("deprecation")
	public OfflinePlayer getOfflinePlayer(String name)
	{
		return getServer().getOfflinePlayer(name);
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
	
	public ArrayList<String> getAllClan()
	{
		ArrayList<String> clan = new ArrayList<String>();
		ResultSet res = null;
		
		try
		{
			res = stat.executeQuery("SELECT clan FROM " + table_clan);
			while(res.next())
			{
				clan.add(res.getString("clan"));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return clan;
	}
	
	public ArrayList<Player> getOnlinePlayersFromClan(String clan)
	{
		ArrayList<Player> players = new ArrayList<Player>();
		ResultSet res = null;
		
		try
		{
			res = stat.executeQuery("SELECT * FROM " + table_players);
			
			while(res.next())
			{
				if(res.getString("clan").equalsIgnoreCase(clan))
				{
					Player onPlayer = getServer().getPlayer(UUID.fromString(res.getString("joueur")));
					if(onPlayer != null)
					{
						players.add(onPlayer);
					}
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return players;
	}
	
	public ArrayList<OfflinePlayer> getOfflinePlayersFromClan(String clan)
	{
		ArrayList<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
		ResultSet res = null;
		
		try
		{
			res = stat.executeQuery("SELECT * FROM " + table_players);
			
			while(res.next())
			{
				if(res.getString("clan").equalsIgnoreCase(clan))
				{
					OfflinePlayer offPlayer = getServer().getOfflinePlayer(UUID.fromString(res.getString("joueur")));
					if(offPlayer != null)
					{
						players.add(offPlayer);
					}
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return players;
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