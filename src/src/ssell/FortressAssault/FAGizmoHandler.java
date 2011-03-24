package ssell.FortressAssault;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import ssell.FortressAssault.FortressAssault;

//------------------------------------------------------------------------------------------

public class FAGizmoHandler 
{
	private final FortressAssault plugin;
	
	public class FAGizmo
	{
		public String team;
		public Block block;
		public String destroyer;
		public boolean destructing;
		
		public FAGizmo( String p_Team, Block p_Block )
		{
			team = p_Team;
			block = p_Block;
			destroyer = null;
			destructing = false;
		}
	}
	
	//--------------------------------------------------------------------------------------
	
	private List< FAGizmo > gizmoList = new ArrayList< FAGizmo >( );
	
	private int teamCount;
	
	//--------------------------------------------------------------------------------------
	
	public FAGizmoHandler( FortressAssault instance, int numberOfTeams )
	{
		teamCount = numberOfTeams;
		plugin = instance;
	}
	
	public boolean addGizmo( Player player, Block block )
	{
		FAGizmo gizmo = new FAGizmo( plugin.getTeam( player ), block );
		
		for( int i = 0; i < gizmoList.size( ); i++ )
		{
			if( gizmoList.get( i ).team.equals( gizmo.team ) )
			{
				player.sendMessage( ChatColor.DARK_RED + "There is already a Gizmo for your team!" );

				return false;
			}
		}
		
		if( plugin.getTeam( player ).equals( "BLUE" ) )
		{
			plugin.getServer( ).broadcastMessage( ChatColor.BLUE + "Blue Team has placed " +
					"their Gizmo!" );
		}
		else
		{
			plugin.getServer( ).broadcastMessage( ChatColor.RED + "Red Team has placed " +
			"their Gizmo!" );
		}
		
		gizmoList.add( gizmo );
		
		return true;
	}
	
	public boolean gizmosPlaced( )
	{
		if( gizmoList.size( ) == teamCount )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public String getPlacedGizmoTeam( )
	{
		if( gizmoList.size( ) != 0 )
		{
			return gizmoList.get( 0 ).team;
		}
		
		return null;
	}
	
	public boolean isGizmo( Block block )
	{
		for( int i = 0; i < gizmoList.size( ); i++ )
		{
			if( gizmoList.get( i ).block == block )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void gizmoHit( Player player, Block block )
	{
		FAGizmo gizmo = null;
		
		for( int i = 0; i < gizmoList.size( ); i++ )
		{
			if( gizmoList.get( i ).block == block )
			{
				gizmo = gizmoList.get( i );
				
				break;
			}
		}
		
		//Has the gizmo been hit by a player on its team?
		if( plugin.getTeam( player ).equals( gizmo.team ) )
		{
			//Same team
			//is the gizmo being saved from destructing?
			if( gizmo.destructing )
			{
				gizmo.destroyer = null;
				gizmo.destructing = false;
				
				if( plugin.getTeam( player ).equals( "BLUE" ) )
				{
					plugin.getServer( ).broadcastMessage( ChatColor.BLUE +
							player.getDisplayName( ) + " saved the Blue Gizmo!" );
				}
				else
				{
					plugin.getServer( ).broadcastMessage( ChatColor.RED +
							player.getDisplayName( ) + " saved the Red Gizmo!" );
				}
			}
			//else do nothing
		}
		else
		{
			//Opposing team
			if( !gizmo.destructing )
			{
				gizmo.destroyer = player.getDisplayName( );
				gizmo.destructing = true;
				
				if( plugin.getTeam( player ).equals( "BLUE" ) )
				{
					plugin.getServer( ).broadcastMessage( ChatColor.RED +
							player.getDisplayName( ) + " attacked the Red Gizmo!" );
				}
				else
				{
					plugin.getServer( ).broadcastMessage( ChatColor.BLUE +
							player.getDisplayName( ) + " attacked the Blue Gizmo!" );
				}
				
				final FAGizmo finalGizmo = gizmo;
				
				//Start counting
				plugin.getServer( ).getScheduler( ).scheduleAsyncDelayedTask( plugin, 
						new Runnable( )
				{
					public void run( )
					{
						destroyGizmo( finalGizmo );
					}
				}, 100 ); //5 seconds
			}
		}
	}
	
	public void destroyGizmo( FAGizmo gizmo )
	{
		//Check if the gizmo has not been saved
		if( gizmo.destructing )
		{
			for( int i = 0; i < gizmoList.size( ); i++ )
			{
				if( gizmoList.get( i ) != gizmo )
				{
					gizmoList.get( i ).destructing = false;
				}
			}
			
			if( gizmo.team.equals( "BLUE" ) )
			{
				plugin.getServer( ).broadcastMessage( ChatColor.RED + "Red Team " + 
						ChatColor.GOLD + "wins!" );
			}
			else
			{
				plugin.getServer( ).broadcastMessage( ChatColor.BLUE + "Blue Team " + 
						ChatColor.GOLD + "wins!" );
			}
			
			//Game is over.
			plugin.gameOver( );	
		}
	}
	
	public void clearList( )
	{
		for( int i = 0; i < gizmoList.size( ); i++ )
		{
			gizmoList.get( i ).block.setType( Material.AIR );
		}
		
		gizmoList.clear( );
	}
	
	public String getDestroyer( )
	{
		for( int i = 0; i < gizmoList.size( ); i++ )
		{
			if( gizmoList.get( i ).destructing )
			{
				return gizmoList.get( i ).destroyer;
			}
		}
		
		return null;
	}
}
