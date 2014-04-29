package fr.pitiqui.celticlan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class ClanCommandExecutor implements CommandExecutor
{
    private Main plugin;
    
    public ClanCommandExecutor(Main plugin)
    {
        this.plugin = plugin;
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
				p.sendMessage(ChatColor.RED + "CeltiClan v0.1.11B");
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
									p.sendMessage(Main.prefix + "Vous devez mettre un nom de clan !");
									break;

								case 2:
									p.sendMessage(Main.prefix + "Vous devez mettre un sigle !");
									break;
									
								case 3:
									try {
										ResultSet res = plugin.stat.executeQuery("SELECT clan FROM " + plugin.table_clan);
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
											p.sendMessage(Main.prefix + ChatColor.RED + "Un clan sous le nom de " + args[1] + " a déjà été créer.");
										}
										else
										{
											plugin.stat.executeUpdate("INSERT INTO " + plugin.table_clan + " (`clan`, `sigle`, `chef`, `invit`) VALUES ('" + args[1] + "', '"+ args[2] +"', '" + p.getUniqueId().toString() + "', '1');");
											plugin.stat.executeUpdate("INSERT INTO " + plugin.table_players + " (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
											p.sendMessage(Main.prefix + ChatColor.GREEN + "Vous avez crée le clan " + args[1] + " !");
										}
									} catch (SQLException e1) {
										e1.printStackTrace();
									}
									break;
									
								default:
									p.sendMessage(Main.prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'avez pas la permission de créer un clan !");
						}
						break;
						
					case "join":
						if(user.has("clan.create"))
						{
							switch (args.length)
							{
								case 1:
									p.sendMessage(Main.prefix + "Vous devez mettre un nom de clan !");
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
										res  = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_clan);
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
											res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_players);
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
												res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_clan);
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
													plugin.stat.executeUpdate("INSERT INTO " + plugin.table_players + " (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
													p.sendMessage(Main.prefix + ChatColor.GREEN + "Vous avez rejoint le clan " + args[1] + " !");
												}
												else
												{
													boolean lol = false;
													res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_invit);
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
														plugin.stat.executeUpdate("INSERT INTO " + plugin.table_players + " (`joueur`, `clan`) VALUES ('" + p.getUniqueId().toString() + "', '"+ args[1] +"');");
														plugin.stat.executeUpdate("DELETE FROM " + plugin.table_invit + " WHERE joueur='" + p.getUniqueId().toString() + "';");
														p.sendMessage(Main.prefix + ChatColor.GREEN + "Vous avez rejoint le clan " + args[1] + " !");
													}
													else
													{
														p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'avez pas été invité !");
													}
												}
											}
											else
											{
												p.sendMessage(Main.prefix + ChatColor.RED + "Vous ne pouvez pas rejoindre votre clan !");
											}
										}
										
										if(findJoin == false)
										{
											p.sendMessage(Main.prefix + ChatColor.RED + "Le clan n'a pas été trouvé.");
										}
									}
									catch (SQLException e)
									{
										e.printStackTrace();
									}
									break;
									
								default:
									p.sendMessage(Main.prefix + ChatColor.RED + "Trop d'arguments !");
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
									p.sendMessage(Main.prefix + ChatColor.RED + "Vous devez précisez si les invitations sont obligatoires ! (true = Invitation obligatoires, false = tout le monde peut rejoindre)");
									break;
									
								case 2:
									try
									{
										if(isChef(p))
										{
											if(args[1].equalsIgnoreCase("true"))
											{
												plugin.stat.executeUpdate("UPDATE " + plugin.table_clan + " SET invit=0 WHERE `chef`='" + p.getUniqueId().toString() +"'");
												p.sendMessage(Main.prefix + ChatColor.GREEN + "Vous avez défini les invitations de votre clan a privé");
												break;
											}
											else if(args[1].equalsIgnoreCase("false"))
											{
												plugin.stat.executeUpdate("UPDATE " + plugin.table_clan + " SET invit=1 WHERE `chef`='" + p.getUniqueId().toString() +"'");
												p.sendMessage(Main.prefix + ChatColor.GREEN + "Vous avez défini les invitations de votre clan a libre");
												break;
											}
											else
											{
												p.sendMessage(Main.prefix + ChatColor.RED + "true = Invitation obligatoires, false = tout le monde peut rejoindre");
											}
										}
										else
										{
											p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'êtes pas proprietaires d'un clan !");
										}
									}
									catch (SQLException e)
									{
										e.printStackTrace();
									}
									break;
	
								default:
									p.sendMessage(Main.prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'avez pas la permission de régler les invitations d'un clan");
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
									p.sendMessage(Main.prefix + ChatColor.GREEN + "Vous avez quittez votre clan.");
									if(isChef(p))
									{
										plugin.stat.executeUpdate("DELETE FROM " + plugin.table_clan + " WHERE chef='" + p.getUniqueId().toString() +"'");
										plugin.stat.executeUpdate("DELETE FROM " + plugin.table_players + " WHERE clan='" + clanName +"'");
										p.sendMessage(Main.prefix + ChatColor.GREEN + "et supprimez votre clan.");
									}
									plugin.stat.executeUpdate("DELETE FROM " + plugin.table_players + " WHERE joueur='" + p.getUniqueId().toString() +"'");
								}
								catch (SQLException e)
								{
									e.printStackTrace();
								}
							}
						}
						else
						{
							p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'avez pas la permission de quitter un clan !");
						}
						break;
						
					case "invit":
						if(user.has("clan.invit"))
						{
							switch (args.length)
							{
								case 1:
									p.sendMessage(Main.prefix + ChatColor.RED + "Vous devez mettre un joueur à inviter !");
									break;
									
								case 2:
									
									if(isChef(p))
									{
										try {
											plugin.stat.executeUpdate("INSERT INTO `" + plugin.table_invit + "`(`clan`, `joueur`) VALUES ('" + getClan(p.getName()) + "','" + getPlayer(args[1]).getUniqueId().toString() + "')");
										} catch (SQLException e) { e.printStackTrace(); }
										
										for(Player p1 : Bukkit.getOnlinePlayers())
										{
											if(getPlayer(args[1]).getUniqueId().toString().equalsIgnoreCase(p1.getUniqueId().toString()))
											{
												p1.sendMessage(Main.prefix + ChatColor.YELLOW + p.getName() + " vous a invité dans le clan " + getClan(p.getName()));
												p1.sendMessage(Main.prefix + ChatColor.YELLOW + "Pour rejoindre le clan, faites /clan join " + getClan(p.getName()));
											}
											else
											{
												Main.invit.put(p1.getUniqueId().toString(), getClan(p.getName()));
											}
										}
										p.sendMessage(Main.prefix + ChatColor.GREEN + "Vous avez invité " + args[1]);
									}
									else
									{
										p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'êtes pas proprietaire d'un clan");
									}
									break;
	
								default:
									p.sendMessage(Main.prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'avez pas la permission d'inviter un joueur !");
						}
						break;
						
					case "list":
						if(user.has("clan.list"))
						{
							switch (args.length)
							{
								case 1:
									sender.sendMessage(Main.prefix + ChatColor.YELLOW + "La liste des clans:");
									for(String clanName : getAllClan())
									{
										sender.sendMessage(ChatColor.YELLOW + clanName);
									}
									break;
									
								case 2:
									sender.sendMessage(Main.prefix + ChatColor.YELLOW + "Liste des joueurs du clan " + args[1] + ":");
									for(OfflinePlayer player : getOfflinePlayersFromClan(args[1]))
									{
										sender.sendMessage(ChatColor.YELLOW + player.getName());
									}
									break;
	
								default:
									p.sendMessage(Main.prefix + ChatColor.RED + "Trop d'arguments !");
									break;
							}
						}
						else
						{
							p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'avez pas la permission de lister les clans / les joueurs d'un clan !");
						}
						break;
						
					case "kick":
							switch(args.length)
							{
								case 1:
									p.sendMessage(Main.prefix + ChatColor.RED + "Mauvaise usage de la commande ! Ex: /clan kick <nom du joueur>");
									break;
									
								case 2:
									try
									{
										if(isChef(p))
										{									
											if(getClan(args[1]).equalsIgnoreCase(getClan(p.getName())))
											{
												OfflinePlayer offPlayer = getOfflinePlayer(args[1]);
												Player onPlayer = getPlayer(args[1]);
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
													plugin.stat.executeUpdate("DELETE FROM " + plugin.table_players + " WHERE joueur='" + onUUID.toString() + "'");
													p.sendMessage(Main.prefix + ChatColor.GREEN + "Le joueur " + plugin.getServer().getPlayer(onUUID).getName() + " a été kické !");
													plugin.getServer().getPlayer(onUUID).sendMessage(Main.prefix + ChatColor.YELLOW + "Vous avez été expulsé de votre clan par " + p.getName() + " !");
												}
												else
												{
													plugin.stat.executeUpdate("DELETE FROM " + plugin.table_players + " WHERE joueur='" + offUUID.toString() + "'");
													p.sendMessage(Main.prefix + ChatColor.GREEN + "Le joueur " + plugin.getServer().getOfflinePlayer(offUUID).getName() + " a été kické !");
												}
											}
											else
											{
												p.sendMessage(Main.prefix + ChatColor.RED + "Le joueur n'est pas dans votre clan !");
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
											p.sendMessage(Main.prefix + ChatColor.GREEN + "Administration du plugin !");
											break;
											
										case 2:
											p.sendMessage(Main.prefix + ChatColor.RED + "Mauvaise usage de la commande ! Ex: /clan delete <nom du clan>");
											break;
											
										case 3:
											try
											{
												boolean find = false;
												ResultSet res = null;
												String clanName = null;
												
												res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_clan);
												
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
													plugin.stat.executeUpdate("DELETE FROM " + plugin.table_clan + " WHERE clan='" + clanName + "'");
													plugin.stat.executeUpdate("DELETE FROM " + plugin.table_players + " WHERE clan='" + clanName + "'");
													p.sendMessage(Main.prefix + ChatColor.GREEN + "Le clan " + clanName + " a été supprimé !");
												}
												else
												{
													p.sendMessage(Main.prefix + ChatColor.RED + "Le clan n'a pas été trouvé !");
												}
											}
											catch (SQLException e)
											{
												e.printStackTrace();
											}
											break;
					
										default:
											p.sendMessage(Main.prefix + ChatColor.RED + "Trop d'arguments !");
											break;
									}
								}
								else
								{
									p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'avez pas la permission de supprimer un clan !");
								}
								break;
								
							case "kick":
								if(user.has("clan.admin.kick"))
								{
									switch(args.length)
									{
										case 1:
											p.sendMessage(Main.prefix + ChatColor.GREEN + "Administration du plugin !");
											break;
											
										case 2:
											p.sendMessage(Main.prefix + ChatColor.RED + "Mauvaise usage de la commande ! Ex: /clan kick <nom du joueur>");
											break;
											
										case 3:
											try
											{
												boolean find = false;
												ResultSet res = null;
												String playName = null;
												
												res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_clan);
												
												while(res.next())
												{
													if(getPlayer(args[2]).getUniqueId().toString().equalsIgnoreCase(res.getString("joueur")))
													{
														playName = getPlayer(args[2]).getUniqueId().toString();
														find = true;
														break;
													}
												}
												
												if(find == true)
												{
													plugin.stat.executeUpdate("DELETE FROM " + plugin.table_players + " WHERE joueur='" + playName + "'");
													p.sendMessage(Main.prefix + ChatColor.GREEN + "Le joueur " + args[2] + " a été kické !");
													for(Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
													{
														if(onlinePlayer.getName().equalsIgnoreCase(args[2]))
														{
															onlinePlayer.sendMessage(Main.prefix + ChatColor.YELLOW + "Vous avez été expulsé de votre clan par " + p.getName() + " !");
														}
													}
												}
												else
												{
													p.sendMessage(Main.prefix + ChatColor.RED + "Le joueur n'appartient pas à un clan !");
												}
											}
											catch (SQLException e)
											{
												e.printStackTrace();
											}
											break;
					
										default:
											p.sendMessage(Main.prefix + ChatColor.RED + "Trop d'arguments !");
											break;
									}
								}
								else
								{
									p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'avez pas la permission d'expulser un joueur !");
								}
								break;
	
							default:
								break;
						}
						break;
						
					case "sethome":
						if(isChef(p))
						{
							plugin.configHome.setElement("home." + getClan(p.getName()) + ".world", p.getLocation().getWorld().getName());
							plugin.configHome.setElement("home." + getClan(p.getName()) + ".x", p.getLocation().getBlockX());
							plugin.configHome.setElement("home." + getClan(p.getName()) + ".y", p.getLocation().getBlockY());
							plugin.configHome.setElement("home." + getClan(p.getName()) + ".z", p.getLocation().getBlockZ());
							plugin.configHome.setElement("home." + getClan(p.getName()) + ".pitch", p.getLocation().getPitch());
							plugin.configHome.setElement("home." + getClan(p.getName()) + ".yaw", p.getLocation().getYaw());
							p.sendMessage(Main.prefix + ChatColor.GREEN + "Le home de votre clan a été placé !");
						}
						else
						{
							p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'êtes pas proprietaire du clan !");
						}
						break;
					
					case "home":
						if(isInClan(p))
						{
							teleportToHome(p, getClan(p.getName()));
						}
						else
						{
							p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'êtes pas dans un clan");
						}
						break;
					
					case "database":
						if(user.has("clan.database"))
						{
							try {
								plugin.stat.executeUpdate("CREATE TABLE clan_players(id INT KEY AUTO_INCREMENT, joueur VARCHAR(255), clan VARCHAR(255))");
								plugin.stat.executeUpdate("CREATE TABLE clan_clan(id INT KEY AUTO_INCREMENT, clan VARCHAR(255), sigle VARCHAR(255), chef VARCHAR(255), invit INT)");
								plugin.stat.executeUpdate("CREATE TABLE clan_invit(id INT KEY AUTO_INCREMENT, clan VARCHAR(255), joueur VARCHAR(255))");
								sender.sendMessage("Database has been created !");
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
						break;
						
					case "reload":
						if(user.has("clan.reload"))
						{
							plugin.reloadconfig();
							sender.sendMessage("CeltiClan reloaded.");
						}
						break;
						
					case "help":
						if(user.has("clan.help"))
						{
							sender.sendMessage(ChatColor.YELLOW + "/clan create <nom du clan> <sigle du clan> : Créer un clan avec les paramètres nom du clan et un sigle (Acronyme)");
							sender.sendMessage(ChatColor.YELLOW + "/clan setinvit <true | false> : " + ChatColor.BOLD + ChatColor.RED + "Uniquement pour les chefs" + ChatColor.YELLOW +" : Permet de définir si les invitations sont requises (true = invit requises, false = entrée libre)");
							sender.sendMessage(ChatColor.YELLOW + "/clan invit <nom du joueur> : " + ChatColor.BOLD + ChatColor.RED + "Uniquement pour les chefs" + ChatColor.YELLOW +" : Inviter un joueur");
							sender.sendMessage(ChatColor.YELLOW + "/clan join <nom du clan> : Rejoindre un clan");
							sender.sendMessage(ChatColor.YELLOW + "/clan leave : Permet de quitter un clan (si vous êtes le chef, cela va supprimer le clan)");
							sender.sendMessage(ChatColor.YELLOW + "/clan list : Permet de voir tous les clans du serveur");
							sender.sendMessage(ChatColor.YELLOW + "/clan list <nom du clan> : Permet de voir les joueurs d'un clan");
							sender.sendMessage(ChatColor.YELLOW + "/clan kick <nom du joueur> : " + ChatColor.BOLD + ChatColor.RED + "Uniquement pour les chefs" + ChatColor.YELLOW +" : Permet d'expulser un joueur du clan");
							sender.sendMessage(ChatColor.YELLOW + "/clan sethome : " + ChatColor.BOLD + ChatColor.RED + "Uniquement pour les chefs" + ChatColor.YELLOW +" : Déterminer le home du clan");
							sender.sendMessage(ChatColor.YELLOW + "/clan home : Téléportation après 5 secondes au home du clan.");
							sender.sendMessage(ChatColor.YELLOW + "/clan info <nom du joueur> : Permet de savoir dans quel clan est le joueur.");
						}
						break;
						
					case "info":
						if(user.has("clan.info"))
						{
							if(args.length == 1)
							{
								sender.sendMessage(Main.prefix + ChatColor.RED + "Pas assez d'arguments !");
							}
							else
							{
								String clan = getClan(args[1]);
								if(clan != null)
								{
									sender.sendMessage(Main.prefix + ChatColor.YELLOW + "Le joueur " + args[1] + " appartient au clan " + clan + " !");
								}
								else
								{
									sender.sendMessage(Main.prefix + ChatColor.YELLOW +  "Le joueur n'est dans aucun clan.");
								}
							}
						}
						break;
						
					case "rename":
						switch (args.length)
						{
							case 1:
								p.sendMessage(Main.prefix + ChatColor.RED + "Vous devez préciser un nouveau nom pour le clan !");
								break;
								
							case 2:
								if(isChef(p))
								{
									if(!clanExist(args[1]))
									{
										try {
											plugin.stat.executeUpdate("UPDATE core_clan SET clan='" + args[1] + "' WHERE chef='" + p.getUniqueId().toString() + "'");
											p.sendMessage(Main.prefix + ChatColor.GREEN + "Vous avez renommé votre clan en " + args[1]);
										} catch (SQLException e) {
											e.printStackTrace();
										}
									}
									else
									{
										p.sendMessage(Main.prefix + ChatColor.RED + "Un clan sous le nom " + args[1] + " existe !");
									}
								}
								else
								{
									p.sendMessage(Main.prefix + ChatColor.RED + "Vous n'êtes pas proprietaire du clan !");
								}
								break;
	
							default:
								p.sendMessage(Main.prefix + ChatColor.RED + "Trop d'arguments !");
								break;
						}
						break;
					
					default:
						p.sendMessage(Main.prefix + ChatColor.RED + "Cette commande n'a pas été trouvé !");
						break;
				}
			}
		}
		return false;
	}
	/**
	 * Return name of the clan for the specified Player name
	 * @param p Player name.
	 * @return Return String of the clan
	 */
	public String getClan(String p)
	{
		ResultSet res = null;
		
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_players);
			
			while(res.next())
			{
				if(res.getString("joueur").equalsIgnoreCase(this.getOfflinePlayer(p).getUniqueId().toString()))
				{
					return res.getString("clan");
				}
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * Return UUID String for the specified clan.
	 * @param clan Specified clan
	 * @return Return UUID String for the specified clan.
	 */
	public String getChef(String clan)
	{
		ResultSet res = null;
		
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_clan);
			
			while(res.next())
			{
				if(res.getString("clan").equalsIgnoreCase(clan))
				{
					return res.getString("chef");
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	@SuppressWarnings("deprecation")
	public Player getPlayer(String name)
	{
		return plugin.getServer().getPlayer(name);
	}
	
	@SuppressWarnings("deprecation")
	public OfflinePlayer getOfflinePlayer(String name)
	{
		return plugin.getServer().getOfflinePlayer(name);
	}
	
	public boolean isInClan(Player player)
	{
		ResultSet res = null;
		
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_players);
			
			while(res.next())
			{
				if(res.getString("joueur").equalsIgnoreCase(player.getUniqueId().toString()))
				{
					return true;
				}
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
		try
		{
			res = plugin.stat.executeQuery("SELECT chef FROM " + plugin.table_clan);
			
			while(res.next())
			{
				if(res.getString("chef").equalsIgnoreCase(player.getUniqueId().toString()))
				{
					return true;
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean clanExist(String clan)
	{
		ResultSet res = null;
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_clan);
			
			while(res.next())
			{
				if(res.getString("clan").equalsIgnoreCase(clan))
				{
					return true;
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isOwnClan(Player p, String clan)
	{
		ResultSet res = null;
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_clan);
			
			while(res.next())
			{
				String player = res.getString("chef");
				String clanName = res.getString("clan");
				if(player.equalsIgnoreCase(p.getUniqueId().toString()) && clanName.equalsIgnoreCase(clan))
				{
					return true;
				}
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
			res = plugin.stat.executeQuery("SELECT clan FROM " + plugin.table_clan);
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
	
	@Deprecated
	public ArrayList<Player> getOnlinePlayersFromClan(String clan)
	{
		ArrayList<Player> players = new ArrayList<Player>();
		ResultSet res = null;
		
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_players);
			
			while(res.next())
			{
				if(res.getString("clan").equalsIgnoreCase(clan))
				{
					Player onPlayer = plugin.getServer().getPlayer(UUID.fromString(res.getString("joueur")));
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
			res = plugin.stat.executeQuery("SELECT * FROM " + plugin.table_players);
			
			while(res.next())
			{
				if(res.getString("clan").equalsIgnoreCase(clan))
				{
					OfflinePlayer offPlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(res.getString("joueur")));
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
	
	public void teleportToHome(final Player p, String clan)
	{
		String world = plugin.configHome.loadString("home." + clan + ".world");
		int x = plugin.configHome.loadInt("home." + clan + ".x");
		int y = plugin.configHome.loadInt("home." + clan + ".y");
		int z = plugin.configHome.loadInt("home." + clan + ".z");
		float yaw = (float) plugin.configHome.loadDouble("home." + clan + ".yaw");
		float pitch = (float) plugin.configHome.loadDouble("home." + clan + ".pitch");
		if(y != 0.0)
		{
			p.sendMessage(Main.prefix + ChatColor.GREEN + "Téléportation dans 5 secondes...");
			plugin.loc = new Location(Bukkit.getServer().getWorld(world), x, y, z, yaw, pitch);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					p.teleport(plugin.loc);
				}
			}, 60L);
		}
		else
		{
			p.sendMessage(Main.prefix + ChatColor.RED + "Le home de votre clan n'a pas été placé !");
		}
	}
}
