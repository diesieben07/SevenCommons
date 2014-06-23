package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.SyncType;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;
import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getObjectType;

/**
 * @author diesieben07
 */
class SyncHandler {

	private List<ClassNode> syncedSupers;

	final ClassNode clazz;
	final SyncType type;
	final Map<Type, SyncGroup> groups = Maps.newHashMap();
	final Map<Type, Syncer> syncers = Maps.newHashMap();

	private final List<SyncedElement> elements = Lists.newArrayList();

	SyncHandler(ClassNode clazz, ClassInfo classInfo) {
		this.clazz = clazz;
		this.type = determineType(classInfo);
	}

	private SyncType determineType(ClassInfo classInfo) {
		SyncType type = null;
		if (ClassInfo.of(Entity.class).isAssignableFrom(classInfo)) {
			type = SyncType.ENTITY;
		} else if (ClassInfo.of(TileEntity.class).isAssignableFrom(classInfo)) {
			type = SyncType.TILE_ENTITY;
		} else if (ClassInfo.of(Container.class).isAssignableFrom(classInfo)) {
			type = SyncType.CONTAINER;
		}

		if (ClassInfo.of(IExtendedEntityProperties.class).isAssignableFrom(classInfo)) {
			if (type == null) {
				type = SyncType.ENTITY_PROPS;
			} else {
				throw new IllegalStateException("Cannot sync on IExtendedEntityProperties class which extends Entity, TileEntity or Container!");
			}
		}

		if (type == null) {
			throw new IllegalStateException(String.format("Can't sync class %s", clazz.name));
		}
		return type;
	}

	void transform() {
		List<ASMVariable> variables = ASMVariables.allWith(clazz, Sync.class, CodePieces.getThis());
		for (int i = 0, variablesSize = variables.size(); i < variablesSize; i++) {
			ASMVariable variable = variables.get(i);
			AnnotationNode annotation = variable.getterAnnotation(Sync.class);

			Syncer syncer = findSyncer(variable, annotation);
			SyncGroup group = findGroup(annotation);

			SyncedElement element = new SyncedElement(i, variable, syncer, group);
			group.addElement(element);
			elements.add(element);
		}

		MethodNode writeIdx = makeWriteIdx(variables.size());
		MethodNode readIdx = makeReadIdx(variables.size());

		for (SyncGroup group : groups.values()) {
			group.createSendMethod(writeIdx);
		}
		createReadMethod(readIdx);
	}

	private MethodNode makeWriteIdx(int count) {
		String desc = ASMUtils.getMethodDescriptor(void.class, PacketBuilder.class, int.class);
		MethodNode method = new MethodNode(ACC_PROTECTED, "_sc$sync$writeIdx", desc, null, null);
		clazz.methods.add(method);
		InsnList insns = method.instructions;

		Class<?> cls = chooseIdxSize(count);
		String methodName = "write" + StringUtils.capitalize(cls.getName());

		String methodDesc = ASMUtils.getMethodDescriptor(PacketBuilder.class, int.class);
		CodePieces.invoke(INVOKEINTERFACE, getInternalName(PacketBuilder.class), methodName, methodDesc,
				CodePieces.of(new VarInsnNode(ALOAD, 1)),
				CodePieces.of(new VarInsnNode(ILOAD, 2)))
				.append(new InsnNode(POP))
				.appendTo(insns);

		insns.add(new InsnNode(RETURN));

		return method;
	}

	private MethodNode makeReadIdx(int count) {
		String desc = ASMUtils.getMethodDescriptor(int.class, PacketBuilder.class);
		MethodNode method = new MethodNode(ACC_PROTECTED, "_sc$sync$readIdx", desc, null, null);
		clazz.methods.add(method);
		InsnList insns = method.instructions;

		Class<?> cls = chooseIdxSize(count);
		String methodName = "read" + StringUtils.capitalize(cls.getName());
		String methodDesc = ASMUtils.getMethodDescriptor(cls);

		CodePieces.invoke(INVOKEINTERFACE, getInternalName(PacketBuilder.class), methodName, methodDesc,
				CodePieces.of(new VarInsnNode(ALOAD, 1)))
				.appendTo(insns);

		insns.add(new InsnNode(IRETURN));
		return method;
	}

	private Class<?> chooseIdxSize(int size) {
		if (size <= Byte.MAX_VALUE) {
			return byte.class;
		} else if (size <= Short.MAX_VALUE) {
			return short.class;
		} else {
			throw new IllegalStateException("Too many Elements to sync!");
		}
	}

