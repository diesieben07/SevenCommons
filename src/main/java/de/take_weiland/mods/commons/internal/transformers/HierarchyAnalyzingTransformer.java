package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.metadata.HasMetadata;
import de.take_weiland.mods.commons.net.ModPacket;
import org.objectweb.asm.tree.ClassNode;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;

/**
 * @author diesieben07
 */
public class HierarchyAnalyzingTransformer extends AbstractASMTransformer {

	private static final ClassInfo modPacketCI = getClassInfo(ModPacket.class);
	private static final ClassInfo hasMetadataCI = getClassInfo(HasMetadata.class);

	@Override
	public boolean transform(ClassNode clazz) {
		ClassInfo classInfo = getClassInfo(clazz);
		if (modPacketCI.isAssignableFrom(classInfo)) {
			PacketTransformer.transform(clazz);
			return true;
		} else if (hasMetadataCI.isAssignableFrom(classInfo)) {
			HasMetadataTransformer.transform(clazz, classInfo);
			return true;
		}
		return false;
	}

	@Override
	public boolean transforms(String className) {
		return !className.startsWith("net/minecraft/")
				&& !className.startsWith("net/minecraftforge/")
				&& !className.startsWith("cpw/mods/fml/");
	}

}
