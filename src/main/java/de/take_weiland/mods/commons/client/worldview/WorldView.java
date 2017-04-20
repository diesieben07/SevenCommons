package de.take_weiland.mods.commons.client.worldview;

import de.take_weiland.mods.commons.internal.client.worldview.WorldViewImpl;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static net.minecraft.client.Minecraft.getMinecraft;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * <p>A WorldView is an alternate viewport into the game world, potentially even in a different dimension than the client
 * is currently in. Each WorldView has an associated {@linkplain #getTextureId() GL texture} which is the rendering target
 * for this alternate viewport.</p>
 * <p>A WorldView can either be rendered at a continuous frame rate, limited by the main game's frame rate, or only on
 * demand.</p>
 * <p>Usually a WorldView is used in conjunction with one or more
 * {@linkplain de.take_weiland.mods.commons.worldview.ClientChunks client-side forced chunks} to ensure useful operation
 * even as the player moves away.</p>
 *
 * @author diesieben07
 */
public interface WorldView {

    /**
     * <p>Match game frame rate.</p>
     */
    int CONTINUOUS          = 0;
    /**
     * <p>Render only on demand using {@link #requestRender()}.</p>
     */
    int ON_DEMAND_RENDERING = -1;

    /**
     * <p>Create a new world view with the given properties. This view will match the games frame rate.</p>
     *
     * @param texWidth  texture width of the view
     * @param texHeight texture height of the view
     * @param dimension dimension ID for the view
     * @param x         x position of the camera
     * @param y         y position of the camera
     * @param z         z position of the camera
     * @param pitch     pitch of the camera
     * @param yaw       yaw of the camera
     * @return a new WorldView
     */
    static WorldView create(int texWidth, int texHeight, int dimension, double x, double y, double z, float pitch, float yaw) {
        return create(texWidth, texHeight, dimension, x, y, z, pitch, yaw, CONTINUOUS);
    }

    /**
     * <p>Create a new world view with the given properties. This view will try to target the given frame rate, if specified.</p>
     *
     * @param texWidth  texture width of the view
     * @param texHeight texture height of the view
     * @param dimension dimension ID for the view
     * @param x         x position of the camera
     * @param y         y position of the camera
     * @param z         z position of the camera
     * @param pitch     pitch of the camera
     * @param yaw       yaw of the camera
     * @param fps       fps count to target, {@link #CONTINUOUS} to match game fps or {@link #ON_DEMAND_RENDERING} to disable continuous rendering
     * @return a new WorldView
     */
    static WorldView create(int texWidth, int texHeight, int dimension, double x, double y, double z, float pitch, float yaw, int fps) {
        return create(texWidth, texHeight, dimension, x, y, z, pitch, yaw, fps, TimeUnit.SECONDS);
    }

    /**
     * <p>Create a new world view with the given properties. This view will try to target the given frame rate, if specified.</p>
     *
     * @param texWidth  texture width of the view
     * @param texHeight texture height of the view
     * @param dimension dimension ID for the view
     * @param x         x position of the camera
     * @param y         y position of the camera
     * @param z         z position of the camera
     * @param pitch     pitch of the camera
     * @param yaw       yaw of the camera
     * @param frames    number of frames to render in one of the given TimeUnit, {@link #CONTINUOUS} to match game fps (ignores {@code unit})
     *                  or {@link #ON_DEMAND_RENDERING} to disable continuous rendering
     * @param unit      time unit for {@code frames}
     * @return a new WorldView
     */
    static WorldView create(int texWidth, int texHeight, int dimension, double x, double y, double z, float pitch, float yaw, int frames, TimeUnit unit) {
        if (getMinecraft().player == null) {
            throw new IllegalStateException("No client world loaded");
        }
        checkArgument(frames >= -1);
        return WorldViewImpl.createImpl(frames, unit, texWidth, texHeight, dimension, x, y, z, pitch, yaw);
    }

    /**
     * <p>Set the viewport position of this view.</p>
     *
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @param pitch the pitch
     * @param yaw   the yaw
     */
    void setPosition(double x, double y, double z, float pitch, float yaw);

    /**
     * <p>Get the viewport position of this view.</p>
     *
     * @return the viewport position
     */
    default Vector3d getPosition() {
        return new Vector3d(getX(), getY(), getZ());
    }

    /**
     * <p>Get the GL texture ID for this view. Once a view is {@linkplain #dispose() disposed}, the return value of
     * this method is unspecified.</p>
     *
     * @return the texture ID
     */
    int getTextureId();

    /**
     * <p>Bind the GL texture for this view using {@link GL11#glBindTexture(int, int)}. The behavior of this method is
     * unspecified if this view is {@linkplain #dispose() disposed}.</p>
     */
    default void bindTexture() {
        glBindTexture(GL_TEXTURE_2D, getTextureId());
    }

    BufferedImage grabScreenshot();

    /**
     * <p>Dispose this view. A disposed view will no longer be rendered and it's texture and other resources will be deleted.</p>
     * <p>All views are automatically disposed when the player leaves the server.</p>
     */
    void dispose();

    /**
     * <p>Whether this view is {@linkplain #dispose() disposed}.</p>
     *
     * @return whether this view is disposed
     */
    boolean isDisposed();

    /**
     * <p>Whether this view is not {@linkplain #dispose() disposed}.</p>
     *
     * @return whether this view is not disposed
     */
    default boolean isActive() {
        return !isDisposed();
    }

    /**
     * <p>Get the rendering mode of this view.</p>
     *
     * @return the rendering mode
     */
    RenderMode getRenderMode();

    /**
     * <p>For non-continuous views request that this view be rendered at the next game frame.</p>
     */
    void requestRender();

    void requestRender(Consumer<? super WorldView> callback);

    /**
     * <p>Get the width of this view's texture in pixels.</p>
     *
     * @return the texture width
     */
    int getTextureWidth();

    /**
     * <p>Get the height of this view's texture in pixels.</p>
     *
     * @return the texture height
     */
    int getTextureHeight();

    /**
     * <p>Get the dimension ID of this view's viewport.</p>
     *
     * @return the dimensionID
     */
    int getDimension();

    /**
     * <p>Get the x position of this view's viewport.</p>
     *
     * @return the x position
     */
    double getX();

    /**
     * <p>Get the y position of this view's viewport.</p>
     *
     * @return the y position
     */
    double getY();

    /**
     * <p>Get the z position of this view's viewport.</p>
     *
     * @return the z position
     */
    double getZ();

    /**
     * <p>Get the pitch of this view's viewport.</p>
     *
     * @return the pitch
     */
    float getPitch();

    /**
     * <p>Get the yaw of this view's viewport.</p>
     *
     * @return the yaw
     */
    float getYaw();

    /**
     * <p>Enumeration of possible {@linkplain #getRenderMode() render modes}.</p>
     */
    enum RenderMode {

        /**
         * <p>Continuous automatic rendering.</p>
         */
        CONTINUOUS,
        /**
         * <p>Rendering only happens by request using {@link #requestRender()}.</p>
         */
        ON_DEMAND

    }

}
