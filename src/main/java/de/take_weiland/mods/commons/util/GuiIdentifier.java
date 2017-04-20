package de.take_weiland.mods.commons.util;

import com.google.common.base.Throwables;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

/**
 * <p>Utility for easy creation of an enumeration of GUI types, usually implemented on an Enum for convenience.</p>
 * <p>Use {@link #builder()} to bind the enumeration to the Container and GuiContainer and register a handler to FML.</p>
 * <p>A typical use and registration looks like this:</p>
 * <pre>
 *     enum MyGui implements GuiIdentifier {
 *
 *         GUI_A, GUI_B;
 *
 *         &#64;Override public Object mod() { return MyMod.instance; }
 *
 *     }
 *
 *     GuiIdentifier.builder()
 *          .add(MyGui.GUI_A, ContainerA::new, () -> GuiContainerA::new)
 *          .add(MyGui.GUI_B, () -> GuiScreenB::new)
 *          .done();
 *
 *     MyGui.GUI_A.open(player, x, y, z);
 * </pre>
 * <p>Purely client side GUIs can also be opened, using {@link GuiConstructor} and {@link Builder#add(GuiIdentifier, Supplier)}.
 * This requires that the GUI opening is initiated from the client thread, where as usually it is done from the server.</p>
 *
 * @author diesieben07
 */
public interface GuiIdentifier {

