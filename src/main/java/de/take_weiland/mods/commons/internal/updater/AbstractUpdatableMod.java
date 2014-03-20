package de.take_weiland.mods.commons.internal.updater;

public abstract class AbstractUpdatableMod implements UpdatableMod {

	protected final UpdateController controller;
	private final ModVersionCollection versions;

	protected ModUpdateState state = ModUpdateState.AVAILABLE;

	public AbstractUpdatableMod(UpdateController controller, ModVersionCollection versions) {
		this.controller = controller;
		this.versions = versions;
	}

	@Override
	public final UpdateController getController() {
		return controller;
	}

	@Override
	public ModVersionCollection getVersions() {
		return versions;
	}

	@Override
	public final boolean transition(ModUpdateState state) {
		if (this.state.canTransition(state)) {
			ModUpdateState old = this.state;
			this.state = state;
			controller.onStateChange(this, old);
			return true;
		}
		return false;
	}

	@Override
	public final ModUpdateState getState() {
		return state;
	}

	@Override
	public String toString() {
		return "AbstractUpdatableMod [" + getModId() + "]";
	}
}