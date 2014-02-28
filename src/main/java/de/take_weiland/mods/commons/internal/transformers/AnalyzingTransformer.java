package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Synced;
import de.take_weiland.mods.commons.trait.TraitMethod;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Combined class for all transformers that search for annotations.
 * Saves on iterating the fields/methods of each class over and over again
 * @author diesieben07
 */
public class AnalyzingTransformer extends AbstractASMTransformer {

	private static final String toNbtDesc = Type.getDescriptor(ToNbt.class);
	private static final String syncedDesc = Type.getDescriptor(Synced.class);
	private static final String traitMethodDesc = Type.getDescriptor(TraitMethod.class);

	@Override
	public void transform(ClassNode clazz) {
		boolean synced = false;
		boolean nbt = false;
		boolean trait = false;
		FieldNode[] fields = clazz.fields.toArray(new FieldNode[clazz.fields.size()]);
		MethodNode[] methods = clazz.methods.toArray(new MethodNode[clazz.methods.size()]);

		fields:
		for (FieldNode field : fields) {
			Iterable<AnnotationNode> anns = JavaUtils.concatNullable(field.visibleAnnotations, field.invisibleAnnotations);
			for (AnnotationNode ann : anns) {
				if (!nbt && ann.desc.equals(toNbtDesc)) {
					NBTTransformer.transform(clazz);
					nbt = true;
					continue;
				}
				if (!synced && ann.desc.equals(syncedDesc)) {
					SyncingTransformer.transform(clazz);
					synced = true;
				}
				if (nbt && synced) {
					break fields;
				}
			}
		}

		methods:
		for (MethodNode method : methods) {
			Iterable<AnnotationNode> anns = JavaUtils.concatNullable(method.visibleAnnotations, method.invisibleAnnotations);
			for (AnnotationNode ann : anns) {
				if (!trait && ann.desc.equals(traitMethodDesc)) {
					HasTraitTransformer.transform(clazz);
					trait = true;
					continue;
				}
				if (!synced && ann.desc.equals(syncedDesc)) {
					SyncingTransformer.transform(clazz);
					synced = true;
				}
				if (synced && trait) {
					break methods;
				}
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
