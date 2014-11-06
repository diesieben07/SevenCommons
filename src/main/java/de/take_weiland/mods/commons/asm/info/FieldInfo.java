package de.take_weiland.mods.commons.asm.info;

import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
import static org.objectweb.asm.Opcodes.ACC_VOLATILE;

/**
 * @author diesieben07
 */
public abstract class FieldInfo extends MemberInfo {

	private final ClassInfo clazz;

	FieldInfo(ClassInfo clazz) {
		this.clazz = clazz;
	}

	/**
	 * <p>Get the type-descriptor of this field.</p>
	 *
	 * @return the descriptor
	 */
	public abstract String desc();

	@Override
	public ClassInfo containingClass() {
		return clazz;
	}

	/**
	 * <p>Determine if this field is {@code volatile}.</p>
	 *
	 * @return true if this field is {@code volatile}
	 */
	public boolean isVolatile() {
		return hasModifier(ACC_VOLATILE);
	}

	/**
	 * <p>Determine if this field is {@code transient}.</p>
	 *
	 * @return true if this field is {@code transient}
	 */
	public boolean isTransient() {
		return hasModifier(ACC_TRANSIENT);
	}

	/**
	 * <p>Determine if this field is an enum constant.</p>
	 *
	 * @return true if this field is an enum constant
	 */
	public boolean isEnumConstant() {
		return hasModifier(ACC_ENUM);
	}

	@Override
	public int hashCode() {
		return clazz.hashCode() ^ name().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FieldInfo)) {
			return false;
		}
		FieldInfo that = (FieldInfo) obj;
		return that.clazz.equals(this.clazz) && that.name().equals(this.name());
	}
}
