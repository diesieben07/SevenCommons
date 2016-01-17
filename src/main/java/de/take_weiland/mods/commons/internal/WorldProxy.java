package de.take_weiland.mods.commons.internal;

/**
 * @author diesieben07
 */
public interface WorldProxy {

    String DOES_CHUNK_EXIST = "_sc$chunkExists";

    boolean _sc$chunkExists(int chunkX, int chunkZ);

}
