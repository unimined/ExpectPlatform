package xyz.wagyourtail.unimined.expect;

public final class Target {
	private Target() {
	}

	/**
	 * @return the currently targeted platform
	 */
	public static String getCurrentTarget() {
		throw new AssertionError("failed to transform method");
	}
}
