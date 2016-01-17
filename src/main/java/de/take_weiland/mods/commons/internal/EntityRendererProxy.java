package de.take_weiland.mods.commons.internal;

/**
 * @author diesieben07
 */
public interface EntityRendererProxy {

    String GET_FOV_MOD_HAND = "_sc$getFovModifierHand";
    String SET_FOV_MOD_HAND = "_sc$setFovModifierHand";

    float _sc$getFovModifierHand();

    void _sc$setFovModifierHand(float f);

    String GET_FOV_MOD_HAND_PREV = "_sc$getFovModifierHandPrev";
    String SET_FOV_MOD_HAND_PREV = "_sc$setFovModifierHandPrev";

    float _sc$getFovModifierHandPrev();

    void _sc$setFovModifierHandPrev(float f);

}
