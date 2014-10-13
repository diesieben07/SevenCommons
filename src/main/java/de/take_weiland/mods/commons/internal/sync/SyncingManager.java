package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.internal.sync.impl.*;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.lang.invoke.*;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class SyncingManager {

	private SyncingManager() {}

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync/SyncingManager";
	public static final String BOOTSTRAP = "inDyBootstrap";
	public static final String CREATE_SYNCER = "createSyncer";
	public static final String CREATE_CONTAINER_SYNCER = "createContainerSyncer";

	public static final int METHOD = 0;
	public static final int FIELD = 1;

	private static final ConcurrentMap<Class<?>, CallSiteProvider> watcherCstrs;
	private static final ConcurrentMap<Class<?>, CallSiteProvider> syncerCstrs;

	static {
		MapMaker mm = new MapMaker().concurrencyLevel(2);
		watcherCstrs = mm.makeMap();
		syncerCstrs = mm.makeMap();
	}

	public static <T> void regSyncer(@Nonnull Class<T> toSync, @Nonnull Class<? extends ValueSyncer<T>> syncer) {
		regCustomSyncer(toSync, makeCnstrProvider(syncer, ValueSyncer.class));
	}

	public static void regCustomSyncer(@Nonnull Class<?> toSync, @Nonnull CallSiteProvider provider) {
		if (syncerCstrs.putIfAbsent(toSync, provider) != null) {
			throw new IllegalArgumentException("ValueSyncer for class " + toSync.getName() + " already registered");
		}
	}

	public static <T> void getContentSyncer(@Nonnull Class<T> toSync, @Nonnull Class<? extends ContentSyncer<T>> syncer) {
		regContentSyncer(toSync, makeCnstrProvider(syncer, ContentSyncer.class));
	}

	public static void regContentSyncer(@Nonnull Class<?> toSync, @Nonnull CallSiteProvider provider) {
		if (watcherCstrs.putIfAbsent(toSync, provider) != null) {
			throw new IllegalArgumentException("ContentSyncer for class " + toSync.getName() + " already registered");
		}
	}

	private static CallSiteProvider makeCnstrProvider(Class<?> syncer, Class<?> baseType) {
		MethodHandles.Lookup lookup = publicLookup();
		try {
			try {
				MethodHandle dfltCstr = lookup.findConstructor(syncer, methodType(void.class)).asType(methodType(baseType));
				return new DirectProvider(new ConstantCallSite(dfltCstr), handlesSubclasses(syncer));
			} catch (NoSuchMethodException e) { }
			try {
				MethodHandle classCstr = lookup.findConstructor(syncer, methodType(void.class, Class.class))
						.asType(methodType(baseType, Class.class));

				return new AnalyzingProviderClass(classCstr, handlesSubclasses(syncer));
			} catch (NoSuchMethodException e) { }
			try {
				MethodHandle typeCstr = lookup.findConstructor(syncer, methodType(void.class, Type.class))
						.asType(methodType(baseType, Type.class));

				return new AnalyzingProviderType(typeCstr, handlesSubclasses(syncer));
			} catch (NoSuchMethodException e) { }
			try {
				MethodHandle memberCstr = lookup.findConstructor(syncer, methodType(void.class, Member.class))
						.asType(methodType(baseType, Member.class));

				return new AnalyzingProviderMember(memberCstr, handlesSubclasses(syncer));
			} catch (NoSuchMethodException e) {
				throw noSuchCstr(syncer);
			}
		} catch (IllegalAccessException e) {
			throw noSuchCstr(syncer);
		}
	}

	private static RuntimeException noSuchCstr(Class<?> clazz) {
		throw new IllegalArgumentException("No valid public constructor found in syncer class " + clazz.getName());
	}

	private static boolean handlesSubclasses(Class<?> clazz) {
		return HandleSubclasses.class.isAssignableFrom(clazz);
	}

	private static class AnalyzingProviderClass extends CallSiteProvider {

		private final MethodHandle cstr;
		private final boolean subclasses;

		AnalyzingProviderClass(MethodHandle cstr, boolean subclasses) {
			this.cstr = cstr;
			this.subclasses = subclasses;
		}

		@Override
		public CallSite get(Class<?> caller, String member, boolean isMethod) {
			Class<?> actualType;
			try {
				if (isMethod) {
					actualType = caller.getDeclaredMethod(member).getReturnType();
				} else {
					actualType = caller.getDeclaredField(member).getType();
				}
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
			return new ConstantCallSite(cstr.bindTo(actualType));
		}

		@Override
		public boolean handlesSubclasses() {
			return subclasses;
		}
	}

	private static class AnalyzingProviderType extends CallSiteProvider {

		private final MethodHandle cstr;
		private final boolean subclasses;

		AnalyzingProviderType(MethodHandle cstr, boolean subclasses) {
			this.cstr = cstr;
			this.subclasses = subclasses;
		}

		@Override
		public CallSite get(Class<?> caller, String member, boolean isMethod) {
			Type actualType;
			try {
				if (isMethod) {
					actualType = caller.getDeclaredMethod(member).getGenericReturnType();
				} else {
					actualType = caller.getDeclaredField(member).getGenericType();
				}
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
			return new ConstantCallSite(cstr.bindTo(actualType));
		}

		@Override
		public boolean handlesSubclasses() {
			return subclasses;
		}
	}

	private static final class AnalyzingProviderMember extends CallSiteProvider {

		private final MethodHandle cstr;
		private final boolean subclasses;

		private AnalyzingProviderMember(MethodHandle cstr, boolean subclasses) {
			this.cstr = cstr;
			this.subclasses = subclasses;
		}

		@Override
		public CallSite get(Class<?> caller, String member, boolean isMethod) {
			try {
				if (isMethod) {
					return new ConstantCallSite(cstr.bindTo(caller.getDeclaredMethod(member)));
				} else {
					return new ConstantCallSite(cstr.bindTo(caller.getDeclaredField(member)));
				}
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public boolean handlesSubclasses() {
			return subclasses;
		}
	}

	public static CallSite inDyBootstrap(MethodHandles.Lookup callerLookup, String name, MethodType type, Class<?> toSync, String memberName, int memberType) {
		if (type.parameterCount() != 0) {
			throw invalidInDyCall();
		}

		switch (name) {
			case CREATE_SYNCER:
				if (type.returnType() != ValueSyncer.class) {
					throw invalidInDyCall();
				}

				return findProvider(toSync, syncerCstrs).get(callerLookup.lookupClass(), memberName, memberType == METHOD);
			case CREATE_CONTAINER_SYNCER:
				if (type.returnType() != ContentSyncer.class) {
					throw invalidInDyCall();
				}
				return findProvider(toSync, watcherCstrs).get(callerLookup.lookupClass(), memberName, memberType == METHOD);
			default:
				throw invalidInDyCall();
		}
	}

	private static CallSiteProvider findProvider(Class<?> toSync, ConcurrentMap<Class<?>, CallSiteProvider> lookup) {
		CallSiteProvider cs = lookup.get(toSync);
		if (cs == null) {
			for (Map.Entry<Class<?>, CallSiteProvider> entry : lookup.entrySet()) {
				if (entry.getKey().isAssignableFrom(toSync) && entry.getValue().handlesSubclasses()) {
					if (cs != null) {
						throw new RuntimeException("Multiple Syncers for " + toSync.getName());
					}
					cs = entry.getValue();
				}
			}
			if (cs == null) {
				throw new RuntimeException("No Syncer found for " + toSync.getName());
			} else {
				if (lookup.putIfAbsent(toSync, cs) != null) {
					return lookup.get(toSync);
				} else {
					return cs;
				}
			}
		} else {
			return cs;
		}
	}

	private static RuntimeException invalidInDyCall() {
		return new RuntimeException("Invalid InvokeDynamic call to syncerBootstrap!");
	}

	public static abstract class CallSiteProvider {

		public abstract CallSite get(Class<?> caller, String member, boolean isMethod);

		public abstract boolean handlesSubclasses();

	}

	private static final class DirectProvider extends CallSiteProvider {

		private final CallSite site;
		private final boolean subclasses;

		DirectProvider(CallSite site, boolean subclasses) {
			this.site = site;
			this.subclasses = subclasses;
		}

		@Override
		public CallSite get(Class<?> caller, String member, boolean isMethod) {
			return site;
		}

		@Override
		public boolean handlesSubclasses() {
			return subclasses;
		}
	}

	static {
		// primitives and their wrappers are handled via ASM

		regSyncer(Item.class, ItemSyncer.class);
		regSyncer(Block.class, BlockSyncer.class);

		regSyncer(ItemStack.class, ItemStackSyncer.class);
		getContentSyncer(ItemStack.class, ItemStackSyncer.Contents.class);

		regSyncer(FluidStack.class, FluidStackSyncer.class);
		getContentSyncer(FluidStack.class, FluidStackSyncer.Contents.class);

		regSyncer(BitSet.class, BitSetSyncer.class);
		getContentSyncer(BitSet.class, BitSetSyncer.Contents.class);

		regSyncer(Enum.class, EnumSyncer.class);

		regSyncer(EnumSet.class, EnumSetSyncer.class);
		getContentSyncer(EnumSet.class, EnumSetSyncer.Contents.class);

		FluidTankSyncer.register();
	}
}
