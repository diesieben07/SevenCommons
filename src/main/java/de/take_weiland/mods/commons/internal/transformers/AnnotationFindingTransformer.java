package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Synced;
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
public class AnnotationFindingTransformer extends AbstractASMTransformer {

	private static final String toNbtDesc = Type.getDescriptor(ToNbt.class);
	private static final String syncedDesc = Type.getDescriptor(Synced.class);

	@Override
	public boolean transform(ClassNode clazz) {
		boolean synced = false;
		boolean nbt = false;
		FieldNode[] fields = clazz.fields.toArray(new FieldNode[clazz.fields.size()]);
		MethodNode[] methods = clazz.methods.toArray(new MethodNode[clazz.methods.size()]);

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
					return true; // Warning: if more tests are added for methods, change this to a break!
				}
			}
		}

		methods:
		for (MethodNode method : methods) {
			Iterable<AnnotationNode> anns = JavaUtils.concatNullable(method.visibleAnnotations, method.invisibleAnnotations);
			for (AnnotationNode ann : anns) {
				if (!synced && ann.desc.equals(syncedDesc)) {
					if (SyncingTransformer.transform(clazz)) {
						return true;
					}
				}
			}
		}
		return nbt || synced;
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/");
	}
}
