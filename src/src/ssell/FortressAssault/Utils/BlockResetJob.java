package ssell.FortressAssault.Utils;

import ssell.FortressAssault.Utils.Volume;

public class BlockResetJob implements Runnable {

	private final Volume volume;

	public BlockResetJob(Volume volume) {
		this.volume = volume;
	}
	
	public void run() {
		volume.resetBlocks();
	}

}
