package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.reflect.*;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SCReflector {

    SCReflector instance = SCReflection.createAccessor(SCReflector.class);

    @SideOnly(Side.CLIENT)
    @Invoke(method = SRGConstants.M_TRANSLATE_KEY_PRIVATE, srg = true)
    String translate(Locale instance, String key);

    @SideOnly(Side.CLIENT)
    @Getter(target = LanguageManager.class, field = SRGConstants.F_CURRENT_LOCALE, srg = true)
    Locale getCurrentLocale();

    @Getter(field = SRGConstants.F_TAG_LIST, srg = true)
    <T extends NBTBase> List<T> getWrappedList(NBTTagList list);

    @Getter(field = SRGConstants.F_TAG_MAP, srg = true)
    Map<String, NBTBase> getWrappedMap(NBTTagCompound nbt);

    @Getter(field = SRGConstants.F_TRACKED_ENTITY_IDS, srg = true)
    IntHashMap getTrackerMap(EntityTracker tracker);

    @Invoke(method = SRGConstants.M_GET_OR_CREATE_CHUNK_WATCHER, srg = true)
    Object getPlayerInstance(PlayerManager playerManager, int chunkX, int chunkZ, boolean create);

    @Getter(field = SRGConstants.F_PLAYERS_WATCHING_CHUNK, srg = true)
    @OverrideTarget("net.minecraft.server.management.PlayerManager$PlayerInstance")
    List<EntityPlayerMP> getWatchers(Object playerInstance);

    @Invoke(method = "func_151251_a", srg = true)
    @OverrideTarget("net.minecraft.server.management.PlayerManager$PlayerInstance")
    void sendToAllWatchingChunk(Object playerInstance, Packet packet);

    @SideOnly(Side.CLIENT)
    @Getter(field = SRGConstants.F_MAP_TEXTURE_OBJECTS, srg = true)
    Map<ResourceLocation, ITextureObject> getTexturesMap(TextureManager manager);

    @Getter(field = SRGConstants.F_UNLOCALIZED_NAME_BLOCK, srg = true)
    String getRawUnlocalizedName(Block block);

    @Invoke(method = SRGConstants.M_SET_HAS_SUBTYPES, srg = true)
    Item setHasSubtypes(Item item, boolean value);

    @Getter(field = SRGConstants.F_ICON_STRING, srg = true)
    String getRawIconName(Item item);

    @Getter(field = SRGConstants.F_UNLOCALIZED_NAME_ITEM, srg = true)
    String getRawUnlocalizedName(Item item);

    @Getter(field = SRGConstants.F_TEXTURE_NAME_BLOCK, srg = true)
    String getRawIconName(Block block);

    @SideOnly(Side.CLIENT)
    @Invoke(method = SRGConstants.M_GET_ICON_STRING, srg = true)
    String getIconName(Item item);

    @SideOnly(Side.CLIENT)
    @Invoke(method = SRGConstants.M_BLOCK_GET_TEXTURE_NAME, srg = true)
    String getIconName(Block block);

    @SideOnly(Side.CLIENT)
    @Invoke(method = SRGConstants.M_ACTION_PERFORMED, srg = true)
    void actionPerformed(GuiScreen screen, GuiButton button);

    @SideOnly(Side.CLIENT)
    @Getter(field = SRGConstants.F_Z_LEVEL, srg = true)
    float getZLevel(Gui gui);

    @Invoke(method = SRGConstants.M_ADD_SLOT_TO_CONTAINER, srg = true)
    Slot addSlot(Container container, Slot slot);

    @Getter(field = SRGConstants.F_CRAFTERS, srg = true)
    List<ICrafting> getCrafters(Container container);

    @Invoke(method = SRGConstants.M_NBT_WRITE, srg = true)
    void write(NBTBase nbt, DataOutput out) throws IOException;

    @Invoke(method = SRGConstants.M_NBT_LOAD, srg = true)
    void load(NBTBase nbt, DataInput in, int depth, NBTSizeTracker tracker) throws IOException;

    @Invoke(method = SRGConstants.M_NEW_NBT_TAG, target = NBTBase.class)
    NBTBase newNBTTag(byte id);

    @Construct
    String createStringShared(char[] arr, boolean dummy);

    @Invoke(method = "clone")
    Object clone(Object t) throws CloneNotSupportedException;

}
