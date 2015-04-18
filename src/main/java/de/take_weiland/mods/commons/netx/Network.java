package de.take_weiland.mods.commons.netx;

import com.google.common.reflect.TypeToken;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author diesieben07
 */
public class Network {

    private static final java.lang.reflect.Type function2ndParam = Function.class.getTypeParameters()[1];

    @Nonnull
    public static NetworkChannelBuilder newChannel(String channel) {
        return new ChannelBuilderImpl(channel);
    }

    static <P extends Packet> Class<P> findPacketClassReflectively(PacketConstructor<P> constructor) {
        Class<?> myClazz = constructor.getClass();

        TypeToken<?> type = TypeToken.of(myClazz);
        Class<?> result;
        result = type.resolveType(function2ndParam).getRawType();
        if (!BasePacket.class.isAssignableFrom(result)) {
            result = BasePacket.class;
        }

        if (result == BasePacket.class) { // class is not a real subtype of Packet, so did not find an actual type parameter
            // try lambda-hackery now
            try {
                Method method = myClazz.getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                Object serForm = method.invoke(constructor);
                if (serForm instanceof SerializedLambda) {
                    SerializedLambda serLambda = (SerializedLambda) serForm;

                    Class<?> returnClass = Packet.class;
                    switch (serLambda.getImplMethodKind()) {
                        case MethodHandleInfo.REF_newInvokeSpecial:
                            returnClass = Class.forName(Type.getObjectType(serLambda.getImplClass()).getClassName());
                            break;
                        case MethodHandleInfo.REF_invokeInterface:
                        case MethodHandleInfo.REF_invokeSpecial:
                        case MethodHandleInfo.REF_invokeStatic:
                        case MethodHandleInfo.REF_invokeVirtual:
                            returnClass = Class.forName(Type.getReturnType(serLambda.getImplMethodSignature()).getClassName());
                            break;
                    }

                    if (BasePacket.class.isAssignableFrom(returnClass) && returnClass != BasePacket.class) {
                        result = returnClass;
                    }
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException ignored) { }
        }
        if (result == BasePacket.class) {
            throw new RuntimeException("Failed to reflectively find type argument of PacketConstructor. " +
                    "Please either refactor your code according to the docs or override getPacketClass.");
        }
        //noinspection unchecked
        return (Class<P>) result;
    }

    public static void main(String[] args) {
        Network.newChannel("channel")
                .register((PacketConstructor<MyPacket>) MyPacket::new, MyPacket::handle)
                .build();
    }

    private static class MyPacket implements Packet {

        private final String s;

        MyPacket(String s) {
            this.s = s;
        }

        MyPacket(ByteBuf buf) {
            this.s = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void write(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, s);
        }

        void handle(EntityPlayer player, Side side) {
            System.out.println(s);
        }
    }
}
