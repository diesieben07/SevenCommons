package de.take_weiland.mods.commons.internal.client.worldview;

import cpw.mods.fml.relauncher.ReflectionHelper;
import de.take_weiland.mods.commons.client.worldview.WorldView;
import de.take_weiland.mods.commons.internal.EntityRendererProxy;
import de.take_weiland.mods.commons.internal.worldview.PacketRequestWorldInfo;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.minecraft.client.Minecraft.getMinecraft;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author diesieben07
 */
public class WorldViewImpl implements WorldView {

    private static final Map<Integer, List<WorldViewImpl>> views        = new HashMap<>();
    private static final Map<Integer, WorldClient>         clientWorlds = new HashMap<>();
    public static final  String                            CLASS_NAME   = "de/take_weiland/mods/commons/internal/client/worldview/WorldViewImpl";

    private final RenderGlobal     rg;
    private       EntityLivingBase viewport;

    private       int texture;
    private final int frameBuffer;
    private final int renderBuffer;
    private final int width;
    private final int height;
    private final int dimension;

    private double x, y, z;
    private float pitch, yaw;

    public static WorldViewImpl createImpl(int fps, TimeUnit unit, int width, int height, int dimension, double x, double y, double z, float pitch, float yaw) {
        int frameBuf = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuf);
        int texture = genTexture(width, height);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);

        int renderBuf = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderBuf);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderBuf);

        WorldViewImpl view;
        if (fps == WorldView.ON_DEMAND_RENDERING) {
            view = new OnDemandRenderingView(texture, frameBuf, renderBuf, width, height, dimension, x, y, z, pitch, yaw);
        } else if (fps != WorldView.CONTINUOUS) {
            view = new ViewWithFPS(texture, frameBuf, renderBuf, width, height, dimension, x, y, z, pitch, yaw, fps, unit);
        } else {
            view = new WorldViewImpl(texture, frameBuf, renderBuf, width, height, dimension, x, y, z, pitch, yaw);
        }
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

    public static void tick() {
        if (getMinecraft().theWorld != null && !getMinecraft().isGamePaused()) {
            clientWorlds.forEach((id, world) -> {
                WorldClient mainWorld = getMinecraft().theWorld;
                if (world != mainWorld) {
                    getMinecraft().theWorld = world;

                    world.updateEntities();
                    world.tick();

                    getMinecraft().theWorld = mainWorld;
                }
            });
        }
    }

    public static void cleanup() {
        clientWorlds.clear();
        views.forEach((dim, list) -> list.forEach(WorldViewImpl::disposeNoListRemove));
        views.clear();
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

    void render() {
        forceRender();
    }

    final void forceRender() {
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
        WorldClient worldBackup = mc.theWorld;
        EffectRenderer effectRendererBackup = mc.effectRenderer;
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

        mc.theWorld = getOrCreateWorld(dimension);
        RenderManager.instance.set(mc.theWorld);

        mc.effectRenderer = new EffectRenderer(mc.theWorld, mc.renderEngine);

        mc.entityRenderer.updateRenderer();
        mc.renderGlobal.updateClouds();
        mc.theWorld.doVoidFogParticles(MathHelper.floor_double(mc.renderViewEntity.posX), MathHelper.floor_double(mc.renderViewEntity.posY), MathHelper.floor_double(mc.renderViewEntity.posZ));
        mc.effectRenderer.updateEffects();

        glViewport(0, 0, mc.displayWidth, mc.displayHeight);
        glBindTexture(GL11.GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glClearColor(1.0f, 0.0f, 0.0f, 0.5f);
        glClear(GL11.GL_COLOR_BUFFER_BIT);


        long nanoTime = ernanotime(entityRenderer);
        int fps = mc.gameSettings.limitFramerate;
        if (mc.isFramerateLimitBelowMax()) {
            entityRenderer.renderWorld(partialTicks, nanoTime + 1000000000 / fps);
        } else {
            entityRenderer.renderWorld(partialTicks, 0L);
        }

        glEnable(GL11.GL_TEXTURE_2D);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glViewport(0, 0, widthBackup, heightBackup);
        glLoadIdentity();

        ((EntityRendererProxy) entityRenderer)._sc$setFovModifierHand(fovHandBackup);
        ((EntityRendererProxy) entityRenderer)._sc$setFovModifierHandPrev(fovHandPrevBackup);

        settings.thirdPersonView = thirdPersonBackup;
        settings.hideGUI = hideGuiBackup;

        mc.displayHeight = heightBackup;
        mc.displayWidth = widthBackup;
        mc.renderGlobal = renderGlobalBackup;
        mc.renderViewEntity = viewportBackup;
        mc.theWorld = worldBackup;
        mc.effectRenderer = effectRendererBackup;

        RenderManager.instance.set(worldBackup);
        mc.entityRenderer.updateRenderer();
    }


    private static long ernanotime(EntityRenderer er) {
        return ReflectionHelper.getPrivateValue(EntityRenderer.class, er, "renderEndNanoTime");
    }

    private static int genTexture(int width, int height) {
        int id = glGenTextures();
        if (id == 0) {
            throw new RuntimeException("failed to generate new GL texture"); // 0 is default texture
        }
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        return id;
    }

    private static List<WorldViewImpl> getViews(Integer dimension) {
        return views.computeIfAbsent(dimension, x -> new ArrayList<>());
    }

    private WorldViewImpl(int texture, int frameBuffer, int renderBuffer, int width, int height, int dimension, double x, double y, double z, float pitch, float yaw) {
        this.texture = texture;
        this.frameBuffer = frameBuffer;
        this.renderBuffer = renderBuffer;
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
        disposeNoListRemove();
        views.get(dimension).remove(this);
    }

    private void disposeNoListRemove() {
        if (isDisposed()) {
            glDeleteFramebuffers(frameBuffer);
            glDeleteRenderbuffers(renderBuffer);

            glDeleteTextures(texture);
            texture = 0;
        }
    }

    @Override
    public boolean isDisposed() {
        return texture == 0;
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
    public RenderMode getRenderMode() {
        return RenderMode.CONTINUOUS;
    }

    @Override
    public void requestRender() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void requestRender(Consumer<? super WorldView> callback) {
        throw new UnsupportedOperationException();
    }

    static final class OnDemandRenderingView extends WorldViewImpl {

        private boolean requested = true;
        private Consumer<? super WorldView> renderCallback;

        private OnDemandRenderingView(int texture, int frameBuffer, int renderBuffer, int width, int height, int dimension, double x, double y, double z, float pitch, float yaw) {
            super(texture, frameBuffer, renderBuffer, width, height, dimension, x, y, z, pitch, yaw);
        }

        @Override
        public RenderMode getRenderMode() {
            return RenderMode.ON_DEMAND;
        }

        @Override
        void render() {
            if (requested) {
                forceRender();
                if (renderCallback != null) {
                    renderCallback.accept(this);
                }
                requested = false;
                renderCallback = null;
            }
        }

        @Override
        public void requestRender() {
            requested = true;
        }

        @Override
        public void requestRender(Consumer<? super WorldView> callback) {
            requested = true;
            if (renderCallback == null) {
                renderCallback = callback;
            } else {
                Consumer<? super WorldView> oldCb = this.renderCallback;
                this.renderCallback = view -> {
                    callback.accept(view);
                    oldCb.accept(view);
                };
            }
        }
    }

    static final class ViewWithFPS extends WorldViewImpl {

        private final long nanosBetweenFrame;
        private       long lastFrame;

        private ViewWithFPS(int texture, int frameBuffer, int renderBuffer, int width, int height, int dimension, double x, double y, double z, float pitch, float yaw, int fps, TimeUnit unit) {
            super(texture, frameBuffer, renderBuffer, width, height, dimension, x, y, z, pitch, yaw);
            this.nanosBetweenFrame = unit.toNanos(1) / fps;
            lastFrame = 0;
        }

        @Override
        void render() {
            long now = System.nanoTime();
            if ((now - lastFrame) >= nanosBetweenFrame) {
                lastFrame = now;
                forceRender();
            }
        }
    }
}
