package de.take_weiland.mods.commons.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.internal.CommonsModContainer;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.Sides;

public final class Packets {

	private Packets() { }

	public static void sendPacketToPlayer(Packet p, EntityPlayer player) {
		PacketDispatcher.sendPacketToPlayer(p, (Player)player);
	}
	
	public static void sendPacketToPlayers(Packet p, Iterable<? extends EntityPlayer> players) {
		sendPacketToPlayers(p, players.iterator());
	}
	
	public static void sendPacketToPlayers(Packet p, Iterator<? extends EntityPlayer> players) {
		while (players.hasNext()) {
			sendPacketToPlayer(p, players.next());
		}
	}
	
	public static void sendPacketToPlayers(Packet p, EntityPlayer... players) {
		for (EntityPlayer player : players) {
			PacketDispatcher.sendPacketToPlayer(p, (Player)player);
		}
	}

	public static void sendPacketToAllTracking(Packet p, Entity entity) {
		if (Sides.logical(entity).isServer()) {
			((WorldServer)entity.worldObj).getEntityTracker().sendPacketToAllPlayersTrackingEntity(entity, p);
		}
	}
	
	public static INetworkManager getNetworkManager(NetHandler netHandler) {
		if (!netHandler.isServerHandler()) {
			return CommonsModContainer.proxy.getNetworkManagerFromClient(netHandler);
		} else if (netHandler instanceof NetServerHandler) {
			return ((NetServerHandler)netHandler).netManager;
		} else {
			return ((NetLoginHandler)netHandler).myTCPConnection;
		}
	}
	
	public static void writeEnum(OutputStream out, Enum<?> e) throws IOException {
		out.write(UnsignedBytes.checkedCast(e.ordinal()));
	}
	
	public static <E extends Enum<E>> E readEnum(InputStream in, Class<E> clazz) throws IOException {
		return JavaUtils.safeArrayAccess(clazz.getEnumConstants(), in.read());
	}
	
	public static void writeNbt(OutputStream out, NBTTagCompound nbt) throws IOException {
		if (nbt == null) {
			out.write(0);
		} else {
			out.write(1);
			CompressedStreamTools.writeCompressed(nbt, out);
		}
	}
	
	public static NBTTagCompound readNbt(InputStream in) throws IOException {
		int isNull = in.read();
		if (isNull == 0) {
			return null;
		} else {
			return CompressedStreamTools.readCompressed(in);
		}
	}
	
	public static void writeFluidStack(DataOutputStream out, FluidStack stack) throws IOException {
		if (stack == null) {
			out.writeShort(-1);
		} else {
			out.writeShort(stack.fluidID);
			out.writeInt(stack.amount);
			writeNbt(out, stack.tag);
		}
	}
	
	public static FluidStack readFluidStack(DataInputStream in) throws IOException {
		short fluidId = in.readShort();
		if (fluidId < 0) {
			return null;
		} else {
			int amount = in.readInt();
			FluidStack stack = new FluidStack(fluidId, amount);
			stack.tag = readNbt(in);
			return stack;
		}
	}
	
}
