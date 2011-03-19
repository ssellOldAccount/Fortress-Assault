package ssell.FortressAssault;

import org.bukkit.block.Block;

import org.bukkit.entity.Player;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockBreakEvent;

//------------------------------------------------------------------------------------------

public class FABlockListener 
	extends BlockListener
{
	public static FortressAssault plugin;
	
	public FABlockListener( FortressAssault instance )
	{
		plugin = instance;
	}
	
	public void onBlockRightClick( BlockBreakEvent event )
	{
		Block block = event.getBlock( );
		Player player = event.getPlayer( );
	}
}
