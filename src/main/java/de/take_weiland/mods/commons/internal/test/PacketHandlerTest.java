package de.take_weiland.mods.commons.internal.test;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.netx.Packet;
import de.take_weiland.mods.commons.netx.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public class PacketHandlerTest implements PacketHandler<TestPacket> {
    @Override
    public void onReceive(TestPacket packet, EntityPlayer player, Side side) {

    }

}

class TestPacket implements Packet {

    @Override
    public void write(ByteBuf buf) {

    }
}
