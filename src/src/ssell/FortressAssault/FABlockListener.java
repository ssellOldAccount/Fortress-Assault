package ssell.FortressAssault;

import org.bukkit.Material;
import org.bukkit.block.Block;

import org.bukkit.entity.Player;

import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import ssell.FortressAssault.FAGizmoHandler;
import ssell.FortressAssault.FortressAssault.FAPlayer;

//------------------------------------------------------------------------------------------

public class FABlockListener 
	extends BlockListener
{
	public final class FASpecialBlock
	{
		public Block block;
		public int power;
		public FASpecialBlock( Block p_block )
		{
			block = p_block;
		}
	}
	public static FortressAssault plugin;
	public final FAGizmoHandler gizmoHandler;
		
	public FABlockListener( FortressAssault instance, FAGizmoHandler gizmo )
	{
		plugin = instance;
		gizmoHandler = gizmo;
	}
	
	public void onBlockDamage( BlockDamageEvent event )
	{
		Block block = event.getBlock( );
		Player player = event.getPlayer( );
		FAPlayer thisPlayer = plugin.getFAPlayer(player);
		if (thisPlayer == null) {
			return;
		}
		
		if( block.getType( ) == Material.OBSIDIAN)
		{
			if( plugin.phase == 2 )
			{
				if( gizmoHandler.isGizmo( block ) )
				{
					//The sponge that was right-clicked is a Gizmo
					gizmoHandler.gizmoHit( thisPlayer, block );
					
					//Can't destroy the Gizmo 
					event.setCancelled( true );
				}
			}
			else if( plugin.phase == 1 )
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
		Player player = event.getPlayer();
		FAPlayer thisPlayer = plugin.getFAPlayer(player);
		if (thisPlayer == null) {
			return;
		}
		if( event.getBlock( ).getType( ) == Material.OBSIDIAN )
		{
			if( plugin.phase == 1 )
			{
				if( !gizmoHandler.addGizmo( thisPlayer, event.getBlock( ) ) )
				{
					event.getBlock( ).setType( Material.AIR );
				}
			}
		}
	}	
}
