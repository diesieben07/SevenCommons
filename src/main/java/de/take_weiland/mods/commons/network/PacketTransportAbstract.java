package de.take_weiland.mods.commons.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.util.MathHelper;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.io.OutputSupplier;
import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.Consumer;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.Sides;
import de.take_weiland.mods.commons.util.SplittingOutputStream;

abstract class PacketTransportAbstract implements PacketTransport {
	
	private static final MapMaker mapMaker = new MapMaker().concurrencyLevel(2).weakKeys();
	
	protected final PacketType[] packets;
	protected final EnumMap<?, Map<INetworkManager, InputStream[]>> trackers;

	protected <E extends Enum<E> & PacketType> PacketTransportAbstract(Class<E> typeClass) {
		E[] types = typeClass.getEnumConstants();
		
		EnumMap<E, Map<INetworkManager, InputStream[]>> trackers = new EnumMap<E, Map<INetworkManager, InputStream[]>>(typeClass);
		PacketType[] packets = new PacketType[types.length];
		
		for (E type : types) {
			int packetId = type.packetId();
			if (!JavaUtils.arrayIndexExists(packets, packetId)) {
				packets = Arrays.copyOf(packets, packetId + 1);
			}
			packets[packetId] = type;
			if (type.isMultipart()) {
				trackers.put(type, mapMaker.<INetworkManager, InputStream[]>makeMap());
			}
		}
		
		this.trackers = trackers;
		this.packets = packets;
	}
	
	protected final PacketType getType(int packetId) {
		PacketType packetType = JavaUtils.safeArrayAccess(packets, packetId);
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
	
	protected final List<byte[]> makeMultiparts(ModPacket packet, final byte[] prefix) {
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
			if (type.isMultipart()) {
				handleMultipart(manager, type, player, in);
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
			
		packet.read(player, side, in);
		packet.execute(player, side);
	}
	
	private void handleMultipart(INetworkManager manager, PacketType type, EntityPlayer player, InputStream in) throws IOException {
		int partIndex = in.read();
		int partCount = in.read();
		
		System.out.println("received part " + partIndex + " / " + partCount);
		
		Map<INetworkManager, InputStream[]> tracker = trackerFor(type);
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

	private Map<INetworkManager, InputStream[]> trackerFor(PacketType type) {
		return trackers.get(type);
	}

	void writePacket(ModPacket packet, ByteArrayOutputStream out) {
		try {
			packet.write(out);
		} catch (IOException e) {
			throw PacketTransports.wrapIOException(packet, e);
		}
		if (out.size() > maxPacketSize()) {
			throw PacketTransports.tooBigException(out.size(), maxPacketSize());
		}
	}

	protected static PacketType checkNonMulti(ModPacket packet) {
		PacketType type = packet.type();
		if (type.isMultipart()) {
			throw new IllegalArgumentException("Cannot make non-multipart packet from multipart!");
		}
		return type;
	}
	
	protected static PacketType checkMulti(ModPacket packet) {
		PacketType type = packet.type();
		if (!type.isMultipart()) {
			throw new IllegalArgumentException("Cannot make multipart packet from non-multipart!");
		}
		return type;
	}
}