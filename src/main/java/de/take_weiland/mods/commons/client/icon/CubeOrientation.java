package de.take_weiland.mods.commons.client.icon;

import net.minecraftforge.common.util.ForgeDirection;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author diesieben07
 */
final class CubeOrientation {

    static IconInstance computeFace(ForgeDirection face, RotatedDirection front) {
        checkArgument(face != ForgeDirection.UNKNOWN);

        return decodeF(matrix[front.getDirection().ordinal()][face.ordinal()][front.getRotation()]);
    }

    /**
     * <p>This matrix encodes all possible cube orientations. For each front and it's rotation each face is assigned a
     * face in the "normal rotation" (front is north, up is up).</p>
     * <p>The arrangement is as follows: {@code [front][face][frontRotation]} with {@code front} and {@code face} being
     * {@code 0-5} and {@code frontRotation} being {@code 0-3}.</p>
     * <p>The numbers in this matrix need to be decoded using {@link #decodeF(short)}.</p>
     */
    //@formatter:off
    static final short[][][] matrix = {
            // front down
            {
                    /* down  */ { f(2, 0, true , false), f(2, 1, true , false), f(2, 2, true , false), f(2, 3, true , false) },
                    /* up    */ { f(3, 0, false, false), f(3, 3, false, false), f(3, 2, false, false), f(3, 1, false, false) },
                    /* north */ { f(1, 2, false, false), f(5, 1, true , false), f(0, 2, true , false), f(4, 3, true , false) },
                    /* south */ { f(0, 2, true , false), f(4, 3, false, false), f(1, 2, false, false), f(5, 1, false, false) },
                    /* west  */ { f(4, 3, false, false), f(1, 2, false, false), f(5, 1, false, false), f(0, 2, true , false) },
                    /* east  */ { f(5, 1, true , false), f(0, 2, true , false), f(4, 3, true , false), f(1, 2, false, false) }
            },

            // front up
            {
                    /* down  */ { f(3, 2, false, false), f(3, 3, false, false), f(3, 2, false, false), f(3, 1, false, false) },
                    /* up    */ { f(2, 2, false, false), f(2, 3, false, false), f(2, 0, false, false), f(2, 1, false, false) },
                    /* north */ { f(0, 2, false, true ), f(4, 1, true , false), f(1, 0, false, false), f(5, 3, true , false) },
                    /* south */ { f(1, 0, false, false), f(5, 3, false, false), f(0, 0, true , false), f(4, 1, false, false) },
                    /* west  */ { f(4, 1, false, false), f(1, 0, false, false), f(5, 3, false, false), f(0, 0, true , false) },
                    /* east  */ { f(5, 1, false, true ), f(0, 2, false, true ), f(4, 1, true , false), f(1, 0, false, false) }
            },

            // front north
            {
                    /* down  */ { f(0, 0, false, false), f(4, 1, true , false), f(1, 0, true , false), f(5, 3, true , false) },
                    /* up    */ { f(1, 0, false, false), f(5, 3, false, false), f(0, 0, true , false), f(4, 1, false, false) },
                    /* north */ { f(2, 0, false, false), f(2, 1, true , false), f(2, 2, false, false), f(2, 3, true , false) },
                    /* south */ { f(3, 0, false, false), f(3, 3, false, false), f(3, 2, false, false), f(3, 1, false, false) },
                    /* west  */ { f(4, 0, false, false), f(1, 3, false, false), f(5, 2, false, false), f(0, 1, false, true ) },
                    /* east  */ { f(5, 0, false, false), f(0, 1, false, false), f(4, 2, false, false), f(1, 1, true , false) }
            },

            // front south
            {
                    /* down  */ { f(0, 2, false, false), f(4, 1, false, true ), f(1, 2, true , false), f(5, 1, true , false) },
                    /* up    */ { f(1, 2, false, false), f(5, 1, false, false), f(0, 0, false, true ), f(4, 3, false, false) },
                    /* north */ { f(3, 0, false, false), f(3, 3, true , false), f(3, 2, false, false), f(3, 1, true , false) },
                    /* south */ { f(2, 0, false, false), f(2, 1, false, false), f(2, 2, false, false), f(2, 3, false, false) },
                    /* west  */ { f(5, 0, false, false), f(0, 3, false, true ), f(4, 2, false, false), f(1, 1, false, false) },
                    /* east  */ { f(4, 0, false, false), f(1, 3, true , false), f(5, 2, false, false), f(0, 3, false, false) }
            },

            // front west
            {
                    /* down  */ { f(0, 1, false, false), f(4, 0, false, true ), f(1, 1, true , false), f(5, 0, true , false) },
                    /* up    */ { f(1, 3, false, false), f(5, 2, false, false), f(0, 1, false, true ), f(4, 0, false, false) },
                    /* north */ { f(5, 0, false, false), f(0, 3, true , true ), f(4, 2, false, false), f(1, 1, true , false) },
                    /* south */ { f(4, 0, false, false), f(1, 3, false, false), f(5, 2, false, false), f(0, 1, false, true ) },
                    /* west  */ { f(2, 0, false, false), f(2, 1, false, false), f(2, 2, false, false), f(2, 3, false, false) },
                    /* east  */ { f(3, 0, false, false), f(3, 3, true , false), f(3, 2, false, false), f(3, 1, true , false) }
            },

            // front east
            {
                    /* down  */ { f(0, 3, false, false), f(4, 0, true , false), f(1, 1, false, true ), f(5, 0, false, true ) },
                    /* up    */ { f(1, 1, false, false), f(5, 0, false, false), f(0, 3, false, true ), f(4, 2, false, false) },
                    /* north */ { f(4, 0, false, false), f(1, 1, false, true ), f(5, 2, false, false), f(0, 3, false, false) },
                    /* south */ { f(5, 0, false, false), f(0, 3, false, true ), f(4, 2, false, false), f(1, 1, false, false) },
                    /* west  */ { f(3, 0, false, false), f(3, 3, false, false), f(3, 2, false, false), f(3, 1, false, false) },
                    /* east  */ { f(2, 0, false, false), f(2, 1, true , false), f(2, 2, false, false), f(2, 3, true , false) }
            }
    };
    //@formatter:on

