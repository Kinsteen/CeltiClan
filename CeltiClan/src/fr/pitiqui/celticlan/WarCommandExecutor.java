package fr.pitiqui.celticlan;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarCommandExecutor implements CommandExecutor
{
    private Main plugin;
	Config config = new Config();
	
	ClanCommandExecutor clan = new ClanCommandExecutor(plugin);
    
	String table_guerre = config.loadString("BDD.guerre.table_guerre");
    
    String prefix = ChatColor.RED + "[CeltiGuerre] " + ChatColor.RESET;
    
    public WarCommandExecutor(Main plugin)
    {
        this.plugin = plugin;
    }
    
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player p = null;
		if(sender instanceof Player)
		{
			p = (Player) sender;
		}
		
		if(cmd.getName().equalsIgnoreCase("guerre"))
		{
			if(args.length == 0)
			{
				sender.sendMessage("CeltiGuerre v0.0.1");
			}
			else
			{
				switch (args[0])
				{
					case "database":
						try
						{
							plugin.stat.executeUpdate("CREATE TABLE guerre_guerre(id INT KEY AUTO_INCREMENT, declareur VARCHAR(255), cible VARCHAR(255), nom VARCHAR(255))");
							sender.sendMessage("Database has been created !");
						}
						catch (SQLException e1)
						{
							e1.printStackTrace();
						}
						break;
				
					case "declare":
						switch (args.length)
						{
							case 1:
								p.sendMessage(prefix + ChatColor.RED + "Vous devez dire a qui vous declarer la guerre");
								break;
							
							case 2:
								p.sendMessage(prefix + ChatColor.RED + "Vous devez dire un nom de guerre");
								break;
							
							case 3:
								if(clan.isChef(p))
								{
									if(!clan.clanExist(args[1]))
									{
										p.sendMessage(prefix + ChatColor.RED + "Le clan n'existe pas !");
									}
									else
									{
										if(clan.isOwnClan(p, args[1]))
										{
											p.sendMessage(prefix + ChatColor.RED + "Vous ne pouvez pas déclarer la guerre à votre propre clan !");
										}
										else
										{
											if(!isAlreadyInGuerre(clan.getClan(p.getName()), args[1]))
											{
												if(!isGuerreExist(args[2]))
												{
													try
													{
														plugin.stat.executeUpdate("INSERT INTO guerre_guerre (`declareur`, `cible`, `nom`) VALUES ('" + clan.getClan(p.getName()) + "', '" + args[1] + "', '" + args[2] + "');");
														p.sendMessage(prefix + ChatColor.GREEN + "Vous avez déclaré la guerre au clan " + args[1] + " !");
														for(Player player : Bukkit.getServer().getOnlinePlayers())
														{
															if(player.getUniqueId().toString().equalsIgnoreCase(clan.getChef(args[1])))
															{
																player.sendMessage(ChatColor.YELLOW + "Le clan " + clan.getClan(p.getName()) + " vous a déclaré la guerre !");
															}
														}
														//plugin.getOfflinePlayer(plugin.getChef(args[1])).getPlayer().sendMessage(prefix + ChatColor.YELLOW + "Le clan " + plugin.getClan(p.getName()) + " vous à déclaré la guerre !");
													}
													catch (SQLException e)
													{
														p.sendMessage("ERREUR SQL !");
														p.sendMessage(e.getMessage());
													}
												}
												else
												{
													p.sendMessage(prefix + ChatColor.RED + "Une guerre existe déjà sous ce nom !");
												}
											}
											else
											{
												p.sendMessage(prefix + ChatColor.RED + "Vous avez déjà declaré la guerre à ce clan !");
											}
										}
									}
								}
								else
								{
									p.sendMessage(prefix + ChatColor.RED + "Vous n'êtes pas propriétaire du clan !");
								}
								break;
	
							default:
								p.sendMessage(prefix + ChatColor.RED + "Trop d'arguments !");
								break;
						}
						
						break;
						
					case "list":
						p.sendMessage(prefix + ChatColor.YELLOW + "Liste de tout les guerres en cours:");
						ResultSet res = null;
						
						try
						{
							res = plugin.stat.executeQuery("SELECT * FROM guerre_guerre");
							
							while(res.next())
							{
								p.sendMessage(ChatColor.YELLOW + res.getString("declareur") + " - " + res.getString("cible"));
							}
						}
						catch(SQLException e)
						{
							e.printStackTrace();
						}
						break;
						
					case "paix":
						switch (args.length)
						{
							case 1:
								p.sendMessage(prefix + ChatColor.RED + "Vous devez dire à qui vous faites la paix !");
								break;
								
							case 2:
								if(isGuerreExistByClan(args[1]))
								{
									try
									{
										plugin.stat.executeUpdate("DELETE FROM guerre_guerre WHERE cible='" + args[1] + "'");
										p.sendMessage(prefix + ChatColor.GREEN + "Vous avez fait la paix avec le clan " + args[1]);
										for(Player player : Bukkit.getServer().getOnlinePlayers())
										{
											if(player.getUniqueId().toString().equalsIgnoreCase(clan.getChef(args[1])))
											{
												player.sendMessage(ChatColor.YELLOW + "Le clan " + clan.getClan(p.getName()) + " vous a fait la paix.");
											}
										}
									}
									catch (SQLException e)
									{
										e.printStackTrace();
									}
								}
								else
								{
									p.sendMessage(prefix + ChatColor.RED + "Vous n'êtes pas en guerre contre " + args[1] + " !");
								}
								break;
	
							default:
								break;
						}
						break;
	
					default:
						sender.sendMessage(prefix + ChatColor.RED + "Commande non trouvé !");
						break;
				}
			}
		}
		return false;
	}
	
	public boolean isGuerreExist(String guerre)
	{
		ResultSet res = null;
		
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM guerre_guerre");
			
			while(res.next())
			{
				if(res.getString("nom").equalsIgnoreCase(guerre))
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
	
	public boolean isGuerreExistByClan(String clan)
	{
		ResultSet res = null;
		
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM guerre_guerre");
			
			while(res.next())
			{
				if(res.getString("cible").equalsIgnoreCase(clan))
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
	
	public boolean isAlreadyInGuerre(String declareur, String cible)
	{
		ResultSet res = null;
		
		try
		{
			res = plugin.stat.executeQuery("SELECT * FROM guerre_guerre");
			
			while(res.next())
			{
				if(res.getString("cible").equalsIgnoreCase(cible) && res.getString("declareur").equalsIgnoreCase(declareur))
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
}
