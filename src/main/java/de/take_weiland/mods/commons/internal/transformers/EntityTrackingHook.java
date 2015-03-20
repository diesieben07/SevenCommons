package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.Packet41EntityEffect;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
* @author diesieben07
*/
public final class EntityTrackingHook extends MethodVisitor {

    private enum Stage {
        START,
        FOUND_NEW,
        FOUND_CSTR,
        FOUND_SEND_PACKET,
        FOUND_GOTO,
        DONE
    }

    private final String packetEntityEff = Type.getInternalName(Packet41EntityEffect.class);
    private final String netServerHandler = Type.getInternalName(NetServerHandler.class);
    private Stage stage = Stage.START;

    public EntityTrackingHook(MethodVisitor mv) {
        super(ASM4, mv);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (stage == Stage.START && opcode == NEW && type.equals(packetEntityEff)) {
            stage = Stage.FOUND_NEW;
        }
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (stage == Stage.FOUND_NEW
                && opcode == INVOKESPECIAL
                && owner.equals(packetEntityEff)
                && name.equals("<init>")) {
            stage = Stage.FOUND_CSTR;
        } else if (stage == Stage.FOUND_CSTR
                && opcode == INVOKEVIRTUAL
                && owner.equals(netServerHandler)
                && name.equals(MCPNames.method(MCPNames.M_SEND_PACKET_TO_PLAYER))) {
            stage = Stage.FOUND_SEND_PACKET;
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (stage == Stage.FOUND_SEND_PACKET && opcode == GOTO) {
            stage = Stage.FOUND_GOTO;
        } else if (stage == Stage.FOUND_GOTO && opcode == GOTO) {
            stage = Stage.DONE;
            super.visitVarInsn(ALOAD, 1); // load first arg, the player
            super.visitVarInsn(ALOAD, 0); // load this

            Type entityType = Type.getObjectType("net/minecraft/entity/Entity");
            Type entityPlayerType = Type.getObjectType("net/minecraft/entity/player/EntityPlayer");

            String owner = "net/minecraft/entity/EntityTrackerEntry";
            String name = MCPNames.field(MCPNames.F_MY_ENTITY);
            String desc = entityType.getDescriptor();
            super.visitFieldInsn(GETFIELD, owner, name, desc);

            String hookClazz = Type.getInternalName(ASMHooks.class);
            name = ASMHooks.ON_START_TRACKING;
            desc = Type.getMethodDescriptor(Type.VOID_TYPE, entityPlayerType, entityType);
            super.visitMethodInsn(INVOKESTATIC, hookClazz, name, desc);
        }
        super.visitJumpInsn(opcode, label);
    }
}
