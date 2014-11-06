package de.take_weiland.mods.commons.asm;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import java.util.List;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

/**
 * <p>Implementation of {@link net.minecraft.launchwrapper.IClassTransformer}, which uses a number of
 * {@link de.take_weiland.mods.commons.asm.ASMClassTransformer ASMClassTransformers} to transform classes.</p>
 * <p>This class makes a best-effort to reuse ClassNodes so that the class bytes don't have to be parsed over and over again.</p>
 *
 * @author diesieben07
 */
public abstract class ASMClassTransformerWrapper implements IClassTransformer {

	private final List<ASMClassTransformer> transformers;

	public ASMClassTransformerWrapper() {
		ImmutableList.Builder<ASMClassTransformer> b = ImmutableList.builder();
		setup(b);
		transformers = b.build();
	}

	@Override
	public final byte[] transform(String name, String transformedName, @Nullable byte[] bytes) {
		try {
			return transform0(transformedName, bytes);
		} catch (Throwable t) {
			System.err.println("Exception during transformation of " + transformedName + " [" + name + "]");
			t.printStackTrace();
			throw Throwables.propagate(t);
		}
	}

	private byte[] transform0(String transformedName, @Nullable byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		String internalName = transformedName.replace('.', '/');
		ClassNode clazz = null;
		ClassInfo classInfo = null;
		boolean changed = false;
		for (ASMClassTransformer transformer : transformers) {
			if (transformer.transforms(internalName)) {
				if (clazz == null) {
					ClassReader cr = new ClassReader(bytes);
					cr.accept((clazz = new ClassNode()), 0);
					classInfo = ClassInfo.of(clazz);
				}
				changed |= transformer.transform(clazz, classInfo);
			}
		}

		if (changed) {
			ClassWriter cw = new ClassInfoClassWriter(COMPUTE_FRAMES);
			clazz.accept(cw);
			return cw.toByteArray();
		}

		return bytes;
	}

	/**
	 * <p>Register your transformers here.</p>
	 *
	 * @param builder a builder to add your transformers to
	 */
	protected abstract void setup(ImmutableList.Builder<ASMClassTransformer> builder);

}
