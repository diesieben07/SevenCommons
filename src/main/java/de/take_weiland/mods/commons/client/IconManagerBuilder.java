package de.take_weiland.mods.commons.client;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author diesieben07
 */
public final class IconManagerBuilder {

    private static final int AXIS_X = 2;
    private static final int AXIS_Y = 0;
    private static final int AXIS_Z = 1;

    private final EnumSet<Axis>                  axes      = EnumSet.noneOf(Axis.class);
    private final EnumMap<ForgeDirection, IIcon> faceIcons = new EnumMap<>(ForgeDirection.class);
    private final IIconRegister register;

    public IconManagerBuilder(IIconRegister register) {
        this.register = register;
    }

    public IconManagerBuilder addRotationAxis(Axis... axes) {
        Collections.addAll(this.axes, axes);
        return this;
    }

    public IconManagerBuilder texture(IIcon icon, ForgeDirection... faces) {
        for (ForgeDirection face : faces) {
            faceIcons.put(face, icon);
        }
        return this;
    }

    public IconManagerBuilder texture(IIcon icon) {
        return texture(icon, ForgeDirection.VALID_DIRECTIONS);
    }

    public IconManagerBuilder textureSides(IIcon icon) {
        return texture(icon, ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST);
    }

    public IconManagerBuilder textureTopBottom(IIcon icon) {
        return texture(icon, ForgeDirection.DOWN, ForgeDirection.UP);
    }

    public IconManager build() {
        if (faceIcons.size() != 6) {
            throw new IllegalStateException("Don't know how to texture sides " + Sets.difference(EnumSet.allOf(ForgeDirection.class), faceIcons.keySet()));
        }

        int i = axes.size();
        if (i == 0) {
            return new NoRotationManager(faceIcons);
        } else {
            return new ManagerImpl(axes, faceIcons);
        }
    }

    private static final class NoRotationManager implements IconManager {

        private final IIcon[] icons;

        NoRotationManager(Map<ForgeDirection, IIcon> iconMap) {
            icons = new IIcon[6];
            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                icons[face.ordinal()] = iconMap.get(face);
            }
        }

        @Override
        public IIcon getIcon(int side, int meta) {
            return icons[side];
        }

