package de.take_weiland.mods.commons.client.icon;

import com.google.common.collect.Iterables;
import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author diesieben07
 */
final class IconManagerImplRotation implements IconManager {

    private final TIntIntMap         lookup;
    private final IIcon[]            icons;
    private final RotatedDirection[] sortedFronts;
    private final short              possibleFronts;


    IconManagerImplRotation(Set<RotatedDirection> validFronts, Map<ForgeDirection, IIcon> faceToIcon, Comparator<RotatedDirection> frontSorter, Map<RotatedDirection, RotatedDirection> remaps) {
        sortedFronts = validFronts.toArray(new RotatedDirection[validFronts.size()]);
        Arrays.sort(sortedFronts, frontSorter);

        lookup = new TIntIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1, -1);
        icons = new IIcon[sortedFronts.length * 6];

        short possibleFronts = 0;

        Map<Triple<IIcon, Integer, Boolean>, IIcon> iconCache = new HashMap<>();

        for (int i = 0; i < sortedFronts.length; i++) {
            RotatedDirection front = sortedFronts[i];
            RotatedDirection remap = remaps.get(front);
            if (remap != null) {
                continue;
            }

            lookup.put(front.encode(), i);
            possibleFronts |= (1 << front.getDirection().ordinal());

            Map<ForgeDirection, IIcon> rotated = rotatedInstance(faceToIcon, front, iconCache);
            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                IIcon rIcon = rotated.get(face);
                icons[face.ordinal() + i * 6] = rIcon;
                for (RotatedDirection remappedFrom : keysPointingTo(remaps, front)) {
                    int idx = Arrays.binarySearch(sortedFronts, remappedFrom, frontSorter);
                    icons[face.ordinal() + idx * 6] = rIcon;
                }
            }
        }

        for (Map.Entry<RotatedDirection, RotatedDirection> entry : remaps.entrySet()) {
            lookup.put(entry.getKey().encode(), Arrays.binarySearch(sortedFronts, entry.getValue(), frontSorter));
        }

        this.possibleFronts = possibleFronts;
    }

    static <K, V> Iterable<K> keysPointingTo(Map<K, V> map, V value) {
        return Iterables.filter(map.keySet(), k -> map.get(k).equals(value));
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

    private static final float UP_DOWN_THRESHOLD = 50f;

    @Override
    public int getMeta(@NotNull @Nonnull EntityLivingBase placer) {
        int updown = Integer.signum((int) (placer.rotationPitch / UP_DOWN_THRESHOLD));
        updown = updown == 1 ? 1 : updown == -1 ? 0 : -1;

        int cardinalDir = (MathHelper.floor_double((placer.rotationYaw / 90) + 2.5) & 3);

        if (updown >= 0 && (possibleFronts & (1 << updown)) != 0) {
            int add = updown == 0 ? ((~cardinalDir & 1) << 1) : 2;
            return getMeta(updown, (cardinalDir + add) & 3);
        } else {
            int dir = Direction.directionToFacing[cardinalDir];
            if ((possibleFronts & (1 << dir)) != 0) {
                return getMeta(dir, 0);
            } else {
                return 0;
            }
        }
    }

    public static void main(String[] args) {
        int updown = Integer.bitCount(Integer.signum((int) (14 / UP_DOWN_THRESHOLD)));
        updown = Integer.numberOfLeadingZeros(updown) - (updown >>> 3);
        System.out.println(UUID.randomUUID());
    }

    @Override
    public RotatedDirection getFront(int meta) {
        return sortedFronts[meta];
    }


}
