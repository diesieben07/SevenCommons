package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.Entity;

/**
 * @author diesieben07
 */
public interface SyncedEntityProperties {

    String CLASS_NAME = "de/take_weiland/mods/commons/internal/SyncedEntityProperties";
    String TICK = "_sc$syncprops$tick";
    String GET_IDX = "_sc$syncprops$index";
    String SET_IDX = "_sc$syncprops$setIndex";
    String GET_OWNER = "_sc$syncprops$owner";
    String SET_OWNER = "_sc$syncprops$setOwner";
    String GET_NAME = "_sc$syncprops$name";
    String SET_NAME = "_sc$syncprops$setName";

    int _sc$syncprops$index();

    void _sc$syncprops$setIndex(int idx);

    String _sc$syncprops$name();

    void _sc$syncprops$setName(String name);

    Entity _sc$syncprops$owner();

    void _sc$syncprops$setOwner(Entity owner);

    void _sc$syncprops$tick();

}
