package de.take_weiland.mods.commons.internal;

import com.google.common.base.Predicate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author diesieben07
 */
public interface PlayerManagerProxy {

    String GET_PLAYER_INSTANCE = "_sc$getPlayerInstance";

    PlayerInstanceAdapter _sc$getPlayerInstance(int x, int z, boolean create);

    // jay for ugly hack. used in worldview.ChunkInstance
    interface PlayerInstanceAdapter extends Predicate<EntityPlayer> {

        String GET_PLAYERS_WATCHING = "_sc$getPlayersWatching";

        List<EntityPlayerMP> _sc$getPlayersWatching();

        @Override
        default boolean apply(@Nullable EntityPlayer player) {
            return !_sc$getPlayersWatching().contains(player);
        }
    }

}
