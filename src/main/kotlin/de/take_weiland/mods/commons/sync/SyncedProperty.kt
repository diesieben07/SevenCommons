//package de.take_weiland.mods.commons.sync
//
//import de.take_weiland.mods.commons.net.packet.raw.NettyAsyncReceive
//import de.take_weiland.mods.commons.net.simple.SimplePacket
//import de.take_weiland.mods.commons.util.thread
//import io.netty.buffer.ByteBuf
//import net.minecraft.entity.Entity
//import net.minecraft.entity.player.EntityPlayer
//import net.minecraft.inventory.Container
//import net.minecraft.network.NetworkManager
//import net.minecraft.tileentity.TileEntity
//import net.minecraft.world.World
//import kotlin.reflect.KMutableProperty1
//import kotlin.reflect.KProperty
//
//abstract class SyncedProperty<CONTAINER, TYPE, in DATA>(protected val obj: CONTAINER) {
//
//    var id: Int = -1
//
//    internal lateinit var property: KMutableProperty1<in CONTAINER, TYPE>
//    internal lateinit var containerType: SyncedContainerType<CONTAINER>
//
//    internal val world: World
//        get() = containerType.getWorld(obj)
//
//    fun init(property: KProperty<*>, containerType: SyncedContainerType<CONTAINER>) {
//        if (property !is KMutableProperty1<*, *>) throw UnsupportedOperationException("Only mutable member properties in a class can be synced.")
//        this.id = property.getPropertyId()
//        this.containerType = containerType
//        @Suppress("UNCHECKED_CAST")
//        this.property = property as KMutableProperty1<in CONTAINER, TYPE>
//    }
//
//    abstract fun write(out: ByteBuf, data: DATA)
//    abstract fun read(input: ByteBuf)
//    abstract fun read(data: DATA)
//
//}
//
//operator fun <C : TileEntity, P : SyncedProperty<C, *, *>> P.provideDelegate(obj: C, property: KProperty<*>): P {
//    init(property, TileEntitySyncedType)
//    return this
//}
//
//operator fun <C : Entity, P : SyncedProperty<C, *, *>> P.provideDelegate(obj: C, property: KProperty<*>): P {
//    init(property, EntitySyncedType)
//    return this
//}
//
//operator fun <C : Container, P : SyncedProperty<C, *, *>> P.provideDelegate(obj: C, property: KProperty<*>): P {
//    init(property, ContainerSyncedType)
//    return this
//}
//
//interface TickingProperty {
//
//    fun update()
//
//}
//
//abstract class SyncEvent<TYPE, DATA>(val newValue: TYPE) : SimplePacket, NettyAsyncReceive {
//
//    abstract fun write(buf: ByteBuf)
//    abstract fun TYPE.toData(): DATA
//
//    private var data: DATA? = null
//
//    final override fun sendTo(manager: NetworkManager) {
//        if (manager.isLocalChannel) {
//            // this needs to happen here so that `newValue` is only accessed from the sending thread
//            // we must not touch it after this point
//            data = newValue.toData()
//
//        }
//        manager.channel().writeAndFlush(this)
//    }
//
//    override fun receiveAsync(player: EntityPlayer) {
//        player.thread.run {
//
//        }
//    }
//}
//
//abstract class SyncedPropertyImmutable<CONTAINER, TYPE, DATA>(obj: CONTAINER, @JvmField var value: TYPE) : SyncedProperty<CONTAINER, TYPE, DATA>(obj) {
//
//    operator fun getValue(obj: CONTAINER, property: KProperty<*>): TYPE = value
//
//    operator fun setValue(obj: CONTAINER, property: KProperty<*>, newValue: TYPE) {
//        if (value != newValue) {
//            value = newValue
//            markDirty(obj, newValue.toData())
//        }
//    }
//
//    abstract fun TYPE.toData(): DATA
//}
//
////abstract class SyncedPropertyIdentityImmutable<CONTAINER, TYPE, in DATA>(obj: CONTAINER, @JvmField var value: TYPE) : SyncedProperty<CONTAINER, TYPE, DATA>(obj) {
////
////    operator fun getValue(obj: CONTAINER, property: KProperty<*>): TYPE = value
////
////    operator fun setValue(obj: CONTAINER, property: KProperty<*>, newValue: TYPE) {
////        if (value !== newValue) {
////            value = newValue
////            markDirty(obj, newValue)
////        }
////    }
////
////}
////
////abstract class SyncedPropertyMutable<CONTAINER, TYPE>(@JvmField var value: TYPE, obj: CONTAINER) : SyncedProperty<CONTAINER, TYPE>(obj), TickingProperty {
////
////    @JvmField
////    var oldValue = value
////
////    inline operator fun getValue(obj: CONTAINER, property: KProperty<*>): TYPE = value
////
////    inline operator fun setValue(obj: CONTAINER, property: KProperty<*>, newValue: TYPE) {
////        value = newValue
////    }
////
////    override fun update() {
////        value.let {
////            if (it != oldValue) {
////                oldValue = value.copy()
//////                markDirty()
////            }
////        }
////    }
////
////    protected abstract fun TYPE.copy(): TYPE
////
////}
//
//internal val dirtyProperties = ChangedPropertyStore()
//
//fun <CONTAINER, TYPE, DATA> SyncedProperty<CONTAINER, TYPE, DATA>.markDirty(obj: CONTAINER, data: DATA) {
//    dirtyProperties[obj, id] = data
//}