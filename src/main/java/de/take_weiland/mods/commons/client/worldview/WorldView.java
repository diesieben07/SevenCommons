package de.take_weiland.mods.commons.client.worldview;

import javax.vecmath.Vector3d;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * @author diesieben07
 */
public interface WorldView {

    void setPosition(double x, double y, double z, float pitch, float yaw);

    default Vector3d getPosition() {
        return new Vector3d(getX(), getY(), getZ());
    }

    int getTextureId();

    default void bindTexture() {
        glBindTexture(GL_TEXTURE_2D, getTextureId());
    }

    void dispose();

    boolean isActive();

    RenderMode getRenderType();

    int getFrameskip();

    void requestRender();

    int getTextureWidth();

    int getTextureHeight();

    int getDimension();

    double getX();

    double getY();

    double getZ();

    float getPitch();

    float getYaw();

    enum RenderMode {

        CONTINUOUS,
        BY_REQUEST

    }

}
