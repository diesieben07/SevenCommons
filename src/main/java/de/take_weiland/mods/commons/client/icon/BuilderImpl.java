package de.take_weiland.mods.commons.client.icon;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static de.take_weiland.mods.commons.client.icon.RotatedDirection.checkFace;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
final class BuilderImpl implements IconManagerBuilder {

    private final List<List<RotatedDirection>>            updates     = new ArrayList<>();
    private final EnumMap<ForgeDirection, IIcon>          faceIcons   = new EnumMap<>(ForgeDirection.class);
    private final Set<RotatedDirection>                   validFronts = new HashSet<>();
    private final Map<RotatedDirection, RotatedDirection> remaps      = new HashMap<>();
    private IIconRegister register;

    BuilderImpl(IIconRegister register, @Nullable String domain) {
        if (domain == null) {
            this.register = register;
        } else {
            this.register = new DomainRegister(register, domain);
        }

        update();
    }

    @Override
    public IconManagerBuilder update() {
        if (!updates.isEmpty()) {
            currentUpdate().sort(null);
        }
        updates.add(new ArrayList<>());
        return this;
    }

    @Override
    public IconManagerBuilder remap(RotatedDirection original, RotatedDirection _new) {
        checkArgument(!original.equals(_new), "Cannot remap %s to itself", original);
        checkState(validFronts.contains(original) && validFronts.contains(_new), "Orientations must be registered before remapping");
        RotatedDirection remapPrev = remaps.get(original);
        checkState(remapPrev == null, "%s is already mapped to %s", original, remapPrev);

        remaps.put(original, _new);
        return this;
    }

    @Override
    public IconManagerBuilder addValidFront(RotatedDirection... directions) {
        for (RotatedDirection direction : directions) {
            if (validFronts.add(direction)) {
                currentUpdate().add(direction);
            }
        }
        return this;
    }

    private List<RotatedDirection> currentUpdate() {
        return updates.get(updates.size() - 1);
    }

    @Override
    public IconManagerBuilder defaultResourceDomain(String domain) {
        checkArgument(!Strings.isNullOrEmpty(domain));

        IIconRegister original = register instanceof DomainRegister ? ((DomainRegister) register).delegate : register;
        register = new DomainRegister(original, domain);
        return this;
    }

    private static final class DomainRegister implements IIconRegister {

        private final IIconRegister delegate;
        final         String        domain;

        private DomainRegister(IIconRegister delegate, String domain) {
            this.delegate = delegate;
            this.domain = domain;
        }

        @Override
        public IIcon registerIcon(String icon) {
            if (icon.indexOf(':') == -1) {
                icon = domain + ':' + icon;
            }
            return delegate.registerIcon(icon);
        }
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
            validFronts.add(new RotatedDirection(ForgeDirection.NORTH, 0));
        }

        if (validFronts.size() == 1) {
            RotatedDirection front = Iterables.getOnlyElement(validFronts);
            return new IconManagerImplNoRotation(IconManagerImplRotation.rotatedInstance(faceIcons, front), front);
        }

        if (useStandardMeta && validFronts.size() > 16) {
            throw new IllegalStateException(String.format("Too many possibilities (%s) for standard metadata (max is 16)", validFronts.size()));
        }

        Comparator<RotatedDirection> frontSorter;
        if (updates.size() == 1) {
            frontSorter = null;
        } else {
            List<RotatedDirection> list = ImmutableList.copyOf(Iterables.concat(updates));
            frontSorter = Ordering.explicit(list);
        }

        // remove indirections in remaps
        for (Map.Entry<RotatedDirection, RotatedDirection> entry : remaps.entrySet()) {
            RotatedDirection actualTarget = entry.getValue();
            RotatedDirection temp = actualTarget;
            do {
                temp = remaps.get(temp);
                if (temp == null) {
                    break;
                }
                actualTarget = temp;
            } while (true);

            entry.setValue(actualTarget);
        }

        return new IconManagerImplRotation(validFronts, faceIcons, frontSorter, remaps);
    }

}
