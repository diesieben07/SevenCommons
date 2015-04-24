package de.take_weiland.mods.commons.netx;

import com.google.common.reflect.TypeToken;
import net.minecraft.network.*;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author diesieben07
 */
final class NicePacketSupport {

    static net.minecraft.network.Packet serverboundPacket(Packet packet) {

    }

    static net.minecraft.network.Packet clientboundPacket(Packet packet) {

    }

    private static final java.lang.reflect.Type function2ndParam = Function.class.getTypeParameters()[1];

    static <P extends Packet> Class<P> findPacketClassReflectively(PacketConstructor<P> constructor) {
        Class<?> myClazz = constructor.getClass();

        TypeToken<?> type = TypeToken.of(myClazz);
        Class<?> result;
        result = type.resolveType(function2ndParam).getRawType();
        if (!Packet.class.isAssignableFrom(result)) {
            result = Packet.class;
        }

        if (result == Packet.class) { // class is not a real subtype of Packet, so did not find an actual type parameter
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

                    if (Packet.class.isAssignableFrom(returnClass) && returnClass != Packet.class) {
                        result = returnClass;
                    }
                }
            } catch (Exception ignored) { }
        }
        if (result == Packet.class) {
            throw new RuntimeException("Failed to reflectively find type argument of PacketConstructor. " +
                    "Please either refactor your code according to the docs or override getPacketClass.");
        }
        //noinspection unchecked
        return (Class<P>) result;
    }
}
