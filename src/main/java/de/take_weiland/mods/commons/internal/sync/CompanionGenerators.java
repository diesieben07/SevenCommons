package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import cpw.mods.fml.common.discovery.ASMDataTable;
import de.take_weiland.mods.commons.internal.AbstractTypeSpec;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncerFactory;
import de.take_weiland.mods.commons.util.JavaUtils;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.asList;

/**
 * @author diesieben07
 */
final class CompanionGenerators {

    private static final ImmutableMultimap<Class<?>, SyncerFactory> FACTORIES = ImmutableMultimap.<Class<?>, SyncerFactory>of(
            Object.class, new BuiltinSyncers()
    );

    static ImmutableMap<Class<?>, MethodHandle> buildAllConstructors() {
        Set<ASMDataTable.ASMData> all = SevenCommons.asmData.getAll(Sync.class.getName());
        ImmutableSet<Class<?>> allClasses = FluentIterable.from(all)
                .transform(new Function<ASMDataTable.ASMData, Class<?>>() {
                    @Nullable
                    @Override
                    public Class<?> apply(ASMDataTable.ASMData input) {
                        try {
                            return Class.forName(input.getClassName());
                        } catch (ClassNotFoundException e) {
                            throw Throwables.propagate(e);
                        }
                    }
                })
                .toSet();

        System.out.println("=== BUILDING ALL ===");
        System.out.println(allClasses);
        System.out.println("====================");

        try {
            Field field = ASMDataTable.class.getDeclaredField("globalAnnotationData");
            field.setAccessible(true);
            System.out.println("All data:");
            System.out.println(field.get(SevenCommons.asmData));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        System.out.println(SevenCommons.asmData);

        return Maps.toMap(allClasses, new Function<Class<?>, MethodHandle>() {
            @Nullable
            @Override
            public MethodHandle apply(@Nullable Class<?> input) {
                return buildConstructor(input);
            }
        });
    }

    private static MethodHandle buildConstructor(Class<?> type) {
        Iterable<? extends Member> fields = asList(type.getDeclaredFields());
        Iterable<? extends Member> methods = asList(type.getDeclaredMethods());

        // TODO use FluentIterable.append in newer guava
        List<CompanionGenerator.SyncedMemberInfo> syncedMembers = FluentIterable.from(Iterables.concat(fields, methods))
                .filter(isSynced())
                .transform(getMemberInfo())
                .toList();

        checkState(!syncedMembers.isEmpty());

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

}
