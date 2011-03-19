package ssell.FortressAssault;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import ssell.FortressAssault.FortressAssault;

//------------------------------------------------------------------------------------------

public class FAEntityListener 
	extends EntityListener
{
	private final FortressAssault plugin;
	
	boolean godEnabled = false;
	
	List< Player > playerList = new ArrayList< Player >( );
	
	//--------------------------------------------------------------------------------------
	
	public FAEntityListener( FortressAssault instance )
	{
		plugin = instance;
	}
	
	/**
	 * Whenever an entity is damaged, this method is called.<br><br>
	 * If godEnabled is true, it is checked if the entity is a player. If
	 * it is a player, and the player is on the playerList list, then
	 * the damage is canceled.
	 */
	public void onEntityDamage( EntityDamageEvent event )
	{
		if( godEnabled )
		{
			Entity entity = event.getEntity( );
			
			if( entity instanceof Player )
			{
				Player player = ( Player )entity;
				
				for( int i = 0; i < playerList.size( ); i++ )
				{
					if( playerList.get( i ) == player )
					{
						event.setCancelled( true );
						
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Enables or disables whether players can be damaged.
	 * 
	 * @param enable
	 */
	public void setGod( boolean enable )
	{
		godEnabled = enable;
	}
	
	/** 
	 * Adds player to list. Player must be in list to not be damaged.
	 * 
	 * @param player
	 * @return
	 */
	public boolean addToList( Player player )
	{
		for( int i = 0; i < playerList.size( ); i++ )
		{
			if( playerList.get( i ) == player )
			{
				return false;
			}
		}
		
		playerList.add( player );
		
		return true;
	}
	
	/**
	 * Clears the list of all players.
	 */
	public void clearList( )
	{
		playerList.clear( );
	}
	
	
}
