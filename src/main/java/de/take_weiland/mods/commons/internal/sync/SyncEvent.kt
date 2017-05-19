package de.take_weiland.mods.commons.internal.sync

import de.take_weiland.mods.commons.internal.CommonMethodHandles
import de.take_weiland.mods.commons.internal.net.InternalPacket
import de.take_weiland.mods.commons.internal.net.NetworkImpl
import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanion
import de.take_weiland.mods.commons.internal.sync_olds.SyncedObjectProxy
import de.take_weiland.mods.commons.net.MCDataInput
import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.Network
import de.take_weiland.mods.commons.net.ProtocolException
import de.take_weiland.mods.commons.reflect.PropertyAccess
import de.take_weiland.mods.commons.sync.TypeSyncer
import de.take_weiland.mods.commons.util.Entities
import de.take_weiland.mods.commons.util.Players
import de.take_weiland.mods.commons.util.Scheduler
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.Container
import net.minecraft.network.NetworkManager
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos

/**
 * @author diesieben07
 */
abstract class SyncEvent : SyncCompanion.ChangeIterator, InternalPacket, Scheduler.Task {

    private var cursor = 0
    internal var changes = arrayOfNulls<ChangedValue<*>>(3)

    fun add(changedValue: ChangedValue<*>) {
        if (changes.size == cursor) {
            grow()
        }
        changes[cursor++] = changedValue
    }

    internal fun done() {
        cursor = 0
    }

    private fun grow() {
        val currLen = changes.size
        val newArr = arrayOfNulls<ChangedValue<*>>(currLen + 2)
        System.arraycopy(changes, 0, newArr, 0, currLen)
        changes = newArr
    }

    internal abstract fun writeMetaInfoToStream(out: MCDataOutput)

    internal abstract fun getObjectDirect(player: EntityPlayer): Any?

    override fun nextFieldId(): Int {
        if (cursor >= changes.size) {
            return SyncCompanion.FIELD_ID_END
        }
        val change = changes[cursor]
        return change?.fieldId ?: SyncCompanion.FIELD_ID_END
    }

    override fun <T_DATA, T_VAL, T_COM> apply(syncer: TypeSyncer<T_VAL, T_COM, T_DATA>, obj: Any, property: PropertyAccess<T_VAL>, cObj: Any, companion: PropertyAccess<T_COM>) {
        // TODO
        val change = changes[cursor++]
        //        syncer.apply(change.data, obj, property, cObj, companion);
    }

    fun send(obj: Any, player: EntityPlayerMP?) {
        if (player == null) {
            send(obj)
        } else {
            done()
            NetworkImpl.sendPacket(this, player)
        }
    }

    internal abstract fun send(obj: Any)

    @Throws(Exception::class)
    override fun `_sc$internal$writeTo`(out: MCDataOutput) {
        writeMetaInfoToStream(out)

        for (change in changes) {
            if (change == null) {
                break
            }
            out.writeVarInt(change.fieldId)
            change.writeData(out)
        }
        out.writeVarInt(0)
    }

    override fun `_sc$internal$channel`(): String {
        return CHANNEL
    }

    override fun `_sc$internal$expectedSize`(): Int {
        return 32
    }

    override fun `_sc$internal$receiveDirect`(side: Byte, manager: NetworkManager) {
        Scheduler.client().execute(this)
    }

    override fun execute(): Boolean {
        try {
            val obj = getObjectDirect(Players.getClient()) as SyncedObjectProxy
            if (obj != null) {
                val companion = obj.`_sc$getCompanion`()
                companion?.applyChanges(obj, this)
            }
        } catch (ignored: ClassCastException) {
            // the cast might fail, in that case we just ignore silently
        }

        return false
    }

    override fun toString(): String {
        return String.format("SyncEvent (type=%s, %s)", type(), data())
    }


    internal abstract fun type(): String

    internal abstract fun data(): Any

    class ForTE(obj: Any) : SyncEvent() {

        private val pos: BlockPos = (obj as TileEntity).pos.toImmutable()

        override fun getObjectDirect(player: EntityPlayer): Any {
            return player.world.getTileEntity(pos)!!
        }

        override fun writeMetaInfoToStream(out: MCDataOutput) {
            out.writeByte(TILE_ENTITY)
            out.writeBlockPos(pos)
        }

        public override fun send(obj: Any) {
            done()
            val te = obj as TileEntity
            val pos = te.pos
            for (player in Players.getTrackingChunk(te.world, pos.x shr 4, pos.z shr 4)) {
                NetworkImpl.sendPacket(this, player)
            }
        }

        override fun type(): String {
            return "TileEntity"
        }

        override fun data(): Any {
            return String.format("pos=%s", pos)
        }

        companion object {

            internal fun readObjectFromStream(player: EntityPlayer, `in`: MCDataInput): Any {
                return player.world.getTileEntity(`in`.readBlockPos())!!
            }
        }
    }

    class ForEntity(obj: Any) : SyncEvent() {

        private val entityID: Int

        init {
            this.entityID = (obj as Entity).entityId
        }

