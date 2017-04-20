package de.take_weiland.mods.commons.internal.tonbt;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author diesieben07
 */
public class ToNbtEventHandler extends ClassValue<NBTField[]> {

    private static final ResourceLocation IDENTIFIER = new ResourceLocation(SevenCommons.MOD_ID, "cap_to_nbt");
    private static final Set<Class<?>> registeredClasses = new HashSet<>();

    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public void onTileEntityConstruct(AttachCapabilitiesEvent event) { // need a raw type because the event system does not handle wildcards -_-
        NBTField[] fields = get(event.getObject().getClass());
        if (fields != null) {
            event.addCapability(IDENTIFIER, new ToNbtCapability(event.getObject(), fields));
        }
    }

    @Override
    protected NBTField[] computeValue(Class<?> type) {
        List<NBTField> fields = new ArrayList<>();



        return fields.isEmpty() ? null : fields.toArray(new NBTField[0]);
    }

//    private static Stream<Property<?>> allProperties(Class<?> clazz) {
//        Stream<Field> fields = JavaUtils.stream(ClassUtils.hierarchy(clazz))
//                .flatMap(c -> Arrays.stream(c.getDeclaredFields()));
//
//        Stream<Method> methods = getUniqueMethods(clazz);
//
//        Stream.concat(fields, methods)
//                .filter(e -> e.isAnnotationPresent(ToNbt.class))
//                .
//
//    }

    /**
     * Returns a stream of all methods, leaving out overridden ones
     * @param clazz
     * @return
     */
    private static Stream<Method> getUniqueMethods(Class<?> clazz) {
        Stream<Method> methods = stream(ClassUtils.hierarchy(clazz, ClassUtils.Interfaces.INCLUDE))
                .map(Class::getDeclaredMethods)
                .flatMap(Stream::of)
                .collect(groupingBy(groupOverrideChain(clazz)))
                .values().stream()
                .flatMap(l -> l.stream().limit(1));

        return null;
    }

    private static <T> Stream<T> stream(Iterable<T> it) {
        return it instanceof Collection ? ((Collection<T>) it).stream() : StreamSupport.stream(it.spliterator(), false);
    }

    private static Predicate<AnnotatedElement> isAnnotated() {
        return e -> e.isAnnotationPresent(ToNbt.class);
    }

    private static Function<Method, Object> groupOverrideChain(Class<?> base) {
        return m -> Pair.of(m.getName(), getResolvedParameterTypes(base, m));
    }

    private static List<Type> getResolvedParameterTypes(Class<?> base, Method method) {
        TypeToken<?> bt = TypeToken.of(base);
        return Stream.of(method.getGenericParameterTypes())
                .map(bt::resolveType)
                .map(TypeToken::getType)
                .collect(Collectors.toList());
    }

}
