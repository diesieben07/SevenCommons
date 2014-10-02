package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.InvokeDynamic;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.IRETURN;

/**
 * @author diesieben07
 */
public class InvokeDynamicTransformer extends AbstractAnalyzingTransformer {
	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		boolean transformed = false;

		for (MethodNode method : clazz.methods) {
			if (ASMUtils.hasAnnotation(method, InvokeDynamic.CLASS_NAME)) {
				doTransform(clazz, method);
				transformed = true;
			}
		}
		return transformed;
	}

	private static void doTransform(ClassNode clazz, MethodNode method) {
		AnnotationNode annotation = ASMUtils.getAnnotation(method, InvokeDynamic.CLASS_NAME);
		String name = ASMUtils.getAnnotationProperty(annotation, InvokeDynamic.NAME);
		String bsOwner = ASMUtils.internalName(ASMUtils.<String>getAnnotationProperty(annotation, InvokeDynamic.BS_OWNER));
		String bsName = ASMUtils.getAnnotationProperty(annotation, InvokeDynamic.BS_NAME);
		Object[] args = parseArgs(clazz, method, ASMUtils.<List<AnnotationNode>>getAnnotationProperty(annotation, InvokeDynamic.BS_ARGS));

		CodePiece invoke = CodePieces.invokeDynamic(name, method.desc, CodePieces.allArgs(method.desc, (method.access & ACC_STATIC) != 0))
				.withBootstrap(bsOwner, bsName, args);
		method.instructions.clear();
		invoke.appendTo(method.instructions);
		method.instructions.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(IRETURN)));
	}

	private static Object[] parseArgs(ClassNode clazz, MethodNode method, List<AnnotationNode> argAnns) {
		if (argAnns == null) {
			return ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
		Object[] args = new Object[argAnns.size()];
		for (int i = 0; i < args.length; i++) {
			AnnotationNode ann = argAnns.get(i);
			switch (ASMUtils.<String>getAnnotationProperty(ann, "type")) {
				case InvokeDynamic.TYPE_STRING:
					args[i] = ASMUtils.getAnnotationProperty(ann, InvokeDynamic.STRING_VALUE);
					break;
				case InvokeDynamic.TYPE_INT:
					args[i] = ASMUtils.getAnnotationProperty(ann, InvokeDynamic.INT_VALUE);
					break;
				case InvokeDynamic.TYPE_LONG:
					args[i] = ASMUtils.getAnnotationProperty(ann, InvokeDynamic.LONG_VALUE);
					break;
				case InvokeDynamic.TYPE_FLOAT:
					args[i] = ASMUtils.getAnnotationProperty(ann, InvokeDynamic.FLOAT_VALUE);
					break;
				case InvokeDynamic.TYPE_DOUBLE:
					args[i] = ASMUtils.getAnnotationProperty(ann, InvokeDynamic.DOUBLE_VALUE);
					break;
				case InvokeDynamic.BOOLEAN_VALUE:
					args[i] = ASMUtils.getAnnotationProperty(ann, InvokeDynamic.BOOLEAN_VALUE);
					break;
			}
			if (args[i] == null) {
				throw new RuntimeException("Missing Bootstrap arg at index " + i + " in method " + method.name + " in class " + clazz.name);
			}
		}
		return args;
	}

}
