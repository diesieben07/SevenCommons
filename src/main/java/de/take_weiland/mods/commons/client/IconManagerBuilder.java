package de.take_weiland.mods.commons.client;

import net.minecraft.util.IIcon;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

/**
 * @author diesieben07
 */
public final class IconManagerBuilder {

    private final EnumSet<BlockFace.Axis> axes = EnumSet.noneOf(BlockFace.Axis.class);

    public IconManagerBuilder addRotationAxis(BlockFace.Axis... axes) {
        Collections.addAll(this.axes, axes);
        return this;
    }

    public IconManager build() {
        return null;
    }

    private static final class ManagerImpl implements IconManager {

        private final Map<BlockFace, IIcon[]> faceToIcons = null;

        @Override
        public IIcon getIcon(int meta, BlockFace face) {
            IIcon icon = faceToIcons.get(face)[meta];

            return null;
        }

        @Override
        public int getMeta(BlockFace front, int frontRotation) {
            return 0;
        }
    }

}