    private static final int FLIP_U      = 0b100;
    private static final int FACE_OFFSET = 3;

    /**
     * <p>Encode face properties, for the above matrix.</p>
     *
     * @param face  the face
     * @param r     the rotation of that face
     * @param flipU flip across the vertical axis
     * @param flipV flip across the horizontal axis
     * @return encoded value, to be used with {@link #decodeF(short)}.
     */
    private static short f(int face, int r, boolean flipU, boolean flipV) {
        if (flipV) {
            // flipV is just flipU and 180 rotation
            r += 2;
            flipU = !flipU;
        }

        return (short) ((r & 0b11) | (flipU ? FLIP_U : 0) | (face << FACE_OFFSET));
    }

    /**
     * <p>Decode result of {@link #f(int, int, boolean, boolean)}.</p>
     *
     * @param f the encoded value
     * @return an {@code IconInstance}
     */
    private static IconInstance decodeF(short f) {
        ForgeDirection face = ForgeDirection.VALID_DIRECTIONS[f >> FACE_OFFSET];
        int rot = f & 0b11;
        boolean flipU = (f & FLIP_U) != 0;
        return new IconInstance(face, rot, flipU);
    }

    final static class IconInstance {

        final ForgeDirection face;
        final int            rotation;
        final boolean        flipU;

        IconInstance(ForgeDirection face, int rotation, boolean flipU) {
            this.face = face;
            this.rotation = rotation;
            this.flipU = flipU;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof IconInstance)) return false;

            IconInstance that = (IconInstance) o;
            return rotation == that.rotation && flipU == that.flipU && face == that.face;

        }

        @Override
        public int hashCode() {
            int result = face.hashCode();
            result = 31 * result + rotation;
            result = 31 * result + (flipU ? 1 : 0);
            return result;
        }
    }


}
