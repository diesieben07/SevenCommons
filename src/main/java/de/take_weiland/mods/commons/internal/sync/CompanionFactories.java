package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import de.take_weiland.mods.commons.internal.AbstractTypeSpec;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.SimpleSyncer;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncerFactory;
import de.take_weiland.mods.commons.util.JavaUtils;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.asList;

/**
 * @author diesieben07
 */
public final class CompanionFactories {

    private static final ListMultimap<Class<?>, SyncerFactory> syncerFactories = ArrayListMultimap.create();

    private static final CompanionFactory companionFactory = new DefaultCompanionFactory();

    public static synchronized void newFactory(Class<?> clazz, SyncerFactory factory) {
        syncerFactories.put(clazz, factory);
    }

    static synchronized MethodHandle makeConstructor(Class<?> clazz) {
        MethodHandle cstr = companionFactory.getCompanionConstructor(clazz);
        checkState(cstr.type().equals(methodType(SyncCompanion.class)));
        return cstr;
    }

    static List<CompanionFactory.SyncedMemberInfo> getSyncedMemberInfo(Class<?> clazz) {
        return getSyncedMembers(clazz)
                .transform(getMemberInfo(clazz))
                .toList();
    }

    static FluentIterable<Member> getSyncedMembers(Class<?> clazz) {
        Iterable<Member> ifaceMembers = getNewlyImplementedInterfaces(clazz)
                .transformAndConcat(getSyncedMembers0());

        return FluentIterable.from(Iterables.concat(ifaceMembers, getSyncedMembers0(clazz)));
    }

    private static FluentIterable<Class<?>> getNewlyImplementedInterfaces(Class<?> clazz) {
        List<Class<?>> superInterfaces;
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null) {
            superInterfaces = ImmutableList.of();
        } else {
            superInterfaces = Arrays.asList(superclass.getInterfaces());
        }

        return FluentIterable.from(Arrays.asList(clazz.getInterfaces()))
                .filter(Predicates.not(Predicates.in(superInterfaces)));
    }

    private static Function<Class<?>, Iterable<Member>> getSyncedMembers0() {
        return new Function<Class<?>, Iterable<Member>>() {
            @Nullable
            @Override
            public Iterable<Member> apply(@Nullable Class<?> clazz) {
                return getSyncedMembers0(clazz);
            }
        };
    }

    private static FluentIterable<Member> getSyncedMembers0(Class<?> clazz) {
        Iterable<? extends Member> fields = asList(clazz.getDeclaredFields());
        Iterable<? extends Member> methods = asList(clazz.getDeclaredMethods());

        // TODO use FluentIterable.append in newer guava
        return FluentIterable.from(Iterables.concat(fields, methods))
                .filter(isSynced());
    }

    private static Function<Member, CompanionFactory.SyncedMemberInfo> getMemberInfo(final Class<?> clazz) {
        return new Function<Member, CompanionFactory.SyncedMemberInfo>() {
            @Override
            public CompanionFactory.SyncedMemberInfo apply(@Nullable Member member) {
                assert member != null;
                CompanionFactory.SyncedMemberInfo info = new CompanionFactory.SyncedMemberInfo(clazz, member, getSyncerFor(member));
                return info;
            }
        };
    }

    private static SimpleSyncer<?, ?> getSyncerFor(Member member) {
        Class<?> type = getType(member);
        if (type.isPrimitive()) type = Object.class;

        Iterable<Class<?>> hierarchy = JavaUtils.hierarchy(type, JavaUtils.Interfaces.INCLUDE);
        if (type.isInterface()) {
            hierarchy = Iterables.concat(hierarchy, Collections.singleton(Object.class));
        }

        TypeSpecification<?> spec = AbstractTypeSpec.getSpec(member);

        for (Class<?> clazz : hierarchy) {
            Collection<SyncerFactory> factories = syncerFactories.get(clazz);
            SimpleSyncer<?, ?> result = null;
            for (SyncerFactory factory : factories) {
                SimpleSyncer<?, ?> gen = factory.getSyncer(spec);
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
        throw new IllegalStateException("No Syncer for " + member);
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

    private CompanionFactories() { }
}
