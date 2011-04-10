package ssell.FortressAssault;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class FAClassAbilities {
	public class FlameThrower extends FAPlayerAbility {
		private int				verticalSearchDistance	= 8;
		public FlameThrower(Player p_Player) {
			super(p_Player);
		}
		public boolean shoot() {
			targetThrough(Material.AIR);
			targetThrough(Material.WATER);
			targetThrough(Material.STATIONARY_WATER);
			targetThrough(Material.FIRE);			
			//Block target = getTargetBlock();
			HashSet<Byte> transparent = new HashSet<Byte>();
			transparent.add((byte)Material.AIR.getId());
			transparent.add((byte)Material.WATER.getId());
			transparent.add((byte)Material.STATIONARY_WATER.getId());
			transparent.add((byte)Material.TORCH.getId());
			transparent.add((byte)Material.LADDER.getId());
			transparent.add((byte)Material.RED_ROSE.getId());
			transparent.add((byte)Material.YELLOW_FLOWER.getId());
			
			Block target = player.getTargetBlock(transparent,6);
			if (target == null) 
			{
				//castMessage(player, "No target");
				return false;
			}
			
			Block block = target.getFace(BlockFace.UP);
			Material material = Material.FIRE;
			block.setType(material);
			return true;
		}
		
		public void burnBlock(int dx, int dy, int dz, Block centerPoint, int radius)
		{
			int x = centerPoint.getX() + dx - radius;
			int y = centerPoint.getY() + dy - radius;
			int z = centerPoint.getZ() + dz - radius;
			Block block = player.getWorld().getBlockAt(x, y, z);
			int depth = 0;
			
			if (block.getType() == Material.AIR)
			{
				while (depth < verticalSearchDistance && block.getType() == Material.AIR)
				{
					depth++;
					block = block.getFace(BlockFace.DOWN);
				}	
			}
			else
			{
				while (depth < verticalSearchDistance && block.getType() != Material.AIR)
				{
					depth++;
					block = block.getFace(BlockFace.UP);
				}
				block = block.getFace(BlockFace.DOWN);
			}

			if (block.getType() == Material.AIR || block.getType() == Material.FIRE)
			{
				return;
			}
			Material material = Material.FIRE;
			
			if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER || block.getType() == Material.ICE || block.getType() == Material.SNOW)
			{
				material = Material.AIR;
			}
			else
			{
				block = block.getFace(BlockFace.UP);
			}
			
			//burnedBlocks.add(block);
			block.setType(material);
		}

		public int checkPosition(int x, int z, int R)
		{
			return (x * x) +  (z * z) - (R * R);
		}
	}
	@SuppressWarnings("unused")
	private final FortressAssault plugin;
	
	
	public FAClassAbilities(FortressAssault instance) {
		plugin = instance;
	}
	public void shootFlamerThrower(Player player) {
		FlameThrower thisShot = new FlameThrower(player);
		thisShot.shoot();
	}



}
