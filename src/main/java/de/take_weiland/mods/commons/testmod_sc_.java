package de.take_weiland.mods.commons;

import com.google.common.collect.ImmutableCollection;
import com.google.common.reflect.Reflection;
import com.google.common.util.concurrent.ListenableFuture;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.metadata.HasMetadata;
import de.take_weiland.mods.commons.metadata.Meta;
import de.take_weiland.mods.commons.metadata.Metadata;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.Serializable;
import java.util.concurrent.Future;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;

@Mod(modid = "testmod_sc_", name = "testmod_sc_", version = "0.1")
//@NetworkMod()
public class testmod_sc_ {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		FMLInterModComms.sendMessage("sevencommons", "setUpdateUrl", "http://www.take-weiland.de/test.txt");
		print(Object.class, String.class);
		print(String.class, Object.class);
		print(Serializable.class, String.class);
		print(Serializable.class, Object.class);
		print(Serializable.class, ImmutableCollection.class);
		print(ListenableFuture.class, Future.class);
		print(Future.class, ListenableFuture.class);

		Reflection.initialize(TestMeta.class);

		TestMeta meta = new TestMeta(12345);
		new TestMeta2(12346);
		System.out.println(Meta.get(new ItemStack(meta, 0, 1), meta));

		System.exit(0);

	}

	private static enum Foobar implements Metadata.Simple {

		DAMAGE0,
		DAMAGE1,
		DAMAGE2

	}

	private static enum Foobar2 implements Metadata.Simple {

		DAMAGE0,
		DAMAGE1,
		DAMAGE2

	}

	public static class TestMeta extends Item implements HasMetadata.Simple<Foobar> {

		public TestMeta(int par1) {
			super(par1);
		}

		@Override
		public Class<Foobar> metaClass() {
			return Foobar.class;
		}
	}

	static class TestMeta2 extends Item implements HasMetadata.Simple<Foobar2> {

		TestMeta2(int par1) {
			super(par1);
		}

		@Override
		public Class<Foobar2> metaClass() {
			return Foobar2.class;
		}
	}

	private void print(Class<?> parent, Class<?> child) {
		boolean is = getClassInfo(parent).isAssignableFrom(getClassInfo(child));
		String s = is ? " instanceof " : " not instanceof ";
		System.out.print(child.getSimpleName() + s + parent.getSimpleName());
		if (is != parent.isAssignableFrom(child)) {
			System.out.println(" !!! Wrong !!!");
		} else {
			System.out.println();
		}
	}
}
