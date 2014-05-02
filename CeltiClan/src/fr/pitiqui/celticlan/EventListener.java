package fr.pitiqui.celticlan;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener
{
	HashMap<String, String> invit = Main.invit;
	HashMap<Player, String> chat = Main.chat;
	
    private Main plugin;
    
    public EventListener(Main plugin)
    {
        this.plugin = plugin;
    }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		if(invit.containsKey(e.getPlayer().getUniqueId().toString()))
		{
			e.getPlayer().sendMessage(Main.prefix + ChatColor.YELLOW + "Quelqu'un vous a invité dans le clan " + invit.get(e.getPlayer().getUniqueId().toString()));
			e.getPlayer().sendMessage(Main.prefix + ChatColor.YELLOW + "Pour rejoindre le clan, faites /clan join " + invit.get(e.getPlayer().getUniqueId().toString()));
		}
		
		chat.put(e.getPlayer(), "p");
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		chat.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		ClanCommandExecutor clan = new ClanCommandExecutor(plugin);
		
		if(chat.get(e.getPlayer()).equalsIgnoreCase("c"))
		{
			e.setCancelled(true);
			
			for(Player p : clan.getOnlinePlayersFromClan(clan.getClan(e.getPlayer().getName())))
			{
				p.sendMessage(ChatColor.GREEN + "[Clan] " + e.getPlayer().getName() + ": " + e.getMessage());
			}
		}
		else if(chat.get(e.getPlayer()).equalsIgnoreCase("a"))
		{
			e.setCancelled(true);
			
			ArrayList<String> allys = clan.getAllys(clan.getClan(e.getPlayer().getName()));
			
			for (int i = 0; i < allys.size(); i++)
			{
				for(Player p : clan.getOnlinePlayersFromClan(allys.get(i)))
				{
					p.sendMessage(ChatColor.BLUE + "[Alliance] " + e.getPlayer().getName() + ": " + e.getMessage());
				}
			}
			
			for(Player p : clan.getOnlinePlayersFromClan(clan.getClan(e.getPlayer().getName())))
			{
				p.sendMessage(ChatColor.BLUE + "[Alliance] " + e.getPlayer().getName() + ": " + e.getMessage());
			}
		}
	}
}
