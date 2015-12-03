package de.take_weiland.mods.commons.client.icon;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

/**
 * @author diesieben07
 */
final class IconManagerImplRotation implements IconManager {

    private final TIntIntMap         lookup;
    private final IIcon[]            icons;
    private final RotatedDirection[] sortedFronts;

    IconManagerImplRotation(Set<RotatedDirection> validFronts, Map<ForgeDirection, IIcon> faceToIcon) {
        sortedFronts = validFronts.toArray(new RotatedDirection[validFronts.size()]);
        Arrays.sort(sortedFronts);

        lookup = new TIntIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1, -1);
        icons = new IIcon[sortedFronts.length * 6];

        Map<Triple<IIcon, Integer, Boolean>, IIcon> iconCache = new HashMap<>();

        for (int i = 0; i < sortedFronts.length; i++) {
            RotatedDirection front = sortedFronts[i];
            lookup.put(front.encode(), i);

            Map<ForgeDirection, IIcon> rotated = rotatedInstance(faceToIcon, front, iconCache);
            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                icons[face.ordinal() + i * 6] = rotated.get(face);
            }

//            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
//                CubeOrientation.IconInstance iconFace = CubeOrientation.computeFace(face, front);
//
//                IIcon origIcon = faceToIcons.get(iconFace.face);
//                Triple<IIcon, Integer, Boolean> key = Triple.of(origIcon, iconFace.rotation, iconFace.flipU);
//
//                IIcon morphed = iconCache.computeIfAbsent(key, k -> MorphedSprite.morph(origIcon, iconFace.rotation, iconFace.flipU, false));
//                icons[face.ordinal() + i * 6] = morphed;
//            }
        }
    }

    static Map<ForgeDirection, IIcon> rotatedInstance(Map<ForgeDirection, IIcon> icons, RotatedDirection front) {
        return rotatedInstance(icons, front, new HashMap<>());
    }

    static Map<ForgeDirection, IIcon> rotatedInstance(Map<ForgeDirection, IIcon> icons, RotatedDirection front, Map<Triple<IIcon, Integer, Boolean>, IIcon> iconCache) {
        Map<ForgeDirection, IIcon> result = new EnumMap<>(ForgeDirection.class);

        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            CubeOrientation.IconInstance iconFace = CubeOrientation.computeFace(face, front);

            IIcon origIcon = icons.get(iconFace.face);
            Triple<IIcon, Integer, Boolean> key = Triple.of(origIcon, iconFace.rotation, iconFace.flipU);

            IIcon morphed = iconCache.computeIfAbsent(key, k -> MorphedSprite.morph(origIcon, iconFace.rotation, iconFace.flipU, false));
            result.put(face, morphed);
        }

        return result;
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
