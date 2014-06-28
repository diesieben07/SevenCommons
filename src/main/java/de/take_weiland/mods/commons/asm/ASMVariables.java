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
import static org.objectweb.asm.Opcodes.*;

/**
 * <p>Factory class for ASMVariables.</p>
 * @author diesieben07
 */
public final class ASMVariables {

	/**
	 * <p>Creates an {@code ASMVariable} that represents the local variable with the given index in the given method.</p>
	 * @param method the method containing the variable
	 * @param idx the local variable to get
	 * @return an ASMVariable that represents the local variable
	 */
	public static ASMVariable local(MethodNode method, int idx) {
		for (LocalVariableNode variable : method.localVariables) {
			if (variable.index == idx) {
				return of(variable);
			}
		}
		throw new IllegalArgumentException("No such local variable");
	}

	/**
	 * <p>Creates an {@code ASMVariable} that represents the given LocalVariableNode.</p>
	 * @param var the local variable
	 * @return an ASMVariable that represents the local variable
	 */
	public static ASMVariable of(LocalVariableNode var) {
		return new ASMLocalVariable(var);
	}

	/**
	 * <p>Creates an {@code ASMVariable} that represents the given non-static field.</p>
	 * @param clazz the class containing the field
	 * @param field the field
	 * @param instance the instance to get the field from
	 * @return an ASMVariable that represents the field
	 */
	public static ASMVariable of(ClassNode clazz, FieldNode field, CodePiece instance) {
		checkNotStatic(field.access, "field");
		return new ASMField(clazz, field, instance);
	}

	/**
	 * <p>Creates an {@code ASMVariable} that represents the given static field.</p>
	 * @param clazz the class containing the field
	 * @param field the field
	 * @return an ASMVariable that represents the field
	 */
	public static ASMVariable of(ClassNode clazz, FieldNode field) {
		checkStatic(field.access, "field");
		return new ASMField(clazz, field, null);
	}

	/**
	 * <p>Creates an {@code ASMVariable} that represents the given non-static getter and setter.</p>
	 * @param clazz the class containing the getter and setter
	 * @param getter the getter method
	 * @param setter the setter method
	 * @param instance the instance to use to invoke the getter and setter
	 * @return an ASMVariable that represents the getter and setter
	 */
	public static ASMVariable of(ClassNode clazz, MethodNode getter, MethodNode setter, CodePiece instance) {
		checkNotStatic(getter.access, "getter");
		checkNotStatic(setter.access, "setter");
		return new GetterSetterPair(clazz, getter, setter, instance);
	}

	/**
	 * <p>Creates an {@code ASMVariable} that represents the given static getter.</p>
	 * @param clazz the class containing the getter
	 * @param getter the getter method
	 * @return an ASMVariable that represents the getter
	 */
	public static ASMVariable of(ClassNode clazz, MethodNode getter) {
		checkStatic(getter.access, "getter");
		return new GetterSetterPair(clazz, getter, null, null);
	}

	/**
	 * <p>Creates an {@code ASMVariable} that represents the given non-static getter.</p>
	 * @param clazz the class containing the getter
	 * @param getter the getter method
	 * @param instance the instance to use to invoke the getter
	 * @return an ASMVariable that represents the getter
	 */
	public static ASMVariable of(ClassNode clazz, MethodNode getter, CodePiece instance) {
		checkNotStatic(getter.access, "getter");
		return new GetterSetterPair(clazz, getter, null, instance);
	}

	/**
	 * <p>Creates an {@code ASMVariable} that represents the given static getter and setter.</p>
	 * @param clazz the class containing the getter and setter
	 * @param getter the getter method
	 * @param setter the setter method
	 * @return an ASMVariable that represents the getter and setter
	 */
	public static ASMVariable of(ClassNode clazz, MethodNode getter, MethodNode setter) {
		checkStatic(getter.access, "getter");
		checkStatic(setter.access, "setter");
		return new GetterSetterPair(clazz, getter, setter, null);
	}

	/**
	 * <p>Creates a List containing all non-static ASMVariables in the given class.</p>
	 * <p>{@link de.take_weiland.mods.commons.asm.ASMUtils#findSetter(org.objectweb.asm.tree.ClassNode, org.objectweb.asm.tree.MethodNode)} is used
	 * to find the corresponding setter for a getter.</p>
	 * @param clazz the class to scan
	 * @param instance the instance to use for the ASMVariables
	 * @return a List of all non-static ASMVariables
	 */
	public static List<ASMVariable> allOf(ClassNode clazz, CodePiece instance) {
		return allOf0(clazz, checkNotNull(instance), defaultSetterProvider(clazz));
	}

