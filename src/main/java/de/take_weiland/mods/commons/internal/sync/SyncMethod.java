package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.EntityProxy;
import de.take_weiland.mods.commons.internal.SyncedEntityProperties;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.net.SimplePacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.MCPNames.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * @author diesieben07
 */
public enum SyncMethod {

	TILE_ENTITY {
		@Override
		public void writeData(MCDataOutputStream stream, Object object) {
			TileEntity te = (TileEntity) object;
			stream.writeCoords(te.xCoord, te.yCoord, te.zCoord);
		}

		@Override
		public Object readData(MCDataInputStream in, EntityPlayer player) {
			int x = in.readInt();
			int y = in.readUnsignedByte();
			int z = in.readInt();
			return player.worldObj.getBlockTileEntity(x, y, z);
		}

		@Override
		public void addDoSyncCall(ClassNode clazz, CodePiece code) {
			addOrOverride(clazz, M_UPDATE_ENTITY, code);
		}

		@Override
		public void sendPacket(SimplePacket packet, Object object) {
			packet.sendToAllTracking((TileEntity) object);
		}
	},
	ENTITY {
		@Override
		public void writeData(MCDataOutputStream stream, Object object) {
			stream.writeInt(((Entity) object).entityId);
		}

		@Override
		public Object readData(MCDataInputStream in, EntityPlayer player) {
			return player.worldObj.getEntityByID(in.readInt());
		}

		@Override
		public void addDoSyncCall(ClassNode clazz, CodePiece code) {
			addOrOverride(clazz, M_ON_UPDATE, code);
		}

		@Override
		public void sendPacket(SimplePacket packet, Object object) {
			packet.sendToAllTracking((Entity) object);
		}
	},
	CONTAINER {
		@Override
		public void writeData(MCDataOutputStream stream, Object object) {
			stream.writeByte(((Container) object).windowId);
		}

		@Override
		public Object readData(MCDataInputStream in, EntityPlayer player) {
			Container c = player.openContainer;
			if (c.windowId == in.readInt()) {
				return c;
			}
			return null;
		}

		@Override
		public void addDoSyncCall(ClassNode clazz, CodePiece code) {
			addOrOverride(clazz, M_DETECT_AND_SEND_CHANGES, code);
		}

		@Override
		public void sendPacket(SimplePacket packet, Object object) {
			packet.sendToViewing((Container) object);
		}
	},
	ENTITY_PROPS {
		@Override
		public void writeData(MCDataOutputStream stream, Object object) {
			SyncedEntityProperties props = (SyncedEntityProperties) object;
			Entity entity = props._sc$syncprops$owner();
			stream.writeInt(entity.entityId);
			stream.writeVarInt(props._sc$syncprops$index());
		}

		@Override
		public Object readData(MCDataInputStream in, EntityPlayer player) {
			int entityId = in.readInt();
			int propsIdx = in.readVarInt();
			Entity entity = player.worldObj.getEntityByID(entityId);
			if (entity != null) {
				return ((EntityProxy) entity)._sc$getSyncedEntityProperties().get(propsIdx);
			}
			return null;
		}

		@Override
		public void addDoSyncCall(ClassNode clazz, CodePiece code) {
			String name = SyncedEntityProperties.TICK;
			String desc = Type.getMethodDescriptor(VOID_TYPE);
			MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
			clazz.methods.add(method);

			code.appendTo(method.instructions);
			method.instructions.add(new InsnNode(RETURN));
		}

		@Override
		public void postTransform(ClassNode clazz, boolean isSuperSynced) {
			if (isSuperSynced) {
				return;
			}
			addProperty(clazz, "_sc$syncprops$idx", INT_TYPE, SyncedEntityProperties.GET_IDX, SyncedEntityProperties.SET_IDX);
			addProperty(clazz, "_sc$syncprops$owner", Type.getObjectType("net/minecraft/entity/Entity"), SyncedEntityProperties.GET_OWNER, SyncedEntityProperties.SET_OWNER);
			addProperty(clazz, "_sc$syncprops$name", Type.getType(String.class), SyncedEntityProperties.GET_NAME, SyncedEntityProperties.SET_NAME);
			clazz.interfaces.add(SyncedEntityProperties.CLASS_NAME);
		}

		private void addProperty(ClassNode clazz, String fieldName, Type fieldType, String getter, String setter) {
			FieldNode field = new FieldNode(ACC_PRIVATE | ACC_TRANSIENT, fieldName, fieldType.getDescriptor(), null, null);
			clazz.fields.add(field);

			String desc = Type.getMethodDescriptor(fieldType);
			MethodNode method = new MethodNode(ACC_PUBLIC | ACC_FINAL, getter, desc, null, null);
			clazz.methods.add(method);
			InsnList insns = method.instructions;
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
			insns.add(new InsnNode(fieldType.getOpcode(IRETURN)));

			desc = Type.getMethodDescriptor(VOID_TYPE, fieldType);
			method = new MethodNode(ACC_PUBLIC | ACC_FINAL, setter, desc, null, null);
			clazz.methods.add(method);
			insns = method.instructions;

			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new VarInsnNode(fieldType.getOpcode(ILOAD), 1));
			insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name, field.desc));
			insns.add(new InsnNode(RETURN));
		}

		@Override
		public void sendPacket(SimplePacket packet, Object object) {
			packet.sendToAllAssociated(((SyncedEntityProperties) object)._sc$syncprops$owner());
		}
	};

	public abstract void writeData(MCDataOutputStream stream, Object object);
	public abstract Object readData(MCDataInputStream in, EntityPlayer player);
	public abstract void addDoSyncCall(ClassNode clazz, CodePiece code);
	public void postTransform(ClassNode clazz, boolean isSuperSynced) { }

	static void addOrOverride(ClassNode clazz, String srgName, CodePiece code) {
		MethodNode method = ASMUtils.findMinecraftMethod(clazz, srgName);
		if (method == null) {
			String name = MCPNames.method(srgName);
			String desc = Type.getMethodDescriptor(VOID_TYPE);
			method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
			CodePieces.invokeSuper(clazz, method).appendTo(method.instructions);
			method.instructions.add(new InsnNode(RETURN));
			clazz.methods.add(method);
		}
		code.prependTo(method.instructions);
	}

	public abstract void sendPacket(SimplePacket packet, Object object);
}
