package de.take_weiland.mods.commons.internal.worldview;

import com.google.common.collect.Iterators;
import de.take_weiland.mods.commons.util.Players;
import gnu.trove.set.hash.THashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author diesieben07
 */
public class ChunkInstance {

    final Set<EntityPlayer> players = new THashSet<>();

    /**
     * <p>Iterate all players viewing this chunk who are not already tracking it via vanilla mechanics.</p>
     *
     * @param world  the world
     * @param chunkX chunk x
     * @param chunkZ chunk z
     * @return iterator
     */
    public Iterator<EntityPlayer> notAlreadyTrackingIterator(World world, int chunkX, int chunkZ) {
        List<EntityPlayerMP> tracking = Players.getTrackingChunk(world, chunkX, chunkZ);
        if (tracking.isEmpty()) {
            return players.iterator();
        } else {
            return Iterators.filter(players.iterator(), p -> !tracking.contains(p));
        }
    }

}