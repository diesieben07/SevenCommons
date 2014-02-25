package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.trait.Trait;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author diesieben07
 */
public class TraitTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		AnnotationNode ann;
		if ((ann = ASMUtils.getAnnotation(clazz, Trait.class)) == null) {
			return;
		}

		ClassNode impl = ASMUtils.getThinClassNode(((Type) ann.values.get(1)).getInternalName());
		for (FieldNode field : impl.fields) {
			if ((field.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
				continue;
			}

			Type fieldType = Type.getType(field.desc);
			String name = TraitUtil.getGetterName(clazz.name, field.name);
			String desc = Type.getMethodDescriptor(fieldType);
			clazz.methods.add(new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, name, desc, null, null));

			name = TraitUtil.getSetterName(clazz.name, field.name);
			desc = Type.getMethodDescriptor(Type.VOID_TYPE, fieldType);
			clazz.methods.add(new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, name, desc, null, null));
		}

	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/");
	}
}
