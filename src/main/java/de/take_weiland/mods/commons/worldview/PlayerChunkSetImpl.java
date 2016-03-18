package de.take_weiland.mods.commons.worldview;

import com.google.common.collect.AbstractIterator;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.set.hash.TLongHashSet;

import javax.annotation.Nonnull;
import java.util.AbstractSet;
import java.util.Iterator;

/**
 * @author diesieben07
 */
final class PlayerChunkSetImpl extends AbstractSet<DimensionalChunk> {

    private final TLongHashSet set;

    PlayerChunkSetImpl(TLongHashSet set) {
        this.set = set;
    }

    @Nonnull
    @Override
    public Iterator<DimensionalChunk> iterator() {
        return new It(set._states, set._set, set.capacity());
    }

    private static class It extends AbstractIterator<DimensionalChunk> {

        private final byte[] states;
        private final long[] set;
        private       int    idx;

        It(byte[] states, long[] set, int capacity) {
            this.states = states;
            this.set = set;
            this.idx = capacity;
        }

        @Override
        protected DimensionalChunk computeNext() {
            do {
                if (--idx == -1) return endOfData();
                if (states[idx] == TPrimitiveHash.FULL)
                    return ServerChunkViewManager.decodeIntoDimensionalChunk(set[idx]);
            } while (true);
        }
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof DimensionalChunk) {
            DimensionalChunk dimensionalChunk = (DimensionalChunk) o;
            return set.contains(ServerChunkViewManager.encodeChunk(dimensionalChunk.getDimension(), dimensionalChunk.getX(), dimensionalChunk.getZ()));
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
}
