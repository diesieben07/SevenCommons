package de.take_weiland.mods.commons.internal.client.worldview;

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
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.minecraft.client.Minecraft.getMinecraft;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author diesieben07
 */
public class WorldViewImpl implements WorldView {

    private static final Map<Integer, List<WorldViewImpl>> views        = new HashMap<>();
    private static final Map<Integer, WorldClient>         clientWorlds = new HashMap<>();
    public static final  String                            CLASS_NAME   = "de/take_weiland/mods/commons/internal/client/worldview/WorldViewImpl";

    final RenderGlobal rg;
    EntityLivingBase viewport;

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

        glViewport(0, 0, mc.displayWidth, mc.displayHeight);
        glBindTexture(GL11.GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glClearColor(1.0f, 0.0f, 0.0f, 0.5f);
        glClear(GL11.GL_COLOR_BUFFER_BIT);

        doRenderWorld(partialTicks, entityRenderer);

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

    void doRenderWorld(float partialTicks, EntityRenderer entityRenderer) {
        entityRenderer.renderWorld(partialTicks, 0);
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

        this.rg = createRenderGlobal();
    }

    RenderGlobal createRenderGlobal() {
        return new RenderGlobal(getMinecraft());
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
            viewport.setDead();
        }
    }

    private static ByteBuffer screenshotBuf;
    private static IntBuffer  intBuf;

    @Override
    public BufferedImage grabScreenshot() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        int byteCount = width * height * 4;

        if (screenshotBuf == null || screenshotBuf.capacity() < byteCount) {
            screenshotBuf = BufferUtils.createByteBuffer(byteCount);
            intBuf = screenshotBuf.asIntBuffer();
        }
        intBuf.clear();

        glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, intBuf);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        int[] ints = new int[width * height];
        intBuf.rewind();
        intBuf.get(ints);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int row = 0; row < height; row++) {
            img.setRGB(0, height - 1 - row, width, 1, ints, row * width, width);
        }
        return img;
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

        private static final int     MAX_UPDATE_TIME_IDLE    = 8000000; // roughly 120fps
        private static final int     MAX_UPDATE_TIME_PENDING = MAX_UPDATE_TIME_IDLE * 2;
        private static final int     MAX_REQUEST_FRAME_DELAY = 5;
        private              boolean requested               = false;
        private Consumer<? super WorldView> renderCallback;

        private int requestPendingFrames = 0;

        private OnDemandRenderingView(int texture, int frameBuffer, int renderBuffer, int width, int height, int dimension, double x, double y, double z, float pitch, float yaw) {
            super(texture, frameBuffer, renderBuffer, width, height, dimension, x, y, z, pitch, yaw);
        }

        @Override
        RenderGlobal createRenderGlobal() {
            return new FreezableRenderGlobal(getMinecraft());
        }

        @Override
        public RenderMode getRenderMode() {
            return RenderMode.ON_DEMAND;
        }

        @Override
        void render() {
            boolean done;
            long maxEndTime = System.nanoTime() + (requested ? MAX_UPDATE_TIME_PENDING : MAX_UPDATE_TIME_IDLE);
            //noinspection StatementWithEmptyBody
            do {
            } while (!(done = rg.updateRenderers(viewport, false)) && (System.nanoTime() - maxEndTime) < 0);

            if (requested) {
                if (done || requestPendingFrames++ == MAX_REQUEST_FRAME_DELAY) {
                    forceRender();
                    if (renderCallback != null) {
                        renderCallback.accept(this);
                    }
                    requested = false;
                    renderCallback = null;
                }
            }
        }

        @Override
        public void requestRender() {
            requested = true;
            requestPendingFrames = 0;
        }

        @Override
        public void requestRender(Consumer<? super WorldView> callback) {
            requestRender();
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

    static final class FreezableRenderGlobal extends RenderGlobal {

        private       boolean      frozen  = false;
        private final List<Update> updates = new ArrayList<>();

        public FreezableRenderGlobal(Minecraft mc) {
            super(mc);
        }

        void freezeWorld() {
            frozen = true;
        }

        void unFreeze() {
            for (Update update : updates) {
                super.markBlocksForUpdate(update.xStart, update.yStart, update.zStart, update.xEnd, update.yEnd, update.zEnd);
            }
            updates.clear();
            frozen = false;
        }

        @Override
        public void markBlocksForUpdate(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd) {
            if (frozen) {
                updates.add(new Update(xStart, yStart, zStart, xEnd, yEnd, zEnd));
            } else {
                super.markBlocksForUpdate(xStart, yStart, zStart, xEnd, yEnd, zEnd);
            }
        }

        private static final class Update {

            private final int xStart, yStart, zStart, xEnd, yEnd, zEnd;

            Update(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd) {
                this.xStart = xStart;
                this.yStart = yStart;
                this.zStart = zStart;
                this.xEnd = xEnd;
                this.yEnd = yEnd;
                this.zEnd = zEnd;
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
