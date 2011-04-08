package ssell.FortressAssault;

import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import ssell.FortressAssault.FortressAssault.FAPlayer;

@SuppressWarnings("unused")
public class FAPlayerListener 
	extends PlayerListener
{
	private final FortressAssault plugin;
	
	public FAPlayerListener( FortressAssault instance, FAEntityListener entity )
	{
		plugin = instance;
	}
	
	public void onPlayerQuit( PlayerQuitEvent event) {
		//check if player that quits is in the game and return their inventory.
		Player player = event.getPlayer( );
		FAPlayer thisPlayer = plugin.getFAPlayer(player);
		if (thisPlayer != null) {
			if (plugin.phase != 0) {
				//this works for restoring items when player goes offline, but there are issues when player come back to restore/equip them 
				/*
				plugin.returnInventory(thisPlayer);
				//force save of offline player.				
				CraftWorld cWorld = (CraftWorld)thisPlayer.world;
				CraftPlayer cPlayer = (CraftPlayer)thisPlayer.player;
				cWorld.getHandle().o().d().a(cPlayer.getHandle());
				*/
				
			}
		}		
	}
	
	public void onPlayerJoin( PlayerJoinEvent event) {
		//check if player that join is in the game. getFAPlayer will update their entityid properly.
		Player player = event.getPlayer( );
		FAPlayer thisPlayer = plugin.getFAPlayer(player);
		if (thisPlayer != null) {
			//player came back during the game, restore their items and give them the proper equipment.
			player = plugin.getServer( ).getPlayer( player.getDisplayName( ) );
			if (plugin.phase != 0) {
				//not sure why but if a player dies then disconnects this will cause them to die when they log back in.
				//plugin.storeInventory(thisPlayer);
				plugin.giveGameItems(player);
			}
		}		
	}
	
	@Override
	public void onPlayerRespawn( PlayerRespawnEvent event )
	{	
		final PlayerRespawnEvent finalEvent = event;
		Player player = event.getPlayer( );
		FAPlayer thisPlayer = plugin.getFAPlayer(player);
		
		if (thisPlayer == null) {
			return;
		}
		thisPlayer.dead = false;				
		
		plugin.getServer( ).getScheduler( ).scheduleAsyncDelayedTask( plugin, new Runnable( ) 
		{
			public void run( )
			{
					delayedCheck( finalEvent );
			}
		}, 20 );
		
	}
	
	
	public void delayedCheck( PlayerRespawnEvent event )
	{	
		Player player = event.getPlayer( );
		FAPlayer thisPlayer = plugin.getFAPlayer(player);
		
		if (thisPlayer == null) {
			return;
		}
		thisPlayer.dead = false;
		player = plugin.getServer( ).getPlayer( player.getDisplayName( ) );

		if (plugin.phase != 0) {
			plugin.giveGameItems(player);
		}
		
	}
}
