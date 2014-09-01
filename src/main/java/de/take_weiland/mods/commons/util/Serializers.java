package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.UUID;

/**
 * @author diesieben07
 */
public final class Serializers {

	private static ByteStreamSerializer<ItemStack> itemStack;

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
