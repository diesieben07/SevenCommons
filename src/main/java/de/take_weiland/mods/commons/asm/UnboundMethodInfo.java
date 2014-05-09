package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
class UnboundMethodInfo extends MethodInfo {

	private final String name;
	private final String desc;
	private final int modifiers;
	private Boolean exists;

	UnboundMethodInfo(String className, String name, String desc, int modifiers) {
		super(className);
		this.name = name;
		this.desc = desc;
		this.modifiers = modifiers;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String desc() {
		return desc;
	}

	@Override
	public int modifiers() {
		return modifiers;
	}

	@Override
	public boolean exists() {
		return exists == null ? (exists = containingClass().hasMethod(name, desc)) : exists;
	}
}
