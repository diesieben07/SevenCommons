package de.take_weiland.mods.commons.client.icon;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static de.take_weiland.mods.commons.client.icon.RotatedDirection.checkFace;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
final class BuilderImpl implements IconManagerBuilder {

    private final EnumMap<ForgeDirection, IIcon> faceIcons   = new EnumMap<>(ForgeDirection.class);
    private final Set<RotatedDirection>          validFronts = new HashSet<>();
    private final IIconRegister register;

    BuilderImpl(IIconRegister register) {
        this.register = register;
    }

    @Override
    public IconManagerBuilder addValidFront(RotatedDirection... directions) {
        Collections.addAll(validFronts, directions);
        return this;
    }

    @Override
    public IIconRegister register() {
        return register;
    }

    @Override
    public IconManagerBuilder texture(IIcon icon, ForgeDirection... faces) {
        for (ForgeDirection face : faces) {
            faceIcons.put(checkFace(face), icon);
        }
        return this;
    }

    @Override
    public IconManager build(boolean useStandardMeta) {
        if (faceIcons.size() != 6) {
            throw new IllegalStateException("Don't know how to texture sides " + Sets.difference(ImmutableSet.copyOf(ForgeDirection.VALID_DIRECTIONS), faceIcons.keySet()));
        }

        if (validFronts.size() == 0) {
            throw new IllegalStateException("No valid front faces defined");
        }

        if (useStandardMeta && validFronts.size() > 16) {
            throw new IllegalStateException(String.format("Too many possibilities (%s) for standard metadata (max is 16)", validFronts.size()));
        }

        return new ManagerImpl(validFronts, faceIcons);
    }

    private static final class ManagerImpl implements IconManager {

        private final TIntIntMap         lookup;
        private final IIcon[]            icons;
        private final RotatedDirection[] sortedFronts;

        ManagerImpl(Set<RotatedDirection> validFronts, Map<ForgeDirection, IIcon> faceToIcons) {
            sortedFronts = validFronts.toArray(new RotatedDirection[validFronts.size()]);
            Arrays.sort(sortedFronts);

            lookup = new TIntIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1, -1);
            icons = new IIcon[sortedFronts.length * 6];

            for (int i = 0; i < sortedFronts.length; i++) {
                RotatedDirection front = sortedFronts[i];
                lookup.put(front.encode(), i);

                Map<Triple<IIcon, Integer, Boolean>, IIcon> iconCache = new HashMap<>();

                for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                    CubeOrientation.IconInstance iconFace = CubeOrientation.computeFace(face, front);

                    IIcon origIcon = faceToIcons.get(iconFace.face);
                    Triple<IIcon, Integer, Boolean> key = Triple.of(origIcon, iconFace.rotation, iconFace.flipU);

                    IIcon morphed = iconCache.computeIfAbsent(key, k -> MorphedSprite.morph(origIcon, iconFace.rotation, iconFace.flipU, false));
                    icons[face.ordinal() + i * 6] = morphed;
                }
            }
        }

        @Override
        public IIcon getIcon(int side, int meta) {
            return icons[side + meta * 6];
        }

        @Override
        public int getMeta(int front, int frontRotation) {
            int meta = lookup.get(RotatedDirection.encode(front, frontRotation));
            if (meta < 0) {
                throw new IllegalArgumentException("Cannot rotate to " + new RotatedDirection(ForgeDirection.VALID_DIRECTIONS[front], frontRotation));
            }
            return meta;
        }

        @Override
        public RotatedDirection getFront(int meta) {
            return sortedFronts[meta];
        }

    }

}
