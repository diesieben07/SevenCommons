package de.take_weiland.mods.commons.asm.info;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * <p>Information about an element that can have annotations, such as a field or class.</p>
 * @author diesieben07
 */
public interface HasAnnotations {

	/**
	 * <p>Get information about an Annotation on this element.</p>
	 * @param annotation the annotation
	 * @return an AnnotationInfo or null if the given annotation is not present
	 */
	@Nullable
	public abstract AnnotationInfo getAnnotation(Class<? extends Annotation> annotation);

	/**
	 * <p>Checks whether the given annotation is present on this element.</p>
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public abstract boolean hasAnnotation(Class<? extends Annotation> annotation);

}
