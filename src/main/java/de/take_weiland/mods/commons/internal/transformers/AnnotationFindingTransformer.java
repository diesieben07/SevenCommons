package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Synced;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

/**
 * Combined class for all transformers that search for annotations.
 * Saves on iterating the fields/methods of each class over and over again
 * @author diesieben07
 */
public class AnnotationFindingTransformer extends AbstractAnalyzingTransformer {

	private static final String toNbtDesc = Type.getDescriptor(ToNbt.class);
	private static final String syncedDesc = Type.getDescriptor(Synced.class);

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		boolean synced = false;
		boolean nbt = false;
		ListIterator<MethodNode> methodsIt = clazz.methods.listIterator();
		ListIterator<FieldNode> fieldIt = clazz.fields.listIterator();
		while (fieldIt.hasNext()) {
			FieldNode field = fieldIt.next();
			Iterable<AnnotationNode> anns = JavaUtils.concatNullable(field.visibleAnnotations, field.invisibleAnnotations);
			for (AnnotationNode ann : anns) {
				if (!nbt && ann.desc.equals(toNbtDesc)) {
					NBTTransformer.transform(clazz, classInfo);
					nbt = true;
					continue;
				}
				if (!synced && ann.desc.equals(syncedDesc)) {
					SyncingTransformer.transform(clazz, classInfo, fieldIt, methodsIt);
					synced = true;
				}
				if (nbt && synced) {
					return true; // Warning: if more tests are added for methods, change this to a break!
				}
			}
		}

		while (methodsIt.hasNext()) {
			MethodNode method = methodsIt.next();
			Iterable<AnnotationNode> anns = JavaUtils.concatNullable(method.visibleAnnotations, method.invisibleAnnotations);
			for (AnnotationNode ann : anns) {
				if (!synced && ann.desc.equals(syncedDesc)) {
					if (SyncingTransformer.transform(clazz, classInfo, fieldIt, methodsIt)) {
						return true;
					}
				}
			}
		}
		return nbt || synced;
	}

}
