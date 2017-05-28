package de.take_weiland.mods.commons.net.mod

/**
 * @author diesieben07
 */
//fun <T : Packet> createReaderGenerator(packetClass: Class<T>): MethodHandle {
//    require(packetClass.constructors.size == 1)
//    val constructor = packetClass.constructors.first()
//    val kotlinConstructor = constructor.kotlinFunction
//
//    val readers = constructor.parameters.asSequence()
//            .zip(kotlinConstructor?.parameters?.asSequence() ?: buildSequence { yield(null) })
//            .map { (parameter, kotlinParameter) ->
//                val type = kotlinParameter?.type?.javaType ?: parameter.parameterizedType
//                val nullable = kotlinParameter?.type?.isMarkedNullable ?: true // TODO figure out if there is a way to get at @Nullable annotations
//                findReadMethodHandle(type, nullable)
//            }
//            .toList()
//
//    val lookup = MethodHandles.publicLookup()
//
//    val constructorMethodHandle = lookup.unreflectConstructor(constructor)
//    val withReaders = MethodHandles.filterArguments(constructorMethodHandle, 0, *readers.toTypedArray())
//    val permuted = MethodHandles.permuteArguments(withReaders, methodType(packetClass, ByteBuf::class.java), *IntArray(withReaders.type().parameterCount()) { 0 })
//
//    return permuted.asType(methodType(Packet::class.java, ByteBuf::class.java))
//}
//
//private fun findReadMethodHandle(valueType: Type, nullable: Boolean = false): MethodHandle {
//    ByteStream
//}