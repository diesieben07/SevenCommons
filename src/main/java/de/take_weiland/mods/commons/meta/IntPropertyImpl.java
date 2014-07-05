package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
class IntPropertyImpl extends GenericProperty<Integer> implements IntProperty {

	private final int shift;
	private final int mask;

	IntPropertyImpl(int shift, int bits) {
		this.shift = shift;
		this.mask = (1 << bits) - 1;
	}

	@Override
	public int intValue(int metadata) {
		return (metadata >> shift) & mask;
	}

	@Override
	public int intValue(World world, int x, int y, int z) {
		return intValue(world.getBlockMetadata(x, y, z));
	}

	@Override
	public int intValue(ItemStack stack) {
		return intValue(stack.getItemDamage());
	}

	@Override
	public int toMeta(int value, int previousMeta) {
		return previousMeta | ((value & mask) << shift);
	}

	@Override
	public Integer value(int metadata) {
		return intValue(metadata);
	}

	@Override
	public int toMeta(Integer value, int previousMeta) {
		return toMeta(value.intValue(), previousMeta);
	}

	@Override
	public Integer[] values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasDistinctValues() {
		return false;
	}
}
