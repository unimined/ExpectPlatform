package xyz.wagyourtail.unimined.expect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a method to only be present on specified platforms.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface PlatformOnly {
	String[] value();

	String FORGE = "forge";
	String NEOFORGE = "neoforge";
	String FABRIC = "fabric";
	String QUIILT = "quilt";
}
