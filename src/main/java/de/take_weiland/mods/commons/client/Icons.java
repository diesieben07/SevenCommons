package de.take_weiland.mods.commons.client;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.templates.Metadata;
import de.take_weiland.mods.commons.templates.Metadata.BlockMeta;
import de.take_weiland.mods.commons.templates.Metadata.ItemMeta;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;

import static net.minecraft.block.SCBlockAccessor.getIconName;
import static net.minecraft.item.SCItemAccessor.getIconName;

@SideOnly(Side.CLIENT)
public final class Icons {

	private Icons() { }
	
	private static final Function<Metadata, String> META_NAME = new Function<Metadata, String>() {

		@Override
		public String apply(Metadata meta) {
			return meta.unlocalizedName();
		}
		
	};
	
	// Block versions
	
	public static Icon register(Block block, IconRegister register, String subName) {
		return registerSingle(register, getIconName(block), subName);
	}
	
	public static Icon[] registerMulti(Block block, IconRegister register, String... subNames) {
		return registerMulti(register, getIconName(block), subNames);
	}
	
	public static Icon register(IconRegister register, BlockMeta meta) {
		return register(meta.getBlock(), register, meta.unlocalizedName());
	}
	
	public static Icon[] registerMulti(IconRegister register, BlockMeta... metas) {
		return registerMetas(register, getIconName(metas[0].getBlock()), metas);
	}
	
	public static Icon register(IconRegister register, BlockMeta meta, String subName) {
		return register(meta.getBlock(), register, getName(meta.unlocalizedName(), subName));
	}
	
	public static Icon[] registerMulti(IconRegister register, BlockMeta[] metas, String subName) {
		return registerMetasWithSub(register, getIconName(metas[0].getBlock()), metas, subName); 
	}
	
	// Item versions
	public static Icon register(Item item, IconRegister register, String subName) {
		return registerSingle(register, getIconName(item), subName);
	}
	
	public static Icon[] registerMulti(Item item, IconRegister register, String... subNames) {
		return registerMulti(register, getIconName(item), subNames);
	}
	
	public static Icon register(IconRegister register, ItemMeta meta) {
		return register(meta.getItem(), register, meta.unlocalizedName());
	}
	
	public static Icon[] registerMulti(IconRegister register, ItemMeta... metas) {
		return registerMetas(register, getIconName(metas[0].getItem()), metas);
	}
	
	public static Icon register(IconRegister register, ItemMeta meta, String subName) {
		return register(meta.getItem(), register, getName(meta.unlocalizedName(), subName));
	}
	
	public static Icon[] registerMulti(IconRegister register, ItemMeta[] metas, String subName) {
		return registerMetasWithSub(register, getIconName(metas[0].getItem()), metas, subName); 
	}
	
	// private helpers
	private static Icon[] registerMetas(IconRegister register, String rootName, Metadata... metas) {
		return registerMulti(register, rootName, JavaUtils.transform(metas, new String[metas.length], META_NAME));
	}
	
	private static Icon[] registerMetasWithSub(IconRegister register, String rootName, Metadata[] metas, final String subName) {
		return registerMulti(register, rootName, JavaUtils.transform(metas, new String[metas.length], Functions.compose(new Function<String, String>() {

			@Override
			public String apply(String metaName) {
				return getName(metaName, subName);
			}
			
		}, META_NAME)));
	}
	
	private static Icon registerSingle(IconRegister register, String rootName, String subName) {
		return register.registerIcon(getName(rootName, subName));
	}

	private static Icon[] registerMulti(IconRegister register, String rootName, String... subNames) {
		int len = subNames.length;
		Icon[] icons = new Icon[len];
		
		for (int i = 0; i < len; ++i) {
			icons[i] = register.registerIcon(getName(rootName, subNames[i]));
		}
		
		return icons;
	}
	
	static String getName(String rootName, String subName) {
		return rootName + "_" + subName;
	}
	
}
