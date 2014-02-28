package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Synced;
import de.take_weiland.mods.commons.trait.TraitMethod;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Combined class for all transformers that search for annotations.
 * Saves on iterating the fields/methods of each class over and over again
 * @author diesieben07
 */
public class AnalyzingTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		boolean synced = false;
		boolean nbt = false;
		boolean trait = false;
		FieldNode[] fields = clazz.fields.toArray(new FieldNode[clazz.fields.size()]);
		MethodNode[] methods = clazz.methods.toArray(new MethodNode[clazz.methods.size()]);
		for (FieldNode field : fields) {
			if (!nbt && ASMUtils.hasAnnotation(field, ToNbt.class)) {
				NBTTransformer.transform(clazz);
				nbt = true;
			}
			if (!synced && ASMUtils.hasAnnotation(field, Synced.class)) {
				SyncingTransformer.transform(clazz);
				synced = true;
			}
			if (synced && nbt) {
				break;
			}
		}
		for (MethodNode method : methods) {
			if (!trait && ASMUtils.hasAnnotation(method, TraitMethod.class)) {
				HasTraitTransformer.transform(clazz);
				trait = true;
			}
			if (!synced && ASMUtils.hasAnnotation(method, Synced.class)) {
				SyncingTransformer.transform(clazz);
				synced = true;
			}
			if (synced && trait) {
				return;
			}
		}
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/");
	}
}
