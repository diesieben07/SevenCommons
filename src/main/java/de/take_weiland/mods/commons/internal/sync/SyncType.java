package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.EntityProxy;
import de.take_weiland.mods.commons.internal.SyncedEntityProperties;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static de.take_weiland.mods.commons.asm.MCPNames.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public enum SyncType {

	TILE_ENTITY {
		@Override
		public void addSyncCall(ClassNode clazz, CodePiece call, boolean isSuperSynced) {
			addOrOverride(clazz, MCPNames.method(M_UPDATE_ENTITY), Type.getMethodDescriptor(VOID_TYPE), call);
		}

		@Override
		public ASMCondition checkServer(ClassNode clazz) {
			String owner = getInternalName(TileEntity.class);
			String name = MCPNames.field(F_WORLD_OBJ_TILEENTITY);
			String desc = Type.getDescriptor(World.class);
			return isServer(CodePieces.getField(owner, name, desc, CodePieces.getThis()));
		}

		@Override
		public void writeObject(Object object, MCDataOutput out) {
			TileEntity te = (TileEntity) object;
			out.writeInt(te.xCoord);
			out.writeByte(te.yCoord);
			out.writeInt(te.zCoord);
		}

		@Override
		public Object readObject(EntityPlayer player, MCDataInputStream in) {
			return player.worldObj.getBlockTileEntity(in.readInt(), in.readUnsignedByte(), in.readInt());
		}

		@Override
		public void sendPacket(Object object, SimplePacket packet) {
			packet.sendToAllTracking((TileEntity) object);
		}
	},
	ENTITY {
		@Override
		public void addSyncCall(ClassNode clazz, CodePiece call, boolean isSuperSynced) {
			addOrOverride(clazz, MCPNames.method(M_ON_UPDATE), Type.getMethodDescriptor(VOID_TYPE), call);
		}

		@Override
		public void writeObject(Object object, MCDataOutput out) {
			out.writeInt(((Entity) object).entityId);
		}

		@Override
		public Object readObject(EntityPlayer player, MCDataInputStream in) {
			return player.worldObj.getEntityByID(in.readInt());
		}

		@Override
		public ASMCondition checkServer(ClassNode clazz) {
			String owner = getInternalName(Entity.class);
			String name = MCPNames.field(F_WORLD_OBJ_ENTITY);
			String desc = getDescriptor(World.class);
			return isServer(CodePieces.getField(owner, name, desc, CodePieces.getThis()));
		}

		@Override
		public void sendPacket(Object object, SimplePacket packet) {
			packet.sendToAllTracking((Entity) object);
		}
	},
	CONTAINER {
		@Override
		public void addSyncCall(ClassNode clazz, CodePiece call, boolean isSuperSynced) {
			addOrOverride(clazz, MCPNames.method(M_DETECT_AND_SEND_CHANGES), Type.getMethodDescriptor(VOID_TYPE), call);
		}

		@Override
		public void writeObject(Object object, MCDataOutput out) {
			out.writeByte(((Container) object).windowId);
		}

		@Override
		public Object readObject(EntityPlayer player, MCDataInputStream in) {
			Container container = player.openContainer;
			return container.windowId == in.readByte() ? container : null;
		}

		@Override
		public ASMCondition checkServer(ClassNode clazz) {
			return null;
		}

		@Override
		public void sendPacket(Object object, SimplePacket packet) {
			packet.sendToViewing((Container) object);
		}
	},
	ENTITY_PROPS {
		@Override
		public void addSyncCall(ClassNode clazz, CodePiece call, boolean isSuperSynced) {
			if (!isSuperSynced) {
				String name = SyncedEntityProperties.TICK;
				String desc = Type.getMethodDescriptor(VOID_TYPE);
				MethodNode method = new MethodNode(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
				clazz.methods.add(method);

				call.appendTo(method.instructions);
				method.instructions.add(new InsnNode(RETURN));
			}
		}

		@Override
		public void initialTransform(ClassNode clazz, boolean isSuperSynced) {
			if (!isSuperSynced) {
				addProperty(clazz, "_sc$syncedprops$o", SyncedEntityProperties.GET_OWNER, SyncedEntityProperties.SET_OWNER, getObjectType("net/minecraft/entity/Entity"));
				addProperty(clazz, "_sc$syncedprops$i", SyncedEntityProperties.GET_IDX, SyncedEntityProperties.SET_IDX, Type.INT_TYPE);
				addProperty(clazz, "_sc$syncedprops$n", SyncedEntityProperties.GET_NAME, SyncedEntityProperties.SET_NAME, getType(String.class));

				clazz.interfaces.add(SyncedEntityProperties.CLASS_NAME);
			}
		}

		private void addProperty(ClassNode clazz, String fieldName, String getterName, String setterName, Type type) {
			FieldNode field = new FieldNode(ACC_PRIVATE | ACC_FINAL, fieldName, type.getDescriptor(), null, null);
			clazz.fields.add(field);

			MethodNode getter = new MethodNode(ACC_PUBLIC | ACC_FINAL, getterName, Type.getMethodDescriptor(type), null, null);
			clazz.methods.add(getter);
			CodePieces.getField(clazz, field, CodePieces.getThis()).appendTo(getter.instructions);
			getter.instructions.add(new InsnNode(type.getOpcode(IRETURN)));

			MethodNode setter = new MethodNode(ACC_PUBLIC | ACC_FINAL, setterName, Type.getMethodDescriptor(VOID_TYPE, type), null, null);
			clazz.methods.add(setter);
			CodePieces.setField(clazz, field, CodePieces.getThis(), CodePieces.of(new VarInsnNode(type.getOpcode(ILOAD), 1))).appendTo(setter.instructions);
			setter.instructions.add(new InsnNode(RETURN));
		}

		@Override
		public void writeObject(Object object, MCDataOutput out) {
			SyncedEntityProperties props = (SyncedEntityProperties) object;
			out.writeInt(props._sc$syncprops$owner().entityId);
			out.writeInt(props._sc$syncprops$index());
		}

		@Override
		public void sendPacket(Object object, SimplePacket packet) {
			packet.sendToAllAssociated(((SyncedEntityProperties) object)._sc$syncprops$owner());
		}

		@Override
		public Object readObject(EntityPlayer player, MCDataInputStream in) {
			int entityId = in.readInt();
			int propsId = in.readInt();

			Entity entity = player.worldObj.getEntityByID(entityId);
			if (entity == null) {
				return null;
			}
			List<SyncedEntityProperties> props = ((EntityProxy) entity)._sc$getSyncedProps();
			if (props == null) {
				return null;
			}
			return JavaUtils.get(props, propsId);
		}

		@Override
		public ASMCondition checkServer(ClassNode clazz) {
			return null;
		}
	};

	public boolean requiresLevelCheck() {
		return this != ENTITY_PROPS;
	}

	public void initialTransform(ClassNode clazz, boolean isSuperSynced) { }

	public abstract void addSyncCall(ClassNode clazz, CodePiece call, boolean isSuperSynced);

	public abstract void writeObject(Object object, MCDataOutput out);

	public abstract void sendPacket(Object object, SimplePacket packet);

	public abstract Object readObject(EntityPlayer player, MCDataInputStream in);

	public abstract ASMCondition checkServer(ClassNode clazz);

	static void addOrOverride(ClassNode clazz, String name, String desc, CodePiece call) {
		MethodNode method = ASMUtils.findMethod(clazz, name, desc);
		if (method == null) {
			method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
			clazz.methods.add(method);

			CodePieces.invokeSuper(clazz, name, getReturnType(desc)).appendTo(method.instructions);
			method.instructions.add(new InsnNode(RETURN));
		} else {
			method.localVariables.clear();
		}
		call.prependTo(method.instructions);
	}

	static ASMCondition isServer(CodePiece world) {
		String owner = getInternalName(World.class);
		String name = MCPNames.field(F_IS_REMOTE);
		String desc = Type.BOOLEAN_TYPE.getDescriptor();
		return ASMCondition.isFalse(CodePieces.getField(owner, name, desc, world));
	}

}
