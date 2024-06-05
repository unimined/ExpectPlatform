package xyz.wagyourtail.unimined.expect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotate public static methods to replace their contents with platform specific implementations.
 */
@Retention(java.lang.annotation.RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ExpectPlatform {

    /**
     * defaults to current package + "/platformname/" + currentClassName + "Impl.class"
     * ie. if you have a class "xyz.wagyourtail.unimined.MyClass" and platform name is "platformname"
     * the default value would be "xyz/wagyourtail/unimined/platformname/MyClassImpl.class"
     *
     * if you would like to override the location of the class on any platforms... change it here.
     */
    Platform[] platforms() default {};

    @interface Platform {
        String name();
        String target();
    }

}