        override fun getObjectDirect(player: EntityPlayer): Any {
            return player.world.getEntityByID(entityID)!!
        }

        override fun writeMetaInfoToStream(out: MCDataOutput) {
            out.writeByte(ENTITY)
            out.writeInt(entityID)
        }

        public override fun send(obj: Any) {
            done()
            for (player in Entities.getTrackingPlayers(obj as Entity)) {
                NetworkImpl.sendPacket(this, player)
            }
        }

        override fun type(): String {
            return "Entity"
        }

        override fun data(): Any {
            return String.format("id=%s", entityID)
        }

        companion object {

            internal fun readObjectFromStream(player: EntityPlayer, `in`: MCDataInput): Any {
                return player.world.getEntityByID(`in`.readInt())!!
            }
        }
    }

    class ForContainer(obj: Any) : SyncEvent() {

        private val windowId: Int

        init {
            this.windowId = (obj as Container).windowId
        }

        override fun getObjectDirect(player: EntityPlayer): Any? {
            return if (player.openContainer.windowId == windowId) player.openContainer else null
        }

        override fun writeMetaInfoToStream(out: MCDataOutput) {
            out.writeByte(CONTAINER)
            out.writeByte(windowId)
        }

        public override fun send(obj: Any) {
            done()
            val container = obj as Container
            val listeners = CommonMethodHandles.getListeners(container)
            for (listener in listeners) {
                if (listener is EntityPlayerMP) {
                    NetworkImpl.sendPacket(this, listener)
                }
            }
        }

        override fun type(): String {
            return "Container"
        }

        override fun data(): Any {
            return String.format("id=%s", windowId)
        }

        companion object {

            internal fun readObjectFromStream(player: EntityPlayer, `in`: MCDataInput): Any? {
                return if (player.openContainer.windowId == `in`.readByte().toInt()) player.openContainer else null
            }
        }
    }

    companion object {

        val CHANNEL = "SC|Sync"

        private val TILE_ENTITY = 0
        private val ENTITY = 1
        private val CONTAINER = 2
        private val IEEP = 3

        fun handle(payload: ByteBuf) {
            val `in` = Network.newInput(payload)

            val player = Players.getClient()

            val type = `in`.readByte().toInt()
            var obj: SyncedObjectProxy?
            try {
                when (type) {
                    TILE_ENTITY -> obj = ForTE.readObjectFromStream(player, `in`) as SyncedObjectProxy
                    ENTITY -> obj = ForEntity.readObjectFromStream(player, `in`) as SyncedObjectProxy
                    CONTAINER -> obj = ForContainer.readObjectFromStream(player, `in`) as SyncedObjectProxy?
                //                case IEEP:
                //                    obj = (SyncedObjectProxy) ForIEEP.readObjectFromStream(player, in);
                //                    break;
                    else -> throw ProtocolException("Invalid SyncType ID")
                }
            } catch (e: ClassCastException) {
                obj = null
            }

            if (obj != null) {
                val companion = obj.`_sc$getCompanion`()
                companion?.applyChanges(obj, `in` as SyncCompanion.ChangeIterator)
            }
        }
    }

    //    public static final class ForIEEP extends SyncEvent {
    //
    //        private final int entityID;
    //        private final String id;
    //
    //        public ForIEEP(Object obj) {
    //            IEEPSyncCompanion companion = (IEEPSyncCompanion) ((SyncedObjectProxy) obj)._sc$getCompanion();
    //            entityID = companion._sc$entity.getEntityId();
    //            id = companion._sc$ident;
    //        }
    //
    //        static Object readObjectFromStream(EntityPlayer player, MCDataInput in) {
    //            int entityID = in.readInt();
    //            String id = in.readString();
    //            Entity entity = player.worldObj.getEntityByID(entityID);
    //            if (entity == null) {
    //                return null;
    //            }
    //            return entity.getExtendedProperties(id);
    //        }
    //
    //        @Override
    //        void writeMetaInfoToStream(MCDataOutput out) {
    //            out.writeByte(IEEP);
    //            out.writeInt(entityID);
    //            out.writeString(id);
    //        }
    //
    //        @Override
    //        Object getObjectDirect(EntityPlayer player) {
    //            Entity entity = player.worldObj.getEntityByID(entityID);
    //            if (entity == null) {
    //                return null;
    //            }
    //            return entity.getExtendedProperties(id);
    //        }
    //
    //        @Override
    //        public void send(Object obj) {
    //            done();
    //            Entity entity = ((IEEPSyncCompanion) ((SyncedObjectProxy) obj)._sc$getCompanion())._sc$entity;
    //            NetworkImpl.sendRawPacket(Entities.getTrackingPlayers(entity), this);
    //            if (entity instanceof EntityPlayerMP) {
    //                NetworkImpl.sendRawPacket((EntityPlayerMP) entity, this);
    //            }
    //        }
    //
    //        @Override
    //        String type() {
    //            return "EntityProperties";
    //        }
    //
    //        @Override
    //        Object data() {
    //            return String.format("EntityID=%s, PropertyID=%s", entityID, id);
    //        }
    //    }
}
