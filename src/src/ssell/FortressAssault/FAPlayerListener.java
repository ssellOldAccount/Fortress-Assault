package ssell.FortressAssault;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

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
		boolean found = false;
		
		Player player = event.getPlayer( );
		
		for( int i = 0; i < plugin.getStrList( true ).size( ); i++ )
		{			
			if( plugin.getStrList( true ).get( i ).equalsIgnoreCase( player.getDisplayName( ) ) )
			{
				found = true;
				
				plugin.getTeamList( true ).add( player );
			}
		}
		
		if( !found )
		{
			for( int i = 0; i < plugin.getStrList( false ).size( ); i++ )
			{				
				if( plugin.getStrList( false ).get( i ).equalsIgnoreCase( player.getDisplayName( ) ) )
				{		
					found = true;
					
					plugin.getTeamList( false ).add( player );
				}
			}
		}

		//Resupply the player
		if( found )
		{			
			player = plugin.getServer( ).getPlayer( player.getDisplayName( ) );
			
			player.getInventory( ).clear( );
		
			player.getInventory( ).setHelmet( new ItemStack( Material.IRON_HELMET, 1 ) );
			player.getInventory( ).setChestplate( new ItemStack( Material.IRON_CHESTPLATE, 1 ) );
			player.getInventory( ).setLeggings( new ItemStack( Material.IRON_LEGGINGS, 1 ) );
			player.getInventory( ).setBoots( new ItemStack( Material.IRON_BOOTS, 1 ) );
			
			player.getInventory( ).addItem( new ItemStack( Material.IRON_SWORD, 1 ) );
			
			player.getInventory( ).addItem( new ItemStack( Material.WOOD_PICKAXE, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.TNT, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.MUSHROOM_SOUP, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.COOKED_FISH, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.BREAD, 1 ) );
			
			//DEPRECATED. need to find alternative
			player.updateInventory( );
		}
		
		//Take care of the dead list in the EntityListener
		int position = entityListener.onDeadList( player.getDisplayName( ) );
		
		if(  position != -1 )
		{
			entityListener.removeFromDeadList( position );
		}
	}
}
