package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.inv.AbstractContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public class TestContainer extends AbstractContainer<TestTE> {

    public TestContainer(EntityPlayer player, World world, int x, int y, int z) {
        super(player, world, x, y, z);
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new Slot(inventory, 0, 10, 10));
    }
}
