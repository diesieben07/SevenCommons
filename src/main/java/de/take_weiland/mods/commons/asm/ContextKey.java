package de.take_weiland.mods.commons.asm;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>A key for defining the context for cloning Labels in CodePieces.</p>
 * @see de.take_weiland.mods.commons.asm.CodePiece#setContextKey(ContextKey)
 * @author diesieben07
 */
public class ContextKey {

	public static ContextKey create() {
		return new ContextKey();
	}

	public static ContextKey create(String label) {
		return new WithLabel(checkNotNull(label, "label"));
	}

	ContextKey() { }

	private static class WithLabel extends ContextKey {

		private final String label;

		WithLabel(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof WithLabel && ((WithLabel) obj).label.equals(label);
		}

		@Override
		public int hashCode() {
			return label.hashCode();
		}
	}

}
