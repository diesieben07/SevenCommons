package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.internal.AbstractTypeSpec;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncerFactory;
import de.take_weiland.mods.commons.util.JavaUtils;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.asList;

/**
 * @author diesieben07
 */
final class CompanionFactories {

    private static final ImmutableMultimap<Class<?>, SyncerFactory> FACTORIES = ImmutableMultimap.<Class<?>, SyncerFactory>of(
            Object.class, new BuiltinSyncers()
    );

    private static final CompanionFactory factory = new DefaultCompanionFactory();

    static synchronized MethodHandle makeConstructor(Class<?> clazz) {
        MethodHandle cstr = factory.getCompanionConstructor(clazz);
        checkState(cstr.type().equals(methodType(SyncCompanion.class)));
        return cstr;
    }

    static void validateInstance(CompanionFactory.SyncedMemberInfo info, SyncerFactory.Instance instance, Class<?> companionHolderType) {
        MethodType checkerType = instance.getChecker().type();
        MethodType writerType = instance.getWriter().type();
        MethodType readerType = instance.getReader().type();

        Class<?> valHoldType = info.getter.type().parameterType(0);

        Class<?> companionType = info.handle.getCompanionType();
        boolean hasCompanion = companionType != null;

        checkArgument(checkerType.returnType() == boolean.class, "checker must return boolean");
        checkArgument(writerType.returnType() == void.class, "writer must return void");
        checkArgument(readerType.returnType() == void.class, "reader must return void");

        String withWithout = hasCompanion ? "with" : "without";
        int add = hasCompanion ? 1 : 0;
        int arg;
        checkArgument(checkerType.parameterCount() == (arg = 1 + add), "checker must take " + arg + " arguments " + withWithout + " companion");
        checkArgument(readerType.parameterCount() == (arg = 2 + add), "reader must take " + arg + " arguments " + withWithout + " companion");
        checkArgument(writerType.parameterCount() == (arg = 2 + add), "writer must take " + arg + " arguments " + withWithout + " companion");

        checkArgument(checkerType.parameterType(0) == valHoldType, "checker must take VH as first argument");
        if (hasCompanion) {
            checkArgument(checkerType.parameterType(1) == companionHolderType, "checker must take CH as second argument");
        }

        checkArgument(readerType.parameterType(0) == valHoldType, "reader must take VH as first argument");
        checkArgument(readerType.parameterType(1 + add) == MCDataInput.class, "reader must take MCDataInput as " + (hasCompanion ? "third" : "second") + " argument");
        if (hasCompanion) {
            checkArgument(readerType.parameterType(1) == companionHolderType, "reader must take CH as second argument");
        }

        checkArgument(writerType.parameterType(0) == MCDataOutput.class, "writer must take MCDataOutput as first argument");
        checkArgument(writerType.parameterType(1) == valHoldType, "writer must take VH as second argument");
        if (hasCompanion) {
            checkArgument(writerType.parameterType(2) == companionHolderType, "writer must take CH as third argument");
        }
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
                SyncerFactory.Handle handle = getHandleFor(member);
                return new CompanionFactory.SyncedMemberInfo(clazz, member, handle);
            }
        };
    }

    private static SyncerFactory.Handle getHandleFor(Member member) {
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

    private CompanionFactories() { }
}
