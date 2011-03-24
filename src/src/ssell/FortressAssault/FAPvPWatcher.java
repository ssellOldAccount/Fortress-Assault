package ssell.FortressAssault;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import ssell.FortressAssault.FortressAssault;

//------------------------------------------------------------------------------------------

public class FAPvPWatcher 
{	
	public class FAPlayer
	{
		public String player;
		public int kills;
		public int deaths;
		public int destructions;
		
		public FAPlayer( Player p_Player )
		{
			player = p_Player.getDisplayName( );
			
			kills = 0;
			deaths = 0;
			destructions = 0;
		}
	}
	
	//--------------------------------------------------------------------------------------
	
	private final FortressAssault parent;
	
	private List< FAPlayer > playerList = new ArrayList< FAPlayer >( );
	//--------------------------------------------------------------------------------------
	
	public FAPvPWatcher( FortressAssault instance )
	{
		parent = instance;
	}
	
	public void add( Player player )
	{
		if( getPlayer( player ) == null )
		{
			playerList.add( new FAPlayer( player ) );
		}
	}
	
	public void remove( Player player )
	{
		for( int i = 0; i < playerList.size( ); i++ )
		{
			if( playerList.get( i ).player == player.getDisplayName( ) )
			{
				playerList.remove( i );
				
				break;
			}
		}
	}
	
	public void clear( )
	{
		playerList.clear( );
	}
	
	public void killEvent( Player killer, Player killed )
	{
		String killerStr = parent.getTeam( killer );
		String killedStr = parent.getTeam( killed );
		
		FAPlayer tempPlayer;
		
		//----------------------------------------------------------------------------------

		if( ( killerStr != null ) && ( killedStr != null ) )
		{
			//If a player killed a teammate.
			if( killerStr.equalsIgnoreCase( killedStr ) )
			{
				if( killerStr.equals( "BLUE" ) )
				{
					parent.getServer( ).broadcastMessage( ChatColor.BLUE +
					killer.getDisplayName( ) + " committed treason on " + killed.getDisplayName( ) );
				}
				else
				{
					parent.getServer( ).broadcastMessage( ChatColor.RED +
							killer.getDisplayName( ) + " committed treason on " + killed.getDisplayName( ) );
				}
				
				tempPlayer = getPlayer( killer );
				
				if( tempPlayer != null )
				{
					tempPlayer.deaths += 1;
				}
			}
			else
			{
				//Valid kill
				
				if( killerStr.equals( "BLUE" ) )
				{
					parent.getServer( ).broadcastMessage( ChatColor.BLUE +
					killer.getDisplayName( ) + " killed " + killed.getDisplayName( ) );
				}
				else
				{
					parent.getServer( ).broadcastMessage( ChatColor.RED +
					killer.getDisplayName( ) + " killed " + killed.getDisplayName( ) );
				}
				
				tempPlayer = getPlayer( killer );
				
				if( tempPlayer != null )
				{
					tempPlayer.kills += 1;
				}
				
				tempPlayer = getPlayer( killed );
				
				if( tempPlayer != null )
				{
					tempPlayer.deaths += 1;
				}
			}
		}
	}
	
	public void destructionEvent( Player player )
	{
		FAPlayer tempPlayer =  getPlayer( player );
		String playerStr = parent.getTeam( player );
		
		if( ( player != null ) && ( playerStr != null ) )
		{
			if( playerStr.equals( "BLUE" ) )
			{
				parent.getServer( ).broadcastMessage( ChatColor.BLUE + 
						player.getDisplayName( ) + " destroyed the Gizmo!" );
			}
			else
			{
				parent.getServer( ).broadcastMessage( ChatColor.RED + 
						player.getDisplayName( ) + " destroyed the Gizmo!" );
			}
			
			tempPlayer.destructions += 1;
		}
	}
	
	public FAPlayer getPlayer( Player player )
	{		
		for( int i = 0; i < playerList.size( ); i++ )
		{
			if( playerList.get( i ).player.equalsIgnoreCase( player.getDisplayName( ) ) )
			{
				return playerList.get( i );
			}
		}
		
		return null;
	}
	
	/**
	 * Prints the result of the battle. Example:<br><br>
	 * # Battle Results<br>
	 * # Name | Kills | Deaths | Destructions<br>
	 * # BlueA | 12 | 9 | 0<br>
	 * # BlueB | 11 | 3 | 1<br>
	 * # RedA | 15 | 16 | 0<br>
	 * # RedB | 3 | 0 |<br>
	 */
	public void printResults( String destroyer )
	{
		boolean blueWon = false;
		
		for( int i = 0; i < playerList.size( ); i++ )
		{
			if( playerList.get( i ).destructions != 0 )
			{
				blueWon = parent.getTeam( 
				parent.getServer( ).getPlayer( playerList.get( i ).player ) ).equals( "BLUE" );
			
				break;
			}
		}
		
		//Sort players by kill total
		sortList( );
		
		parent.getServer( ).broadcastMessage( ChatColor.YELLOW + "# Battle Results" );
		parent.getServer( ).broadcastMessage( ChatColor.YELLOW + 
				"# Name | Kills | Deaths | Destructions" );
			
		for( int i = 0; i < playerList.size( ); i++ )
		{
			FAPlayer player = playerList.get( i );
			
			if( player.player.equalsIgnoreCase( destroyer ) )
			{
				player.destructions += 1;
			}
			
			if( parent.getTeam( 
				parent.getServer( ).getPlayer( playerList.get( i ).player ) ).equals( "BLUE" ) )
			{
				parent.getServer( ).broadcastMessage( ChatColor.YELLOW + "# " +
				ChatColor.BLUE + player.player + " | " + player.kills + " | " +
				player.deaths + " | " + player.destructions );
			}
			else
			{
				parent.getServer( ).broadcastMessage( ChatColor.YELLOW + "# " +
				ChatColor.RED + player.player + " | " + player.kills + " | " +
				player.deaths + " | " + player.destructions );
			}
		}
	}
	
	/**
	 * Sorts the playerList in order of number of kills.
	 */
	public void sortList( )
	{
		List< FAPlayer > tempList = new ArrayList< FAPlayer >( );
		
		for( int i = 0; i < playerList.size( ); i++ )
		{
			if( i == 0 )
			{
				tempList.add( playerList.get( i ) );
			}
			else
			{
				for( int j = 0; j < tempList.size( ); j++ )
				{
					//Iterate through until a value less than current is found.
					
					if( tempList.get( j ).kills <= playerList.get( i ).kills )
					{
						tempList.add( j, playerList.get( i ) );
						
						break;
					}
				}
			}
		}
		
		playerList = tempList;
	}
}
