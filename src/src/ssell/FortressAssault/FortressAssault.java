package ssell.FortressAssault;

//------------------------------------------------------------------------------------------

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

//------------------------------------------------------------------------------------------

/**
 * Assault Fortress : Player vs Player Mod<br>
 * Minecraft - Bukkit<br><br>
 * http://www.1143pm.com/
 * 
 * @author Steven Sell
 */
public class FortressAssault
	extends JavaPlugin
{
	//Objects
	
	private static final Logger log = Logger.getLogger( "Minecraft" );
	
	//The order of these is critical
	private final FAGizmoHandler gizmoHandler = new FAGizmoHandler( this, 2 );
	private final FABlockListener blockListener = new FABlockListener( this, gizmoHandler );
	private final FAPvPWatcher pvpWatcher = new FAPvPWatcher( this );
	private final FAEntityListener entityListener = new FAEntityListener( this );
	private final FAPlayerListener playerListener = new FAPlayerListener( this, entityListener );
	
	private int resources = 2;			//Default resource level (normal)
	private int timeLimit = 1;			//Default time limit to build
	
	private final List< Player > blueTeam = new ArrayList< Player >( );
	private final List< Player > redTeam = new ArrayList< Player >( );
	private final List< String > blueStrList = new ArrayList< String >( );
	private final List< String > redStrList = new ArrayList< String >( );
	
	private boolean fortify = false;
	private boolean assault = false;
	
	//--------------------------------------------------------------------------------------

	public FAPvPWatcher getWatcher( )
	{
		return pvpWatcher;
	}
	
	public void onDisable( ) 
	{
		log.info( "Fortress Assault is disabled!" );
	}

	/**
	 * Called when the Mod starts.
	 */
	public void onEnable( ) 
	{
		PluginManager pluginMgr = getServer( ).getPluginManager( );
		
		//Register for events
		pluginMgr.registerEvent( Event.Type.BLOCK_DAMAGED, blockListener, 
				                 Event.Priority.High, this );
		pluginMgr.registerEvent( Event.Type.BLOCK_PLACED, blockListener,
				                 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.ENTITY_DAMAGED, entityListener,
								 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_RESPAWN, playerListener,
								 Event.Priority.Normal, this );
		
		log.info( "Fortress Assault v0.4.0 is enabled!" );
	}
	
	/**
	 * Called when a command registered to Fortress Assault through
	 * the plugin.yml is used.
	 */
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		String[] split = args;
		String commandName = command.getName().toLowerCase();
	        
		if ( sender instanceof Player ) 
		{
			if( commandName.equals( "fastart" ) )
			{
				startEvent( ( Player )sender );
				
				return true;
			}
			else if( commandName.equals( "fastop" ) )
			{
				stopEvent( ( Player )sender );
				
				return true;
			}
			else if( commandName.equals( "faadd" ) )
			{
				if( split.length == 0 )
				{
					sender.sendMessage( ChatColor.DARK_RED + "Invalid format! " +
							            "'/faAdd TEAMCOLOR PLAYER' BLUE or RED only." );
				}
				else
				{
					addPlayer( ( Player )sender, split[ 0 ], split[ 1 ] );
				}
				
				return true;
			}
			else if( commandName.equals( "faresource" ) )
			{
				setResources( ( Player )sender, split[ 0 ] );
				
				return true;
			}
			else if( commandName.equals( "fatime" ) )
			{
				setTime( ( Player )sender, split[ 0 ] );
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Sets the resource level when /faResource is inputted.<br><br>
	 * 1 = low<br>
	 * 2 = normal<br>
	 * 3 = high<br>
	 * 
	 * @param sender Player who issued the command
	 * @param str Passed from onCommand
	 */
	private void setResources( Player sender, String str )
	{
		try
		{
			resources = Integer.parseInt( str.trim( ) );
			
			if( resources < 1 )
			{
				resources = 1;
			}
			else if( resources > 3 )
			{
				resources = 3;
			}
			
			switch( resources )
			{
			case 1:
				sender.sendMessage( ChatColor.GREEN + "Resources set to Low" );
				break;
				
			case 2:
				sender.sendMessage( ChatColor.GREEN + "Resources set to Normal" );
				break;
				
			case 3:
				sender.sendMessage( ChatColor.GREEN + "Resources set to Lots" );
				break;
				
			default:
				break;
			}
		}
		catch( NumberFormatException nfe )
		{
			//Invalid command
			sender.sendMessage( ChatColor.DARK_RED + "Use '/faResource #' # = 1, 2, or 3." );
		}
	}
	
	/**
	 * Sets the time limit.
	 * 
	 * @param sender Player who issued the command
	 * @param str Passed from onCommand
	 */
	public void setTime( Player sender, String str )
	{
		try
		{
			timeLimit = Integer.parseInt( str.trim( ) );
			
			if( timeLimit <= 0 )
			{
				timeLimit = 1;
			}
			
			sender.sendMessage( ChatColor.GREEN + "Time Limit set to " + timeLimit );
		}
		catch( NumberFormatException nfe )
		{
			//Invalid command
			sender.sendMessage( ChatColor.DARK_RED + "Use '/faTime #' Integer values ony." );
		}
	}
	
	/**
	 * Adds a player to the specified team.<br>
	 * Team must be either BLUE or RED.
	 * 
	 * @param sender Player who sent the command
	 * @param team Team specified
	 * @param toAdd Player to add to the specified team
	 */
	public void addPlayer( Player sender, String team, String toAdd )
	{
		if( team.equalsIgnoreCase( "blue" ) || team.equalsIgnoreCase( "red" ) )
		{
			Player tempPlayer = getServer( ).getPlayer( toAdd );
			
			if( tempPlayer.getDisplayName( ).equalsIgnoreCase( toAdd ) )
			{
				if( tempPlayer != null )
				{
					if( team.equalsIgnoreCase( "blue" ) )
					{
						blueTeam.add( tempPlayer );
						blueStrList.add( toAdd );
						
						getServer( ).broadcastMessage( ChatColor.BLUE + toAdd + " added to Blue Team!" );
					}
					else
					{
						redTeam.add( tempPlayer );
						redStrList.add( toAdd );
						
						getServer( ).broadcastMessage( ChatColor.RED + toAdd + " added to Red Team!" );
					}
				}
			}
			else
			{
				sender.sendMessage( ChatColor.DARK_RED + "Player not found!" );
			}
		}
		else
		{
			sender.sendMessage( ChatColor.DARK_RED + "Invalid team. Must choose BLUE or RED" );
		}
	}
	
	/**
	 * Stops the event but does not clear the lists.<br>
	 * Player that made the command must be part of the current game if one is occuring.
	 */
	public void stopEvent( Player sender )
	{
		//Either phase is occuring
		if( ( fortify == true ) || ( assault == true ) )
		{
			//If the sender is part of the game
			if( getTeam( sender ) != "null" )
			{
				getServer( ).broadcastMessage( ChatColor.YELLOW + sender.getDisplayName( ) +
						" has stopped the current game of Fortress Assault." );
				
				fortify = false;
				assault = false;
				
				//returnInventories( );
				
				pvpWatcher.clear( );
				
				entityListener.pvpListen( false );
				blockListener.setPhase( false, false );
			}
			else
			{
				sender.sendMessage( ChatColor.DARK_RED + "You are not a member of the current game!" );
			}
			
		}
		else
		{
			sender.sendMessage( ChatColor.DARK_RED + "No Fortress Assault game occuring." );
		}
	}
	
	/**
	 * Called when a player submits the '/faStart' command.<br><br>
	 * First checks to make sure each team has at least one member, and warns if
	 * the teams are unbalanced.<br><br>
	 * Then it replaces each player's inventory, sets a form of god mode on them,
	 * and finally begins several counters for warnings and the assault start.
	 * 
	 * @param sender
	 */
	public boolean startEvent( Player sender )
	{
		if( ( fortify == true ) || ( assault == true ) )
		{
			sender.sendMessage( ChatColor.DARK_RED + "You cannot start a game when one is already occuring!" );
			
			return false;
		}
		else
		{
			fortify = true;
			assault = false;
			
			//Want to warn about each separate team
			if( blueTeam.size( ) == 0 )
			{
				sender.sendMessage( ChatColor.DARK_RED + "Blue Team needs atleast one member!" );
			}
			
			if( redTeam.size( ) == 0 )
			{
				sender.sendMessage( ChatColor.DARK_RED + "Red Team needs atleast one member!" );
			}
			
			//If either of the teams were not ready, go no further.
			if( ( redTeam.size( ) == 0 ) || ( blueTeam.size( ) == 0 ) )
			{
				return false;
			}
			
			//So the teams each have at least one member.
			//Send a warning message if the teams are unbalanced but don't do anything about it.
			
			if( blueTeam.size( ) != redTeam.size( ) )
			{
				sender.sendMessage( ChatColor.YELLOW + "Warning! Teams unbalanced. /faStop if you want to correct this" );
			}
			
			//Replace the inventories and turn God mode on
			replaceInventoriesFortify( );
			setGodMode( true );
			
			blockListener.setPhase( true, false );
			
			//----------------------------------------------------------------------------------
			// Set up the counters
		
			getServer( ).getScheduler( ).scheduleSyncDelayedTask( this , new Runnable( ) 
			{
			    public void run( ) 
			    {
			        beginAssault( );
			    }
			}, ( long )( timeLimit * 60 * 20 ) ); //timeLimit to seconds, then 20 ticks per sec.
	
			if( timeLimit > 5 )
			{
				//5 minute warning
				getServer( ).getScheduler( ).scheduleSyncDelayedTask( this, new Runnable( )
				{
					public void run( )
					{
						timeWarningMinutes( 5 );
					}
				}, ( long )( ( timeLimit - 5 ) * 60 * 20 ) );
			}
			
			if( timeLimit > 1 )
			{
				//1 minute warning
				getServer( ).getScheduler( ).scheduleSyncDelayedTask( this, new Runnable( )
				{
					public void run( )
					{
						timeWarningMinutes( 1 );
					}
				}, ( long )( ( timeLimit - 1 ) * 60 * 20 ) );
			}
			
			//30 seconds warning
			getServer( ).getScheduler( ).scheduleSyncDelayedTask( this, new Runnable( )
			{
				public void run( )
				{
					timeWarningSeconds( 30 );
				}
			}, ( long )( ( ( timeLimit * 60 ) - 30 ) * 20 ) );
			
			//10 second warning
			getServer( ).getScheduler( ).scheduleSyncDelayedTask( this, new Runnable( )
			{
				public void run( )
				{
					timeWarningSeconds( 10 );
				}
			}, ( long )( ( ( timeLimit * 60 ) - 10 ) * 20 ) );
			
			return true;
		}
	}
	
	/**
	 * Sends a warning to the entire server about how many minutes remain.
	 * 
	 * @param timeLeft
	 */
	public void timeWarningMinutes( int timeLeft )
	{
		getServer( ).broadcastMessage( ChatColor.YELLOW + "" + timeLeft + " minutes remaining!" );
	}
	
	/**
	 * Sends a warning to the entire server about how many seconds remain.
	 * 
	 * @param timeLeft
	 */
	public void timeWarningSeconds( int timeLeft )
	{
		getServer( ).broadcastMessage( ChatColor.YELLOW + "" + timeLeft + " seconds remaining!" );
	}
	
	/**
	 * Turns god mode on or off.<br><br>
	 * This is enabled during the fortify phase, and then disabled during assault.
	 * @param enabled
	 */
	public void setGodMode( boolean enabled )
	{
		if( enabled )
		{
			for( int i = 0; i < blueTeam.size( ); i++ )
			{
				entityListener.addToList( blueTeam.get( i ) );		
			}
			
			for( int i = 0; i < redTeam.size( ); i++ )
			{
				entityListener.addToList( redTeam.get( i ) );
			}
		}
		else
		{
			entityListener.clearList( );
		}
		
		entityListener.setGod( enabled );
	}
	
	/**
	 * When the Fortify Phase begins, the players inventory is replaced to
	 * ensure everyone has equal footing. Their old inventories are logged
	 * and replaced after the game ends.
	 */
	private void replaceInventoriesFortify( )
	{
		for( int i = 0; i < blueTeam.size( ); i++ )
		{
			Player temp = blueTeam.get( i );
			
			temp.sendMessage( ChatColor.YELLOW + "Replacing your inventory. You will get it back after the event." );
			
			//
			//Inventory stash code here!
			//
			
			temp.getInventory( ).clear( );
			
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_PICKAXE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_AXE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_SPADE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.STONE, ( resources * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.COBBLESTONE, ( resources * 3 * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.WOOD, ( int )( resources * 0.5 * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.TORCH, 64 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.SPONGE, 1 ) );
		}
		
		for( int i = 0; i < redTeam.size( ); i++ )
		{
			Player temp = redTeam.get( i );
			
			temp.sendMessage( ChatColor.YELLOW + "Replacing your inventory. You will get it back after the event." );
			
			//
			//Inventory stash code here!
			//
			
			temp.getInventory( ).clear( );
			
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_PICKAXE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_AXE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_SPADE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.STONE, ( resources * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.COBBLESTONE, ( resources * 3 * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.WOOD, ( int )( resources * 0.5 * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.TORCH, 64 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.SPONGE, 1 ) );
		}
	}
	
	/**
	 * Gives the players what they need to assault the opposing fortress.
	 */
	private void replaceInventoriesAssault( )
	{
		for( int i = 0; i < blueTeam.size( ); i++ )
		{
			Player temp = blueTeam.get( i );
			
			temp.getInventory( ).clear( );
			
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_SWORD, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_HELMET, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_CHESTPLATE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_LEGGINGS, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_BOOTS, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.STONE_PICKAXE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.TNT, 3 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.LADDER, 6 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.MUSHROOM_SOUP, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.COOKED_FISH, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.BREAD, 1 ) );
		}
		
		for( int i = 0; i < redTeam.size( ); i++ )
		{
			Player temp = redTeam.get( i );
			
			temp.getInventory( ).clear( );
			
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_SWORD, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_HELMET, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_CHESTPLATE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_LEGGINGS, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_BOOTS, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.STONE_PICKAXE, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.TNT, 3 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.LADDER, 6 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.MUSHROOM_SOUP, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.COOKED_FISH, 1 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.BREAD, 1 ) );
		}
	}
	
	/**
	 * Called when the time limit is up.
	 */
	private void beginAssault( )
	{
		fortify = false;
		assault = true;
		
		//Make sure both teams have placed their gizmos
		if( gizmoHandler.gizmosPlaced( ) )
		{
			getServer( ).broadcastMessage( ChatColor.DARK_RED + "Begin your assault!" );
			
			replaceInventoriesAssault( );
			setGodMode( false );
			
			entityListener.pvpListen( true );
			blockListener.setPhase( false, true );
		}
		else
		{
			noGizmoGameOver( );
		}
	}
	
	/**
	 * Returns which team the specified player is on.
	 * 
	 * @param player
	 * @return
	 */
	public String getTeam( Player player )
	{
		for( int i = 0; i < blueTeam.size( ); i++ )
		{
			if( blueTeam.get( i ).getDisplayName( ).equalsIgnoreCase( player.getDisplayName( ) ) )
			{
				return "BLUE";
			}
		}
		
		for( int i = 0; i < redTeam.size( ); i++ )
		{
			if( redTeam.get( i ).getDisplayName( ).equalsIgnoreCase( player.getDisplayName( ) ) )
			{
				return "RED";
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the list of players for the specified team.
	 * 
	 * @param blue True if want blue list, False if red list.
	 * @return
	 */
	public List< Player > getTeamList( boolean blue )
	{
		if( blue )
		{
			return blueTeam;
		}
		
		return redTeam;
	}
	
	/**
	 * Returns the list containing the player names.
	 * 
	 * @param blue True if want blue list, False if red list.
	 * @return
	 */
	public List< String > getStrList( boolean blue )
	{
		if( blue )
		{
			return blueStrList;
		}
		
		return redStrList;
	}
	
	/**
	 * The game ended under normal conditions.<br>
	 * Prints the results and resets the mod.
	 */
	public void gameOver( )
	{
		fortify = false;
		assault = false;
		
		pvpWatcher.printResults( gizmoHandler.getDestroyer( ) );
		pvpWatcher.clear( );
		
		blueTeam.clear( );
		redTeam.clear( );
		
		entityListener.pvpListen( false );
		blockListener.setPhase( false, false );
	}	
	
	/**
	 * If one of the teams did not place their Gizmo, then the game is over.<br>
	 * The team that did place a Gizmo is declared the winner.
	 */
	public void noGizmoGameOver( )
	{
		if( gizmoHandler.getPlacedGizmoTeam( ).equalsIgnoreCase( "BLUE" ) )
		{
			getServer( ).broadcastMessage( ChatColor.BLUE + "Blue Team " +
					ChatColor.GOLD + "wins! Other team did not place a Gizmo!" );
		}
		else if( gizmoHandler.getPlacedGizmoTeam( ).equalsIgnoreCase( "RED" ) )
		{
			getServer( ).broadcastMessage( ChatColor.RED + "Red Team " +
					ChatColor.GOLD + "wins! Other team did not place a Gizmo!" );
		}
		else
		{
			getServer( ).broadcastMessage( ChatColor.GOLD + "Neither team placed a Gizmo! No winners." );
		}
		
		fortify = false;
		assault = false;
		
		pvpWatcher.clear( );
		
		blueTeam.clear( );
		redTeam.clear( );
		
		gizmoHandler.clearList( );
		
		entityListener.pvpListen( false );
		blockListener.setPhase( false, false );
	}
}
