package xyz.wagyourtail.unimined.expect;

public final class Target {
	private Target() {
	}

	/**
	 * @return the currently targeted platform
	 */
	public static String getCurrentTarget() {
		throw new AssertionError("stub method, this shouldn't even be on your runtime classpath!");
	}
}
