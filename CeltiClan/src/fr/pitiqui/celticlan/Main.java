package fr.pitiqui.celticlan;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
	String table_ally;
	
	Config config = new Config();
	ConfigHome configHome = new ConfigHome();

	Connection connection = null;
	Statement stat;
	
	ClanCommandExecutor clan = new ClanCommandExecutor(this);
	
	Location loc = null;
	
	static HashMap<String, String> invit = new HashMap<String, String>();
	
	/**
	 * First argument: the player
	 * Second argument: the channel
	 * "p": public
	 * "c": clan
	 * "a": alliance
	 */
	static HashMap<Player, String> chat = new HashMap<Player, String>();
	
	public void onEnable()
	{
		config.setDataFolder(getDataFolder());
		configHome.setDataFolder(getDataFolder());
		
		/*config.setElement("BDD.clan.host", "localhost");
		config.setElement("BDD.clan.port", "3306");
		config.setElement("BDD.clan.database", "CeltiClan");
		config.setElement("BDD.clan.user", "root");
		config.setElement("BDD.clan.pass", "");
		config.setElement("BDD.clan.table_invit", "clan_invit");
		config.setElement("BDD.clan.table_players", "clan_players");
		config.setElement("BDD.clan.table_clan", "clan_clan");
		config.setElement("BDD.guerre.table_guerre", "guerre_guerre");*/

		configHome.initConfig("", "");
		
		config.loadConfigFile();
		configHome.loadConfigFile();
		
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
		getCommand("clan").setExecutor(new ClanCommandExecutor(this));
		//getCommand("guerre").setExecutor(new WarCommandExecutor(this));
		
		host = config.loadString("BDD.clan.host");
		bdd = config.loadString("BDD.clan.database");
		port = config.loadInt("BDD.clan.port");
		user = config.loadString("BDD.clan.user");
		pass = config.loadString("BDD.clan.pass");
		table_players = config.loadString("BDD.clan.table_players");
		table_clan = config.loadString("BDD.clan.table_clan");
		table_invit = config.loadString("BDD.clan.table_invit");
		table_ally = config.loadString("BDD.clan.table_ally");
				
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
		
		for(Player p : getServer().getOnlinePlayers())
		{
			chat.put(p, "p");
		}
	}
	
	public void onDisable()
	{
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