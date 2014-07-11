package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.sync.InstanceCreator;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class SyncingTransformer implements ASMClassTransformer {
	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (!ASMUtils.hasAnnotationOnAnything(clazz, Sync.class)) {
			return false;
		}

		new Impl(clazz).transform();

		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("cpw/mods/fml/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("org/apache/")
				&& !internalName.startsWith("de/take_weiland/mods/commons/sync");
	}

	static class Impl {

		private final ClassNode clazz;
		private final Map<Type, ASMVariable> adapterCache = Maps.newHashMap();
		private int adapterCount;

		Impl(ClassNode clazz) {
			this.clazz = clazz;
		}

		void transform() {
			List<ASMVariable> syncedVars = ASMVariables.allWith(clazz, Sync.class, CodePieces.getThis());
			List<VarSyncer> syncers = createSyncers(syncedVars);
		}

		private List<VarSyncer> createSyncers(List<ASMVariable> vars) {
			List<VarSyncer> syncers = Lists.newArrayListWithCapacity(vars.size());
			for (ASMVariable var : vars) {
				if (ASMUtils.isPrimitive(var.getType())) {
					syncers.add(new VarSyncer.ForPrimitive(var, createCompanion(var)));
				} else {
					syncers.add(new VarSyncer.ForObject(var, getAdapter(var)));
				}
			}
			return syncers;
		}

		private ASMVariable createCompanion(ASMVariable var) {
			String name = var.name() + "_sc$companion";
			FieldNode field = new FieldNode(ACC_PRIVATE | ACC_TRANSIENT, name, var.getType().getDescriptor(), null, null);
			clazz.fields.add(field);
			return ASMVariables.of(clazz, field, CodePieces.getThis());
		}

		private ASMVariable getAdapter(ASMVariable var) {
			Type type = var.getType();
			ASMVariable adapterCreator = adapterCache.get(type);
			if (adapterCreator == null) {
				String name = "_sc$adapterCreator" + adapterCount++;
				String desc = Type.getDescriptor(InstanceCreator.class);
				FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_TRANSIENT, name, desc, null, null);
				clazz.fields.add(field);

				// TODO: support Collections
				String owner = SyncAdapter.CLASS_NAME;
				name = SyncAdapter.CREATOR;
				desc = ASMUtils.getMethodDescriptor(InstanceCreator.class, Class.class);
				CodePiece fetchAdapter = CodePieces.invokeStatic(owner, name, desc, CodePieces.constant(type));
				CodePiece storeAdapter = CodePieces.setField(clazz, field, fetchAdapter);
				ASMUtils.initializeStatic(clazz, storeAdapter);

				adapterCreator = ASMVariables.of(clazz, field);
				adapterCache.put(type, adapterCreator);
			}

			String name = var.name() + "_sc$syncAdapter";
			String desc = Type.getDescriptor(SyncAdapter.class);
			FieldNode adapterInstance = new FieldNode(ACC_PRIVATE | ACC_FINAL | ACC_TRANSIENT, name, desc, null, null);
			clazz.fields.add(adapterInstance);

			ASMVariable adapterInstanceVar = ASMVariables.of(clazz, adapterInstance, CodePieces.getThis());

			String owner = InstanceCreator.CLASS_NAME;
			name = InstanceCreator.NEW_INSTANCE;
			desc = ASMUtils.getMethodDescriptor(Object.class);
			CodePiece newAdapter = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, adapterCreator.get());
			CodePiece adapterInit = adapterInstanceVar.set(CodePieces.castTo(SyncAdapter.class, newAdapter));
			ASMUtils.initialize(clazz, adapterInit);
			return adapterInstanceVar;
		}
	}
}
