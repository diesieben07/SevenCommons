package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import de.take_weiland.mods.commons.internal.AbstractTypeSpec;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncerFactory;
import de.take_weiland.mods.commons.util.JavaUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.asList;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class CompanionObjects {

    public static final String METHOD_NEW_COMPANION = "makeNewCompanion";

    private static Multimap<Class<?>, SyncerFactory> FACTORIES = ImmutableMultimap.<Class<?>, SyncerFactory>of(
            Object.class, new BuiltinSyncers()
    );

    private static final ClassValue<MethodHandle> CONSTRUCTORS = new ClassValue<MethodHandle>() {
        @Override
        protected MethodHandle computeValue(Class<?> type) {
            return compileCompanion(type);
        }
    };

    public static SyncerCompanion makeNewCompanion(Object instance) {
        try {
            return (SyncerCompanion) CONSTRUCTORS.get(instance.getClass()).invokeExact();
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    private static MethodHandle compileCompanion(Class<?> type) {
        Iterable<? extends Member> fields = asList(type.getDeclaredFields());
        Iterable<? extends Member> methods = asList(type.getDeclaredMethods());

        // TODO use FluentIterable.append in newer guava
        List<CompanionGenerator.SyncedMemberInfo> syncedMembers = FluentIterable.from(Iterables.concat(fields, methods))
                .filter(isSynced())
                .transform(getMemberInfo())
                .toList();

        MethodHandle cstr = new BytecodeEmittingGenerator(type, syncedMembers).generateCompanionConstructor();
        checkState(cstr.type().equals(methodType(SyncerCompanion.class)));
        return cstr;
    }

    private static Function<Member, CompanionGenerator.SyncedMemberInfo> getMemberInfo() {
        return new Function<Member, CompanionGenerator.SyncedMemberInfo>() {
            @Override
            public CompanionGenerator.SyncedMemberInfo apply(@Nullable Member member) {
                assert member != null;
                SyncerFactory.Handle handle = getHandleFor(member);
                return new CompanionGenerator.SyncedMemberInfo(member, handle);
            }
        };
    }

    static SyncerFactory.Handle getHandleFor(Member member) {
        Class<?> type = getType(member);
        if (type.isPrimitive()) type = Object.class;

        Iterable<Class<?>> hierarchy = JavaUtils.hierarchy(type, JavaUtils.Interfaces.INCLUDE);
        if (type.isInterface()) {
            hierarchy = Iterables.concat(hierarchy, Collections.singleton(Object.class));
        }

        TypeSpecification<?> spec = AbstractTypeSpec.getSpec(member);

        for (Class<?> clazz : hierarchy) {
            Collection<SyncerFactory> factories = FACTORIES.get(clazz);
            SyncerFactory.Handle result = null;
            for (SyncerFactory factory : factories) {
                SyncerFactory.Handle gen = factory.get(spec);
                if (result == null) {
                    result = gen;
                } else if (gen != null) {
                    throw new IllegalStateException("Multiple Syncers for " + member);
                }
            }
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException("No SyncerGenerator for " + member);
    }

    private static Class<?> getType(Member member) {
        return member instanceof Field ? ((Field) member).getType() : ((Method) member).getReturnType();
    }

    private static Predicate<Member> isSynced() {
        return new Predicate<Member>() {
            @Override
            public boolean apply(@Nullable Member input) {
                assert input != null;
                return ((AnnotatedElement) input).isAnnotationPresent(Sync.class);
            }
        };
    }

    private static Function<Member, SyncerFactory.Handle> getInstance() {
        return new Function<Member, SyncerFactory.Handle>() {
            @Override
            public SyncerFactory.Handle apply(@Nullable Member input) {
                assert input != null;
                return getHandleFor(input);
            }
        };
    }

    private CompanionObjects() { }
}
