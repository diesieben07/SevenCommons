package de.take_weiland.mods.commons.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.reflect.Getter;
import de.take_weiland.mods.commons.reflect.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.entity.EntityTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;

import java.util.List;
import java.util.Map;

/**
 * Accessor interface for various private fields and methods around the Minecraft code.<br />
 * If the field/method to access is static, the instance parameter is ignored, null can be passed.<br />
 * @see de.take_weiland.mods.commons.reflect.SCReflection
 */
public interface SCReflector {

	public static final SCReflector instance = SCReflection.createAccessor(SCReflector.class);

	/**
	 * For cleaner code use {@link de.take_weiland.mods.commons.nbt.NBT#asList(net.minecraft.nbt.NBTTagList)}
	 */
	@Unsafe
	@Getter(field = "field_74747_a", srg = true)
	<T extends NBTBase> List<T> getWrappedList(NBTTagList list);

	@Unsafe
	@Getter(field = "field_74784_a", srg = true)
	Map<String, NBTBase> getWrappedMap(NBTTagCompound nbt);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = "field_78507_R", srg = true)
	float getFovHand(EntityRenderer e);

	@SideOnly(Side.CLIENT)
	@Setter(field = "field_78507_R", srg = true)
	void setFovHand(EntityRenderer e, float fov);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = "field_78506_S", srg = true)
	float getFovHandPrev(EntityRenderer e);
	
	@SideOnly(Side.CLIENT)
	@Setter(field = "field_78506_S", srg = true)
	void setFovHandPrev(EntityRenderer e, float fovPrev);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = "field_71428_T", srg = true)
	Timer getTimer(Minecraft mc);
	
	@Getter(field = "field_72794_c", srg = true)
	IntHashMap getTrackerMap(EntityTracker tracker);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = "field_110585_a", srg = true)
	Map<ResourceLocation, TextureObject> getTexturesMap(TextureManager manager);
	
	@Unsafe
	@Getter(field = "field_73291_a", srg = true)
	Map<Class<? extends Packet>, Integer> getClassToIdMap(Packet dummy);

	@SideOnly(Side.CLIENT)
	@Getter(field = "field_73819_m", srg = true)
	boolean isEnabled(GuiTextField textField);

	@SideOnly(Side.CLIENT)
	@Getter(field = "field_73824_r", srg = true)
	int getDisabledColor(GuiTextField textField);

	@SideOnly(Side.CLIENT)
	@Getter(field = "field_73825_q", srg = true)
	int getEnabledColor(GuiTextField textField);

	@SideOnly(Side.CLIENT)
	@Getter(field = "field_73821_k", srg = true)
	boolean canLooseFocus(GuiTextField textField);

	@SideOnly(Side.CLIENT)
	@Getter(field = "field_73887_h", srg = true)
	List<GuiButton> getButtonList(GuiScreen screen);

	@Getter(field = "field_77991_e", srg = true)
	int getRawDamage(ItemStack stack);

	@Getter(field = "field_71968_b", srg = true)
	String getRawUnlocalizedName(Block block);

}
