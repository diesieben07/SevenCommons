package de.take_weiland.mods.commons.client.icon;

import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.internal.IconProviderInternal;
import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
final class IconManagerImplRotation extends AbstractIconManager {

    private final TIntIntMap             lookup;
    private final IconProviderInternal[] icons;
    private final RotatedDirection[]     sortedFronts;
    private final short                  possibleFronts;
    private final int                    invMeta;

    IconManagerImplRotation(Set<RotatedDirection> validFronts, Map<ForgeDirection, IconProviderInternal> faceToIcon, @Nullable Comparator<RotatedDirection> frontSorter, Map<RotatedDirection, RotatedDirection> remaps) {
        sortedFronts = validFronts.toArray(new RotatedDirection[validFronts.size()]);
        Arrays.sort(sortedFronts, frontSorter);

        int invRotIndex = Arrays.binarySearch(sortedFronts, new RotatedDirection(ForgeDirection.SOUTH, 0));

        lookup = new TIntIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1, -1);
        icons = new IconProviderInternal[(sortedFronts.length + (invRotIndex < 0 ? 1 : 0)) * 6];

        short possibleFronts = 0;

        Map<Triple<IconProviderInternal, Integer, Boolean>, IconProviderInternal> iconCache = new HashMap<>();

        for (int i = 0; i < sortedFronts.length; i++) {
            RotatedDirection front = sortedFronts[i];
            RotatedDirection remap = remaps.get(front);
            if (remap != null) {
                continue;
            }

            lookup.put(front.encode(), i);
            possibleFronts |= (1 << front.getDirection().ordinal());

            Map<ForgeDirection, IconProviderInternal> rotated = rotatedInstance(faceToIcon, front, iconCache);
            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                IconProviderInternal rIcon = rotated.get(face);
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

        if (invRotIndex < 0) {
            invMeta = sortedFronts.length;
            Map<ForgeDirection, IconProviderInternal> rotated = rotatedInstance(faceToIcon, new RotatedDirection(ForgeDirection.SOUTH, 0), iconCache);
            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                icons[face.ordinal() + invMeta * 6] = rotated.get(face);
            }
        } else {
            invMeta = invRotIndex;
        }

        this.possibleFronts = possibleFronts;
    }

    static <K, V> Iterable<K> keysPointingTo(Map<K, V> map, V value) {
        return Iterables.filter(map.keySet(), k -> map.get(k).equals(value));
    }

    static Map<ForgeDirection, IconProviderInternal> rotatedInstance(Map<ForgeDirection, IconProviderInternal> icons, RotatedDirection front) {
        return rotatedInstance(icons, front, new HashMap<>());
    }

    static Map<ForgeDirection, IconProviderInternal> rotatedInstance(Map<ForgeDirection, IconProviderInternal> icons, RotatedDirection front, Map<Triple<IconProviderInternal, Integer, Boolean>, IconProviderInternal> iconCache) {
        Map<ForgeDirection, IconProviderInternal> result = new EnumMap<>(ForgeDirection.class);

        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            CubeOrientation.IconInstance iconFace = CubeOrientation.computeFace(face, front);

            IconProviderInternal origIcon = icons.get(iconFace.face);
            Triple<IconProviderInternal, Integer, Boolean> key = Triple.of(origIcon, iconFace.rotation, iconFace.flipU);

            IconProviderInternal morphed = iconCache.computeIfAbsent(key, k -> MorphedSprite.morph(origIcon, iconFace.rotation, iconFace.flipU, false));
            result.put(face, morphed);
        }

        return result;
    }

    @Override
    int inventoryMeta() {
        return invMeta;
    }

    @Override
    IconProviderInternal getIcon0(int side, int meta) {
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
    public int getMeta(EntityLivingBase placer) {
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

    @Override
    public RotatedDirection getFront(int meta) {
        return sortedFronts[meta];
    }


}
