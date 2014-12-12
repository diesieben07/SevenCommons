package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.internal.EnumSetHandling;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.properties.ClassProperty;
import de.take_weiland.mods.commons.properties.Types;
import de.take_weiland.mods.commons.util.ByteStreamSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
public final class BuiltinSerializers {

	public enum UUID implements NBTSerializer.NullSafe<java.util.UUID>, ByteStreamSerializer<java.util.UUID> {
		INSTANCE;

		@Override
		public void write(java.util.UUID instance, @Nonnull MCDataOutputStream out) {
			out.writeUUID(instance);
		}

		@Override
		public java.util.UUID read(MCDataInputStream in) {
			return in.readUUID();
		}

		@Override
		public NBTBase serialize(java.util.UUID instance) {
			return NBT.writeUUID(instance);
		}

		@Override
		public java.util.UUID deserialize(NBTBase nbt) {
			return NBT.readUUID(nbt);
		}
	}

	public enum ItemStack implements NBTSerializer.NullSafe<net.minecraft.item.ItemStack>, ByteStreamSerializer<net.minecraft.item.ItemStack> {
		INSTANCE;

		@Override
		public void write(net.minecraft.item.ItemStack instance, @Nonnull MCDataOutputStream out) {
			out.writeItemStack(instance);
		}

		@Override
		public net.minecraft.item.ItemStack read(MCDataInputStream in) {
			return in.readItemStack();
		}

		@Override
		public NBTBase serialize(net.minecraft.item.ItemStack instance) {
			return NBT.writeItemStack(instance);
		}

		@Override
		public net.minecraft.item.ItemStack deserialize(NBTBase nbt) {
			return NBT.readItemStack(nbt);
		}
	}

	public enum FluidStack implements NBTSerializer.NullSafe<net.minecraftforge.fluids.FluidStack>, ByteStreamSerializer<net.minecraftforge.fluids.FluidStack> {
		INSTANCE;

		@Override
		public void write(net.minecraftforge.fluids.FluidStack instance, @Nonnull MCDataOutputStream out) {
			out.writeFluidStack(instance);
		}

		@Override
		public net.minecraftforge.fluids.FluidStack read(MCDataInputStream in) {
			return in.readFluidStack();
		}

		@Override
		public NBTBase serialize(@Nullable net.minecraftforge.fluids.FluidStack instance) {
			return NBT.writeFluidStack(instance);
		}

		@Override
		public net.minecraftforge.fluids.FluidStack deserialize(@Nullable NBTBase nbt) {
			return NBT.readFluidStack(nbt);
		}
	}

	public enum FluidTank implements NBTSerializer.Contents<net.minecraftforge.fluids.FluidTank>, ByteStreamSerializer.Contents<net.minecraftforge.fluids.FluidTank> {
		INSTANCE;

		@Override
		public NBTBase serialize(@Nonnull net.minecraftforge.fluids.FluidTank instance) {
			return instance.writeToNBT(new NBTTagCompound());
		}

		@Override
		public void deserialize(NBTBase nbt, @Nonnull net.minecraftforge.fluids.FluidTank instance) {
			instance.readFromNBT((NBTTagCompound) nbt);
		}


		@Override
		public void write(@Nonnull net.minecraftforge.fluids.FluidTank instance, @Nonnull MCDataOutputStream out) {
			out.writeFluidStack(instance.getFluid());
		}

		@Override
		public void read(@Nonnull net.minecraftforge.fluids.FluidTank instance, @Nonnull MCDataInputStream in) {
			instance.setFluid(in.readFluidStack());
		}
	}

	public enum Block implements NBTSerializer.NullSafe<net.minecraft.block.Block>, ByteStreamSerializer<net.minecraft.block.Block> {
		INSTANCE;

		@Override
		public void write(net.minecraft.block.Block instance, @Nonnull MCDataOutputStream out) {
			out.writeBlock(instance);
		}