        @Override
        public int getMeta(int front, int frontRotation) {
            return 0;
        }
    }

    /**
     * <p>Create a list of all possible rotation combinations with the given rotation axes.</p>
     *
     * @param axes the rotation axes
     * @return all possible rotational combinations of the given rotation axes
     */
    private static List<BlockOrientation> getPossibleOrientations(Set<Axis> axes) {
        ArrayList<BlockOrientation> result = Sets.cartesianProduct(Collections.nCopies(3, ImmutableSet.of(0, 1, 2, 3))).stream()
                .map(list -> Maps.toMap(EnumSet.allOf(Axis.class), axis -> list.get(axis.ordinal())))
                .filter(combination -> combinationAllowed(combination, axes))
                .map(BlockOrientation::new)
//                .distinct()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));

//        result.sort(null);
        return result;
    }

    private static boolean combinationAllowed(Map<Axis, Integer> combination, Set<Axis> axes) {
        return combination.entrySet().stream()
                .filter(entry -> !axes.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .mapToInt(Integer::valueOf)
                .sum() == 0;
    }

    private static ForgeDirection applyRotation(ForgeDirection face, Map<Axis, Integer> rotations, boolean reverse) {
        for (Map.Entry<Axis, Integer> entry : rotations.entrySet()) {
            int n = entry.getValue();
            if (reverse) {
                n = 4 - n;
            }
            face = rotate(face, entry.getKey(), n);
        }
        return face;
    }

    public static void main(String[] args) {
        List<BlockOrientation> orientations = getPossibleOrientations(EnumSet.of(Axis.Y));
        System.out.println(orientations.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n")));
    }

    private static int encode(int x, int y, int z, int face) {
        return x | (y << 2) | (z << 4) | (face << 6);
    }

    private static final class ManagerImpl implements IconManager {

        private final TIntIntMap lookup;
        private final IIcon[]    icons;

        ManagerImpl(EnumSet<Axis> axes, Map<ForgeDirection, IIcon> faceToIcons) {
            List<BlockOrientation> orientations = getPossibleOrientations(axes);
            System.out.println(orientations);

            lookup = new TIntIntHashMap();

            icons = new IIcon[orientations.size() * 6];

            int j = 1;

            for (int i = 0; i < orientations.size(); i++) {
                BlockOrientation orientation = orientations.get(i);
                int front = orientation.getFront().ordinal();
                int frontRotation = orientation.getFrontRotation();
                lookup.put(frontRotation | (front << 2), i);

                for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                    ForgeDirection rotated = orientation.rotate(face, true);
                    int faceRotation = orientation.getAxisRotations().get(Axis.get(rotated));

                    IIcon icon;
                    if (faceRotation == 0) {
                        icon = faceToIcons.get(rotated);
                    } else {
                        icon = new RotatedSprite(faceToIcons.get(rotated), faceRotation);
                    }

                    icons[face.ordinal() + i * 6] = icon;
                }
            }
            System.out.println(orientations);
            System.out.println(lookup);
            System.out.println(Arrays.toString(icons));
        }

        @Override
        public IIcon getIcon(int side, int meta) {
            return icons[side + meta * 6];
        }

        @Override
        public int getMeta(int front, int frontRotation) {
            return lookup.get(frontRotation | (front << 2));
        }
    }

    static ForgeDirection rotate(ForgeDirection dir, Axis axis, int n) {
        for (int i = 0; i < n; i++) {
            dir = rotate(dir, axis);
        }
        return dir;
    }

    static ForgeDirection rotate(ForgeDirection dir, Axis axis) {
        ForgeDirection fdAxis;
        switch (axis) {
            case Y:
                fdAxis = ForgeDirection.DOWN;
                break;
            case X:
                fdAxis = ForgeDirection.WEST;
                break;
            case Z:
                fdAxis = ForgeDirection.NORTH;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return dir.getRotation(fdAxis);
    }

    private static final class BlockOrientation implements Comparable<BlockOrientation> {

        private final Map<Axis, Integer> rotations;

        BlockOrientation(Map<Axis, Integer> rotations) {
            this.rotations = rotations;
        }

        ForgeDirection getFront() {
            return applyRotation(ForgeDirection.NORTH, rotations, false);
        }

        int getFrontRotation() {
            return rotations.get(Axis.get(getFront()));
        }

        Map<Axis, Integer> getAxisRotations() {
            return rotations;
        }

        ForgeDirection rotate(ForgeDirection face, boolean reverse) {
            return applyRotation(face, rotations, reverse);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlockOrientation)) return false;

            BlockOrientation that = (BlockOrientation) o;
            return rotations.equals(that.rotations);

        }

        @Override
        public int hashCode() {
            return rotations.hashCode();
        }

        @Override
        public int compareTo(BlockOrientation o) {
            return ComparisonChain.start()
                    .compare(rotations.get(Axis.Y), o.rotations.get(Axis.Y))
                    .compare(rotations.get(Axis.Z), o.rotations.get(Axis.Z))
                    .compare(rotations.get(Axis.X), o.rotations.get(Axis.X))
                    .result();
        }

        @Override
        public String toString() {
            return rotations.toString() + " / " + getFront() + '@' + getFrontRotation();
        }
    }

    private static class TestIcon implements IIcon {

        final String name;

        private TestIcon(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int getIconWidth() {
            return 0;
        }

        @Override
        public int getIconHeight() {
            return 0;
        }

        @Override
        public float getMinU() {
            return 0;
        }

        @Override
        public float getMaxU() {
            return 0;
        }

        @Override
        public float getInterpolatedU(double p_94214_1_) {
            return 0;
        }

        @Override
        public float getMinV() {
            return 0;
        }

        @Override
        public float getMaxV() {
            return 0;
        }

        @Override
        public float getInterpolatedV(double p_94207_1_) {
            return 0;
        }

        @Override
        public String getIconName() {
            return null;
        }
    }

}
