package de.take_weiland.mods.commons.net;

import com.google.common.primitives.UnsignedBytes;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.internal.SCModContainer;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.SCContainerAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.management.PlayerInstance;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
	
	public static void sendPacketToAllAssociated(Packet p, Entity entity) {
		if (Sides.logical(entity).isServer()) {
			((WorldServer)entity.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(entity, p);
		}
	}
	
	public static void sendPacketToAllTracking(Packet p, TileEntity te) {
		sendPacketToAllTrackingChunk(p, te.worldObj, te.xCoord >> 4, te.zCoord >> 4);
	}
	
	public static void sendPacketToAllTrackingChunk(Packet p, World w, int chunkX, int chunkZ) {
		if (Sides.logical(w).isServer()) {
			PlayerInstance pi = ((WorldServer)w).getPlayerManager().getOrCreateChunkWatcher(chunkX, chunkZ, false);
			if (pi != null) {
				pi.sendToAllPlayersWatchingChunk(p);
			}
		}
	}
	
	public static void sendPacketToViewing(Packet packet, Container c) {
//		Could use the shorter
//		Packets.sendPacketToPlayers(packet, Iterators.filter(crafters.iterator(), EntityPlayerMP.class));
//		but this version here avoids the object spam of the above
		List<ICrafting> crafters = SCContainerAccessor.getCrafters(c);
		int len = crafters.size();
		for (int i = 0; i < len; ++i) {
			ICrafting crafter = crafters.get(i);
			if (crafter instanceof EntityPlayerMP) {
				PacketDispatcher.sendPacketToPlayer(packet, (Player) crafter);
			}
		}
	}
	
	public static INetworkManager getNetworkManager(NetHandler netHandler) {
		if (!netHandler.isServerHandler()) {
			return SCModContainer.proxy.getNetworkManagerFromClient(netHandler);
		} else if (netHandler instanceof NetServerHandler) {
			return ((NetServerHandler)netHandler).netManager;
		} else {
			return ((NetLoginHandler)netHandler).myTCPConnection;
		}
	}
	
	public static void writeEnum(DataOutput out, Enum<?> e) throws IOException {
		out.writeByte(UnsignedBytes.checkedCast(e.ordinal()));
	}
	
	public static <E extends Enum<E>> E readEnum(DataInput in, Class<E> clazz) throws IOException {
		return JavaUtils.byOrdinal(clazz, in.readUnsignedByte());
	}
	
	public static void writeNbt(DataOutput out, NBTTagCompound nbt) throws IOException {
		if (nbt == null) {
			out.writeBoolean(true);
		} else {
			out.writeBoolean(false);
			CompressedStreamTools.write(nbt, out);
		}
	}
	
	public static NBTTagCompound readNbt(DataInput in) throws IOException {
		if (in.readBoolean()) {
			return null;
		} else {
			return CompressedStreamTools.read(in);
		}
	}
	
	public static void writeFluidStack(DataOutput out, FluidStack stack) throws IOException {
		if (stack == null) {
			out.writeShort(-1);
		} else {
			out.writeShort(stack.fluidID);
			out.writeInt(stack.amount);
			writeNbt(out, stack.tag);
		}
	}
	
	public static FluidStack readFluidStack(DataInput in) throws IOException {
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
	
	public static void writeEnum(WritableDataBuf out, Enum<?> e) {
		out.putVarInt(e.ordinal());
	}
	
	public static <E extends Enum<E>> E readEnum(DataBuf in, Class<E> clazz) {
		return JavaUtils.byOrdinal(clazz, in.getVarInt());
	}
	
	public static void writeNbt(WritableDataBuf out, NBTTagCompound nbt) {
		if (nbt == null) {
			out.putBoolean(true);
		} else {
			out.putBoolean(false);
			try {
				CompressedStreamTools.write(nbt, out.asDataOutput());
			} catch (IOException e) {
				throw new AssertionError("Impossible");
			}
		}
	}
	
	public static NBTTagCompound readNbt(DataBuf in) {
		if (in.getBoolean()) {
			return null;
		} else {
			try {
				return CompressedStreamTools.read(in.asDataInput());
			} catch (IOException e) {
				throw new AssertionError("Impossible");
			}
		}
	}
	
	public static void writeFluidStack(WritableDataBuf out, FluidStack stack) {
		if (stack == null) {
			out.putVarInt(-1);
		} else {
			out.putVarInt(stack.fluidID);
			out.putVarInt(stack.amount);
			writeNbt(out, stack.tag);
		}
	}
	
	public static FluidStack readFluidStack(DataBuf in) {
		int fluidId = in.getVarInt();
		if (fluidId < 0) {
			return null;
		} else {
			int amount = in.getVarInt();
			FluidStack stack = new FluidStack(fluidId, amount);
			stack.tag = readNbt(in);
			return stack;
		}
	}

	public static void writeItemStack(WritableDataBuf out, ItemStack stack) {
		if (stack == null) {
			out.putShort(-1);
		} else {
			out.putShort(stack.itemID);
			out.putByte(stack.stackSize);
			out.putShort(stack.getItemDamage());
			writeNbt(out, stack.stackTagCompound);
		}
	}

	public static ItemStack readItemStack(DataBuf in) {
		int id = in.getShort();
		if (id < 0) {
			return null;
		} else {
			int size = in.getByte();
			int damage = in.getShort();
			ItemStack stack = new ItemStack(id, size, damage);
			stack.stackTagCompound = readNbt(in);
			return stack;
		}
	}

}
