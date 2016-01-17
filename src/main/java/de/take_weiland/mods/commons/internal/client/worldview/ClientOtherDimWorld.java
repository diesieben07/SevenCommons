package de.take_weiland.mods.commons.internal.client.worldview;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

/**
 * @author diesieben07
 */
public class ClientOtherDimWorld extends WorldClient {

    public ClientOtherDimWorld(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profiler) {
        super(netHandler, settings, dimension, difficulty, profiler);
    }

}
