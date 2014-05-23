package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @author diesieben07
 */
public final class ASMVariables {

	public static ASMVariable local(MethodNode method, int idx) {
		return of(method.localVariables.get(idx));
	}

	public static ASMVariable of(LocalVariableNode var) {
		return new ASMLocalVariable(var);
	}

	public static ASMVariable of(ClassNode clazz, FieldNode field) {
		checkStatic(field.access, "field");
		return new ASMField(clazz, field, null);
	}

	public static ASMVariable of(ClassNode clazz, FieldNode field, CodePiece instance) {
		checkNotStatic(field.access, "field");
		return new ASMField(clazz, field, checkNotNull(instance, "instance"));
	}

	public static ASMVariable of(ClassNode clazz, MethodNode getter) {
		checkStatic(getter.access, "getter");
		return new GetterSetterPair(checkNotNull(clazz, "clazz"), getter, null, null);
	}

	public static ASMVariable of(ClassNode clazz, MethodNode getter, CodePiece instance) {
		checkNotStatic(getter.access, "getter");
		return new GetterSetterPair(checkNotNull(clazz, "clazz"), getter, null, checkNotNull(instance, "instance"));
	}

	public static ASMVariable of(ClassNode clazz, MethodNode getter, MethodNode setter) {
		checkStatic(getter.access, "getter");
		checkStatic(setter.access, "setter");
		return new GetterSetterPair(checkNotNull(clazz, "clazz"), getter, setter, null);
	}

	public static ASMVariable of(ClassNode clazz, MethodNode getter, MethodNode setter, CodePiece instance) {
		checkNotStatic(getter.access, "getter");
		checkNotStatic(setter.access, "setter");
		return new GetterSetterPair(checkNotNull(clazz, "clazz"), getter, setter, checkNotNull(instance, "instance"));
	}

	public static Collection<ASMVariable> allOf(ClassNode clazz, CodePiece instance) {

	}

	public static Collection<ASMVariable> allWith(ClassNode clazz, CodePiece instance, Class<? extends Annotation> annotation) {
		return ImmutableList.copyOf(Iterators.concat(
				Iterators.transform(
						ASMUtils.methodsWith(clazz, annotation).iterator(),
						getterAsVariable(clazz, defaultSetterProvider(clazz))),
				Iterators.transform(
						ASMUtils.fieldsWith(clazz, annotation).iterator(),
						fieldAsVariable(clazz))
		));
	}

	private static Function<MethodNode, MethodNode> defaultSetterProvider(final ClassNode clazz) {
		return new Function<MethodNode, MethodNode>() {
			@Override
			public MethodNode apply(MethodNode getter) {
				return ASMUtils.findSetter(clazz, getter);
			}
		};
	}

	private static Function<FieldNode, ASMVariable> fieldAsVariable(final ClassNode clazz) {
		return new Function<FieldNode, ASMVariable>() {
			@Override
			public ASMVariable apply(FieldNode field) {
				return ASMVariables.of(clazz, field);
			}
		};
	}

	private static Function<MethodNode, ASMVariable> getterAsVariable(final ClassNode clazz, final CodePiece instance, final Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		return new Function<MethodNode, ASMVariable>() {
			@Override
			public ASMVariable apply(MethodNode method) {
				return ASMVariables.of(clazz, method, setterProvider.apply(method), instance);
			}
		};
	}

	private static void checkStatic(int mod, String type) {
		if ((mod & ACC_STATIC) == 0) {
			throw new IllegalArgumentException(type + " must be static");
		}
	}

	private static void checkNotStatic(int mod, String type) {
		if ((mod & ACC_STATIC) == ACC_STATIC) {
			throw new IllegalArgumentException(type + " must not be static");
		}
	}

	private ASMVariables() { }

}
