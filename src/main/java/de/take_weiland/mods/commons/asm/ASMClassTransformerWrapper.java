package de.take_weiland.mods.commons.asm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * <p>Implementation of {@link net.minecraft.launchwrapper.IClassTransformer} which uses a number of {@link de.take_weiland.mods.commons.asm.ASMClassTransformer ASMClassTransformers}
 * to transform classes.</p>
 * <p>This class makes a best-effort to reuse ClassNodes so that the class bytes don't have to be parsed over and over again.</p>
 *
 * @author diesieben07
 */
public abstract class ASMClassTransformerWrapper implements IClassTransformer {

	private final ArrayList<ASMClassTransformer> transformers;

	protected ASMClassTransformerWrapper() {
		transformers = Lists.newArrayList();
		setup();
		transformers.trimToSize();
	}

	@Override
	public final byte[] transform(String name, String transformedName, byte[] bytes) {
		String internalName = transformedName.replace('.', '/');
		ClassNode clazz = null;
		ClassInfo classInfo = null;
		boolean changed = false;
		for (ASMClassTransformer transformer : transformers) {
			if (transformer.transforms(internalName)) {
				if (clazz == null) {
					ClassReader cr = new ClassReader(bytes);
					cr.accept((clazz = new ClassNode()), 0);
					classInfo = ASMUtils.getClassInfo(clazz);
				}
				changed |= transformer.transform(clazz, classInfo);
			}
		}

		if (changed) {
			ClassWriter cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
			clazz.accept(cw);
			return cw.toByteArray();
		}

		return bytes;
	}

	/**
	 * register your transformers here
	 */
	protected abstract void setup();

	/**
	 * register an {@link de.take_weiland.mods.commons.asm.ASMClassTransformer}
	 * @param transformer the transformer to register
	 */
	protected final void register(ASMClassTransformer transformer) {
		transformers.add(Preconditions.checkNotNull(transformer, "transformer must not be null"));
	}

}
