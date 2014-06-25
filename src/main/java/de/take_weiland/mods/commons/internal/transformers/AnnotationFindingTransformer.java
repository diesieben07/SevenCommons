package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.nbt.ToNbt;
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

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		// TODO
		boolean nbt = false;
		ListIterator<MethodNode> methodsIt = clazz.methods.listIterator();
		for (FieldNode field : clazz.fields) {
			Iterable<AnnotationNode> anns = JavaUtils.concatNullable(field.visibleAnnotations, field.invisibleAnnotations);
			for (AnnotationNode ann : anns) {
				if (!nbt && ann.desc.equals(toNbtDesc)) {
					NBTTransformer.transform(clazz, classInfo, methodsIt);
					nbt = true;
				}
			}
		}

		return nbt;
	}

}
