package ssell.FortressAssault;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public abstract class FAPlayerAbility
{
	public String name;
	public Player player;		
	
	protected final HashMap<Material, Boolean>	targetThroughMaterials	= new HashMap<Material, Boolean>();
	private int									lastX, lastY, lastZ;
	private int									targetX, targetY, targetZ;
	private double								length, hLength;
	private double								xOffset, yOffset, zOffset;
	private double								xRotation, yRotation;		
	private Location							playerLocation;
	private boolean								allowMaxRange			= false;
	private boolean								reverseTargeting		= false;
	private boolean								targetingComplete		= false;
	
	private int									targetHeightRequired	= 1;
	private int									range					= 200;
	private double								viewHeight				= 1.65;
	private double								step = 0.2;
	
	public FAPlayerAbility( Player p_Player )
	{
		name = ChatColor.stripColor(p_Player.getDisplayName());
		player = p_Player;
		playerLocation = player.getLocation();
		length = 0;
		targetHeightRequired = 1;
		xRotation = (playerLocation.getYaw() + 90) % 360;
		yRotation = playerLocation.getPitch() * -1;
		reverseTargeting = false;

		targetX = (int) Math.floor(playerLocation.getX());
		targetY = (int) Math.floor(playerLocation.getY() + viewHeight);
		targetZ = (int) Math.floor(playerLocation.getZ());
		lastX = targetX;
		lastY = targetY;
		lastZ = targetZ;
		targetingComplete = false;
	}
	public void targetThrough(Material mat)
	{
		targetThroughMaterials.put(mat, true);
	}
	/**
	 * Returns the block at the cursor, or null if out of range
	 * 
	 * @return The target block
	 */
	public Block getTargetBlock()
	{
		findTargetBlock();
		return getCurBlock();
	}
	protected void findTargetBlock()
	{
		if (targetingComplete)
		{
			return;
		}

		while (getNextBlock() != null)
		{
			Block block = getCurBlock();
			if (isTargetable(block.getType()))
			{
				boolean enoughSpace = true;
				for (int i = 1; i < targetHeightRequired; i++)
				{
					block = block.getFace(BlockFace.UP);
					if (!isTargetable(block.getType()))
					{
						enoughSpace = false;
						break;
					}
				}
				if (enoughSpace) break;
			}
		}
		targetingComplete = true;
	}
	/**
	 * Returns the current block along the line of vision
	 * 
	 * @return The block
	 */
	public Block getCurBlock()
	{
		if (length > range && !allowMaxRange)
		{
			return null;
		}
		else
		{
			return getBlockAt(targetX, targetY, targetZ);
		}
	}
	
	/**
	 * Move "steps" forward along line of vision and returns the block there
	 * 
	 * @return The block at the new location
	 */
	public Block getNextBlock()
	{
		lastX = targetX;
		lastY = targetY;
		lastZ = targetZ;

		do
		{
			length += step;

			hLength = (length * Math.cos(Math.toRadians(yRotation)));
			yOffset = (length * Math.sin(Math.toRadians(yRotation)));
			xOffset = (hLength * Math.cos(Math.toRadians(xRotation)));
			zOffset = (hLength * Math.sin(Math.toRadians(xRotation)));

			targetX = (int) Math.floor(xOffset + playerLocation.getX());
			targetY = (int) Math.floor(yOffset + playerLocation.getY() + viewHeight);
			targetZ = (int) Math.floor(zOffset + playerLocation.getZ());

		}
		while ((length <= range) && ((targetX == lastX) && (targetY == lastY) && (targetZ == lastZ)));

		if (length > range)
		{
			if (allowMaxRange)
			{
				return getBlockAt(targetX, targetY, targetZ);
			}
			else
			{
				return null;
			}
		}

		return getBlockAt(targetX, targetY, targetZ);
	}
	public Block getBlockAt(int x, int y, int z)
	{
		World world = player.getWorld();
		return world.getBlockAt(x, y, z);
	}
	public boolean isTargetable(Material mat)
	{
		Boolean checkMat = targetThroughMaterials.get(mat);
		if (reverseTargeting)
		{
			return(checkMat != null && checkMat);
		}
		return (checkMat == null || !checkMat);
	}
	public double getDistance(Player player, Block target)
	{
		Location loc = player.getLocation();
		return Math.sqrt
		(
			Math.pow(loc.getX() - target.getX(), 2) 
		+ 	Math.pow(loc.getY() - target.getY(), 2)
		+ 	Math.pow(loc.getZ() - target.getZ(), 2)
		);
	}
}
