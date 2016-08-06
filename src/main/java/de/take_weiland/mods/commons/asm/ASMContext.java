package de.take_weiland.mods.commons.asm;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>Context for ASM class generation.</p>
 *
 * @author diesieben07
 */
public interface ASMContext {

    /**
     * <p>The {@code ClassWriter} in use.</p>
     *
     * @return the ClassWriter
     */
    ClassWriter cw();

    /**
     * <p>Push the given Object as a constant.</p>
     * <p>Where possible this uses the class' constant pool, otherwise a {@code static final} field is used.</p>
     *
     * @param mv       the current MethodVisitor
     * @param constant the constant
     * @param type     the type of the resulting constant
     */
    void pushConstant(MethodVisitor mv, @Nullable Object constant, Class<?> type);

    /**
     * <p>Push the result of the given function as a constant when applied to the linked class file.</p>
     *  @param mv       the current MethodVisitor
     * @param producer the function producing the constant value
     * @param type     the type of the resulting constant
     */
    <T> void pushLateConstant(MethodVisitor mv, Function<Class<?>, ? extends T> producer, Class<T> type);

    /**
     * <p>Link the class using the {@link net.minecraft.launchwrapper.LaunchClassLoader}.</p>
     *
     * @return the linked class
     */
    default Class<?> link() {
        return link(Launch.classLoader);
    }

    /**
     * <p>Link the class using the given {@code ClassLoader}.</p>
     *
     * @return the linked class
     */
    Class<?> link(ClassLoader classLoader);

    /**
     * <p>Link and instantiate the class using the {@link net.minecraft.launchwrapper.LaunchClassLoader}. The
     * class must have constructor without any parameters.</p>
     *
     * @return the new instance
     */
    default Object linkInstantiate() {
        return linkInstantiate(Launch.classLoader);
    }

    /**
     * <p>Link and instantiate the class using the given {@code ClassLoader}. The
     * class must have constructor without any parameters.</p>
     *
     * @return the new instance
     */
    Object linkInstantiate(ClassLoader classLoader);

    /**
     * <p>Add a callback for when the {@code &lt;clinit&gt;} method is being emitted. The callback will be called
     * after any constant fields for {@link #pushConstant(MethodVisitor, Object, Class)} are initialized.</p>
     *
     * @param hook the callback
     */
    default void addStaticInitHook(Consumer<? super MethodVisitor> hook) {
        addStaticInitHook(hook, false);
    }

    /**
     * <p>Add a callback for when the {@code &lt;clinit&gt;} method is being emitted. If {@code preConstantInit} is
     * {@code true}, the callback will be called before any constant fields for {@link #pushConstant(MethodVisitor, Object, Class)}
     * are initialized.</p>
     *
     * @param hook            the callback
     * @param preConstantInit true to be called back before initializing any constant fields
     */
    void addStaticInitHook(Consumer<? super MethodVisitor> hook, boolean preConstantInit);

}