	/**
	 * <p>Creates a List containing all static ASMVariables in the given class.</p>
	 * <p>{@link de.take_weiland.mods.commons.asm.ASMUtils#findSetter(org.objectweb.asm.tree.ClassNode, org.objectweb.asm.tree.MethodNode)} is used
	 * to find the corresponding setter for a getter.</p>
	 * @param clazz the class to scan
	 * @return a List of all static ASMVariables
	 */
	public static List<ASMVariable> allOf(ClassNode clazz) {
		return allOf0(clazz, null, defaultSetterProvider(clazz));
	}

	/**
	 * <p>Creates a List containing all non-static ASMVariables in the given class.</p>
	 * @param clazz the class to scan
	 * @param instance the instance to use for the ASMVariables
	 * @param setterProvider a Function that provides the setter for a given getter, or null if no setter is found
	 * @return a List of all non-static ASMVariables
	 */
	public static List<ASMVariable> allOf(ClassNode clazz, CodePiece instance, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		return allOf0(clazz, checkNotNull(instance), setterProvider);
	}

	/**
	 * <p>Creates a List containing all static ASMVariables in the given class.</p>
	 * @param clazz the class to scan
	 * @param setterProvider a Function that provides the setter for a given getter, or null if no setter is found
	 * @return a List of all static ASMVariables
	 */
	public static List<ASMVariable> allOf(ClassNode clazz, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		return allOf0(clazz, null, setterProvider);
	}

	private static List<ASMVariable> allOf0(ClassNode clazz, CodePiece instance, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
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

	/**
	 * <p>Creates a List containing all static ASMVariables in the given class that have the given annotation.</p>
	 * <p>{@link de.take_weiland.mods.commons.asm.ASMUtils#findSetter(org.objectweb.asm.tree.ClassNode, org.objectweb.asm.tree.MethodNode)} is used
	 * to find the corresponding setter for a getter.</p>
	 * @param clazz the class to scan
	 * @return a List of all static ASMVariables
	 */
	public static List<ASMVariable> allWith(ClassNode clazz, Class<? extends Annotation> annotation) {
		return allWith0(clazz, annotation, null, defaultSetterProvider(clazz));
	}

	/**
	 * <p>Creates a List containing all non-static ASMVariables in the given class that have the given annotation.</p>
	 * <p>{@link de.take_weiland.mods.commons.asm.ASMUtils#findSetter(org.objectweb.asm.tree.ClassNode, org.objectweb.asm.tree.MethodNode)} is used
	 * to find the corresponding setter for a getter.</p>
	 * @param clazz the class to scan
	 * @param instance the instance to use for the ASMVariables
	 * @return a List of all non-static ASMVariables
	 */
	public static List<ASMVariable> allWith(ClassNode clazz, Class<? extends Annotation> annotation, CodePiece instance) {
		return allWith0(clazz, annotation, checkNotNull(instance), defaultSetterProvider(clazz));
	}

	/**
	 * <p>Creates a List containing all static ASMVariables in the given class that have the given annotation.</p>
	 * @param clazz the class to scan
	 * @param setterProvider a Function that provides the setter for a given getter, or null if no setter is found
	 * @return a List of all static ASMVariables
	 */
	public static List<ASMVariable> allWith(ClassNode clazz, Class<? extends Annotation> annotation, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		return allWith0(clazz, annotation, null, setterProvider);
	}

	/**
	 * <p>Creates a List containing all non-static ASMVariables in the given class that have the given annotation.</p>
	 * @param clazz the class to scan
	 * @param instance the instance to use for the ASMVariables
	 * @param setterProvider a Function that provides the setter for a given getter, or null if no setter is found
	 * @return a List of all non-static ASMVariables
	 */
	public static List<ASMVariable> allWith(ClassNode clazz, Class<? extends Annotation> annotation, CodePiece instance, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		return allWith0(clazz, annotation, checkNotNull(instance), setterProvider);
	}

	private static List<ASMVariable> allWith0(ClassNode clazz, Class<? extends Annotation> annotation, CodePiece instance, Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		checkNotNull(setterProvider, "setterProvider");
		return ImmutableList.copyOf(Iterators.concat(
				methodsAsVariables(ASMUtils.methodsWith(clazz, annotation).iterator(), clazz, instance, setterProvider),
				fieldsAsVariables(ASMUtils.fieldsWith(clazz, annotation).iterator(), clazz, instance)));
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
				if (instance == null) {
					return ASMVariables.of(clazz, field);
				} else {
					return ASMVariables.of(clazz, field, instance);
				}
			}
		};
	}

	private static Function<MethodNode, ASMVariable> getterAsVariable(final ClassNode clazz, final CodePiece instance, final Function<? super MethodNode, ? extends MethodNode> setterProvider) {
		return new Function<MethodNode, ASMVariable>() {
			@Override
			public ASMVariable apply(MethodNode method) {
				return new GetterSetterPair(clazz, method, setterProvider.apply(method), instance);
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
