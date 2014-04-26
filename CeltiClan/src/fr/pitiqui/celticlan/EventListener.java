package fr.pitiqui.celticlan;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener
{
	HashMap<String, String> invit = Main.invit;
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		if(invit.containsKey(e.getPlayer().getUniqueId().toString()))
		{
			e.getPlayer().sendMessage(Main.prefix + ChatColor.YELLOW + "Quelq'un vous a invité dans le clan " + invit.get(e.getPlayer().getUniqueId().toString()));
			e.getPlayer().sendMessage(Main.prefix + ChatColor.YELLOW + "Pour rejoindre le clan, faites /clan join " + invit.get(e.getPlayer().getUniqueId().toString()));
		}
	}
}
