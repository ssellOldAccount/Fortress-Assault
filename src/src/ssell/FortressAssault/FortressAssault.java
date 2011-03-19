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
	
	private final FABlockListener blockListener = new FABlockListener( this );
	private final FAEntityListener entityListener = new FAEntityListener( this );
	
	private int resources = 2;			//Default resource level (normal)
	private int timeLimit = 15;			//Default time limit to build
	
	private final List< Player > blueTeam = new ArrayList< Player >( );
	private final List< Player > redTeam = new ArrayList< Player >( );
	
	//--------------------------------------------------------------------------------------

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
		
		//Register relevant block event
		//pluginMgr.registerEvent( Event.Type.BLOCK_RIGHTCLICKED, blockListener, 
		//		                 Event.Priority.High, this );
		
		pluginMgr.registerEvent( Event.Type.ENTITY_DAMAGED, entityListener,
								 Event.Priority.Normal, this );
		
		log.info( "Fortress Assault v0.2.0 is enabled!" );
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
			
			if( tempPlayer != null )
			{
				if( team.equalsIgnoreCase( "blue" ) )
				{
					blueTeam.add( tempPlayer );
					
					getServer( ).broadcastMessage( ChatColor.BLUE + toAdd + " added to Blue Team!" );
				}
				else
				{
					redTeam.add( tempPlayer );
					
					getServer( ).broadcastMessage( ChatColor.RED + toAdd + " added to Red Team!" );
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
			sender.sendMessage( ChatColor.YELLOW + "Warning! Teams unbalanced. /faStop if you want to correct this." );
		}
		
		//Replace the inventories and turn God mode on
		replaceInventoriesFortify( );
		setGodMode( true );
		
		//----------------------------------------------------------------------------------
		// Set up the counters
		
		getServer( ).getScheduler( ).scheduleAsyncDelayedTask( this , new Runnable( ) 
		{
		    public void run( ) 
		    {
		        beginAssault( );
		    }
		}, ( long )( timeLimit * 60 * 20 ) ); //timeLimit to seconds, then 20 ticks per sec.

		if( timeLimit > 5 )
		{
			//5 minute warning
			getServer( ).getScheduler( ).scheduleAsyncDelayedTask( this, new Runnable( )
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
			getServer( ).getScheduler( ).scheduleAsyncDelayedTask( this, new Runnable( )
			{
				public void run( )
				{
					timeWarningMinutes( 1 );
				}
			}, ( long )( ( timeLimit - 1 ) * 60 * 20 ) );
		}
		
		//30 seconds warning
		getServer( ).getScheduler( ).scheduleAsyncDelayedTask( this, new Runnable( )
		{
			public void run( )
			{
				timeWarningSeconds( 30 );
			}
		}, ( long )( ( ( timeLimit * 60 ) - 30 ) * 20 ) );
		
		//10 second warning
		getServer( ).getScheduler( ).scheduleAsyncDelayedTask( this, new Runnable( )
		{
			public void run( )
			{
				timeWarningSeconds( 10 );
			}
		}, ( long )( ( ( timeLimit * 60 ) - 10 ) * 20 ) );
		
		return true;
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
			
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_PICKAXE ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_AXE ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_SPADE ) );
			temp.getInventory( ).addItem( new ItemStack( Material.STONE, ( resources * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.COBBLESTONE, ( resources * 3 * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.WOOD, ( int )( resources * 0.5 * 64 ) ) );
			temp.getInventory( ).addItem( new ItemStack( Material.TORCH, 64 ) );
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
			
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_SWORD ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_HELMET ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_CHESTPLATE ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_LEGGINGS ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_BOOTS ) );
			temp.getInventory( ).addItem( new ItemStack( Material.STONE_PICKAXE ) );
			temp.getInventory( ).addItem( new ItemStack( Material.TNT, 3 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.LADDER, 6 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.MUSHROOM_SOUP ) );
			temp.getInventory( ).addItem( new ItemStack( Material.COOKED_FISH ) );
			temp.getInventory( ).addItem( new ItemStack( Material.BREAD ) );
		}
		
		for( int i = 0; i < redTeam.size( ); i++ )
		{
			Player temp = redTeam.get( i );
			
			temp.getInventory( ).clear( );
			
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_SWORD ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_HELMET ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_CHESTPLATE ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_LEGGINGS ) );
			temp.getInventory( ).addItem( new ItemStack( Material.IRON_BOOTS ) );
			temp.getInventory( ).addItem( new ItemStack( Material.STONE_PICKAXE ) );
			temp.getInventory( ).addItem( new ItemStack( Material.TNT, 3 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.LADDER, 6 ) );
			temp.getInventory( ).addItem( new ItemStack( Material.MUSHROOM_SOUP ) );
			temp.getInventory( ).addItem( new ItemStack( Material.MUSHROOM_SOUP ) );
			temp.getInventory( ).addItem( new ItemStack( Material.MUSHROOM_SOUP ) );
		}
	}
	
	/**
	 * Called when the time limit is up.
	 */
	private void beginAssault( )
	{
		getServer( ).broadcastMessage( ChatColor.DARK_RED + "Begin your assault!" );
		
		replaceInventoriesAssault( );
		setGodMode( false );
	}
	
	
	
}
