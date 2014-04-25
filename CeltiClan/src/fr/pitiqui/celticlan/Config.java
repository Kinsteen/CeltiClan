package fr.pitiqui.celticlan;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config
{
	private static File file;
	static FileConfiguration fileConfig;
	
	public static void loadConfigFile()
	{
		file = new File(Main.dataFolder(), "config.yml");
		if(!file.exists())
		{
			fileConfig = YamlConfiguration.loadConfiguration(file);
			
			fileConfig.set("BDD.clan.host", "");
			fileConfig.set("BDD.clan.port", "");
			fileConfig.set("BDD.clan.database", "");
			fileConfig.set("BDD.clan.user", "");
			fileConfig.set("BDD.clan.pass", "");
	 
			try {
				fileConfig.save(file);
			} catch(IOException ex) {
	 
			}
		}
		fileConfig = YamlConfiguration.loadConfiguration(file);
	}
	
	public static void saveConfigFile()
	{
		file = new File(Main.dataFolder(), "config.yml");
		fileConfig = YamlConfiguration.loadConfiguration(file);
		
		try // Puis on sauvegarde!
		{
			fileConfig.save(file);
		} catch(IOException ex)	{
		
		}
	}
	
	public static Object loadElement(String path)
	{
		return fileConfig.get(path);
	}
	
	public static int loadInt(String path)
	{
		return fileConfig.getInt(path);
	}
	
	public static String loadString(String path)
	{
		return fileConfig.getString(path);
	}
	
	public static double loadDouble(String path)
	{
		return fileConfig.getDouble(path);
	}
	
	public static boolean loadBoolean(String path)
	{
		return fileConfig.getBoolean(path);
	}

	public static void setElement(String path, Object var)
	{
		fileConfig.set(path, var);
		try {
			fileConfig.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
