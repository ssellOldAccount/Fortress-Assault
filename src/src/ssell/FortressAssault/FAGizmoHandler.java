package ssell.FortressAssault;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import ssell.FortressAssault.FortressAssault;
import ssell.FortressAssault.FortressAssault.FAPlayer;
import ssell.FortressAssault.FortressAssault.Team;

//------------------------------------------------------------------------------------------

public class FAGizmoHandler 
{
	private final FortressAssault plugin;
	
	public class FAGizmo
	{
		public Team team;
		public Block block;
		public FAPlayer destroyer;
		public boolean destructing;
		public int destructingtask;
		
		public FAGizmo( Team p_Team, Block p_Block )
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
	
	public boolean addGizmo( FAPlayer thisPlayer, Block block )
	{		
		FAGizmo gizmo = new FAGizmo( thisPlayer.team, block );
		
		for( int i = 0; i < gizmoList.size( ); i++ )
		{
			if( gizmoList.get( i ).team.equals( gizmo.team ) )
			{
				thisPlayer.player.sendMessage( ChatColor.DARK_RED + "There is already a Gizmo for your team!" );

				return false;
			}
		}
		plugin.getServer( ).broadcastMessage( plugin.getTeamColor(thisPlayer.team) + thisPlayer.team.toString()+" Team has placed their Gizmo!" );
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
	
	public Team getPlacedGizmoTeam( )
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
	
	public void gizmoHit( FAPlayer player, Block block )
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
		if( player.team.equals( gizmo.team ) )
		{
			//Same team
			//is the gizmo being saved from destructing?
			if( gizmo.destructing )
			{
				gizmo.destroyer = null;
				gizmo.destructing = false;
				plugin.getServer( ).getScheduler( ).cancelTask(gizmo.destructingtask);
				plugin.getServer( ).broadcastMessage( plugin.getTeamColor(player.team) + player.name + " saved the "+player.team.toString()+" Gizmo!" );
			}
		}
		else
		{
			//Opposing team
			if( !gizmo.destructing )
			{
				gizmo.destroyer = player;
				gizmo.destructing = true;
				
				plugin.getServer( ).broadcastMessage( plugin.getTeamColor(player.team) + player.name + " attacked the "+gizmo.team.toString()+" Gizmo!" );
				
				final FAGizmo finalGizmo = gizmo;
				
				//Start counting
				gizmo.destructingtask = plugin.getServer( ).getScheduler( ).scheduleAsyncDelayedTask( plugin, 
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
			FAPlayer thisPlayer = getDestroyer();
			
			plugin.getServer( ).broadcastMessage( plugin.getTeamColor(thisPlayer.team) + thisPlayer.team.toString()+" Team " +	ChatColor.GOLD + "wins!" );
			thisPlayer.destructions++;
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
	
	public FAPlayer getDestroyer( )
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
