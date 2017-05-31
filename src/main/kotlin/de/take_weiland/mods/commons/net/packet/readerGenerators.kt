package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.internal.SevenCommons
import de.take_weiland.mods.commons.util.toImmutableList
import io.netty.buffer.ByteBuf
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.methodType
import java.lang.reflect.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.coroutines.experimental.buildSequence
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author diesieben07
 */

fun main(args: Array<String>) {
}

fun <T : BasePacket> createReaderGenerator(packetClass: Class<T>): MethodHandle {
    require(packetClass.constructors.size == 1)
    val constructor = packetClass.constructors.first()
    val kotlinConstructor = constructor.kotlinFunction

    val readers = constructor.parameters.asSequence()
            .zip(kotlinConstructor?.parameters?.asSequence() ?: buildSequence { yield(null) })
            .map { (parameter, kotlinParameter) ->
                val type = kotlinParameter?.type?.javaType ?: parameter.parameterizedType
                val nullable = kotlinParameter?.type?.isMarkedNullable ?: true // TODO figure out if there is a way to get at @Nullable annotations
                findReadMethodHandle(type, nullable)
            }
            .toList()

    val lookup = MethodHandles.publicLookup()

    val constructorMethodHandle = lookup.unreflectConstructor(constructor)
    val withReaders = MethodHandles.filterArguments(constructorMethodHandle, 0, *readers.toTypedArray())
    val permuted = MethodHandles.permuteArguments(withReaders, methodType(packetClass, ByteBuf::class.java), *IntArray(withReaders.type().parameterCount()) { 0 })

    return permuted.asType(methodType(Packet::class.java, ByteBuf::class.java))
}

@Target(CLASS, FILE)
@Retention(RUNTIME)
internal annotation class SearchForPacketReaders

private val markedMethods =
        SevenCommons.asmData.getAll(SearchForPacketReaders::class.java.name).asSequence()
                .map { Class.forName(it.className) }
                .flatMap { it.methods.asSequence() }
                .toImmutableList()

private fun findReadMethodHandle(valueType: Type, nullable: Boolean = false): MethodHandle {
    return markedMethods.asSequence()
            .filter {
                it.parameterTypes.let { parameters ->
                    parameters.size == 1 && parameters[0] == ByteBuf::class.java
                }
            }
            .filter {
                if (it.returnType != valueType) {
                    false
                } else {
                    it.kotlinFunction?.let { kf ->
                        !(kf.returnType.isMarkedNullable && !nullable)
                    } ?: nullable
                }
            }
            .map { MethodHandles.publicLookup().unreflect(it) }
            .first()
}