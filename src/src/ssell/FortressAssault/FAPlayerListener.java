package ssell.FortressAssault;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class FAPlayerListener 
	extends PlayerListener
{
	private final FortressAssault plugin;
	private final FAEntityListener entityListener;
	
	public FAPlayerListener( FortressAssault instance, FAEntityListener entity )
	{
		plugin = instance;
		entityListener = entity;
	}
	
	@Override
	public void onPlayerRespawn( PlayerRespawnEvent event )
	{	
		final PlayerRespawnEvent finalEvent = event;
				
		plugin.getServer( ).getScheduler( ).scheduleSyncDelayedTask( plugin, new Runnable( ) 
		{
			public void run( )
			{
				delayedCheck( finalEvent );
			}
		}, 20 );
	}
	
	public void delayedCheck( PlayerRespawnEvent event )
	{
		boolean found = false;
		
		Player player = event.getPlayer( );
		
		for( int i = 0; i < plugin.getStrList( true ).size( ); i++ )
		{
			System.out.println( plugin.getStrList( true ).get( i ) + "\t" + player.getDisplayName( ) );
			
			if( plugin.getStrList( true ).get( i ).equalsIgnoreCase( player.getDisplayName( ) ) )
			{
				found = true;
				
				plugin.getServer().broadcastMessage( ChatColor.BLUE + 
						player.getDisplayName( ) + " readded to blue" );
				
				plugin.getTeamList( true ).add( player );
			}
		}
		
		if( !found )
		{
			for( int i = 0; i < plugin.getStrList( false ).size( ); i++ )
			{
				System.out.println( plugin.getStrList( true ).get( i ) + "\t" + player.getDisplayName( ) );
				
				if( plugin.getStrList( false ).get( i ).equalsIgnoreCase( player.getDisplayName( ) ) )
				{
					plugin.getServer().broadcastMessage( ChatColor.RED + 
							player.getDisplayName( ) + " readded to red" );
					
					plugin.getTeamList( false ).add( player );
				}
			}
		}
		
		//Take care of the dead list in the EntityListener
		int position = entityListener.onDeadList( player.getDisplayName( ) );
		if(  position != -1 )
		{
			entityListener.removeFromDeadList( position );
		}
	}
}
