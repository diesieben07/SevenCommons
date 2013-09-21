package de.take_weiland.mods.commons.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.util.MathHelper;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.OutputSupplier;
import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.network.MultipartPacket.MultipartPacketType;
import de.take_weiland.mods.commons.util.CollectionUtils;
import de.take_weiland.mods.commons.util.Consumer;
import de.take_weiland.mods.commons.util.Sides;
import de.take_weiland.mods.commons.util.SplittingOutputStream;

abstract class PacketTransportAbstract implements PacketTransport {
	
	protected final PacketType[] packets;

	protected PacketTransportAbstract(PacketType[] types) {
		PacketType[] packets = new PacketType[types.length];
		for (PacketType type : types) {
			int packetId = type.packetId();
			if (!CollectionUtils.arrayIndexExists(packets, packetId)) {
				packets = Arrays.copyOf(packets, packetId + 1);
			}
			packets[packetId] = type;
		}
		this.packets = packets;
	}
	
	protected final PacketType getType(int packetId) {
		PacketType packetType = CollectionUtils.safeArrayAccess(packets, packetId);
		if (packetType == null) {
			throw new NetworkException(String.format("Unknown PacketId: %d", Integer.valueOf(packetId)));
		}
		return packetType;
	}
	
	protected final ModPacket newPacket(PacketType packetType) {
		try {
			return packetType.packetClass().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new NetworkException(String.format("Failed to instantiate Packet class %s", packetType.toString()), e);
		}
	}
	
	protected final List<byte[]> makeMultiparts(MultipartPacket packet, final byte[] prefix) {
		final int maxSize = maxPacketSize();
		
		final int expectedSize = packet.expectedSize();
		final int expectedChunks = MathHelper.ceiling_float_int((float)expectedSize / maxSize);
		final int leftoverSize = expectedChunks * maxSize - expectedSize; 

		final List<byte[]> streams = Lists.newArrayListWithExpectedSize(expectedChunks);
		
		final OutputSupplier<ByteArrayOutputStream> streamSupplier = new OutputSupplier<ByteArrayOutputStream>() {
			
			private int chunkCounter = 0;
			
			@Override
			public ByteArrayOutputStream getOutput() throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream((chunkCounter == expectedChunks ? leftoverSize : maxSize) + prefix.length);
				
				System.out.println("sending part " + chunkCounter + " / " + expectedChunks + "[Expect]");
				
				out.write(prefix);
				
				out.write(UnsignedBytes.checkedCast(chunkCounter++));
				out.write(0); // placeholder for totalChunkCount
				
				return out;
			}
		};
		
		final Consumer<ByteArrayOutputStream> streamConsumer = new Consumer<ByteArrayOutputStream>() {
			
			@Override
			public void apply(ByteArrayOutputStream input) {
				streams.add(input.toByteArray());
			}
			
		};
		
		try {
			SplittingOutputStream<ByteArrayOutputStream> out = new SplittingOutputStream<ByteArrayOutputStream>(maxSize, streamSupplier, streamConsumer);
			packet.write(out);
			out.close();
		} catch (IOException e) {
			throw PacketTransports.wrapIOException(packet, e);
		}
		
		System.out.println("making " + streams.size() + " packets");
		
		return streams;
	}
	
	protected final void finishPacketRecv(INetworkManager manager, int packetId, EntityPlayer player, InputStream in) {
		PacketType type = getType(packetId);
		try {
			if (type instanceof MultipartPacketType) {
				handleMultipart(manager, (MultipartPacketType)type, player, in);
			} else {
				receivePacket(manager, type, player, in);
			}
		} catch (IOException e) {
			throw PacketTransports.wrapIOException(type, e);
		}
	}
	
	private void receivePacket(INetworkManager manager, PacketType type, EntityPlayer player, InputStream in) throws IOException {
		ModPacket packet = newPacket(type);
		Side side = Sides.logical(player);
		if (!packet.isValidForSide(side)) {
			throw new NetworkException(String.format("Received Packet %s for invalid side %s", packet.getClass().getName(), side));
		}
			
		packet.read(player, in);
		packet.execute(player, side);
	}
	
	private void handleMultipart(INetworkManager manager, MultipartPacketType type, EntityPlayer player, InputStream in) throws IOException {
		int partIndex = in.read();
		int partCount = in.read();
		
		System.out.println("received part " + partIndex + " / " + partCount);
		
		Map<INetworkManager, InputStream[]> tracker = type.tracker();
		InputStream[] parts = tracker.get(manager);
		if (parts == null) {
			tracker.put(manager, (parts = new InputStream[partCount]));
		}
		parts[partIndex] = in;
		
		boolean complete = true;
		for (InputStream part : parts) {
			if (part == null) {
				complete = false;
				break;
			}
		}
		
		if (complete) {
			tracker.remove(manager); // cleanup for gc
			SequenceInputStream allStreams = new SequenceInputStream(Iterators.asEnumeration(Iterators.forArray(parts)));
			receivePacket(manager, type, player, allStreams);
		}
	}

	void writePacket(ModPacket packet, ByteArrayOutputStream out) {
		try {
			packet.write(out);
		} catch (IOException e) {
			throw PacketTransports.wrapIOException(packet, e);
		}
		int size = out.size();
		if (size > maxPacketSize()) {
			throw PacketTransports.tooBigException(size, maxPacketSize());
		}
	}
}