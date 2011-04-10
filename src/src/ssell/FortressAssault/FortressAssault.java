package ssell.FortressAssault;

//------------------------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import ssell.FortressAssault.Utils.InventoryStash;

//import ssell.FortressAssault.FAPvPWatcher.FAPlayer;
//import ssell.FortressAssault.FortressAssault.Team;
//------------------------------------------------------------------------------------------

/**
 * Fortress Assault : Player vs Player Mod<br>
 * Minecraft - Bukkit<br><br>
 * http://www.1143pm.com/
 * 
 * @author Steven Sell
 */
public class FortressAssault
	extends JavaPlugin
{
	public enum Team { NONE, RED, BLUE, HUMAN, ZOMBIE }
	public enum ClassType { NONE, SCOUT, DEMOMAN, ENGINEER, PYRO, SOLDIER, SPY, MEDIC, SNIPER, HEAVY }
	public final class FAPlayer implements Comparable<Object>
	{
		public String name;
		public Player player;
		public Team team;
		public ClassType classtype;
		public int kills;
		public int deaths;
		public int destructions;
		public int itemset;
		public World world;
		public boolean dead;
		public boolean disconnected;

		public FAPlayer( Player p_Player )
		{
			name = ChatColor.stripColor(p_Player.getDisplayName());
			player = p_Player;
			team = Team.NONE;
			classtype = ClassType.NONE;
			kills = 0;
			deaths = 0;
			destructions = 0;
			itemset = 0;
			world = p_Player.getWorld();
			disconnected = false;
			if (p_Player.getHealth() < 0 ) {
				dead = true;
			} else {
				dead = false;
			}
		}

		@Override
		public int compareTo(Object anotherPlayer) {
			return this.kills - ((FAPlayer) anotherPlayer).kills;
		}
	}
	//Objects
	
	private static final Logger log = Logger.getLogger( "Minecraft" );
	
	//The order of these is critical
	private final FAGizmoHandler gizmoHandler = new FAGizmoHandler( this, 2 );
	private final FABlockListener blockListener = new FABlockListener( this, gizmoHandler );
	private final FAPvPWatcher pvpWatcher = new FAPvPWatcher( this );
	private final FAClassAbilities classAbilities = new FAClassAbilities( this );
	private final FAEntityListener entityListener = new FAEntityListener( this );
	private final FAPlayerListener playerListener = new FAPlayerListener( this, entityListener );
	
	private int resources = 2;			//Default resource level (normal)
	private int timeLimit = 1;			//Default time limit to build
	
	public List< FAPlayer > playerList = new ArrayList< FAPlayer >( );
	private HashMap<String, InventoryStash> inventories = new HashMap<String, InventoryStash>();
	
	public int phase = 0;	
	
	//--------------------------------------------------------------------------------------

	public FAPvPWatcher getWatcher( )
	{
		return pvpWatcher;
	}
	public FAClassAbilities getAbilities( )
	{
		return classAbilities;
	}
	
	public void onDisable( ) 
	{
		if (phase != 0) {
			stopGame();
		}
		log.info( "Fortress Assault is disabled!" );
	}

	/**
	 * Called when the Mod starts.
	 */
	public void onEnable( ) 
	{
		PluginManager pluginMgr = getServer( ).getPluginManager( );
		
		//Register for events
		pluginMgr.registerEvent( Event.Type.BLOCK_DAMAGE, blockListener, 
				                 Event.Priority.High, this );
		pluginMgr.registerEvent( Event.Type.BLOCK_PLACE, blockListener,
				                 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.ENTITY_DAMAGE, entityListener,
								 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.ENTITY_DEATH, entityListener,
				 				 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_RESPAWN, playerListener,
								 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_QUIT, playerListener,
				 				 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_JOIN, playerListener,
				 				 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_DROP_ITEM, playerListener,
				 				 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_MOVE, playerListener,
				 				 Event.Priority.Normal, this );
		
		log.info( "Fortress Assault v1.2.2 is enabled!" );
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
			if( commandName.equalsIgnoreCase("fastart" ) )
			{
				startEvent( ( Player )sender );
				
				return true;
			}
			else if( commandName.equalsIgnoreCase( "fastop" ) )
			{
				stopEvent( ( Player )sender );
				
				return true;
			}
			else if( commandName.equalsIgnoreCase( "faadd" ) )
			{
				Player thePlayer = ( Player )sender;
				int bluecount = getTeamCount(Team.BLUE);
				int redcount = getTeamCount(Team.RED);
				String nextTeam = "RED";
				if (redcount>bluecount) {
					nextTeam = "BLUE";
				}
				if( split.length == 0 )
				{				
					addPlayer( ( Player )sender, nextTeam, ChatColor.stripColor(thePlayer.getDisplayName()) );
				}
				else if (split.length == 1)
				{					
					addPlayer( ( Player )sender, nextTeam, split[ 0 ] );
				}
				else if (split.length == 2)
				{
					addPlayer( ( Player )sender, split[ 0 ], split[ 1 ] );
				}
				
				return true;
			}
			else if( commandName.equalsIgnoreCase( "faresource" ) )
			{
				setResources( ( Player )sender, split[ 0 ] );
				
				return true;
			}
			else if( commandName.equalsIgnoreCase( "fatime" ) )
			{
				setTime( ( Player )sender, split[ 0 ] );
				
				return true;
			}
			else if( commandName.equalsIgnoreCase( "fateams" ) )
			{
				showScore(( Player )sender);
			}
			else if( commandName.equalsIgnoreCase( "fareturn" ) )
			{
				if (phase == 0) {
					Player player = ( Player )sender;
					FAPlayer thisPlayer = getFAPlayer(player);
					if (hasPlayerInventory(ChatColor.stripColor(thisPlayer.name))) {
						player.sendMessage(ChatColor.GREEN + "Here is your inventory back.");						
						restorePlayerInventory(thisPlayer);
					} else {
						player.sendMessage(ChatColor.RED + "You have no stored inventory to retrieve.");
					}
				}
			}
		}
		
		return false;
	}
	public int getTeamCount(Team theTeam) {
		int count = 0;
		for (int x=0;x<playerList.size();x++) {
			FAPlayer thisPlayer = playerList.get(x);
			if (thisPlayer != null) {
				if (thisPlayer.team == theTeam) {
					count++;
				}
			}
		}
		return count;
	}
	public void showScore(Player player) {
		player.sendMessage( ChatColor.YELLOW + "# Name | Kills | Deaths | Destructions");
		Collections.sort(playerList);
		for (int x=0;x<playerList.size();x++) {
			FAPlayer thisPlayer = playerList.get(x);
			ChatColor color = getTeamColor(thisPlayer.team);
			String sep = ChatColor.YELLOW + " | " + color;
			player.sendMessage( color + thisPlayer.name + sep + Integer.toString(thisPlayer.kills)+ sep + Integer.toString(thisPlayer.deaths) + sep +Integer.toString(thisPlayer.destructions));					
		}								
	}
	public void showScoreAll() {
		getServer( ).broadcastMessage( ChatColor.YELLOW + "# Name | Kills | Deaths | Destructions");
		Collections.sort(playerList);
		for (int x=0;x<playerList.size();x++) {
			FAPlayer thisPlayer = playerList.get(x);
			ChatColor color = getTeamColor(thisPlayer.team);
			String sep = ChatColor.YELLOW + " | " + color;
			getServer( ).broadcastMessage( color + thisPlayer.name + sep + Integer.toString(thisPlayer.kills)+ sep + Integer.toString(thisPlayer.deaths) + sep +Integer.toString(thisPlayer.destructions));					
		}	
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
		Player tempPlayer = getServer().getPlayer(ChatColor.stripColor(toAdd));
		
		//Valid player
		if( tempPlayer != null )
		{
			//Player not already on a team
			Team thisteam;
			if( getFAPlayer(tempPlayer) == null )
			{
				FAPlayer newPlayer = new FAPlayer( tempPlayer );
				try {
					thisteam = Team.valueOf(team.toUpperCase());
				} catch(IllegalArgumentException e) {
					getServer( ).broadcastMessage( ChatColor.DARK_RED+"That is not a valid team!");
					return;
				}
				newPlayer.team = thisteam;
				playerList.add(newPlayer);						
				getServer( ).broadcastMessage( getTeamColor(thisteam) + newPlayer.name + " added to "+thisteam.toString()+" Team!" );
			}
			else
			{
				FAPlayer thisPlayer = getFAPlayer(tempPlayer);
				try {
					thisteam = Team.valueOf(team.toUpperCase());
				} catch(IllegalArgumentException e) {
					getServer( ).broadcastMessage( ChatColor.DARK_RED+"That is not a valid team!");
					return;
				}
				thisPlayer.team = thisteam;
				getServer( ).broadcastMessage( ChatColor.YELLOW + toAdd + " changed to "+thisteam.toString()+" Team!" );
			}
		}
		else
		{
			sender.sendMessage( ChatColor.DARK_RED + "Player not found!" );
		}
	}
	public ChatColor getTeamColor(Team team) {
		ChatColor teamColor = ChatColor.YELLOW;
		switch(team) {
		case BLUE:
			teamColor = ChatColor.BLUE;
			break;
		case RED:
			teamColor = ChatColor.RED;
			break;
		case ZOMBIE:
			teamColor = ChatColor.GREEN;
			break;
		case HUMAN:
			teamColor = ChatColor.AQUA;
			break;			
		}		
		return teamColor;
	}
	
	/**
	 * Get the game player object<br>
	 * 
	 * @param player the bukkit Player.
	 * @param entity the bukkit Entity.
	 * 
	 */
	public FAPlayer getFAPlayer(Player player) {
		for (int x=0;x<playerList.size();x++) {
			FAPlayer thisPlayer = playerList.get(x);
			try {
				if (thisPlayer.name.equalsIgnoreCase(ChatColor.stripColor(player.getDisplayName()))) {	
					if (thisPlayer.player.getEntityId() != player.getEntityId()) {				
						//fix player reference in case they reconnected.
						thisPlayer.player = player;					
					}	
					return thisPlayer;
				}
			} catch(NullPointerException e) {
				continue;
			}
		}
		return null;
	}
	public FAPlayer getFAPlayer(Entity entity) {
		for (int x=0;x<playerList.size();x++) {
			FAPlayer thisPlayer = playerList.get(x);
			if (thisPlayer.player.getEntityId()==entity.getEntityId()) {	
				return thisPlayer;
			}
		}
		return null;
	}
	
	/**
	 * Stops the event but does not clear the lists.<br>
	 * Player that made the command must be part of the current game if one is occuring.
	 */
	public void stopEvent( Player sender )
	{
		//Either phase is occuring
		if(phase != 0)
		{
			//If the sender is part of the game
			FAPlayer thisPlayer = getFAPlayer(sender);
			if( thisPlayer != null )
			{
				getServer( ).broadcastMessage( ChatColor.YELLOW + thisPlayer.name +	" has stopped the current game of Fortress Assault." );				
				getServer( ).getScheduler( ).cancelTasks( this );
				
				stopGame();
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
	public void stopGame() {
		phase = 0;
		
		gizmoHandler.clearList( );			
		giveGameItems();
		restorePlayerInventory();
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
		if( phase != 0 )
		{
			sender.sendMessage( ChatColor.DARK_RED + "You cannot start a game when one is already occuring!" );
			
			return false;
		}
		else
		{		
			//Want to warn about each separate team
			if( playerList.size( ) == 0 )
			{
				sender.sendMessage( ChatColor.DARK_RED + "You don't have any players" );
				return false;
			}
			
			phase = 1;
			
			getServer( ).broadcastMessage( ChatColor.YELLOW + "Start Fortifying!" );
			
			//So the teams each have at least one member.
			//Send a warning message if the teams are unbalanced but don't do anything about it.
			
			/*
			if( blueTeam.size( ) != redTeam.size( ) )
			{
				sender.sendMessage( ChatColor.YELLOW + "Warning! Teams unbalanced. /faStop if you want to correct this" );
			}
			*/
			resetScoreboard();
			cleanUpPlayerList();
			replaceInventoriesFortify( );
			
			
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
	public void resetScoreboard() 
	{
		for (int i=0;i<playerList.size();i++) {
			FAPlayer thisPlayer = playerList.get(i);
			thisPlayer.kills = 0;
			thisPlayer.deaths = 0;
			thisPlayer.destructions = 0;
		}
	}
	
	
	/**
	 * Give the players the items they need for the current phase
	 * 
	 * @param player
	 */	

	public void giveGameItems() 
	{
		for (int i=0;i<playerList.size();i++) {
			giveGameItems(playerList.get(i).player);
		}
	}	
	@SuppressWarnings("deprecation")
	public void giveGameItems(Player player) {
		if (player == null) {
			return;
		}
		//make sure entity is correct
		player = getServer().getPlayer(ChatColor.stripColor(player.getDisplayName()));
		FAPlayer thisPlayer = getFAPlayer(player);	
		if (thisPlayer == null || thisPlayer.dead || thisPlayer.disconnected || thisPlayer.itemset == phase) {
			return;
		}
		switch (phase) {
		//game not running
		case 0:
			player.getInventory( ).clear( );
			player.getInventory( ).setHelmet(null);
			player.getInventory( ).setChestplate(null);
			player.getInventory( ).setLeggings(null);
			player.getInventory( ).setBoots(null);
			break;
		//fortify phase
		case 1:
			player.getInventory( ).clear( );
			
			if (thisPlayer.team == Team.BLUE || thisPlayer.team == Team.ZOMBIE) {
				player.getInventory( ).setHelmet( new ItemStack( Material.CHAINMAIL_HELMET, 1 ) );
				player.getInventory( ).setChestplate( new ItemStack( Material.CHAINMAIL_CHESTPLATE, 1 ) );
				player.getInventory( ).setLeggings( new ItemStack( Material.CHAINMAIL_LEGGINGS, 1 ) );
				player.getInventory( ).setBoots( new ItemStack( Material.CHAINMAIL_BOOTS, 1 ) );
			} else {
				player.getInventory( ).setHelmet( new ItemStack( Material.GOLD_HELMET, 1 ) );
				player.getInventory( ).setChestplate( new ItemStack( Material.GOLD_CHESTPLATE, 1 ) );
				player.getInventory( ).setLeggings( new ItemStack( Material.GOLD_LEGGINGS, 1 ) );
				player.getInventory( ).setBoots( new ItemStack( Material.GOLD_BOOTS, 1 ) );				
			}
		
			player.getInventory( ).addItem( new ItemStack( Material.OBSIDIAN, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.IRON_PICKAXE, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.IRON_AXE, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.IRON_SPADE, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.STONE, ( resources * 64 ) ) );
			player.getInventory( ).addItem( new ItemStack( Material.COBBLESTONE, ( resources * 3 * 64 ) ) );
			player.getInventory( ).addItem( new ItemStack( Material.WOOD, ( int )( resources * 0.5 * 64 ) ) );
			player.getInventory( ).addItem( new ItemStack( Material.TORCH, 64 ) );
			break;
		//attack phase
		case 2:
			player.getInventory( ).clear( );
			
			if (thisPlayer.team == Team.BLUE || thisPlayer.team == Team.ZOMBIE) {
				player.getInventory( ).setHelmet( new ItemStack( Material.CHAINMAIL_HELMET, 1 ) );
				player.getInventory( ).setChestplate( new ItemStack( Material.CHAINMAIL_CHESTPLATE, 1 ) );
				player.getInventory( ).setLeggings( new ItemStack( Material.CHAINMAIL_LEGGINGS, 1 ) );
				player.getInventory( ).setBoots( new ItemStack( Material.CHAINMAIL_BOOTS, 1 ) );
			} else {
				player.getInventory( ).setHelmet( new ItemStack( Material.GOLD_HELMET, 1 ) );
				player.getInventory( ).setChestplate( new ItemStack( Material.GOLD_CHESTPLATE, 1 ) );
				player.getInventory( ).setLeggings( new ItemStack( Material.GOLD_LEGGINGS, 1 ) );
				player.getInventory( ).setBoots( new ItemStack( Material.GOLD_BOOTS, 1 ) );				
			}
			
			player.getInventory( ).addItem( new ItemStack( Material.IRON_SWORD, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.STONE_PICKAXE, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.TNT, 3 ) );
			player.getInventory( ).addItem( new ItemStack( Material.LADDER, 6 ) );
			player.getInventory( ).addItem( new ItemStack( Material.MUSHROOM_SOUP, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.COOKED_FISH, 1 ) );
			player.getInventory( ).addItem( new ItemStack( Material.BREAD, 1 ) );
			break;
		}
		thisPlayer.itemset = phase;
		//DEPRECATED. need to find alternative
		player.updateInventory( );
	}
	
	/**
	 * When the Fortify Phase begins, the players inventory is replaced to
	 * ensure everyone has equal footing. Their old inventories are logged
	 * and replaced after the game ends.
	 */
	private void replaceInventoriesFortify( )
	{
		for( int i = 0; i < playerList.size( ); i++ )
		{
			FAPlayer thisPlayer = playerList.get(i);			
			if (thisPlayer != null) {
				keepPlayerInventory(thisPlayer.player);
				giveGameItems(thisPlayer.player);
			}

		}
	}
	
	/**
	 * Gives the players what they need to assault the opposing fortress.
	 */
	private void replaceInventoriesAssault( )
	{
		for( int i = 0; i < playerList.size( ); i++ )
		{
			FAPlayer thisPlayer = playerList.get(i);
			if (thisPlayer != null) {
				giveGameItems(thisPlayer.player);
			}			
		}
	}
	
	/**
	 * Called when the time limit is up.
	 */
	private void beginAssault( )
	{
		phase = 2;
		
		//Make sure both teams have placed their gizmos
		if( gizmoHandler.gizmosPlaced( ) )
		{
			getServer( ).broadcastMessage( ChatColor.DARK_RED + "Begin your assault!" );
			
			replaceInventoriesAssault( );
			
		}
		else
		{
			noGizmoGameOver( );
		}
	}	
		
	/**
	 * The game ended under normal conditions.<br>
	 * Prints the results and resets the mod.
	 */
	public void gameOver( )
	{
		getServer( ).getScheduler( ).cancelTasks( this );		
		showScoreAll();
		stopGame();	
	}	
		
	/**
	 * If one of the teams did not place their Gizmo, then the game is over.<br>
	 * The team that did place a Gizmo is declared the winner.
	 */
	public void noGizmoGameOver( )
	{
		//getTeamColor
		Team gizmoTeam = gizmoHandler.getPlacedGizmoTeam();
		if (gizmoTeam != null) {
			getServer( ).broadcastMessage( getTeamColor(gizmoTeam) + gizmoTeam.toString()+" Team " + ChatColor.GOLD + "wins! Other team did not place a Gizmo!" );
		} else {
			getServer( ).broadcastMessage( ChatColor.GOLD + "Neither team placed a Gizmo! No winners." );
		}
		
		getServer( ).getScheduler( ).cancelTasks( this );
		
		stopGame();					
	}

	public void cleanUpPlayerList()
	{
		for (int i=0;i<playerList.size();i++) {
			FAPlayer thisPlayer = playerList.get(i);
			if (thisPlayer.player.isOnline() == false) {
				getServer( ).broadcastMessage( ChatColor.DARK_RED + "Removing offline player "+ thisPlayer.name );
				playerList.remove(i);
			}
		}
	}
	

	public boolean hasPlayerInventory(String playerName) {
		return inventories.containsKey(playerName);
	}

	public void keepPlayerInventory(Player player) {
		//make sure entity is correct
		player = getServer().getPlayer(ChatColor.stripColor(player.getDisplayName()));
		FAPlayer thisPlayer = getFAPlayer(player);
		if (thisPlayer.dead) {
			//don't try to store anything if they are dead.
			return;
		}
		if (hasPlayerInventory(ChatColor.stripColor(player.getDisplayName()))) {
			restorePlayerInventory(thisPlayer);
		}
		PlayerInventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		inventories.put(player.getName(), new InventoryStash(contents, inventory.getHelmet(), inventory.getChestplate(), inventory.getLeggings(), inventory.getBoots()));	
	}
	public void restorePlayerInventory() 
	{
		for (int i=0;i<playerList.size();i++) {
			FAPlayer thisPlayer = playerList.get(i);
			if (thisPlayer != null) {
				if (hasPlayerInventory(thisPlayer.name)) {
					if (thisPlayer.dead) {
						thisPlayer.player.sendMessage(ChatColor.RED + "You were dead when game ended, please use /faReturn to get your inventory back.");
					} else {
						restorePlayerInventory(thisPlayer);
					}
				}
			}
		}
	}
	public void restorePlayerInventory(FAPlayer thisPlayer) {
		//make sure entity is correct
		Player player = getServer().getPlayer(ChatColor.stripColor(thisPlayer.name));
		if (player != null) {			
			InventoryStash originalContents = inventories.remove(player.getName());
			PlayerInventory playerInv = player.getInventory();
			if(originalContents != null && playerInv != null) {
				playerInvFromInventoryStash(playerInv, originalContents);
			} else {
				getServer( ).broadcastMessage( ChatColor.DARK_RED + "[FA] INV NULL");
			}
		} else {
			if (thisPlayer != null) {
				if (thisPlayer.disconnected) { 
					thisPlayer.itemset = 0;
					PlayerInventory playerInv = thisPlayer.player.getInventory();
					InventoryStash originalContents = inventories.remove(thisPlayer.name);
					playerInv.clear();
					playerInv.setHelmet(null);
					playerInv.setChestplate(null);
					playerInv.setLeggings(null);
					playerInv.setBoots(null);
					playerInvFromInventoryStash(playerInv, originalContents);
					//force save of offline player.				
					CraftWorld cWorld = (CraftWorld)thisPlayer.world;
					CraftPlayer cPlayer = (CraftPlayer)thisPlayer.player;
					cWorld.getHandle().o().d().a(cPlayer.getHandle());
				}
			}
		}
	}
	
	private void playerInvFromInventoryStash(PlayerInventory playerInv,	InventoryStash originalContents) {
		//playerInv.clear();
		//playerInv.clear(playerInv.getSize() + 0);
		//playerInv.clear(playerInv.getSize() + 1);
		//playerInv.clear(playerInv.getSize() + 2);
		//playerInv.clear(playerInv.getSize() + 3);	// helmet/blockHead
		for(ItemStack item : originalContents.getContents()) {
			try {
				if(item != null) {
					if (item.getType() != Material.AIR) {
						if(item.getTypeId() != 0) {
							playerInv.addItem(item);
						}
					}
				}
			} catch (NullPointerException e) {
				//bad item
				getServer( ).broadcastMessage( ChatColor.DARK_RED + "[FA] BAD ITEM CAN'T STORE");
				continue;
			}
		}
		if(originalContents.getHelmet() != null && originalContents.getHelmet().getType() != Material.AIR) {
			playerInv.setHelmet(originalContents.getHelmet());
		}
		if(originalContents.getChest() != null && originalContents.getChest().getType() != Material.AIR) {
			playerInv.setChestplate(originalContents.getChest());
		}
		if(originalContents.getLegs() != null && originalContents.getLegs().getType() != Material.AIR) {
			playerInv.setLeggings(originalContents.getLegs());
		}
		if(originalContents.getFeet() != null && originalContents.getFeet().getType() != Material.AIR) {
			playerInv.setBoots(originalContents.getFeet());
		}
	}

	public InventoryStash getPlayerInventory(String playerName) {
		if(inventories.containsKey(playerName)) return inventories.get(playerName);
		return null;
	}
}
