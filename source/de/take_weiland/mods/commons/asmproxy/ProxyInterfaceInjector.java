package de.take_weiland.mods.commons.asmproxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableMap;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;

public final class ProxyInterfaceInjector extends SelectiveTransformer {

	private final Map<Class<? extends Annotation>, Injector> handlers = ImmutableMap.of(
			Getter.class, new GetterInjector(),
			Setter.class, new SetterInjector());
	
	@Override
	protected boolean transform(ClassNode clazz) {
		Collection<Class<?>> proxies = ProxyInterfaceRegistry.getProxyInterfaces(ASMUtils.undoInternalName(clazz.name));
		boolean transformed = false;
		
		proxies:
		for (Class<?> proxy : proxies) {
			Method[] proxyMethods = proxy.getDeclaredMethods();
			
			for (Method method : proxyMethods) {
				for (Annotation ann : method.getAnnotations()) {
					if (handlers.containsKey(ann.annotationType())) {
						if (handlers.get(ann.annotationType()).inject(clazz, method)) {
							transformed = true;
						} else {
							continue proxies;
						}
					} else {
						System.err.println("No handler for class " + ann.getClass().getSimpleName());
					}
				}
			}
			clazz.interfaces.add(ASMUtils.makeNameInternal(proxy.getCanonicalName())); // only add the interface if we added all the methods properly
		}
		return transformed;
	}

	@Override
	protected boolean transforms(String className) {
		return ProxyInterfaceRegistry.hasProxyInterface(className);
	}

	static interface Injector {
		
		boolean inject(ClassNode clazz, Method proxyMethod);
		
	}
	
	static class GetterInjector implements Injector {

		@Override
		public boolean inject(ClassNode clazz, Method proxyMethod) {
			Getter ann = proxyMethod.getAnnotation(Getter.class);
			
			if (proxyMethod.getParameterTypes().length != 0) {
				System.err.println("Invalid parameters in @Getter method " + proxyMethod.getName() + " in " + proxyMethod.getDeclaringClass().getSimpleName());
				return false;
			}
			
			String field = ASMUtils.useMcpNames() ? ann.mcpName() : ann.obfName();
			Type expectedType = Type.getType(ASMUtils.getFieldDescriptor(clazz, field));
			
			Type returnType = Type.getReturnType(proxyMethod);
			
			boolean cast = false;
			if (!returnType.equals(expectedType)) {
				System.err.println("!! Warning: Invalid return type in @Getter method " + proxyMethod.getName() + " in " + proxyMethod.getDeclaringClass().getSimpleName() + ". Things may break !!");
				cast = true;
			}
			
			MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, proxyMethod.getName(), Type.getMethodDescriptor(proxyMethod), null, null);
			
			InsnList insns = method.instructions;
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // load this
			
			insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, field, expectedType.getDescriptor())); // get the field
			
			if (cast) {
				insns.add(new TypeInsnNode(Opcodes.CHECKCAST, returnType.getDescriptor()));
			}
			
			insns.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN))); // get the appropriate return type
			
			clazz.methods.add(method);
			
			return true;
		}
		
	}
	
	static class SetterInjector implements Injector {

		@Override
		public boolean inject(ClassNode clazz, Method proxyMethod) {
			Setter ann = proxyMethod.getAnnotation(Setter.class);
			Type returnType = Type.getReturnType(proxyMethod);
			if (!returnType.equals(Type.VOID_TYPE)) {
				System.err.println("Invalid return type in @Setter method " + proxyMethod.getName() + " in " + proxyMethod.getDeclaringClass().getSimpleName());
				return false;
			}
			Class<?>[] parameters = proxyMethod.getParameterTypes();
			if (parameters.length != 1) {
				System.err.println("Invalid parameter count in @Setter method " + proxyMethod.getName() + " in " + proxyMethod.getDeclaringClass().getSimpleName());
				return false;
			}
			
			String field = ASMUtils.useMcpNames() ? ann.mcpName() : ann.obfName();
			Type expectedType = Type.getType(ASMUtils.getFieldDescriptor(clazz, field));
			Type paramType = Type.getType(parameters[0]);
			
			boolean cast = false;
			if (!paramType.equals(expectedType)) {
				System.err.println("!! Warning: Unexpected parameter type in @Setter method " + proxyMethod.getName() + " in " + proxyMethod.getDeclaringClass().getSimpleName() + ". Things might break!");
				System.err.println("Expected: " + expectedType.getClassName() + " but got: " + paramType.getClassName());
				cast = true;
			}
			
			MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, proxyMethod.getName(), Type.getMethodDescriptor(proxyMethod), null, null);
			InsnList insns = method.instructions;
			
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insns.add(new VarInsnNode(paramType.getOpcode(Opcodes.ILOAD), 1));
			
			if (cast) {
				insns.add(new TypeInsnNode(Opcodes.CHECKCAST, expectedType.getDescriptor()));
			}
			
			insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, field, expectedType.getDescriptor()));
			
			insns.add(new InsnNode(Opcodes.RETURN));
			
			clazz.methods.add(method);
			return true;
		}
		
	}
	
}
