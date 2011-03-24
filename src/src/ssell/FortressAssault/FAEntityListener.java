package ssell.FortressAssault;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import ssell.FortressAssault.FortressAssault;
import ssell.FortressAssault.FAPvPWatcher;

//------------------------------------------------------------------------------------------

public class FAEntityListener 
	extends EntityListener
{
	private final FortressAssault plugin;
	private final FAPvPWatcher pvpWatcher;
	
	boolean godEnabled = false;
	boolean listenToKills = false;
	
	List< Player > playerList = new ArrayList< Player >( );
	List< String > deadPlayers = new ArrayList< String >( );
	
	//--------------------------------------------------------------------------------------
	
	public FAEntityListener( FortressAssault instance )
	{
		plugin = instance;
		pvpWatcher = instance.getWatcher( );
	}
	
	/**
	 * Whenever an entity is damaged, this method is called.<br><br>
	 * If godEnabled is true, it is checked if the entity is a player. If
	 * it is a player, and the player is on the playerList list, then
	 * the damage is canceled.
	 */
	@Override 
	public void onEntityDamage( EntityDamageEvent event )
	{
		super.onEntityDamage(event);

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
		else if( listenToKills )
		{
			if( event instanceof EntityDamageByEntityEvent )
			{				
				EntityDamageByEntityEvent damageEvent = ( EntityDamageByEntityEvent  )event;
				
				if( ( damageEvent.getDamager( ) instanceof Player ) &&
					  damageEvent.getEntity( ) instanceof Player )
				{					
					Player victim = ( Player )damageEvent.getEntity( );
					Player attacker = ( Player )damageEvent.getDamager( );
					
					int damage = event.getDamage( );
					int oldHealth = victim.getHealth( );
					int newHealth = oldHealth - damage;
					
					if( newHealth <= 0 )
					{
						if( onDeadList( victim.getDisplayName( ) ) == -1 )
						{
							deadPlayers.add( victim.getDisplayName( ) );
							
							victim.getInventory( ).clear( );
							
							pvpWatcher.killEvent( attacker,  victim );
						}
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
	
	public void pvpListen( boolean enable )
	{
		listenToKills = enable;
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
		pvpWatcher.add( player );
		
		return true;
	}
	
	/**
	 * Clears the list of all players.
	 */
	public void clearList( )
	{
		playerList.clear( );
	}
	
	public boolean onList( Player player )
	{
		for( int i = 0; i < playerList.size( ); i++ )
		{
			if( playerList.get( i ) == player )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public int onDeadList( String name )
	{
		for( int i = 0; i < deadPlayers.size( ); i++ )
		{
			if( deadPlayers.get( i ).equalsIgnoreCase( name ) )
			{
				return i;
			}
		}
		
		return -1;
	}
	
	public void removeFromDeadList( int location )
	{
		deadPlayers.remove( location );
	}
}
