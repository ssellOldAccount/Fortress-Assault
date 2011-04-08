package ssell.FortressAssault;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

import ssell.FortressAssault.FortressAssault;
import ssell.FortressAssault.FAPvPWatcher;
import ssell.FortressAssault.FortressAssault.FAPlayer;

//------------------------------------------------------------------------------------------

public class FAEntityListener 
	extends EntityListener
{
	private final FortressAssault plugin;
	private final FAPvPWatcher pvpWatcher;
		
	//--------------------------------------------------------------------------------------
	
	public FAEntityListener( FortressAssault instance )
	{
		plugin = instance;
		pvpWatcher = instance.getWatcher( );
	}
	
	/**
	 * Whenever an entity is damaged, this method is called.<br><br>
	 * If godEnabled is true, it is checked if the entity is a player. If
	 * it is a player, and the player is on the playerList list, then
	 * the damage is canceled.
	 */
	@Override 
	public void onEntityDamage( EntityDamageEvent event )
	{
		super.onEntityDamage(event);
		Entity entity = event.getEntity( );
		if( entity instanceof Player )
		{
			Player player = ( Player )entity;
			FAPlayer thisPlayer = plugin.getFAPlayer(player);
			if (thisPlayer == null) {
				//not in the game so ignore
				return;
			} else {
				if( plugin.phase == 1 )
				{
					//phase 1 so no damage to players.
					event.setCancelled( true );
					return;
				}
				else if( plugin.phase == 2 )
				{
					if( event instanceof EntityDamageByEntityEvent )
					{				
						EntityDamageByEntityEvent damageEvent = ( EntityDamageByEntityEvent  )event;
						
						if( ( damageEvent.getDamager( ) instanceof Player ) &&
							  damageEvent.getEntity( ) instanceof Player )
						{					
							//player damaged by player
							Player victim = ( Player )damageEvent.getEntity( );
							Player attacker = ( Player )damageEvent.getDamager( );
							
							
							
							int damage = event.getDamage();
							int oldHealth = victim.getHealth( );
							int newHealth = oldHealth - damage;
							
							if (thisPlayer.dead) {
								//already dead
								return;
							}
												
							if( newHealth <= 0 )
							{						
								//health says they are dead but lets make sure.								
								event.setDamage(999);
								thisPlayer.dead = true;
								//to stop loot from dropping
								victim.getInventory( ).clear( );							
								pvpWatcher.killEvent( attacker,  victim );
							}				
						} else if (damageEvent.getEntity( ) instanceof Player) {
							//player damaged by mob
							Player victim = ( Player )damageEvent.getEntity( );
							int damage = event.getDamage();
							int oldHealth = victim.getHealth( );
							int newHealth = oldHealth - damage;
							if (thisPlayer.dead) {
								//already dead
								return;
							}
							if( newHealth <= 0 )
							{						
								//health says they are dead but lets make sure.								
								event.setDamage(999);
								thisPlayer.dead = true;
								//to stop loot from dropping
								victim.getInventory( ).clear( );
							}
						}
					} else {
						//player damaged by something else, maybe lava?
						int damage = event.getDamage();
						int oldHealth = player.getHealth( );
						int newHealth = oldHealth - damage;
						if (thisPlayer.dead) {
							//already dead
							return;
						}
						if( newHealth <= 0 )
						{						
							//health says they are dead but lets make sure.								
							event.setDamage(999);
							thisPlayer.dead = true;
							//to stop loot from dropping
							player.getInventory( ).clear( );
						}
					}
				}
			}
		} else {
			//not a player so we don't care
			return;
		}
	}
	public void onEntityDeath( EntityDeathEvent event ) {
		Entity entity = event.getEntity();
		FAPlayer thisPlayer = plugin.getFAPlayer(entity);
		if (thisPlayer != null) {
			//player died by some other cause lets mark them as dead
			thisPlayer.dead = true;
		}
	}
}
