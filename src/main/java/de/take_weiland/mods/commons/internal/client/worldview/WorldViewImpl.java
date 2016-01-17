package de.take_weiland.mods.commons.internal.client.worldview;

import de.take_weiland.mods.commons.client.worldview.WorldView;
import de.take_weiland.mods.commons.internal.EntityRendererProxy;
import de.take_weiland.mods.commons.internal.worldview.PacketRequestWorldInfo;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.lwjgl.BufferUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.Minecraft.getMinecraft;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author diesieben07
 */
public final class WorldViewImpl implements WorldView {

    private static final Map<Integer, List<WorldViewImpl>> views        = new HashMap<>();
    private static final Map<Integer, WorldClient>         clientWorlds = new HashMap<>();
    public static final  String                            CLASS_NAME   = "de/take_weiland/mods/commons/internal/client/worldview/WorldViewImpl";

    private final RenderGlobal     rg;
    private       EntityLivingBase viewport;
    private       long             renderEndNanoTime;

    private       int texture;
    private final int width, height;
    private final int    dimension;
    private       double x, y, z;
    private float pitch, yaw;

    public static WorldViewImpl create(int frameskip, int width, int height, int dimension, double x, double y, double z, float pitch, float yaw) {
        int texture = genTexture(width, height);

        WorldViewImpl view = new WorldViewImpl(texture, width, height, dimension, x, y, z, pitch, yaw);
        getViews(dimension).add(view);

        WorldClient world = clientWorlds.get(dimension);

        if (world != null) {
            view.initWorld(world);
        }

        return view;
    }

    public static WorldClient getOrCreateWorld(int dimension) {
        WorldClient world = clientWorlds.get(dimension);
        if (world == null) {
            WorldSettings settings = new WorldSettings(0, WorldSettings.GameType.SURVIVAL, false, false, WorldType.DEFAULT);
            world = new ClientOtherDimWorld(getMinecraft().getNetHandler(), settings, dimension, getMinecraft().theWorld.difficultySetting, getMinecraft().theWorld.theProfiler);

            WorldClient wc = world;
            new PacketRequestWorldInfo(dimension).sendToServer()
                    .thenAcceptAsync(packet -> {
                        wc.setSpawnLocation(packet.spawnX, packet.spawnY, packet.spawnZ);
                        wc.skylightSubtracted = packet.skylightSubtracted;
                        wc.setWorldTime(packet.worldTime);
                    }, Scheduler.client());
            setWorld(dimension, world);
        }
        return world;
    }

    public static final String SET_CLIENT_WORLD = "setMainClientWorld";

    // callback from asm
    @SuppressWarnings("unused")
    public static void setMainClientWorld(WorldClient world) {
        if (world != null) {
            setWorld(world.provider.dimensionId, world);
        }
    }

    private static void setWorld(int dimension, WorldClient world) {
        clientWorlds.put(dimension, world);

        List<WorldViewImpl> views = WorldViewImpl.views.get(dimension);
        if (views != null) {
            for (WorldViewImpl view : views) {
                view.initWorld(world);
            }
        }
    }

    public static void renderAll() {
        views.forEach((dim, views) -> views.forEach(WorldViewImpl::render));
    }

    private void initWorld(WorldClient world) {
        rg.setWorldAndLoadRenderers(world);
        viewport = new ViewEntity(world);
        updateViewportPosition();
        world.spawnEntityInWorld(viewport);
    }

    private void updateViewportPosition() {
        viewport.setPositionAndRotation(x, y, z, yaw, pitch);
    }

    private void render() {
        if (viewport == null) {
            return;
        }

        float partialTicks = 1f;

        Minecraft mc = getMinecraft();
        updateViewportPosition();

        GameSettings settings = mc.gameSettings;
        EntityRenderer entityRenderer = mc.entityRenderer;

        RenderGlobal renderGlobalBackup = mc.renderGlobal;
        EntityLivingBase viewportBackup = mc.renderViewEntity;
        int heightBackup = mc.displayHeight;
        int widthBackup = mc.displayWidth;
        int thirdPersonBackup = settings.thirdPersonView;
        boolean hideGuiBackup = settings.hideGUI;

        float fovHandBackup = ((EntityRendererProxy) entityRenderer)._sc$getFovModifierHand();
        float fovHandPrevBackup = ((EntityRendererProxy) entityRenderer)._sc$getFovModifierHandPrev();

        mc.renderGlobal = rg;
        mc.renderViewEntity = viewport;
        mc.displayHeight = height;
        mc.displayWidth = width;

        settings.thirdPersonView = 0;
        settings.hideGUI = true;

        ((EntityRendererProxy) entityRenderer)._sc$setFovModifierHand(1F);
        ((EntityRendererProxy) entityRenderer)._sc$setFovModifierHandPrev(1F);

        int fps = mc.gameSettings.limitFramerate;
        if (mc.isFramerateLimitBelowMax()) {
            entityRenderer.renderWorld(partialTicks, 0L);
        } else {
            entityRenderer.renderWorld(partialTicks, renderEndNanoTime + (1000000000 / fps));
        }

        glBindTexture(GL_TEXTURE_2D, texture);
        glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, width, height, 0);

        renderEndNanoTime = System.nanoTime();

        ((EntityRendererProxy) entityRenderer)._sc$setFovModifierHand(fovHandBackup);
        ((EntityRendererProxy) entityRenderer)._sc$setFovModifierHandPrev(fovHandPrevBackup);

        settings.thirdPersonView = thirdPersonBackup;
        settings.hideGUI = hideGuiBackup;

        mc.displayHeight = heightBackup;
        mc.displayWidth = widthBackup;
        mc.renderGlobal = renderGlobalBackup;
        mc.renderViewEntity = viewportBackup;
    }

    private static int genTexture(int width, int height) {
        int id = glGenTextures();
        if (id == 0) {
            throw new RuntimeException("failed to generate new GL texture"); // 0 is default texture
        }
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, BufferUtils.createByteBuffer(3 * width * height));

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        return id;
    }

    private static List<WorldViewImpl> getViews(Integer dimension) {
        return views.computeIfAbsent(dimension, x -> new ArrayList<>());
    }

    private WorldViewImpl(int texture, int width, int height, int dimension, double x, double y, double z, float pitch, float yaw) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;

        this.rg = new RenderGlobal(getMinecraft());
    }

    @Override
    public void dispose() {
        if (isActive()) {
            glDeleteTextures(texture);
            views.get(dimension).remove(this);
            texture = 0;
        }
    }

    @Override
    public boolean isActive() {
        return texture != 0;
    }

    @Override
    public void setPosition(double x, double y, double z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @Override
    public int getTextureId() {
        return texture;
    }

    @Override
    public int getTextureWidth() {
        return width;
    }

    @Override
    public int getTextureHeight() {
        return height;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public RenderMode getRenderType() {
        return null;
    }

    @Override
    public int getFrameskip() {
        return 0;
    }

    @Override
    public void requestRender() {

    }
}
