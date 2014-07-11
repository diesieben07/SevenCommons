package de.take_weiland.mods.commons.asm;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import de.take_weiland.mods.commons.internal.SevenCommons;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public final class MCPNames {

	private static final Map<String, String> fields;
	private static final Map<String, String> methods;

	private static final String SYS_PROP = "sevencommons.mappingsFile";

	static {
		if (use()) {
			String mappingsDir;
			String prop = System.getProperty(SYS_PROP);
			if (prop == null) {
				mappingsDir = "./../build/unpacked/mappings/";
			} else {
				mappingsDir = prop;
			}

			fields = readMappings(new File(mappingsDir + "fields.csv"));
			methods = readMappings(new File(mappingsDir + "methods.csv"));
		} else {
			methods = fields = null;
		}
	}

	public static boolean use() {
		return SevenCommons.MCP_ENVIRONMENT;
	}

	public static String field(String srg) {
		if (use()) {
			return fields.get(srg);
		} else {
			return srg;
		}
	}

	public static String method(String srg) {
		if (use()) {
			return methods.get(srg);
		} else {
			return srg;
		}
	}

	private static Map<String, String> readMappings(File file) {
		if (!file.isFile()) {
			throw new RuntimeException("Couldn't find MCP mappings. Please provide system property " + SYS_PROP);
		}
		try {
			SevenCommons.LOGGER.fine("Reading SRG->MCP mappings from " + file);
			return Files.readLines(file, Charsets.UTF_8, new MCPFileParser());
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read SRG->MCP mappings", e);
		}
	}

	private static class MCPFileParser implements LineProcessor<Map<String, String>> {

		private static final Splitter splitter = Splitter.on(',').trimResults();
		private final Map<String, String> map = Maps.newHashMap();
		private boolean foundFirst;

		@Override
		public boolean processLine(String line) throws IOException {
			if (!foundFirst) {
				foundFirst = true;
				return true;
			}

			Iterator<String> splitted = splitter.split(line).iterator();
			try {
				String srg = splitted.next();
				String mcp = splitted.next();
				if (!map.containsKey(srg)) {
					map.put(srg, mcp);
				}
			} catch (NoSuchElementException e) {
				throw new IOException("Invalid Mappings file!", e);
			}

			return true;
		}

		@Override
		public Map<String, String> getResult() {
			return ImmutableMap.copyOf(map);
		}
	}

	public static final String CLASS_ENTITY = "net/minecraft/entity/Entity";

	public static final String M_SPAWN_BABY = "func_75388_i";

	public static final String F_TARGET_MATE = "field_75391_e";

	public static final String F_THE_ANIMAL = "field_75390_d";

	public static final String M_CLONE_PLAYER = "func_71049_a";

	public static final String M_CONVERT_TO_VILLAGER = "func_82232_p";

	public static final String M_SET_WORLD_AND_RESOLUTION = "func_73872_a";

	public static final String F_BUTTON_LIST = "field_73887_h";

	public static final String F_TAG_LIST = "field_74747_a";

	public static final String F_TAG_MAP = "field_74784_a";

	public static final String F_FOV_MODIFIER_HAND_PREV = "field_78506_S";

	public static final String F_FOV_MODIFIER_HAND = "field_78507_R";

	public static final String F_TRACKED_ENTITY_IDS = "field_72794_c";

	public static final String F_MAP_TEXTURE_OBJECTS = "field_110585_a";

	public static final String F_MY_ENTITY = "field_73132_a";

	public static final String M_TRY_START_WATCHING_THIS = "func_73117_b";

	public static final String M_ON_UPDATE = "func_70071_h_";

	public static final String M_UPDATE_ENTITY = "func_70316_g";

	public static final String M_DETECT_AND_SEND_CHANGES = "func_75142_b";

	public static final String F_IS_REMOTE = "field_72995_K";

	public static final String F_WORLD_OBJ_TILEENTITY = "field_70331_k";

	public static final String F_WORLD_OBJ_ENTITY = "field_70170_p";

	public static final String F_TIMER = "field_71428_T";

	public static final String F_PACKET_CLASS_TO_ID_MAP = "field_73291_a";

	public static final String F_IS_ENABLED = "field_73819_m";

	public static final String F_DISABLED_COLOR = "field_73824_r";

	public static final String F_ENABLED_COLOR = "field_73825_q";

	public static final String F_CAN_LOOSE_FOCUS = "field_73821_k";

	public static final String M_SEND_PACKET_TO_PLAYER = "func_72567_b";

	public static final String M_REMOVE_ENTITY = "func_72900_e";

	public static final String M_WRITE_ENTITY_TO_NBT = "func_70014_b";

	public static final String M_READ_ENTITY_FROM_NBT = "func_70037_a";

	public static final String M_WRITE_TO_NBT_TILEENTITY = "func_70310_b";

	public static final String M_READ_FROM_NBT_TILEENTITY = "func_70307_a";

	public static final String F_ITEM_DAMAGE = "field_77991_e";

	public static final String M_REGISTER_EXT_PROPS = "registerExtendedProperties";

	public static final String M_READ_PACKET_DATA = "func_73267_a";

	public static final String M_WRITE_PACKET_DATA = "func_73273_a";

	public static final String M_GET_PACKET_SIZE = "func_73284_a";

	public static final String F_UNLOCALIZED_NAME_BLOCK = "field_71968_b";

	public static final String M_SET_HAS_SUBTYPES = "func_77627_a";

	public static final String F_ICON_STRING = "field_111218_cA";

	public static final String F_UNLOCALIZED_NAME_ITEM = "field_77774_bZ";

	public static final String F_TEXTURE_NAME_BLOCK = "field_111026_f";

	public static final String M_ACTION_PERFORMED = "func_73875_a";

	public static final String F_Z_LEVEL = "field_73735_i";

	public static final String M_ADD_SLOT_TO_CONTAINER = "func_75146_a";

	public static final String M_MERGE_ITEM_STACK = "func_75135_a";

	public static final String F_CRAFTERS = "field_75149_d";

	public static final String M_GET_ICON_STRING = "func_111208_A";

	public static final String M_GET_TEXTURE_NAME = "func_111023_E";

	private MCPNames() {
	}

}
