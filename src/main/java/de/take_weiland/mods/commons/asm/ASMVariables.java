package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

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

	public static ASMVariable of(ClassNode clazz, FieldNode field, CodePiece instance) {
		if (instance == null) {
			checkStatic(field.access, "field");
		} else {
			checkNotStatic(field.access, "field");
		}
		return new ASMField(clazz, field, instance);
	}

	public static ASMVariable of(ClassNode clazz, FieldNode field) {
		return of(clazz, field, null);
	}

	public static ASMVariable of(ClassNode clazz, MethodNode getter, MethodNode setter, CodePiece instance) {
		if (instance == null) {
			checkNotStatic(getter.access, "getter");
			if (setter != null) checkNotStatic(setter.access, "setter");
		} else {
			checkStatic(getter.access, "getter");
			if (setter != null) checkStatic(setter.access, "setter");
		}
		return new GetterSetterPair(checkNotNull(clazz, "clazz"), getter, setter, instance);
	}

	public static ASMVariable of(ClassNode clazz, MethodNode getter) {
		return of(clazz, getter, null, null);
	}

	public static ASMVariable of(ClassNode clazz, MethodNode getter, CodePiece instance) {
		return of(clazz, getter, null, instance);
	}

	public static ASMVariable of(ClassNode clazz, MethodNode getter, MethodNode setter) {
		return of(clazz, getter, setter, null);
	}

	public static List<ASMVariable> allOf(ClassNode clazz, CodePiece instance) {
		return allOf(clazz, instance, defaultSetterProvider(clazz));
	}

	public static List<ASMVariable> allOf(ClassNode clazz, CodePiece instance, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		boolean useStatic = instance == null;
		Predicate<FieldNode> fieldFilter = useStatic ? isFieldStatic() : Predicates.not(isFieldStatic());
		Predicate<MethodNode> methodFilter = Predicates.and(
				useStatic ? isMethodStatic() : Predicates.not(isMethodStatic()),
				isGetter()
			);
		return ImmutableList.copyOf(Iterators.concat(
			fieldsAsVariables(Iterators.filter(clazz.fields.iterator(), fieldFilter), clazz, instance),
			methodsAsVariables(Iterators.filter(clazz.methods.iterator(), methodFilter), clazz, instance, setterProvider)
		));
	}

	public static List<ASMVariable> allWith(ClassNode clazz, Class<? extends Annotation> annotation, CodePiece instance, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		return ImmutableList.copyOf(Iterators.concat(
				methodsAsVariables(ASMUtils.methodsWith(clazz, annotation).iterator(), clazz, instance, setterProvider),
				fieldsAsVariables(ASMUtils.fieldsWith(clazz, annotation).iterator(), clazz, instance)));
	}

	public static List<ASMVariable> allWith(ClassNode clazz, Class<? extends Annotation> annotation) {
		return allWith(clazz, annotation, null);
	}

	public static List<ASMVariable> allWith(ClassNode clazz, Class<? extends Annotation> annotation, CodePiece instance) {
		return allWith(clazz, annotation, instance, defaultSetterProvider(clazz));
	}

	private static Predicate<FieldNode> isFieldStatic() {
		return new Predicate<FieldNode>() {

			@Override
			public boolean apply(FieldNode field) {
				return (field.access & ACC_STATIC) == ACC_STATIC;
			}
		};
	}

	private static Predicate<MethodNode> isMethodStatic() {
		return new Predicate<MethodNode>() {

			@Override
			public boolean apply(MethodNode method) {
				return (method.access & ACC_STATIC) == ACC_STATIC;
			}
		};
	}

	private static Predicate<MethodNode> isGetter() {
		return new Predicate<MethodNode>() {
			@Override
			public boolean apply(MethodNode method) {
				return Type.getReturnType(method.desc).getSort() != Type.VOID && Type.getArgumentTypes(method.desc).length == 0;
			}
		};
	}


	private static Function<MethodNode, MethodNode> defaultSetterProvider(final ClassNode clazz) {
		return new Function<MethodNode, MethodNode>() {
			@Override
			public MethodNode apply(MethodNode getter) {
				return ASMUtils.findSetter(clazz, getter);
			}
		};
	}

	private static Iterator<ASMVariable> fieldsAsVariables(Iterator<FieldNode> fields, ClassNode clazz, CodePiece instance) {
		return Iterators.transform(fields, fieldAsVariable(clazz, instance));
	}

	private static Iterator<ASMVariable> methodsAsVariables(Iterator<MethodNode> methods, ClassNode clazz, CodePiece instance, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		return Iterators.transform(methods, getterAsVariable(clazz, instance, setterProvider));
	}

	private static Function<FieldNode, ASMVariable> fieldAsVariable(final ClassNode clazz, final CodePiece instance) {
		return new Function<FieldNode, ASMVariable>() {
			@Override
			public ASMVariable apply(FieldNode field) {
				return ASMVariables.of(clazz, field, instance);
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
