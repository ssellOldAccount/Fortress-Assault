package ssell.FortressAssault;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import org.bukkit.entity.Player;

import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import ssell.FortressAssault.FAGizmoHandler;

//------------------------------------------------------------------------------------------

public class FABlockListener 
	extends BlockListener
{
	public static FortressAssault plugin;
	public final FAGizmoHandler gizmoHandler;
	
	private boolean fortifyPhase = false;
	private boolean assaultPhase = false;
	
	public FABlockListener( FortressAssault instance )
	{
		plugin = instance;
		gizmoHandler = new FAGizmoHandler( plugin, 2 );
	}
	
	public void onBlockDamage( BlockDamageEvent event )
	{
		Block block = event.getBlock( );
		Player player = event.getPlayer( );
		
		plugin.getServer( ).broadcastMessage( ChatColor.YELLOW + "Damage" );
		
		if( block.getType( ) == Material.SPONGE )
		{
			if( assaultPhase )
			{
				if( gizmoHandler.isGizmo( block ) )
				{
					//The sponge that was right-clicked is a Gizmo
					gizmoHandler.gizmoHit( player, block );
					
					//Cant destroy the Gizmo 
					event.setCancelled( true );
				}
			}
			else if( fortifyPhase )
			{
				if( gizmoHandler.isGizmo( block ) )
				{					
					//Cant destroy the Gizmo 
					event.setCancelled( true );
				}
			}
		}
	}
	
	public void onBlockPlace( BlockPlaceEvent event )
	{
		if( event.getBlock( ).getType( ) == Material.SPONGE )
		{
			if( fortifyPhase )
			{
				if( !gizmoHandler.addGizmo( event.getPlayer( ), event.getBlock( ) ) )
				{
					event.getBlock( ).setType( Material.AIR );
				}
			}
		}
	}
	
	public void setPhase( boolean isFortify, boolean isAssault )
	{
		fortifyPhase = isFortify;
		assaultPhase = isAssault;
	}
}
