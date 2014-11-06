package de.take_weiland.mods.commons.asm.info;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;

/**
 * <p>Information about an Annotation.</p>
 *
 * @author diesieben07
 */
public abstract class AnnotationInfo {

	private final HasAnnotations holder;

	AnnotationInfo(HasAnnotations holder) {
		this.holder = holder;
	}

	/**
	 * <p>Get the type of annotation this AnnotationInfo represents.</p>
	 * @return a Type
	 */
	public abstract Type type();

	/**
	 * <p>Get the element this annotation is on.</p>
	 * @return the element
	 */
	public HasAnnotations getHolder() {
		return holder;
	}

	/**
	 * <p>Checks if this annotation has the given property.</p>
	 * @param prop the property
	 * @return true if the property is present
	 */
	public abstract boolean hasProperty(String prop);

	/**
	 * <p>Get the value of the given property for this annotation.</p>
	 * @param prop the property
	 * @return the value of the property
	 */
	public abstract <T> Optional<T> getProperty(String prop);

	@Override
	public int hashCode() {
		return type().hashCode() ^ holder.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AnnotationInfo)) {
			return false;
		}
		AnnotationInfo that = (AnnotationInfo) obj;
		return that.type().equals(this.type()) && that.holder.equals(this.holder);
	}
}