    /**
     * <p>Create a new builder for registering your GUIs.</p>
     *
     * @return a builder
     */
    static Builder builder() {
        try {
            return (Builder) GuiIdentifierBuilderImpl.builderConstructor.invokeExact();
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    /**
     * <p>A per-mod unique identification number for this GUI. Usually implicitly implemented by using an Enum.</p>
     *
     * @return the identification number
     */
    int ordinal();

    /**
     * <p>The mod object associated with this GUI for use in {@link FMLCommonHandler#findContainerFor(Object)}. GUIs registered to the same builder must return the same Object here.</p>
     *
     * @return the mod object
     */
    Object mod();

    /**
     * <p>Open this GUI for the given player without any arguments.</p>
     *
     * @param player the player
     */
    default void open(EntityPlayer player) {
        open(player, 0, 0, 0);
    }

    /**
     * <p>Open this GUI for the given player with a single argument.</p>
     *
     * @param player the player
     * @param x      the argument
     */
    default void open(EntityPlayer player, int x) {
        open(player, x, 0, 0);
    }

    /**
     * <p>Open this GUI for the given player with 2 arguments.</p>
     *
     * @param player the player
     * @param x      the 1st argument
     * @param y      the 2nd argument
     */
    default void open(EntityPlayer player, int x, int y) {
        open(player, x, y, 0);
    }

    /**
     * <p>Open this GUI for the given player with 3 arguments.</p>
     *
     * @param player the player
     * @param x      1st argument
     * @param y      2nd argument
     * @param z      3rd argument
     */
    default void open(EntityPlayer player, int x, int y, int z) {
        player.openGui(mod(), ordinal(), player.world, x, y, z);
    }

    default void open(EntityPlayer player, Vec3i pos) {
        player.openGui(mod(), ordinal(), player.world, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * <p>Used for registering GUIs, returned by {@link #builder()}.</p>
     */
    interface Builder {

        /**
         * <p>Bind the given identifier to the given Container and GuiContainer.</p>
         * <p>The GuiContainer constructor is wrapped in a Supplier to isolate any referenced client-only classes (see {@linkplain GuiIdentifier example usage}).
         * The Supplier will be queried once if the environment is client side, otherwise the Supplier will be immediately discarded.</p>
         *
         * @param id                   the identifier
         * @param containerConstructor the container constructor
         * @param guiConstructor       the gui constructor
         * @return this for convenience
         */
        <C extends Container> Builder add(GuiIdentifier id, ContainerConstructor<C> containerConstructor, Supplier<GuiContainerConstructor<? extends C>> guiConstructor);

        /**
         * <p>Bind the given identifier to the given Container and GuiContainer.</p>
         * <p>The GuiContainer constructor is wrapped in a Supplier to isolate any referenced client-only classes (see {@linkplain GuiIdentifier example usage}).
         * The Supplier will be queried once if the environment is client side, otherwise the Supplier will be immediately discarded.</p>
         *
         * @param id                   the identifier
         * @param containerConstructor the container constructor
         * @param guiConstructor       the gui constructor
         * @return this for convenience
         */
        default <C extends Container> Builder add(GuiIdentifier id, ContainerConstructor.NoArgs<C> containerConstructor, Supplier<GuiContainerConstructor<? extends C>> guiConstructor) {
            return add(id, (ContainerConstructor<C>) containerConstructor, guiConstructor);
        }

        /**
         * <p>Bind the given identifier to the given Container and GuiContainer.</p>
         * <p>The GuiContainer constructor is wrapped in a Supplier to isolate any referenced client-only classes (see {@linkplain GuiIdentifier example usage}).
         * The Supplier will be queried once if the environment is client side, otherwise the Supplier will be immediately discarded.</p>
         *
         * @param id                   the identifier
         * @param containerConstructor the container constructor
         * @param guiConstructor       the gui constructor
         * @return this for convenience
         */
        default <C extends Container, T extends TileEntity> Builder add(GuiIdentifier id, ContainerConstructor.ForTileEntity<C, T> containerConstructor, Supplier<GuiContainerConstructor<? extends C>> guiConstructor) {
            return add(id, (ContainerConstructor<C>) containerConstructor, guiConstructor);
        }

        /**
         * <p>Bind the given identifier to the given Container and GuiContainer.</p>
         * <p>The GuiContainer constructor is wrapped in a Supplier to isolate any referenced client-only classes (see {@linkplain GuiIdentifier example usage}).
         * The Supplier will be queried once if the environment is client side, otherwise the Supplier will be immediately discarded.</p>
         *
         * @param id                   the identifier
         * @param containerConstructor the container constructor
         * @param guiConstructor       the gui constructor
         * @return this for convenience
         */
        default <C extends Container> Builder add(GuiIdentifier id, ContainerConstructor.ForBlockPosition<C> containerConstructor, Supplier<GuiContainerConstructor<? extends C>> guiConstructor) {
            return add(id, (ContainerConstructor<C>) containerConstructor, guiConstructor);
        }

        /**
         * <p>Bind the given identifier to the given GuiScreen.</p>
         *
         * @param id             the identifier
         * @param guiConstructor the gui constructor
         * @return this for convenience
         */
        Builder add(GuiIdentifier id, Supplier<GuiConstructor> guiConstructor);

        /**
         * <p>Finish registration and register an appropriate {@link IGuiHandler}.</p>
         */
        void done();

    }

    /**
     * <p>Supplier for new Container instances.</p>
     */
    @FunctionalInterface
    interface ContainerConstructor<C extends Container> extends ContainerConstructorInternal {

        /**
         * <p>Create a new Container instance with the given parameters.</p>
         *
         * @param player the player
         * @param world  the world
         * @param x      1st argument
         * @param y      2nd argument
         * @param z      3rd argument
         * @return a new Container
         */
        @Override
        C newInstance(EntityPlayer player, World world, int x, int y, int z);

        /**
         * <p>Version of {@code ContainerConstructor} that provides no additional arguments.</p>
         */
        @FunctionalInterface
        interface NoArgs<C extends Container> extends ContainerConstructor<C> {

            /**
             * <p>Create a new Container instance with the given parameters.</p>
             *
             * @param player the player
             * @return a new Container
             */
            C newInstance(EntityPlayer player);

            @Override
            default C newInstance(EntityPlayer player, World world, int x, int y, int z) {
                return newInstance(player);
            }
        }


        /**
         * <p>Version of {@code ContainerConstructor} that provides the TileEntity given by the coordinates.</p>
         */
        @FunctionalInterface
        interface ForTileEntity<C extends Container, T extends TileEntity> extends ContainerConstructor<C> {
            /**
             * <p>Create a new Container instance with the given parameters.</p>
             *
             * @param player the player
             * @param tile   the TileEntity
             * @return a new Container
             */
            C newInstance(EntityPlayer player, T tile);

            @Override
            default C newInstance(EntityPlayer player, World world, int x, int y, int z) {
                //noinspection unchecked
                return newInstance(player, (T) world.getTileEntity(new BlockPos(x, y, z)));
            }
        }

        @FunctionalInterface
        interface ForBlockPosition<C extends Container> extends ContainerConstructor<C> {

            /**
             * <p>Create a new Container instance with the given parameters.</p>
             *
             * @param player the player
             * @param world  the world
             * @param pos    the block position
             * @return a new Container
             */
            C newInstance(EntityPlayer player, World world, BlockPos pos);

            @Override
            default C newInstance(EntityPlayer player, World world, int x, int y, int z) {
                return newInstance(player, world, new BlockPos(x, y, z));
            }
        }
    }

    /**
     * <p>Constructor for a GuiScreen.</p>
     */
    @FunctionalInterface
    interface GuiConstructor {

        /**
         * <p>Create a new GuiScreen instance.</p>
         *
         * @return a new GuiScreen
         */
        @SideOnly(Side.CLIENT)
        GuiScreen newInstance();
    }

    /**
     * <p>Constructor for a GuiContainer.</p>
     */
    interface GuiContainerConstructor<C extends Container> {

        /**
         * <p>Create a new GuiContainer with the given Container.</p>
         *
         * @param container the Container
         * @return a new GuiContainer
         */
        @SideOnly(Side.CLIENT)
        GuiContainer newInstance(C container);

    }

}

