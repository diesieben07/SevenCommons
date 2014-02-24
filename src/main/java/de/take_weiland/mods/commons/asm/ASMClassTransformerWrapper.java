package de.take_weiland.mods.commons.asm;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

/**
 * <p>Implementation of {@link net.minecraft.launchwrapper.IClassTransformer} which uses a number of {@link de.take_weiland.mods.commons.asm.ASMClassTransformer ASMClassTransformers}
 * to transform classes.</p>
 * <p>This class makes a best-effort to reuse ClassNodes so that the class bytes don't have to be parsed over and over again.</p>
 *
 * @author diesieben07
 */
public abstract class ASMClassTransformerWrapper implements IClassTransformer {

	private final List<ASMClassTransformer> transformers;

	protected ASMClassTransformerWrapper() {
		transformers = Lists.newArrayList();
		setup();
		sortTransformers();
	}

	@Override
	public final byte[] transform(String name, String transformedName, byte[] bytes) {
		String internalName = transformedName.replace('.', '/');
		ClassReader reader = new ClassReader(bytes);
		ClassNode node = null;
		boolean wasExpandFrames = false;
		int lastWriterFlags = -1;
		for (ASMClassTransformer transformer : transformers) {
			if (transformer.transforms(internalName)) {
				int readerFlags = transformer.getClassReaderFlags();
				int writerFlags = transformer.getClassWriterFlags();

				if (lastWriterFlags >= 0 && writerFlags != lastWriterFlags && node != null) { // last one needs to be saved first
					ClassWriter writer = new ClassWriter(writerFlags);
					node.accept(writer);
					bytes = writer.toByteArray();
					reader = new ClassReader(bytes);
					node = null; // clear out the node, it needs to be recreated
				}
				lastWriterFlags = writerFlags;

				boolean wantsExpandFrames = (readerFlags & EXPAND_FRAMES) == EXPAND_FRAMES;
				if (node == null || (wantsExpandFrames != wasExpandFrames)) {
					node = new ClassNode();
					reader.accept(node, readerFlags);
				}
				wasExpandFrames = wantsExpandFrames;
				try {
					transformer.transform(node);
				} catch (Throwable e) {
					System.err.println("Exception during transformation of: " + transformedName);
					e.printStackTrace();
					throw Throwables.propagate(e);
				}
			}
		}
		if (node != null) {
			ClassWriter writer = new ClassWriter(lastWriterFlags);
			node.accept(writer);
			bytes = writer.toByteArray();
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

	private void sortTransformers() {
		Collections.sort(transformers, new Comparator<ASMClassTransformer>() {

			@Override
			public int compare(ASMClassTransformer t1, ASMClassTransformer t2) {
				int wf1 = t1.getClassWriterFlags();
				int wf2 = t2.getClassWriterFlags();
				if (wf1 != wf2) {
					return Ints.compare(wf1, wf2);
				}
				int rf1 = t1.getClassReaderFlags();
				int rf2 = t2.getClassReaderFlags();
				if ((rf1 & ClassReader.EXPAND_FRAMES) != 0 && (rf2 & ClassReader.EXPAND_FRAMES) == 0) {
					return -1;
				} else if ((rf2 & ClassReader.EXPAND_FRAMES) != 0 && (rf1 & ClassReader.EXPAND_FRAMES) == 0) {
					return 1;
				} else {
					return Ints.compare(t1.getClassReaderFlags(), t2.getClassReaderFlags());
				}
			}

		});
	}
}