	private MethodNode createReadMethod(MethodNode readIdx) {
		String name = "_sc$sync$read";
		String desc = ASMUtils.getMethodDescriptor(int.class, DataBuf.class);
		MethodNode method = new MethodNode(ACC_PROTECTED, name, desc, null, null);
		clazz.methods.add(method);
		InsnList insns = method.instructions;

		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();
		insns.add(start);

		final int $this = 0;
		final int $buf = 1;
		final int $idx = 2;

		method.localVariables.add(new LocalVariableNode("this", getObjectType(clazz.name).getDescriptor(), null, start, end, $this));
		method.localVariables.add(new LocalVariableNode("buf", getDescriptor(DataBuf.class), null, start, end, $buf));
		method.localVariables.add(new LocalVariableNode("idx", getDescriptor(int.class), null, start, end, $idx));

		CodePiece getBuf = CodePieces.of(new VarInsnNode(ALOAD, $buf));
		CodePiece getIdx = CodePieces.of(new VarInsnNode(ILOAD, $idx));

		CodePieces.invoke(clazz, readIdx, CodePieces.getThis(), getBuf)
				.append(new VarInsnNode(ISTORE, $idx))
				.appendTo(insns);

		SwitchBuilder sb = new SwitchBuilder();
		for (SyncedElement element : elements) {
			sb.add(element.index, element.variable.set(element.syncer.read(element.companion.get(), getBuf)));
			if (!ASMUtils.isPrimitive(element.variable.getType())) {
				sb.add(-(element.index + 1), element.variable.set(CodePieces.constantNull()));
			}
		}

		sb._default(CodePieces.instantiate(IllegalStateException.class).append(new InsnNode(ATHROW)));

		sb.build(getIdx).appendTo(insns);

		getIdx.append(new InsnNode(IRETURN)).appendTo(insns);

		insns.add(end);
		return method;
	}

	private SyncGroup findGroup(AnnotationNode annotation) {
		Type ptType = ASMUtils.getAnnotationProperty(annotation, "target");
		SyncGroup group = groups.get(ptType);
		if (group == null) {
			group = new SyncGroup(this, ptType, createPacketTarget(ptType));
			groups.put(ptType, group);
		}
		return group;
	}

	private ASMPacketTarget createPacketTarget(Type type) {
		if (type == null) {
			return DefaultPacketTarget.instance();
		} else {
			return new CustomPacketTarget(type, obtainInstance(type));
		}
	}

	private Syncer findSyncer(ASMVariable variable, AnnotationNode annotation) {
		Type syncerType = ASMUtils.getAnnotationProperty(annotation, "syncer");
		Syncer syncer;
		if (syncerType == null) {
			syncer = Syncer.forType(variable.getType());
		} else {
			syncer = syncers.get(syncerType);
			if (syncer == null) {
				CodePiece syncerInstance = obtainInstance(syncerType);
				syncer = new CustomSyncer(syncerInstance, variable.getType());
				syncers.put(syncerType, syncer);
			}
		}
		return syncer;
	}

	private CodePiece obtainInstance(Type type) {
		CodePiece result = tryFindInSuper(type);
		if (result != null) {
			return result;
		}

		ClassNode targetClazz = ASMUtils.getThinClassNode(type.getInternalName());

		boolean canBeStatic = false;

		CodePiece instanceLoader = CodePieces.obtainInstance(clazz, targetClazz,
				new Type[] { Type.getObjectType(clazz.name) },
				CodePieces.getThis());

		if (instanceLoader == null) {
			instanceLoader = CodePieces.obtainInstance(clazz, targetClazz,
					new Type[] { Type.getType(Object.class) },
					CodePieces.getThis());

			if (instanceLoader == null) {
				instanceLoader = CodePieces.obtainInstance(clazz, targetClazz);
				canBeStatic = true;
				if (instanceLoader == null) {
					throw new IllegalStateException(String.format("Failed to obtain instance of %s for @Synced class %s", type.getInternalName(), clazz.name));
				}
			}
		}

		String name = instanceFieldName(type);
		String desc = type.getDescriptor();
		int access = ACC_PROTECTED | ACC_TRANSIENT | (canBeStatic ? ACC_STATIC : 0);
		FieldNode field = new FieldNode(access, name, desc, null, null);
		clazz.fields.add(field);

		if (canBeStatic) {
			ASMUtils.initializeStatic(clazz, CodePieces.setField(clazz, field, instanceLoader));
			return CodePieces.getField(clazz, field);
		} else {
			ASMUtils.initialize(clazz, CodePieces.setField(clazz, field, CodePieces.getThis(), instanceLoader));
			return CodePieces.getField(clazz, field, CodePieces.getThis());
		}
	}

	private CodePiece tryFindInSuper(Type t) {
		String name = instanceFieldName(t);
		List<ClassNode> supers = syncedSupers();
		for (ClassNode clazz : supers) {
			for (FieldNode field : clazz.fields) {
				if (field.name.equals(name)) {
					checkState(field.desc.equals(t.getDescriptor()));
					if ((field.access & ACC_STATIC) == ACC_STATIC) {
						return CodePieces.getField(clazz, field);
					} else {
						return CodePieces.getField(clazz, field, CodePieces.getThis());
					}
				}
			}
		}
		return null;
	}

	private List<ClassNode> syncedSupers() {
		if (syncedSupers == null) {
			ImmutableList.Builder<ClassNode> b = ImmutableList.builder();
			if (!"java/lang/Object".equals(clazz.superName)) {
				ClassNode current = clazz;
				do {
					current = ASMUtils.getThinClassNode(current.superName);
					if (ASMUtils.hasAnnotationOnAnything(current, Sync.class)) {
						b.add(current);
					}
				} while (!"java/lang/Object".equals(current.superName));
			}

			syncedSupers = b.build();
		}
		return syncedSupers;
	}

	private static String instanceFieldName(Type t) {
		checkArgument(!ASMUtils.isPrimitive(t), "no instance field for primitives");
		return "_sc$instanceCache$" + t.getInternalName().replace('/', '_');
	}

}
