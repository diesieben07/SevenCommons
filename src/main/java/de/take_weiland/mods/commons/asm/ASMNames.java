package de.take_weiland.mods.commons.asm;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public final class ASMNames {

	private static final Map<String, String> fields;
	private static final Map<String, String> methods;

	private static final String SYS_PROP = "sevencommons.mappingsFile";

	static {
		if (!ASMUtils.useMcpNames()) {
			methods = fields = null;
		} else {
			String mappingsDir;
			String prop = System.getProperty(SYS_PROP);
			if (prop == null) {
				mappingsDir = "./../build/unpacked/mappings/";
			} else {
				mappingsDir = prop;
			}

			fields = readMappings(new File(mappingsDir + "fields.csv"));
			methods = readMappings(new File(mappingsDir + "methods.csv"));
		}
	}

	public static String field(String srg) {
		if (!ASMUtils.useMcpNames()) {
			return srg;
		} else {
			return fields.get(srg);
		}
	}

	public static String method(String srg) {
		if (!ASMUtils.useMcpNames()) {
			return srg;
		} else {
			return methods.get(srg);
		}
	}

	private static Map<String, String> readMappings(File file) {
		if (!file.isFile()) {
			throw new RuntimeException("Couldn't find MCP mappings. Please provide system property " + SYS_PROP);
		}
		try {
			System.out.println("reading mappings from " + file);
			return Files.readLines(file, Charsets.UTF_8, new MCPFileParser());
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read MCP mappings", e);
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
			} catch(NoSuchElementException e) {
				throw new IOException("Invalid Mappings file!", e);
			}

			return true;
		}

		@Override
		public Map<String, String> getResult() {
			return ImmutableMap.copyOf(map);
		}
	}

	public static final String M_SPAWN_BABY_SRG = "func_75388_i";
	public static final String M_SPAWN_BABY_MCP = "spawnBaby";
	
	public static final String F_TARGET_MATE_SRG = "field_75391_e";
	public static final String F_TARGET_MATE_MCP = "targetMate";
	
	public static final String F_THE_ANIMAL_SRG = "field_75390_d";
	public static final String F_THE_ANIMAL_MCP = "theAnimal";
	
	public static final String M_CLONE_PLAYER_SRG = "func_71049_a";
	public static final String M_CLONE_PLAYER_MCP = "clonePlayer";
	
	public static final String M_CONVERT_TO_VILLAGER_SRG = "func_82232_p";
	public static final String M_CONVERT_TO_VILLAGER_MCP = "convertToVillager";
	
	public static final String M_SET_WORLD_AND_RESOLUTION_SRG = "func_73872_a";
	public static final String M_SET_WORLD_AND_RESOLUTION_MCP = "setWorldAndResolution";

	public static final String F_BUTTON_LIST_MCP = "buttonList";
	
	public static final String F_TAG_LIST_SRG = "field_74747_a";
	public static final String F_TAG_LIST_MCP = "tagList";

	public static final String F_TAG_MAP_MCP = "tagMap";

	public static final String F_FOV_MODIFIER_HAND_PREV_MCP = "fovModifierHandPrev";

	public static final String F_FOV_MODIFIER_HAND_MCP = "fovModifierHand";

	public static final String F_TRACKED_ENTITY_IDS_MCP = "trackedEntityIDs";

	public static final String F_MAP_TEXTURE_OBJECTS_MCP = "mapTextureObjects";
	
	public static final String F_MY_ENTITY_MCP = "myEntity";
	public static final String F_MY_ENTITY_SRG = "field_73132_a";
	
	public static final String M_TRY_START_WATCHING_THIS_MCP = "tryStartWachingThis";
	public static final String M_TRY_START_WATCHING_THIS_SRG = "func_73117_b";
	
	public static final String M_ON_UPDATE_MCP = "onUpdate";
	public static final String M_ON_UPDATE_SRG = "func_70071_h_";
	
	public static final String M_UPDATE_ENTITY_MCP = "updateEntity";
	public static final String M_UPDATE_ENTITY_SRG = "func_70316_g";
	
	public static final String M_DETECT_AND_SEND_CHANGES_MCP = "detectAndSendChanges";
	public static final String M_DETECT_AND_SEND_CHANGES_SRG = "func_75142_b";
	
	public static final String F_IS_REMOTE_MCP = "isRemote";
	public static final String F_IS_REMOTE_SRG = "field_72995_K";
	
	public static final String F_WORLD_OBJ_TILEENTITY_MCP = "worldObj";
	public static final String F_WORLD_OBJ_TILEENTITY_SRG = "field_70331_k";
	
	public static final String F_WORLD_OBJ_ENTITY_MCP = "worldObj";
	public static final String F_WORLD_OBJ_ENTITY_SRG = "field_70170_p";
	
	public static final String F_TIMER_MCP = "timer";

	public static final String F_PACKET_CLASS_TO_ID_MAP_MCP = "packetClassToIdMap";

	public static final String F_IS_ENABLED_MCP = "isEnabled";

	public static final String F_DISABLED_COLOR_MCP = "disabledColor";

	public static final String F_ENABLED_COLOR_MCP = "enabledColor";

	public static final String F_CAN_LOOSE_FOCUS_MCP = "canLoseFocus";

	public static final String M_SEND_PACKET_TO_PLAYER_MCP = "sendPacketToPlayer";
	public static final String M_SEND_PACKET_TO_PLAYER_SRG = "func_72567_b";

	public static final String M_WRITE_ENTITY_TO_NBT_MCP = "writeEntityToNBT";
	public static final String M_WRITE_ENTITY_TO_NBT_SRG = "func_70014_b";

	public static final String M_READ_ENTITY_FROM_NBT_MCP = "readEntityFromNBT";
	public static final String M_READ_ENTITY_FROM_NBT_SRG = "func_70037_a";

	public static final String M_WRITE_TO_NBT_TILEENTITY_MCP = "writeToNBT";
	public static final String M_WRITE_TO_NBT_TILEENTITY_SRG = "func_70310_b";

	public static final String M_READ_FROM_NBT_TILEENTITY_MCP = "readFromNBT";
	public static final String M_READ_FROM_NBT_TILEENTITY_SRG = "func_70307_a";

	public static final String F_ITEM_DAMAGE_MCP = "itemDamage";

	public static final String M_REGISTER_EXT_PROPS = "registerExtendedProperties";

	public static final String M_READ_PACKET_DATA_MCP = "readPacketData";
	public static final String M_READ_PACKET_DATA_SRG = "func_73267_a";

	public static final String M_WRITE_PACKET_DATA_MCP = "writePacketData";
	public static final String M_WRITE_PACKET_DATA_SRG = "func_73273_a";

	public static final String M_GET_PACKET_SIZE_MCP = "getPacketSize";
	public static final String M_GET_PACKET_SIZE_SRG = "func_73284_a";

	public static final String F_UNLOCALIZED_NAME_BLOCK_MCP = "unlocalizedName";

	private ASMNames() { }
	
}
