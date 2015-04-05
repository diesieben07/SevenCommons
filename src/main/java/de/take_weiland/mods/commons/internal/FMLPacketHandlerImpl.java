package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.transformers.ModPacketCstrAdder;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.SCReflector;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import org.apache.commons.lang3.ArrayUtils;
import sun.reflect.ConstructorAccessor;
import sun.reflect.ReflectionFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

public final class FMLPacketHandlerImpl implements IPacketHandler, PacketHandler {

	public static final int STREAMS_INITIAL_CAP = 64;

	private final String channel;
	private final Logger logger;

    private static ImmutableMap<Class<? extends ModPacket>, ModPacketInfo> packetInfo = ImmutableMap.of();

    private final Map<Integer, MethodHandle> packetCstrs;

    private static final InstantiationStrategy strategy;

    static {
        // transformer checks if sun.reflect.ReflectionFactory is available
        // if that is the case (most of the time, really), we don't need to transform the packet class
        if (ModPacketCstrAdder.isNeeded) {
            strategy = new TransformerStrategy();
        } else {
            try {
                strategy = (InstantiationStrategy) Class.forName("de.take_weiland.mods.commons.internal.FMLPacketHandlerImpl$ReflectionFactStrategy").newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e); // impossible
            }
        }
    }

	public FMLPacketHandlerImpl(String channel, Map<Class<? extends ModPacket>, Integer> packets) {
		this.channel = channel;
        this.packetCstrs = buildConstructors(packets);
        updatePacketInfo(packets);

		String logChannel = "SCNet|" + channel;
		FMLLog.makeLog(logChannel);
		logger = Logger.getLogger(logChannel);
	}

    private Map<Integer, MethodHandle> buildConstructors(Map<Class<? extends ModPacket>, Integer> packets) {
        ImmutableMap.Builder<Integer, MethodHandle> builder = ImmutableMap.builder();

        for (Map.Entry<Class<? extends ModPacket>, Integer> entry : packets.entrySet()) {
            builder.put(entry.getValue(), strategy.makeNoArgCstr(entry.getKey()));
        }

        return builder.build();
    }


    private void updatePacketInfo(Map<Class<? extends ModPacket>, Integer> newPackets) {
        synchronized (FMLPacketHandlerImpl.class) {
            ImmutableMap.Builder<Class<? extends ModPacket>, ModPacketInfo> builder = ImmutableMap.builder();
            builder.putAll(packetInfo);

            for (Map.Entry<Class<? extends ModPacket>, Integer> entry : newPackets.entrySet()) {
                Class<? extends ModPacket> clazz = entry.getKey();
                Integer id = entry.getValue();

                builder.put(clazz, new ModPacketInfo(clazz, this, id));
            }
            packetInfo = builder.build();
        }
    }


	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player fmlPlayer) {
		MCDataInputStream in = MCDataInputStream.create(packet.data, 0, packet.length);
		EntityPlayer player = (EntityPlayer) fmlPlayer;

		int id = in.readVarInt();

		ModPacket modPacket = newPacket(id);
		handlePacket(in, player, modPacket, packetInfo.get(modPacket.getClass()));
	}

	final void handlePacket(MCDataInputStream in, EntityPlayer player) {
        ModPacket packet = newPacket(in.readVarInt());
        handlePacket(in, player, packet, packetInfo.get(packet.getClass()));
	}

	static void handlePacket(MCDataInputStream in, EntityPlayer player, ModPacket modPacket, ModPacketInfo info) {
		Side side = Sides.logical(player);
        try {
            if (!info.isValidTarget(side)) {
				throw new ProtocolException(String.format("Packet received on wrong Side!"));
			}
			modPacket.read(in, player, side);
			modPacket.execute(player, side);
		} catch (ProtocolException pe) {
            String kickMsg = pe.getKickMessage();
			if (kickMsg != null && side.isServer()) {
				((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer(kickMsg);
			}
			info.handler.logException(modPacket, pe, player);
		} catch (IOException e) {
			info.handler.logException(modPacket, e, player);
		}
	}

    public static Packet makePacketForModPacket(ModPacket packet) {
        ModPacketInfo info = packetInfo.get(packet.getClass());
        return info.handler.buildPacket(packet, info);
    }

	private Packet buildPacket(ModPacket mp, ModPacketInfo info) {
		MCDataOutputStream out = MCDataOutputStream.create(mp.expectedSize() + 1); // packetID should rarely take more than one byte (more than 127)
		out.writeVarInt(info.packetID);
		mp.write(out);
		out.lock();
		return new Packet250Fake(mp, info, channel, out.backingArray(), out.length());
	}

	@Override
	public MCDataOutput createStream(int packetId) {
		return createStream(packetId, STREAMS_INITIAL_CAP);
	}

	@Override
	public MCDataOutput createStream(int packetId, int initialCapacity) {
		MCDataOutputStream stream = MCDataOutputStream.create(initialCapacity + 1);
		stream.writeVarInt(packetId);
		return stream;
	}

	@Override
	public SimplePacket makePacket(MCDataOutput stream) {
		stream.lock();
		return new Packet250FakeNoMP(this, channel, stream.backingArray(), stream.length());
	}

	private void logException(ModPacket packet, Exception e, EntityPlayer player) {
        logger.log(Level.WARNING, String.format("Unhandled %s during Packet read of Packet %s for player %s", e.getClass().getSimpleName(), packet.getClass().getSimpleName(), player.username), e);
	}

	private ModPacket newPacket(int id) {
		try {
			return (ModPacket) packetCstrs.get(id).invokeExact();
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}

    static {
        Map<Class<? extends Packet>, Integer> classToIdMap = SCReflector.instance.getClassToIdMap();
        classToIdMap.put(Packet250Fake.class, 250);
        classToIdMap.put(Packet250FakeNoMP.class, 250);
    }

    private static abstract class InstantiationStrategy {

        abstract MethodHandle makeNoArgCstr(Class<? extends ModPacket> clazz);

    }

    static final class ReflectionFactStrategy extends InstantiationStrategy {

        private static final MethodHandle cstrAccNewInstance;
        private static final Constructor<?> modPacketCstr;

        static {
            try {
                MethodHandle rawNewInst = publicLookup().findVirtual(ConstructorAccessor.class, "newInstance", methodType(Object.class, Object[].class));
                cstrAccNewInstance = MethodHandles.insertArguments(rawNewInst, 1, (Object) ArrayUtils.EMPTY_OBJECT_ARRAY);
                modPacketCstr = ModPacket.class.getConstructor();
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        MethodHandle makeNoArgCstr(Class<? extends ModPacket> clazz) {
            MethodHandle result;

            // go up the hierarchy until we find a no-arg constructor
            // then generate a fake constructor that invokes that no-arg constructor
            // but creates an instance of the desired class
            Class<?> test = clazz;
            while (true) {
                try {
                    Constructor<?> cstr = test.getDeclaredConstructor();
                    cstr.setAccessible(true);
                    if (test == clazz) {
                        result = publicLookup().unreflectConstructor(cstr);
                    } else {
                        ReflectionFactory factory = ReflectionFactory.getReflectionFactory();
                        ConstructorAccessor ca = factory.getConstructorAccessor(factory.newConstructorForSerialization(clazz, cstr));
                        result = cstrAccNewInstance.bindTo(ca);
                    }
                    break;
                } catch (NoSuchMethodException e) {
                    if (test == ModPacket.class) {
                        throw new AssertionError("missing constructor in ModPacket.class");
                    }
                    test = test.getSuperclass();
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e); // impossible
                }
            }

            return result.asType(methodType(ModPacket.class));
        }
    }

    static final class TransformerStrategy extends InstantiationStrategy {

        @Override
        MethodHandle makeNoArgCstr(Class<? extends ModPacket> clazz) {
            try {
                return publicLookup().findConstructor(clazz, methodType(void.class))
                        .asType(methodType(ModPacket.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Packet transformer failed on " + clazz.getName());
            }
        }
    }

    static final class ModPacketInfo {

        private final PacketDirection.Dir direction;
        final int packetID;
        final FMLPacketHandlerImpl handler;

        ModPacketInfo(Class<?> clazz, FMLPacketHandlerImpl handler, int packetID) {
            PacketDirection dir = clazz.getAnnotation(PacketDirection.class);
            direction = dir == null ? PacketDirection.Dir.BOTH_WAYS : dir.value();

            this.packetID = packetID;
            this.handler = handler;
        }

        boolean isValidTarget(Side side) {
            Side allowed = direction.validTarget;
            return allowed == null || side == allowed;
        }
    }
}