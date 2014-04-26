package fr.pitiqui.celticlan;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config
{
	File file;
	FileConfiguration fileConfig;
	File dataFolder;
	
	public void setDataFolder(File dataFolderFunc)
	{
		dataFolder = dataFolderFunc;
	}
	
	public void loadConfigFile()
	{
		file = new File(dataFolder, "config.yml");
		if(!file.exists())
		{
			fileConfig = YamlConfiguration.loadConfiguration(file);
	 
			try {
				fileConfig.save(file);
			} catch(IOException ex) {
	 
			}
		}
		fileConfig = YamlConfiguration.loadConfiguration(file);
	}
	
	public void initConfig(String path1, String var1)
	{
		loadConfigFile();
		if(!file.exists())
		{
			fileConfig.set(path1, var1);
			saveConfigFile();
		}
	}
	public void initConfig(String path1, String var1, String path2, String var2)
	{
		loadConfigFile();
		if(!file.exists())
		{
			fileConfig.set(path1, var1);
			fileConfig.set(path2, var2);
			saveConfigFile();
		}
	}
	public void initConfig(String path1, String var1, String path2, String var2, String path3, String var3)
	{
		loadConfigFile();
		if(!file.exists())
		{
			fileConfig.set(path1, var1);
			fileConfig.set(path2, var2);
			fileConfig.set(path3, var3);
			saveConfigFile();
		}
	}
	public void initConfig(String path1, String var1, String path2, String var2, String path3, String var3, String path4, String var4)
	{
		loadConfigFile();
		if(!file.exists())
		{
			fileConfig.set(path1, var1);
			fileConfig.set(path2, var2);
			fileConfig.set(path3, var3);
			fileConfig.set(path4, var4);
			saveConfigFile();
		}
	}
	public void initConfig(String path1, String var1, String path2, String var2, String path3, String var3, String path4, String var4, String path5, String var5)
	{
		loadConfigFile();
		if(!file.exists())
		{
			fileConfig.set(path1, var1);
			fileConfig.set(path2, var2);
			fileConfig.set(path3, var3);
			fileConfig.set(path4, var4);
			fileConfig.set(path5, var5);
			saveConfigFile();
		}
	}
	public void initConfig(String path1, String var1, String path2, String var2, String path3, String var3, String path4, String var4, String path5, String var5, String path6, String var6)
	{
		loadConfigFile();
		if(!file.exists())
		{
			fileConfig.set(path1, var1);
			fileConfig.set(path2, var2);
			fileConfig.set(path3, var3);
			fileConfig.set(path4, var4);
			fileConfig.set(path5, var5);
			fileConfig.set(path6, var6);
			saveConfigFile();
		}
	}
	public void initConfig(String path1, String var1, String path2, String var2, String path3, String var3, String path4, String var4, String path5, String var5, String path6, String var6, String path7, String var7)
	{
		loadConfigFile();
		if(!file.exists())
		{
			fileConfig.set(path1, var1);
			fileConfig.set(path2, var2);
			fileConfig.set(path3, var3);
			fileConfig.set(path4, var4);
			fileConfig.set(path5, var5);
			fileConfig.set(path6, var6);
			fileConfig.set(path7, var7);
			saveConfigFile();
		}
	}
	
	public void saveConfigFile()
	{
		file = new File(dataFolder, "config.yml");
		fileConfig = YamlConfiguration.loadConfiguration(file);
		
		try
		{
			fileConfig.save(file);
		} catch(IOException ex)	{
		
		}
	}
	
	public Object loadElement(String path)
	{
		return fileConfig.get(path);
	}
	
	public int loadInt(String path)
	{
		return fileConfig.getInt(path);
	}
	
	public String loadString(String path)
	{
		return fileConfig.getString(path);
	}
	
	public double loadDouble(String path)
	{
		return fileConfig.getDouble(path);
	}
	
	public boolean loadBoolean(String path)
	{
		return fileConfig.getBoolean(path);
	}

	public void setElement(String path, Object var)
	{
		fileConfig.set(path, var);
		try {
			fileConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initConfig(String path1, String var1, String path2,
			String var2, String path3, String var3, String path4,
			String var4, String path5, String var5, String path6,
			String var6, String path7, String var7, String path8,
			String var8)
	{
		loadConfigFile();
		if(!file.exists())
		{
			fileConfig.set(path1, var1);
			fileConfig.set(path2, var2);
			fileConfig.set(path3, var3);
			fileConfig.set(path4, var4);
			fileConfig.set(path5, var5);
			fileConfig.set(path6, var6);
			fileConfig.set(path7, var7);
			fileConfig.set(path8, var8);
			try {
				fileConfig.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
