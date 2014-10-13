package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.internal.sync.impl.*;
import de.take_weiland.mods.commons.sync.ContainerSyncer;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.PropertySyncer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import java.lang.invoke.*;
import java.lang.reflect.Type;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Map;

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

	private static final Map<Class<?>, CallSiteProvider> watcherCstrs = Maps.newHashMap();
	private static final Map<Class<?>, CallSiteProvider> syncerCstrs = Maps.newHashMap();

	public static <T> void regSyncer(@Nonnull Class<T> toSync, @Nonnull Class<? extends PropertySyncer<T>> syncer) {
		regCustomSyncer(toSync, makeCnstrProvider(syncer, PropertySyncer.class));
	}

	public static void regCustomSyncer(@Nonnull Class<?> toSync, @Nonnull MethodHandle syncerCreator, boolean subclasses) {
		MethodHandle adapted = syncerCreator.asType(methodType(PropertySyncer.class));
		regCustomSyncer(toSync, new DirectProvider(new ConstantCallSite(adapted), subclasses));
	}

	public static void regCustomSyncer(@Nonnull Class<?> toSync, @Nonnull CallSiteProvider provider) {
		syncerCstrs.put(toSync, provider);
	}

	public static <T> void regContainerSyncer(@Nonnull Class<T> toSync, @Nonnull Class<? extends ContainerSyncer<T>> syncer) {
		regCustomWatcher(toSync, makeCnstrProvider(syncer, ContainerSyncer.class));
	}

	public static void regCustomWatcher(@Nonnull Class<?> toSync, @Nonnull MethodHandle watcherCreator, boolean subclasses) {
		MethodHandle adapted = watcherCreator.asType(methodType(ContainerSyncer.class));
		regCustomWatcher(toSync, new DirectProvider(new ConstantCallSite(adapted), subclasses));
	}

	public static void regCustomWatcher(@Nonnull Class<?> toSync, @Nonnull CallSiteProvider provider) {
		watcherCstrs.put(toSync, provider);
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
			} catch (NoSuchMethodException e) {
				throw noSuchCstr(syncer);
			}
		} catch (IllegalAccessException e) {
			throw noSuchCstr(syncer);
		}
	}

	private static RuntimeException noSuchCstr(Class<?> clazz) {
		throw new RuntimeException("No valid public constructor found in syncer class " + clazz.getName());
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
			return false;
		}
	}

	public static CallSite inDyBootstrap(MethodHandles.Lookup callerLookup, String name, MethodType type, Class<?> toSync, String memberName, int memberType) {
		if (type.parameterCount() != 0) {
			throw invalidInDyCall();
		}

		switch (name) {
			case CREATE_SYNCER:
				if (type.returnType() != PropertySyncer.class) {
					throw invalidInDyCall();
				}

				return findProvider(toSync, syncerCstrs).get(callerLookup.lookupClass(), memberName, memberType == METHOD);
			case CREATE_CONTAINER_SYNCER:
				if (type.returnType() != ContainerSyncer.class) {
					throw invalidInDyCall();
				}
				return findProvider(toSync, watcherCstrs).get(callerLookup.lookupClass(), memberName, memberType == METHOD);
			default:
				throw invalidInDyCall();
		}
	}

	private static CallSiteProvider findProvider(Class<?> toSync, Map<Class<?>, CallSiteProvider> lookup) {
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
				lookup.put(toSync, cs);
			}
		}
		return cs;
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
		regContainerSyncer(ItemStack.class, ItemStackSyncer.Contents.class);

		regSyncer(FluidStack.class, FluidStackSyncer.class);
		regContainerSyncer(FluidStack.class, FluidStackSyncer.Contents.class);

		regContainerSyncer(FluidTank.class, FluidTankSyncer.class);

		regSyncer(BitSet.class, BitSetSyncer.class);
		regContainerSyncer(BitSet.class, BitSetSyncer.Contents.class);

		regSyncer(Enum.class, EnumSyncer.class);

		regSyncer(EnumSet.class, EnumSetSyncer.class);
		regContainerSyncer(EnumSet.class, EnumSetSyncer.Contents.class);
	}
}
