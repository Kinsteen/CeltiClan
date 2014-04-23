package fr.pitiqui.celticlan;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener
{
	Main plugin = new Main();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		String clan = plugin.getClan(e.getPlayer());
		plugin.chat.put(e.getPlayer().getUniqueId().toString(), clan);
	}
	
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		
	}
}