		@Override
		public net.minecraft.block.Block read(MCDataInputStream in) {
			return in.readBlock();
		}

		@Override
		public NBTBase serialize(@Nullable net.minecraft.block.Block instance) {
			return NBT.writeBlock(instance);
		}

		@Override
		public net.minecraft.block.Block deserialize(@Nullable NBTBase nbt) {
			return NBT.readBlock(nbt);
		}
	}

	public enum Item implements NBTSerializer.NullSafe<net.minecraft.item.Item>, ByteStreamSerializer<net.minecraft.item.Item> {

		INSTANCE;

		@Override
		public NBTBase serialize(@Nullable net.minecraft.item.Item instance) {
			return NBT.writeItem(instance);
		}

		@Override
		public net.minecraft.item.Item deserialize(@Nullable NBTBase nbt) {
			return NBT.readItem(nbt);
		}

		@Override
		public void write(net.minecraft.item.Item instance, @Nonnull MCDataOutputStream out) {
			out.writeItem(instance);
		}

		@Override
		public net.minecraft.item.Item read(MCDataInputStream in) {
			return in.readItem();
		}
	}

	public static class EnumSet<E extends Enum<E>> implements NBTSerializer.NullSafe<java.util.EnumSet<E>>, ByteStreamSerializer<java.util.EnumSet<E>> {

		private static final Type enumSetElements = EnumSet.class.getTypeParameters()[0];

		public static EnumSet create(ClassProperty<?> element) {
			Class<?> enumType = element.getType().resolveType(enumSetElements).getRawType();
			if (enumType.isEnum()) {
				//noinspection unchecked
				return new EnumSet(enumType);
			} else {
				throw new RuntimeException("Cannot find content type of EnumSet " + element);
			}
		}

		private final Class<E> enumClass;

		public EnumSet(Class<E> enumClass) {
			this.enumClass = enumClass;
		}

		@Override
		public void write(java.util.EnumSet<E> instance, @Nonnull MCDataOutputStream out) {
			out.writeEnumSet(instance);
		}

		@Override
		public java.util.EnumSet<E> read(MCDataInputStream in) {
			return in.readEnumSet(enumClass);
		}

		@Override
		public NBTBase serialize(@Nullable java.util.EnumSet<E> instance) {
			return NBT.writeEnumSet(instance);
		}

		@Override
		public java.util.EnumSet<E> deserialize(@Nullable NBTBase nbt) {
			return NBT.readEnumSet(nbt, enumClass);
		}
	}

	private enum EnumSetUnkownType implements NBTSerializer<java.util.EnumSet<?>>, ByteStreamSerializer<java.util.EnumSet<?>> {

		INSTANCE;

		private static final Field enumSetTypeField;

		static {
			Field found = null;
			for (Field field : java.util.EnumSet.class.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) && field.getType() == Class.class) {
					found = field;
					break;
				}
			}
			if (found == null) {
				throw new RuntimeException("Could not find EnumSet type field!");
			}
			found.setAccessible(true);
			enumSetTypeField = found;
		}

		@SuppressWarnings("unchecked")
		private static <E extends Enum<E>> Class<E> getEnumSetType(java.util.EnumSet<E> enumSet) {
			try {
				return (Class<E>) enumSetTypeField.get(enumSet);
			} catch (IllegalAccessException e) {
				throw Throwables.propagate(e);
			}
		}

		@Override
		public NBTBase serialize(java.util.EnumSet<?> instance) {
			if (instance == null) {
				return NBT.serializedNull();
			} else {
				Class<?> enumClass = getEnumSetType(instance);
				String enumTypeID = Types.getID(enumClass);
				NBTTagCompound nbt = new NBTTagCompound();

				nbt.setString("t", enumTypeID);
				nbt.setLong("d", EnumSetHandling.INSTANCE.asLong(instance));
			}
		}
	}



}
