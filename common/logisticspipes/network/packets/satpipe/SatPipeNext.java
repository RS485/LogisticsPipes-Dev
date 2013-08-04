package logisticspipes.network.packets.satpipe;

import logisticspipes.logic.PipeFluidSatellite;
import logisticspipes.logic.PipeItemsSatelliteLogistics;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class SatPipeNext extends CoordinatesPacket {

	public SatPipeNext(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SatPipeNext(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (pipe.pipe.logic instanceof PipeItemsSatelliteLogistics) {
			((PipeItemsSatelliteLogistics) pipe.pipe.logic).setNextId(player);
		}
		if (pipe.pipe.logic instanceof PipeFluidSatellite) {
			((PipeFluidSatellite) pipe.pipe.logic).setNextId(player);
		}
	}
	
}

