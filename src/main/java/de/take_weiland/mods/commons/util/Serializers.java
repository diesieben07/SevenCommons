package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.UUID;

/**
 * <p>Utilities for working with {@link de.take_weiland.mods.commons.util.ByteStreamSerializer} and
 * {@link de.take_weiland.mods.commons.util.ByteStreamSerializable}.</p>
 *
 * @author diesieben07
 */
public final class Serializers {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/util/Serializers";
	public static final String DESERIALIZE = "deserialize";

	public static <T extends ByteStreamSerializable> T deserialize(Class<T> clazz, MCDataInputStream in) {
		// Gets replaced with an InvokeDynamic call to SerializerUtil.bootstrap via SerializersTransformer
		throw new AssertionError("ASM Transformer failed?!");
	}

	private static ByteStreamSerializer<ItemStack> itemStack;

	/**
	 * <p>Get a ByteStreamSerializer for serializing ItemStacks. The serializer will support null values.</p>
	 * @return a ByteStreamSerializer
	 */
	public static ByteStreamSerializer<ItemStack> forItemStack() {
		if (itemStack == null) {
			itemStack = new ByteStreamSerializer<ItemStack>() {
				@Override
				public void write(ItemStack instance, MCDataOutputStream out) {
					out.writeItemStack(instance);
				}

				@Override
				public ItemStack read(MCDataInputStream in) {
					return in.readItemStack();
				}
			};
		}
		return itemStack;
	}

	private static ByteStreamSerializer<FluidStack> fluidStack;

	/**
	 * <p>Get a ByteStreamSerializer for serializing FluidStacks. The serializer will support null values.</p>
	 * @return a ByteStreamSerializer
	 */
	public static ByteStreamSerializer<FluidStack> forFluidStack() {
		if (fluidStack == null) {
			fluidStack = new ByteStreamSerializer<FluidStack>() {
				@Override
				public void write(FluidStack instance, MCDataOutputStream out) {
					out.writeFluidStack(instance);
				}

				@Override
				public FluidStack read(MCDataInputStream in) {
					return in.readFluidStack();
				}
			};
		}
		return fluidStack;
	}

	private static ByteStreamSerializer<UUID> uuid;

	/**
	 * <p>Get a ByteStreamSerializer for serializing UUIDs. The serializer will support null values.</p>
	 * @return a ByteStreamSerializer
	 */
	public static ByteStreamSerializer<UUID> forUUID() {
		if (uuid == null) {
			uuid = new ByteStreamSerializer<UUID>() {
				@Override
				public void write(UUID instance, MCDataOutputStream out) {
					out.writeUUID(instance);
				}

				@Override
				public UUID read(MCDataInputStream in) {
					return in.readUUID();
				}
			};
		}
		return uuid;
	}

	private static ByteStreamSerializer<String> string;

	/**
	 * <p>Get a ByteStreamSerializer for serializing Strings. The serializer will support null values.</p>
	 * @return a ByteStreamSerializer
	 */
	public static ByteStreamSerializer<String> forString() {
		if (string == null) {
			string = new ByteStreamSerializer<String>() {

				@Override
				public void write(String instance, MCDataOutputStream out) {
					out.writeString(instance);
				}

				@Override
				public String read(MCDataInputStream in) {
					return in.readString();
				}
			};
		}
		return string;
	}


	private Serializers() { }

}
