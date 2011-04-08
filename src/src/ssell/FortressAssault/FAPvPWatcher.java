package ssell.FortressAssault;

import org.bukkit.entity.Player;

import ssell.FortressAssault.FortressAssault;
import ssell.FortressAssault.FortressAssault.FAPlayer;

//------------------------------------------------------------------------------------------

public class FAPvPWatcher 
{		
	//--------------------------------------------------------------------------------------
	
	private final FortressAssault parent;
	
	//--------------------------------------------------------------------------------------
	
	public FAPvPWatcher( FortressAssault instance )
	{
		parent = instance;
	}
		
	public void killEvent( Player killer, Player killed )
	{
		FAPlayer FAkiller = parent.getFAPlayer( killer );
		FAPlayer FAkilled = parent.getFAPlayer( killed );		
		
		//----------------------------------------------------------------------------------

		if( ( FAkiller != null ) && ( FAkilled != null ) )
		{
			//If a player killed a team mate.
			if( FAkiller.team == FAkilled.team )
			{
				parent.getServer( ).broadcastMessage( parent.getTeamColor(FAkiller.team) +
				FAkiller.name + " committed treason on " + FAkilled.name );				
				FAkiller.deaths += 1;
			}
			else
			{
				parent.getServer( ).broadcastMessage( parent.getTeamColor(FAkiller.team) +
				FAkiller.name + " killed " + FAkilled.name );
				
				FAkiller.kills += 1;
				FAkilled.deaths += 1;
			}
		}
	}
	
	public void destructionEvent( Player player )
	{
		FAPlayer thisPlayer = parent.getFAPlayer( player );
		
		if( thisPlayer != null )
		{
			parent.getServer( ).broadcastMessage( parent.getTeamColor(thisPlayer.team) + thisPlayer.name + " destroyed the Gizmo!" );		
			thisPlayer.destructions += 1;
		}
	}	
}
