package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.net.SimplePacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import static de.take_weiland.mods.commons.asm.MCPNames.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public enum SyncType {

	TILE_ENTITY {
		@Override
		public void addSyncCall(ClassNode clazz, CodePiece call) {
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
		public void writeObject(Object object, MCDataOutputStream out) {
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
		public void addSyncCall(ClassNode clazz, CodePiece call) {
			addOrOverride(clazz, MCPNames.method(M_ON_UPDATE), Type.getMethodDescriptor(VOID_TYPE), call);
		}

		@Override
		public void writeObject(Object object, MCDataOutputStream out) {
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
		public void addSyncCall(ClassNode clazz, CodePiece call) {
			addOrOverride(clazz, MCPNames.method(M_DETECT_AND_SEND_CHANGES), Type.getMethodDescriptor(VOID_TYPE), call);
		}

		@Override
		public void writeObject(Object object, MCDataOutputStream out) {
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
	};

	public abstract void addSyncCall(ClassNode clazz, CodePiece call);

	public abstract void writeObject(Object object, MCDataOutputStream out);

	public abstract void sendPacket(Object object, SimplePacket packet);

	public abstract Object readObject(EntityPlayer player, MCDataInputStream in);

	public abstract ASMCondition checkServer(ClassNode clazz);

	static void addOrOverride(ClassNode clazz, String name, String desc, CodePiece call) {
		MethodNode method = ASMUtils.findMethod(clazz, name, desc);
		if (method == null) {
			method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
			clazz.methods.add(method);

			CodePieces.invokeSuper(clazz, method).appendTo(method.instructions);
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
		return ASMCondition.ifFalse(CodePieces.getField(owner, name, desc, world));
	}

}
